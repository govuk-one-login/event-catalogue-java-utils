package uk.gov.ValidateEvent;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class ValidateEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateEvent.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // JSON Schema draft 2019-09, matching the catalogue schemas and the TS Ajv2019 validator.
    private static final JsonSchemaFactory SCHEMA_FACTORY =
            JsonSchemaFactory.getInstance(VersionFlag.V201909);

    private ValidateEvent() {
    }

    
    public static boolean validateEvent(String eventJson) {
        if (eventJson == null || eventJson.isBlank()) {
            LOGGER.error("Event validation failed: event payload was null or empty");
            return false;
        }
        try {
            return validateEvent(MAPPER.readTree(eventJson));
        } catch (Exception e) {
            // Never block the event: a parse failure is logged and treated as "not matching".
            LOGGER.error("Event validation failed: could not parse event JSON", e);
            return false;
        }
    }

    
    public static boolean validateEvent(JsonNode event) {
        try {
            if (event == null || !event.hasNonNull("event_name")) {
                LOGGER.error("Event validation failed: missing event_name");
                return false;
            }

            String eventName = event.get("event_name").asText();

            String schemaJson = SchemaLoader.getSchemaAsString(eventName);
            if (schemaJson == null) {
                LOGGER.error("Invalid event_name: no schema found for '{}'", eventName);
                return false;
            }

            JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaJson);
            Set<ValidationMessage> errors = schema.validate(event);

            if (errors.isEmpty()) {
                LOGGER.info("Event '{}' matches its schema", eventName);
                return true;
            }

            LOGGER.error("Event '{}' does not match its schema: {}", eventName, errors);
            return false;
        } catch (Exception e) {
            // The validator must never throw and never block the event reaching TxMA.
            LOGGER.error("Event validation failed unexpectedly; treating event as not matching", e);
            return false;
        }
    }
}
