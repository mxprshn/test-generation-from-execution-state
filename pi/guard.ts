import { appendFileSync, existsSync, readFileSync, realpathSync } from "node:fs";
import { isAbsolute, relative, resolve } from "node:path";
import type { ExtensionAPI } from "@earendil-works/pi-coding-agent";

/**
 * Pi extension that confines the agent to its workspace:
 *   - write/edit: only the generated test class;
 *   - read/grep/find/ls: only paths inside the workspace (symlinks resolved);
 *   - bash: only the named case commands, exactly as given (optionally backgrounded with "&").
 * Denied calls return an error to the agent and are logged. Every command run and every version of
 * the generated class are logged for the runner.
 *
 * Environment (set by the runner): GUARD_CONFIG (JSON: workspace, output_test_file, commands),
 * GUARD_DENIED_LOG, GUARD_COMMAND_LOG, GUARD_OUTPUT_VERSION_LOG.
 */
interface GuardConfig {
	workspace: string;
	output_test_file: string;
	commands: Record<string, string[]>;
}

function shellQuote(value: string): string {
	return /^[A-Za-z0-9_@%+=:,./-]+$/.test(value) ? value : `'${value.replace(/'/g, "'\\''")}'`;
}

function normalize(command: string): string {
	return command.trim().replace(/\s+/g, " ");
}

function stripBackground(command: string): { core: string; background: boolean } {
	let core = normalize(command);
	let background = false;
	if (core.endsWith("&") && !core.endsWith("&&")) {
		background = true;
		core = normalize(core.slice(0, -1));
	}
	const redirect = /\s(?:>|>>)\s*\S+(?:\s+2>&1)?$/;
	while (redirect.test(core)) {
		core = normalize(core.replace(redirect, ""));
	}
	return { core, background };
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

	const allowed = new Map<string, string>();
	for (const [name, argv] of Object.entries(config.commands ?? {})) {
		allowed.set(normalize(argv.map(shellQuote).join(" ")), name);
		allowed.set(normalize(argv.join(" ")), name);
	}

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
			const raw = normalize(String(event.input.command ?? ""));
			const { core, background } = stripBackground(raw);
			const name = allowed.get(core);
			if (name === undefined) {
				return deny(
					"bash",
					raw,
					`only named case commands are allowed (optionally backgrounded with a trailing &): ${Object.keys(config.commands ?? {}).join(", ")}`,
				);
			}
			pending.set(event.toolCallId, { name, command: raw, started: Date.now() });
			record({ event: "start", name, command: raw, background, timestamp: Date.now() });
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
