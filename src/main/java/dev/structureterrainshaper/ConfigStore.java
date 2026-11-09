package dev.structureterrainshaper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("structure_terrain_shaper.json");
    private static Config current = new Config(8, new LinkedHashMap<>());
    private ConfigStore() {}
    public static synchronized Config get() { return current; }
    public static synchronized void load() {
        if (!Files.exists(FILE)) { save(); return; }
        try {
            Config loaded = GSON.fromJson(Files.readString(FILE), Config.class);
            if (loaded != null) current = loaded.normalized();
        } catch (Exception e) { StructureTerrainShaper.LOGGER.error("Unable to read config", e); }
    }
    public static synchronized void update(Config next) { current = next.normalized(); save(); }
    private static void save() {
        try { Files.createDirectories(FILE.getParent()); Files.writeString(FILE, GSON.toJson(current)); }
        catch (IOException e) { StructureTerrainShaper.LOGGER.error("Unable to save config", e); }
    }
    public record Config(int blendDistance, Map<String, String> overrides) {
        public Config { overrides = overrides == null ? new LinkedHashMap<>() : new LinkedHashMap<>(overrides); }
        public Config normalized() { return new Config(Math.max(2, Math.min(24, blendDistance)), overrides); }
        public String overrideFor(String id) { return overrides.getOrDefault(id, "inherit"); }
        public Config withOverride(String id, String value) { Map<String,String> copy = new LinkedHashMap<>(overrides); copy.put(id, value); return new Config(blendDistance, copy); }
        public Config withBlendDistance(int value) { return new Config(value, overrides); }
    }
}
