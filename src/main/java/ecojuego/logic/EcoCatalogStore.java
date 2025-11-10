
package src.main.java.ecojuego.logic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class EcoCatalogStore {

    private static final Path DATA_DIR = Path.of("data");
    private static final Path FILE = DATA_DIR.resolve("catalog.db");

    private EcoCatalogStore() {
    }

    public static synchronized List<EcoItem> load() {
        ensureDir();
        if (Files.notExists(FILE)) {
            List<EcoItem> defaults = new ArrayList<>(EcoData.defaultItems());
            save(defaults);
            return defaults;
        }
        List<EcoItem> items = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split("\\|", -1);
                if (parts.length != 4) {
                    continue;
                }
                Category category = parseCategory(parts[0]);
                if (category == null) {
                    continue;
                }
                String description = decode(parts[1]);
                String reason = decode(parts[2]);
                String handling = decode(parts[3]);
                if (description.isBlank()) {
                    continue;
                }
                items.add(new EcoItem(description, category, reason, handling));
            }
        } catch (IOException ignored) {
            return new ArrayList<>(EcoData.defaultItems());
        }
        if (items.isEmpty()) {
            items.addAll(EcoData.defaultItems());
        }
        return items;
    }

    public static synchronized void save(List<EcoItem> items) {
        ensureDir();
        List<String> lines = new ArrayList<>();
        lines.add("# EcoJuego catalog");
        for (EcoItem item : items) {
            lines.add(item.category().name() + "|"
                + encode(item.description()) + "|"
                + encode(item.reason()) + "|"
                + encode(item.handling()));
        }
        try {
            Files.write(
                FILE,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
        } catch (IOException ignored) {
        }
    }

    private static void ensureDir() {
        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException ignored) {
        }
    }

    private static Category parseCategory(String value) {
        try {
            return Category.valueOf(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String encode(String value) {
        String safe = value == null ? "" : value;
        return Base64.getUrlEncoder().encodeToString(safe.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String encoded) {
        try {
            return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }
}
