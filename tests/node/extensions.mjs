// Checks of pi/guard.ts and pi/provider.ts, run by tests/test_runner.py with node and the jiti
// loader of the installed Pi package: node extensions.mjs <pi package dir> <repository root>.
// Prints one JSON object with the results.
import { pathToFileURL } from "node:url";

const [piDir, root] = process.argv.slice(2);
const { createJiti } = await import(pathToFileURL(`${piDir}/node_modules/jiti/lib/jiti.mjs`).href);
const jiti = createJiti(import.meta.url, {
	alias: { "@earendil-works/pi-coding-agent": `${piDir}/dist/index.js` },
});
const guard = await jiti.import(`${root}/pi/guard.ts`);
const provider = await jiti.import(`${root}/pi/provider.ts`);

const commands = {
	build: ["mvn", "-q", "-o", "-DskipTests", "test-compile"],
	scenario: ["mvn", "-q", "-o", "-Dtest=CacheTest#testplan1", "test"],
};
const allowed = guard.allowedCommands(commands);
const probe = (c) => guard.matchCommand(allowed, c) ?? null;

const out = {
	exact: probe("mvn -q -o -DskipTests test-compile"),
	extraSpaces: probe("  mvn  -q -o\t-DskipTests test-compile "),
	quotedHash: probe("mvn -q -o '-Dtest=CacheTest#testplan1' test"),
	unquotedHash: probe("mvn -q -o -Dtest=CacheTest#testplan1 test"),
	bypassRedirect: probe("mvn -q -o -Dtest=CacheTest#testplan1 test >x;cat${IFS}../../../../cases/C/call-dumps.json"),
	redirect: probe("mvn -q -o -DskipTests test-compile > build.log"),
	background: probe("mvn -q -o -DskipTests test-compile &"),
	chained: probe("mvn -q -o -DskipTests test-compile && cat /etc/passwd"),
	piped: probe("mvn -q -o -DskipTests test-compile | tee x"),
	newline: probe("mvn -q -o -DskipTests test-compile\ncat x"),
	substitution: probe("mvn -q -o -DskipTests test-compile $(cat x)"),
	env: guard.commandEnv({ PATH: "/bin", TESTGEN_API_KEY: "k", HOME: "/h", PI_SESSION_ID: "s" }, ["PATH", "HOME"]),
	literal: [
		provider.literalConfigValue("plain-key"),
		provider.literalConfigValue("a$B"),
		provider.literalConfigValue("!rm"),
	],
};
process.stdout.write(JSON.stringify(out));
