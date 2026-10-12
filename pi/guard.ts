import { appendFileSync, existsSync, readFileSync, realpathSync } from "node:fs";
import { isAbsolute, relative, resolve } from "node:path";
import type { ExtensionAPI } from "@earendil-works/pi-coding-agent";
import { createBashTool } from "@earendil-works/pi-coding-agent";

/**
 * Pi extension that confines the agent to its workspace:
 *   - write/edit: only the generated test class;
 *   - read/grep/find/ls: only paths inside the workspace (symlinks resolved);
 *   - bash: only the named case commands, exactly as given: no redirection, no background, no
 *     second command (the command string must equal an allowed argv joined by spaces, up to
 *     repeated spaces or tabs; a line break is never accepted).
 * Denied calls return an error to the agent and are logged. Every command run and every version of
 * the generated class are logged for the runner.
 *
 * Commands run with an environment reduced to GUARD_ENV_ALLOW (the runner's command environment):
 * no API key, no Pi or guard variables, no session metadata reach the project code.
 *
 * Environment (set by the runner): GUARD_CONFIG (JSON: workspace, output_test_file, commands),
 * GUARD_DENIED_LOG, GUARD_COMMAND_LOG, GUARD_OUTPUT_VERSION_LOG.
 */
interface GuardConfig {
	workspace: string;
	output_test_file: string;
	commands: Record<string, string[]>;
	/** names of the environment variables that commands may see */
	command_env: string[];
}

export function commandEnv(env: NodeJS.ProcessEnv, allowed: string[]): NodeJS.ProcessEnv {
	const keep = new Set(allowed);
	const out: NodeJS.ProcessEnv = {};
	for (const [k, v] of Object.entries(env)) {
		if (keep.has(k) && v !== undefined) {
			out[k] = v;
		}
	}
	return out;
}

function shellQuote(value: string): string {
	return /^[A-Za-z0-9_@%+=:,./-]+$/.test(value) ? value : `'${value.replace(/'/g, "'\\''")}'`;
}

/** Spaces and tabs collapsed; null when the command spans several lines (bash would run each). */
export function canonicalCommand(command: string): string | null {
	if (/[\r\n\u2028\u2029]/.test(command)) {
		return null;
	}
	return command.trim().replace(/[ \t]+/g, " ");
}

export function allowedCommands(commands: Record<string, string[]>): Map<string, string> {
	const allowed = new Map<string, string>();
	for (const [name, argv] of Object.entries(commands ?? {})) {
		allowed.set(argv.map(shellQuote).join(" "), name);
		allowed.set(argv.join(" "), name);
	}
	return allowed;
}

/** Name of the allowed command that `command` is, or undefined (deny). */
export function matchCommand(allowed: Map<string, string>, command: string): string | undefined {
	const canonical = canonicalCommand(command);
	return canonical === null ? undefined : allowed.get(canonical);
}

