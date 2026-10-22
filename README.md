# event-catalogue-java-utils
Java validator for GOV.UK One Login events prior to sending to TxMA. Works alongside the event catalogue.

## The event-catalogue schema artifact

The JSON schemas this validator checks events against are **not bundled** in this
library. They are published separately from the
[event-catalogue](https://github.com/govuk-one-login/event-catalogue) repository as a
Maven artifact (`uk.gov.di.model:event-catalogue-schema`) to GitHub Packages:

```
https://maven.pkg.github.com/govuk-one-login/event-catalogue
```

The schema is treated as a **provided (peer) dependency**: the consuming application
supplies it on the runtime classpath, choosing the schema version it needs. This lets
consumers control the catalogue version and avoids this library re-releasing on every
catalogue bump.

### Used by the tests

This project's own tests resolve the schema artifact so they can validate real events.
It is declared as a `testImplementation` dependency in `lib/build.gradle`:

```groovy
testImplementation 'uk.gov.di.model:event-catalogue-schema:51.1.1'
```

Resolving it (in tests or in a consuming build) requires GitHub Packages credentials,
read from Gradle properties (`gpr.user` / `gpr.key`) or the `GITHUB_ACTOR` /
`GITHUB_TOKEN` environment variables used in CI.

## Consuming this library

This library is itself published to GitHub Packages as
`uk.gov.di.model:event-catalogue-java-utils`:

```
https://maven.pkg.github.com/govuk-one-login/event-catalogue-java-utils
```

A consuming Gradle build needs the two GitHub Packages repositories (this library and
the schema), the library dependency, and an `event-catalogue-schema` version of its
choice on the runtime classpath:

```groovy
repositories {
    mavenCentral()
    maven {
        url = uri('https://maven.pkg.github.com/govuk-one-login/event-catalogue-java-utils')
        credentials {
            username = System.getenv('GITHUB_ACTOR')
            password = System.getenv('GITHUB_TOKEN')
        }
    }
    maven {
        url = uri('https://maven.pkg.github.com/govuk-one-login/event-catalogue')
        credentials {
            username = System.getenv('GITHUB_ACTOR')
            password = System.getenv('GITHUB_TOKEN')
        }
    }
}

dependencies {
    implementation 'uk.gov.di.model:event-catalogue-java-utils:<version>'
    // Provide the schema version your application targets.
    implementation 'uk.gov.di.model:event-catalogue-schema:<schema-version>'
}
```

Then validate an event before sending it to TxMA:

```java
import uk.gov.ValidateEvent.ValidateEvent;

// From a raw JSON string...
boolean ok = ValidateEvent.validateEvent(eventJson);

// ...or from an already-parsed Jackson JsonNode.
boolean ok = ValidateEvent.validateEvent(eventNode);
```

`validateEvent` returns `true` when the event matches its schema and `false`
otherwise (including for null/blank input, malformed JSON, a missing `event_name`,
or an unknown event name); it does not throw on bad input.

## Formatting

This project uses [Spotless](https://github.com/diffplug/spotless) for code formatting. To automatically fix formatting violations, run:

```bash
./gradlew spotlessApply
```
