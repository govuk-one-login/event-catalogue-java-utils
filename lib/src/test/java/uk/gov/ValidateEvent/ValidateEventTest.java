package uk.gov.ValidateEvent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidateEventTest {

    @Test
    void returnsTrueForEventMatchingItsSchema() {
        // Valid AUTH_LOG_IN_SUCCESS: all required fields present, correct types,
        // and no additional (disallowed) top-level properties.
        String event = """
                {
                  "event_name": "AUTH_LOG_IN_SUCCESS",
                  "timestamp": 1700000000,
                  "event_timestamp_ms": 1700000000000,
                  "component_id": "https://auth.account.gov.uk"
                }
                """;

        assertTrue(ValidateEvent.validateEvent(event),
                "A well-formed AUTH_LOG_IN_SUCCESS event should match its schema");
    }

    @Test
    void returnsFalseWhenEventDoesNotMatchSchema() {
        // Missing required fields (timestamp, component_id, event_timestamp_ms)
        // and a wrong type for event_timestamp_ms would also fail; here we omit requireds.
        String event = """
                {
                  "event_name": "AUTH_LOG_IN_SUCCESS"
                }
                """;

        assertFalse(ValidateEvent.validateEvent(event),
                "An event missing required fields should not match its schema");
    }

    @Test
    void returnsFalseForWrongFieldType() {
        // event_timestamp_ms must be an integer, not a string.
        String event = """
                {
                  "event_name": "AUTH_LOG_IN_SUCCESS",
                  "timestamp": 1700000000,
                  "event_timestamp_ms": "not-a-number",
                  "component_id": "https://auth.account.gov.uk"
                }
                """;

        assertFalse(ValidateEvent.validateEvent(event),
                "An event with a wrong field type should not match its schema");
    }

    @Test
    void returnsFalseForUnknownEventName() {
        String event = """
                {
                  "event_name": "THIS_EVENT_DOES_NOT_EXIST",
                  "timestamp": 1700000000
                }
                """;

        assertFalse(ValidateEvent.validateEvent(event),
                "An event with an unknown event_name has no schema and should not validate");
    }

    @Test
    void returnsFalseWhenEventNameMissing() {
        String event = """
                {
                  "timestamp": 1700000000
                }
                """;

        assertFalse(ValidateEvent.validateEvent(event),
                "An event without event_name cannot be matched to a schema");
    }

    @Test
    void neverThrowsOnMalformedJson() {
        assertDoesNotThrow(() -> ValidateEvent.validateEvent("{ this is not valid json"));
        assertFalse(ValidateEvent.validateEvent("{ this is not valid json"),
                "Malformed JSON should be treated as not matching, not throw");
    }

    @Test
    void neverThrowsOnNullOrEmpty() {
        assertDoesNotThrow(() -> ValidateEvent.validateEvent((String) null));
        assertFalse(ValidateEvent.validateEvent((String) null));
        assertFalse(ValidateEvent.validateEvent(""));
        assertFalse(ValidateEvent.validateEvent("   "));
    }

    @Test
    void schemaLoaderRecognisesKnownAndUnknownEvents() {
        assertTrue(SchemaLoader.isEvent("AUTH_LOG_IN_SUCCESS"));
        assertFalse(SchemaLoader.isEvent("THIS_EVENT_DOES_NOT_EXIST"));
        assertFalse(SchemaLoader.isEvent(null));
        assertFalse(SchemaLoader.isEvent(""));
    }
}
