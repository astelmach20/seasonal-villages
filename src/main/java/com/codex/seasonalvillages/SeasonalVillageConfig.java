package com.codex.seasonalvillages;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

public final class SeasonalVillageConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "seasonal-villages.json";

    private static SeasonalVillageConfig current = defaults();

    private int daysPerSeason = 8;
    private int villageRadius = 48;
    private int minimumVillagersForVillage = 3;
    private int tickInterval = 200;
    private int decorationRadius = 28;
    private int decorationAttempts = 80;
    private int maxDecorationsPerPass = 12;
    private boolean seasonalDecorations = true;
    private boolean seasonalGifts = true;
    private boolean villagerBuffs = true;

    public static SeasonalVillageConfig defaults() {
        return new SeasonalVillageConfig();
    }

    public static SeasonalVillageConfig get() {
        return current;
    }

    public static SeasonalVillageConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        SeasonalVillageConfig config = defaults();

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                SeasonalVillageConfig loaded = GSON.fromJson(reader, SeasonalVillageConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (IOException | RuntimeException exception) {
                SeasonalVillagesMod.LOGGER.warn("Failed to read {}, using defaults", path, exception);
            }
        }

        current = config.validate();
        writeCurrent(path);
        return current;
    }

    private static void writeCurrent(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(current, writer);
            }
        } catch (IOException exception) {
            SeasonalVillagesMod.LOGGER.warn("Failed to write {}", path, exception);
        }
    }

    private SeasonalVillageConfig() {
    }

    private SeasonalVillageConfig validate() {
        daysPerSeason = clamp(daysPerSeason, 1, 120);
        villageRadius = clamp(villageRadius, 12, 128);
        minimumVillagersForVillage = clamp(minimumVillagersForVillage, 1, 32);
        tickInterval = clamp(tickInterval, 20, 12_000);
        decorationRadius = clamp(decorationRadius, 4, villageRadius);
        decorationAttempts = clamp(decorationAttempts, 1, 512);
        maxDecorationsPerPass = clamp(maxDecorationsPerPass, 0, 64);
        return this;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public int daysPerSeason() {
        return daysPerSeason;
    }

    public int villageRadius() {
        return villageRadius;
    }

    public int minimumVillagersForVillage() {
        return minimumVillagersForVillage;
    }

    public int tickInterval() {
        return tickInterval;
    }

    public int decorationRadius() {
        return decorationRadius;
    }

    public int decorationAttempts() {
        return decorationAttempts;
    }

    public int maxDecorationsPerPass() {
        return maxDecorationsPerPass;
    }

    public boolean seasonalDecorations() {
        return seasonalDecorations;
    }

    public boolean seasonalGifts() {
        return seasonalGifts;
    }

    public boolean villagerBuffs() {
        return villagerBuffs;
    }
}
