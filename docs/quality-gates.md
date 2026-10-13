# PR Quality Gates

Every pull request into `main` must pass the quality gates below. They are
enforced by the [`PR Quality Gate`](.github/workflows/pr-quality-gate.yml)
GitHub Actions workflow, which runs on `pull_request` and on pushes to `main`.

| Gate | How it runs | Fails the build when |
|------|-------------|----------------------|
| Build | `./gradlew build` | Compilation errors |
| Unit tests | `test` task (part of `build`) | Any test fails |
| Linting | `checkstyleMain` / `checkstyleTest` (part of `check` → `build`) | Any Checkstyle violation (`maxWarnings = 0`) |
| Static analysis | `./gradlew sonar -Dsonar.qualitygate.wait=true` | SonarCloud quality gate is red |

Run everything locally before pushing:

```bash
./gradlew build          # compile + test + checkstyle
./gradlew sonar          # static analysis (needs SONAR_TOKEN)
```

## Checkstyle (linting)

- Plugin applied in [`lib/build.gradle`](lib/build.gradle).
- Rule set: [`config/checkstyle/checkstyle.xml`](config/checkstyle/checkstyle.xml).
- Reports: `lib/build/reports/checkstyle/`.

Adjust rules by editing the XML. To adopt gradually on a larger codebase,
temporarily raise `maxWarnings` in `lib/build.gradle`, then ratchet back to `0`.

## SonarCloud project setup

The build is already configured for SonarCloud (root `build.gradle`):

- Organization: `govuk-one-login`
- Project key: `govuk-one-login_event-catalogue-java-utils`
- Host: `https://sonarcloud.io`
- Coverage: JaCoCo XML from `:lib` is reported via `sonar.coverage.jacoco.xmlReportPaths`.

One-time setup (requires org admin on SonarCloud):

1. **Create the project**
   - Go to <https://sonarcloud.io> → the `govuk-one-login` organization → *Analyze new project*.
   - Select the `event-catalogue-java-utils` repository. Confirm the generated
     project key matches `govuk-one-login_event-catalogue-java-utils`.
2. **Choose the analysis method: "With GitHub Actions".**
3. **Create the `SONAR_TOKEN` secret**
   - In SonarCloud, generate a project analysis token (*My Account → Security*,
     or the per-project token shown during onboarding).
   - In GitHub: repo (or org) *Settings → Secrets and variables → Actions →
     New repository secret*, name `SONAR_TOKEN`, paste the token.
4. **Disable SonarCloud Automatic Analysis**
   - Project *Administration → Analysis Method* → turn **off** *Automatic
     Analysis*. It conflicts with CI-based analysis and will cause errors.
5. **Set the "New Code" definition**
   - Project *Administration → New Code* → *Previous version* (or *Number of
     days*) so PRs are measured against new code.
6. **Attach the quality gate**
   - *Administration → Quality Gate*. The default *Sonar way* gate is a good
     baseline (fails on new bugs, vulnerabilities, security hotspots, and when
     new-code coverage / duplication thresholds are not met).
   - `sonar.qualitygate.wait=true` in the workflow makes CI block until this
     gate's result is returned, so a red gate fails the PR check.

## Enforce the gate on merges (branch protection)

Running the checks is not enough — GitHub must *require* them to merge:

1. Repo *Settings → Branches → Add branch ruleset / protection rule* for `main`.
2. Enable **Require status checks to pass before merging**.
3. Add these required checks:
   - `Build, test, lint and analyze` (the workflow job)
   - `SonarCloud Code Analysis` (the check SonarCloud posts on the PR)
4. Enable **Require branches to be up to date before merging**.
5. (Recommended) **Require a pull request before merging** so direct pushes to
   `main` are blocked.

With this in place, a PR into `main` cannot be merged unless the build, unit
tests, Checkstyle linting, and the SonarCloud quality gate all pass.