export default function (pi: ExtensionAPI) {
	const configPath = process.env.GUARD_CONFIG;
	if (!configPath) {
		return;
	}
	const config = JSON.parse(readFileSync(configPath, "utf-8")) as GuardConfig;
	const workspace = resolve(config.workspace);
	const outputPath = resolve(workspace, config.output_test_file);
	const deniedLog = process.env.GUARD_DENIED_LOG;
	const commandLog = process.env.GUARD_COMMAND_LOG;
	const versionLog = process.env.GUARD_OUTPUT_VERSION_LOG;
	let lastOutputVersion: string | undefined;

	// Record every state of the generated test class, so that the first attempt stays observable
	// after later repairs (the runner only sees the final file).
	function recordOutputVersion(toolName: string) {
		if (!versionLog || !existsSync(outputPath)) {
			return;
		}
		let content: string;
		try {
			content = readFileSync(outputPath, "utf-8");
		} catch {
			return;
		}
		if (content === lastOutputVersion) {
			return;
		}
		lastOutputVersion = content;
		appendFileSync(
			versionLog,
			`${JSON.stringify({ tool: toolName, timestamp: Date.now(), content })}\n`,
		);
	}

	const allowed = allowedCommands(config.commands);

	// bash with the reduced environment (no session metadata, nothing outside command_env)
	const bashTool = createBashTool(workspace, {
		exposeSessionEnvironment: false,
		spawnHook: ({ command, cwd, env }) => ({ command, cwd, env: commandEnv(env, config.command_env ?? []) }),
	});
	pi.registerTool({
		...bashTool,
		execute: async (id, params, signal, onUpdate) => bashTool.execute(id, params, signal, onUpdate),
	});

	const pending = new Map<string, { name: string; command: string; started: number }>();

	function record(entry: Record<string, unknown>) {
		if (commandLog) {
			appendFileSync(commandLog, `${JSON.stringify(entry)}\n`);
		}
	}

	function deny(kind: string, target: string, reason: string) {
		if (deniedLog) {
			appendFileSync(
				deniedLog,
				`${JSON.stringify({ kind, path: target, reason, timestamp: Date.now() })}\n`,
			);
		}
		return { block: true, reason };
	}

	const readRoot = canonical(workspace);

	function canonical(path: string): string {
		// resolve symlinks of the existing prefix so that a link cannot lead outside
		let probe = path;
		const rest: string[] = [];
		while (!existsSync(probe)) {
			const parent = resolve(probe, "..");
			if (parent === probe) {
				return path;
			}
			rest.unshift(probe.slice(parent.length + 1));
			probe = parent;
		}
		try {
			return resolve(realpathSync(probe), ...rest);
		} catch {
			return path;
		}
	}

	function readable(raw: string | undefined): boolean {
		const target = canonical(
			raw === undefined || raw === "" ? workspace : isAbsolute(raw) ? resolve(raw) : resolve(workspace, raw),
		);
		const rel = relative(readRoot, target);
		return rel === "" || (rel !== ".." && !rel.startsWith("../") && !isAbsolute(rel));
	}

	pi.on("tool_call", async (event) => {
		if (
			event.toolName === "read" ||
			event.toolName === "grep" ||
			event.toolName === "find" ||
			event.toolName === "ls"
		) {
			const raw = (event.input as { path?: string }).path;
			if (!readable(raw)) {
				return deny(event.toolName, String(raw), "reads are restricted to the workspace");
			}
			return undefined;
		}
		if (event.toolName === "write" || event.toolName === "edit") {
			const raw = event.input.path as string;
			const target = isAbsolute(raw) ? resolve(raw) : resolve(workspace, raw);
			const inside = !relative(workspace, target).startsWith("..");
			if (!inside) {
				return deny(event.toolName, raw, "writes outside the workspace are not allowed");
			}
			if (target !== outputPath) {
				return deny(event.toolName, raw, `writes are restricted to ${config.output_test_file}`);
			}
			return undefined;
		}
		if (event.toolName === "bash") {
			const raw = String(event.input.command ?? "");
			const name = matchCommand(allowed, raw);
			if (name === undefined) {
				return deny(
					"bash",
					raw.slice(0, 2000),
					`only the named case commands are allowed, exactly as written: ${Object.keys(config.commands ?? {}).join(", ")}`,
				);
			}
			pending.set(event.toolCallId, { name, command: raw, started: Date.now() });
			record({ event: "start", name, command: raw, timestamp: Date.now() });
			return undefined;
		}
		return undefined;
	});

	pi.on("tool_result", async (event) => {
		if (event.toolName === "write" || event.toolName === "edit") {
			recordOutputVersion(event.toolName);
			return undefined;
		}
		if (event.toolName !== "bash") {
			return undefined;
		}
		const started = pending.get(event.toolCallId);
		pending.delete(event.toolCallId);
		const text = (event.content ?? [])
			.map((block) => (block as { text?: string }).text ?? "")
			.join("\n");
		const match = /Command exited with code (\d+)/.exec(text);
		const exitCode = match ? Number(match[1]) : event.isError ? null : 0;
		record({
			event: "end",
			name: started?.name ?? null,
			command: started?.command ?? null,
			isError: Boolean(event.isError),
			exit_code: exitCode,
			succeeded: !event.isError && exitCode === 0,
			duration_ms: started ? Date.now() - started.started : null,
			timestamp: Date.now(),
			output_tail: text.slice(-4000),
		});
		return undefined;
	});
}
