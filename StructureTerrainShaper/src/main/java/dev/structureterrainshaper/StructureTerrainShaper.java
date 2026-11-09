package dev.structureterrainshaper;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StructureTerrainShaper implements ModInitializer {
    public static final String MOD_ID = "structure_terrain_shaper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override public void onInitialize() {
        ConfigStore.load();
        LOGGER.info("Structure Terrain Shaper initialized");
    }
}
