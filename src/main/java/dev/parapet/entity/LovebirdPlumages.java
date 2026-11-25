package dev.parapet.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class LovebirdPlumages {
    private static final String CATALOG_PATH = "/data/parapet/plumage/lovebird.json";
    private static final Map<String, String> SPECIES_BY_ID = readCatalog();
    public static final List<String> IDS = List.copyOf(SPECIES_BY_ID.keySet());

    private LovebirdPlumages() {}

    public static String speciesOf(String id) {
        return Objects.requireNonNull(SPECIES_BY_ID.get(id), () -> "Unknown lovebird plumage: " + id);
    }

    private static Map<String, String> readCatalog() {
        InputStream stream = Objects.requireNonNull(LovebirdPlumages.class.getResourceAsStream(CATALOG_PATH), CATALOG_PATH);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Map<String, String> speciesById = new LinkedHashMap<>();
            for (JsonElement entry : JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("variants")) {
                JsonObject variant = entry.getAsJsonObject();
                String id = variant.get("id").getAsString();
                String species = variant.get("species").getAsString();
                if (!id.matches("(peach|fischer|masked)_[a-z0-9_]+") || !species.equals(id.substring(0, id.indexOf('_')))
                        || speciesById.putIfAbsent(id, species) != null) {
                    throw new IllegalStateException("Invalid lovebird catalogue entry: " + id);
                }
            }
            if (!"peach_green".equals(speciesById.keySet().stream().findFirst().orElse(null))) {
                throw new IllegalStateException("The first lovebird plumage must be peach_green");
            }
            return Collections.unmodifiableMap(speciesById);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + CATALOG_PATH, e);
        }
    }
}
