// Checks of the komar-mode command forms of pi/guard.ts, run by tests/test_komar.py with node and the jiti
// loader of the installed Pi package: node komar.mjs <pi package dir> <repository root>, with
// {"scenario": [...], "commands": [...]} on stdin. Prints one JSON object with the results.
import { readFileSync } from "node:fs";
import { pathToFileURL } from "node:url";

const [piDir, root] = process.argv.slice(2);
const { createJiti } = await import(pathToFileURL(`${piDir}/node_modules/jiti/lib/jiti.mjs`).href);
const jiti = createJiti(import.meta.url, {
	alias: { "@earendil-works/pi-coding-agent": `${piDir}/dist/index.js` },
});
const guard = await jiti.import(`${root}/pi/guard.ts`);
const input = JSON.parse(readFileSync(0, "utf-8"));

const out = {
	kinds: input.commands.map((c) => guard.komarCommandKind(c, input.scenario) ?? null),
	spec: ["komar/a.yaml", "komar/run-2.v1.yaml", "komar/a.jsonl", "komar/../a.yaml", "src/a.yaml"].map((p) =>
		guard.KOMAR_SPEC.test(p),
	),
};
process.stdout.write(JSON.stringify(out));
