# Cases on the projects of Pankti, RICK and MicroTestCarver

`cases/tools-projects.csv` lists 31 cases on four projects the three execution-based test generators
were evaluated on by their authors. Only the autonomous mode (and, outside this repository, the three
tools) runs on them: there are no call dumps or preliminary tests, so `baseline`, `dump` and `rendered`
are not run. The 70 cases of `cases/cases.csv` and their results are not affected (their input hashes
are unchanged; `tests/test_pipeline.py::test_inputs_of_the_study_cases_unchanged`).

| Project | Commit | Evaluated by | Cases |
|---|---|---|---|
| PDFBox 2.0.24 (module `pdfbox`) | 8876e8e | RICK (same commit), Pankti (2.0.21) | 14 |
| GraphHopper 5.3 (module `core`) | af5ac0b | RICK (same commit) | 8 |
| Spring PetClinic 2.7.3 | a5cbb85 | MicroTestCarver | 8 |
| Lab-Insurance (micronaut-microservices-poc, module `pricing-service`) | 79416dd | MicroTestCarver ("1.0.0") | 1 |

Selection: targets come from the authors' own lists (PDFBox: Pankti, Table 3; GraphHopper: the RICK
replication package; PetClinic and Lab-Insurance: classes of MicroTestCarver, Table IV); the scenario
is an existing test of the project that reaches the target without naming it, found by counting the
target's invocations per test method under a debugger. Every case passed a debugger check on JDK 11
(stop at entry and exit of every invocation, arguments, fields of `this` and the return value read).
`cases/<id>/meta.yaml` keeps, per case, which tools are expected to apply (by their selection rules),
the evidence of the target and the debugger check; the runner does not read it.

## Running them

The projects run on JDK 11 (MicroTestCarver records only there). The pipeline runs one JDK per run, so
these cases have their own config:

```yaml
case_list: tools-projects.csv
jdk: 11
modes: [autonomous]
paths:
  java_home: /path/to/jdk-11.0.32.1+1    # scripts/install_jdk11.sh <dir>
```

`scripts/run.py` refuses a run whose `jdk` differs from a project's `jdk` in projects.yaml, or whose
`paths.java_home` is another JDK, or whose `JAVA_HOME` (the tracer and PIT use it) differs from
`paths.java_home`. Then, as for the study: `scripts/setup.sh config.yaml` (tools, projects cloned and
built online once), `python3 -m evaluation.components --config config.yaml --extra` (component and
test-class lists of these projects into `evaluation/normalisation-extra.json`, kept apart from the
lists of the study), `python3 -m evaluation.reference --config config.yaml`, `scripts/run.py`.
`scripts/check_scenarios.py --config config.yaml` runs the preparation of a cell without the agent
(export, hidden tests removed, prebuild, scenario check) for every case.

Project specifics:

* **Lab-Insurance** has no aggregator pom at the repository root: the commands use `-f pricing-service`,
  and the project's `prebuild` (projects.yaml) installs the sibling modules `command-bus-api`,
  `command-bus` and `pricing-service-api` into the local Maven repository in every fresh workspace before
  the scenario. The scenario and the generated test run with `-DargLine=…` that raises the Micronaut
  HTTP client read timeout to 300 s and switches Consul and Zipkin off (they are not running; under the
  debugger the client otherwise times out after 10 s while the server thread is stopped).
* **PDFBox** downloads test PDFs in `generate-test-resources` (download-maven-plugin). The plugin keeps
  them in `<maven repository>/.cache/download-maven-plugin` with their checksums: once the project was
  built online (scripts/setup.sh), every offline workspace gets them from that cache.
* **PetClinic** runs `./mvnw` (Maven 3.8.2): the wrapper distribution must be in `$HOME/.m2/wrapper`
  of the command environment (downloaded by the online build of setup).
* **GraphHopper**: only the module `core` is used (`web-bundle` needs npm and `git://`).

Evaluation on JDK 11: the BTrace tracer, its helper jars (compiled with `--release 11`), the JDI server of
the autonomous mode and PIT 1.15.8 run on JDK 11 (checked: references of all 31 cases; PIT on PDFBox,
GraphHopper, PetClinic and Lab-Insurance with an existing test of the target class). PIT needs the JUnit
Platform launcher of the project's platform version in the Maven repository: Spring PetClinic uses 1.8.2
and Lab-Insurance 1.7.0, which the project builds do not download; scripts/setup.sh fetches them when
the case list is tools-projects.csv. `evaluation/normalisation-extra.json` masks `hashCode` for
Lab-Insurance (cached in the executable methods of Micronaut proxies, it differs between runs).
