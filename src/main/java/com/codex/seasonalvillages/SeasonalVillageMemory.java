package com.codex.seasonalvillages;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public final class SeasonalVillageMemory extends PersistentState {
    private static final String STATE_ID = SeasonalVillagesMod.MOD_ID + "_memory";
    private static final String DECORATED_CHUNKS_KEY = "decoratedChunks";
    private static final String CHUNK_KEY = "chunk";
    private static final String MARKER_KEY = "marker";

    private static final Type<SeasonalVillageMemory> TYPE = new Type<>(
            SeasonalVillageMemory::new,
            SeasonalVillageMemory::fromNbt,
            null
    );

    private final Map<String, String> decoratedChunkMarkers = new HashMap<>();

    public static SeasonalVillageMemory get(PersistentStateManager stateManager) {
        return stateManager.getOrCreate(TYPE, STATE_ID);
    }

    private static SeasonalVillageMemory fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        SeasonalVillageMemory memory = new SeasonalVillageMemory();
        NbtList chunks = nbt.getList(DECORATED_CHUNKS_KEY, NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < chunks.size(); i++) {
            NbtCompound entry = chunks.getCompound(i);
            String chunk = entry.getString(CHUNK_KEY);
            String marker = entry.getString(MARKER_KEY);
            if (!chunk.isBlank() && !marker.isBlank()) {
                memory.decoratedChunkMarkers.put(chunk, marker);
            }
        }

        return memory;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList chunks = new NbtList();
        decoratedChunkMarkers.forEach((chunk, marker) -> {
            NbtCompound entry = new NbtCompound();
            entry.putString(CHUNK_KEY, chunk);
            entry.putString(MARKER_KEY, marker);
            chunks.add(entry);
        });
        nbt.put(DECORATED_CHUNKS_KEY, chunks);
        return nbt;
    }

    public boolean shouldDecorate(String worldId, int chunkX, int chunkZ, Season season, long yearIndex) {
        String chunkKey = worldId + ":" + chunkX + ":" + chunkZ;
        String marker = season.id() + ":" + yearIndex;
        String previous = decoratedChunkMarkers.put(chunkKey, marker);
        boolean shouldDecorate = !marker.equals(previous);
        if (shouldDecorate) {
            markDirty();
        }
        return shouldDecorate;
    }

    public int decoratedChunkCount() {
        return decoratedChunkMarkers.size();
    }
}
