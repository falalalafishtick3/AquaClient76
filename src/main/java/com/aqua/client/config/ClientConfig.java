package com.aqua.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path = FabricLoader.getInstance().getConfigDir().resolve("aquaclient.json");
    public HighlightSettings storage = new HighlightSettings();
    public HighlightSettings spawner = new HighlightSettings();

    public void load() {
        try {
            if (Files.exists(path)) {
                ClientConfig loaded = GSON.fromJson(Files.readString(path), ClientConfig.class);
                if (loaded != null) {
                    if (loaded.storage != null) storage = loaded.storage;
                    if (loaded.spawner != null) spawner = loaded.spawner;
                }
            }
        } catch (Exception ignored) { }
    }

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this));
        } catch (Exception ignored) { }
    }
}
