package com.codex.seasonalvillages;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SeasonalVillagesMod implements ModInitializer {
    public static final String MOD_ID = "seasonal_villages";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Item VILLAGE_ALMANAC = Registry.register(
            Registries.ITEM,
            id("village_almanac"),
            new VillageAlmanacItem(new Item.Settings().maxCount(1))
    );

    @Override
    public void onInitialize() {
        SeasonalVillageConfig.load();
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(VILLAGE_ALMANAC));
        ServerTickEvents.END_WORLD_TICK.register(SeasonalVillageManager::tickWorld);
        LOGGER.info("Seasonal Villages initialized");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
