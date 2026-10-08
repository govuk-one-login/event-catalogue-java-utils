# event-catalogue-java-utils
Java validator for GOV.UK One Login events prior to sending to TxMA. Works alongside the event catalogue.

## Usage

This library validates events against the GOV.UK One Login event-catalogue schemas but
does **not** bundle them. The consuming application must provide the schema artifact on
its runtime classpath (analogous to a peer dependency), choosing the schema version it
wants to validate against:

```gradle
implementation 'uk.gov.di.model:event-catalogue-schema:<version>'
```

`SchemaLoader` reads the schema JSON resources from the classpath at
`/uk/gov/di/model/schema/<EVENT_NAME>.json`. If the artifact is absent at runtime,
`validateEvent` finds no schema and returns `false` for every event (validation never
throws and never blocks the event).
