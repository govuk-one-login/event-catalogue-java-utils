package uk.gov.ValidateEvent;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;


public final class SchemaLoader {

    private static final String SCHEMA_RESOURCE_DIR = "/uk/gov/di/model/schema/";

    private SchemaLoader() {
    }


    public static boolean isEvent(String eventName) {
        if (eventName == null || eventName.isBlank()) {
            return false;
        }
        try (InputStream input = openSchema(eventName)) {
            return nonNull(input);
        } catch (IOException e) {
            return false;
        }
    }

    
    public static String getSchemaAsString(String eventName) {
        if (eventName == null || eventName.isBlank()) {
            return null;
        }
        try (InputStream input = openSchema(eventName)) {
            if (nonNull(input)) {
                return new String(input.readAllBytes(), UTF_8);
            }
        } catch (IOException ignored) {
            // treat unreadable schema as absent
        }
        return null;
    }

    private static InputStream openSchema(String eventName) {
        return SchemaLoader.class.getResourceAsStream(SCHEMA_RESOURCE_DIR + eventName + ".json");
    }
}
