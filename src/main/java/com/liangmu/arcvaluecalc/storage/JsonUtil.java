package com.liangmu.arcvaluecalc.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.liangmu.arcvaluecalc.ArcValueCalc;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class JsonUtil {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private JsonUtil() {
    }

    public static JsonElement read(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }

    public static void write(Path path, JsonElement json) throws IOException {
        writeAtomic(path, json, false);
    }

    public static void writeAtomicWithBackup(Path path, JsonElement json) throws IOException {
        writeAtomic(path, json, true);
    }

    private static void writeAtomic(Path path, JsonElement json, boolean backup) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = parent == null
                ? Files.createTempFile(path.getFileName().toString(), ".tmp")
                : Files.createTempFile(parent, path.getFileName().toString(), ".tmp");
        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            GSON.toJson(json, writer);
        }
        try {
            if (backup && Files.exists(path)) {
                Path backupPath = path.resolveSibling(path.getFileName() + ".bak");
                Files.copy(path, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            ArcValueCalc.LOGGER.error("Failed to atomically write JSON file {}", path, e);
            throw e;
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
