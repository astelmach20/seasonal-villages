package com.codex.seasonalvillages;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public final class SeasonalVillageManager {
    private SeasonalVillageManager() {
    }

    public static void tickWorld(ServerWorld world) {
        SeasonalVillageConfig config = SeasonalVillageConfig.get();
        if (!World.OVERWORLD.equals(world.getRegistryKey())) {
            return;
        }

        if (world.getTime() % config.tickInterval() != 0) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            tickNearPlayer(world, player);
        }
    }

    public static VillageSnapshot snapshot(ServerWorld world, BlockPos center) {
        SeasonalVillageConfig config = SeasonalVillageConfig.get();
        Season season = Season.fromWorldTime(world.getTimeOfDay(), config.daysPerSeason());
        long yearIndex = Season.yearIndex(world.getTimeOfDay(), config.daysPerSeason());
        int nearbyVillagers = findVillagers(world, center).size();

        return new VillageSnapshot(
                season,
                yearIndex,
                nearbyVillagers,
                nearbyVillagers >= config.minimumVillagersForVillage()
        );
    }

    private static void tickNearPlayer(ServerWorld world, ServerPlayerEntity player) {
        SeasonalVillageConfig config = SeasonalVillageConfig.get();
        VillageSnapshot snapshot = snapshot(world, player.getBlockPos());
        if (!snapshot.activeVillage()) {
            return;
        }

        List<VillagerEntity> villagers = findVillagers(world, player.getBlockPos());
        if (config.villagerBuffs()) {
            applySeasonalCare(snapshot.season(), villagers);
        }

        ChunkPos chunkPos = new ChunkPos(player.getBlockPos());
        String worldId = world.getRegistryKey().getValue().toString();
        SeasonalVillageMemory memory = SeasonalVillageMemory.get(world.getPersistentStateManager());
        if (memory.shouldDecorate(worldId, chunkPos.x, chunkPos.z, snapshot.season(), snapshot.yearIndex())) {
            if (config.seasonalDecorations()) {
                decorateVillage(world, player.getBlockPos(), snapshot.season());
            }
            if (config.seasonalGifts()) {
                shareSeasonalGoods(world, villagers, snapshot.season());
            }
        }
    }

    private static List<VillagerEntity> findVillagers(ServerWorld world, BlockPos center) {
        SeasonalVillageConfig config = SeasonalVillageConfig.get();
        Box searchArea = Box.of(
                Vec3d.ofCenter(center),
                config.villageRadius() * 2.0,
                32.0,
                config.villageRadius() * 2.0
        );

        return world.getEntitiesByType(EntityType.VILLAGER, searchArea, VillagerEntity::isAlive);
    }

    private static void applySeasonalCare(Season season, List<VillagerEntity> villagers) {
        for (VillagerEntity villager : villagers) {
            switch (season) {
                case SPRING -> villager.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 260, 0, true, false));
                case SUMMER -> villager.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 260, 0, true, false));
                case AUTUMN -> villager.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 260, 0, true, false));
                case WINTER -> villager.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 260, 0, true, false));
            }
        }
    }

    private static void decorateVillage(ServerWorld world, BlockPos center, Season season) {
        SeasonalVillageConfig config = SeasonalVillageConfig.get();
        Random random = world.getRandom();
        int placed = 0;

        for (int attempt = 0; attempt < config.decorationAttempts()
                && placed < config.maxDecorationsPerPass(); attempt++) {
            BlockPos pos = topAirPosNear(world, center, random, config.decorationRadius());
            if (pos == null) {
                continue;
            }

            BlockState state = decorationFor(season, random);
            if (state.canPlaceAt(world, pos) && world.setBlockState(pos, state, Block.NOTIFY_LISTENERS)) {
                placed++;
                world.spawnParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5,
                        pos.getY() + 0.75,
                        pos.getZ() + 0.5,
                        2,
                        0.25,
                        0.2,
                        0.25,
                        0.01
                );
            }
        }
    }

    private static BlockPos topAirPosNear(ServerWorld world, BlockPos center, Random random, int radius) {
        int x = center.getX() + random.nextInt(radius * 2 + 1) - radius;
        int z = center.getZ() + random.nextInt(radius * 2 + 1) - radius;
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos support = pos.down();

        if (!world.isAir(pos)) {
            return null;
        }

        BlockState supportState = world.getBlockState(support);
        if (!supportState.isSideSolidFullSquare(world, support, Direction.UP) || !isVillageDecorationSupport(supportState)) {
            return null;
        }

        return pos;
    }

    private static boolean isVillageDecorationSupport(BlockState state) {
        return state.isOf(Blocks.GRASS_BLOCK)
                || state.isOf(Blocks.DIRT)
                || state.isOf(Blocks.COARSE_DIRT)
                || state.isOf(Blocks.ROOTED_DIRT)
                || state.isOf(Blocks.PODZOL)
                || state.isOf(Blocks.MYCELIUM)
                || state.isOf(Blocks.DIRT_PATH)
                || state.isOf(Blocks.STONE)
                || state.isOf(Blocks.COBBLESTONE)
                || state.isOf(Blocks.MOSSY_COBBLESTONE)
                || state.isOf(Blocks.STONE_BRICKS)
                || state.isOf(Blocks.OAK_PLANKS)
                || state.isOf(Blocks.SPRUCE_PLANKS)
                || state.isOf(Blocks.BIRCH_PLANKS)
                || state.isOf(Blocks.SAND)
                || state.isOf(Blocks.RED_SAND);
    }

    private static BlockState decorationFor(Season season, Random random) {
        return switch (season) {
            case SPRING -> random.nextBoolean()
                    ? Blocks.POPPY.getDefaultState()
                    : Blocks.DANDELION.getDefaultState();
            case SUMMER -> random.nextBoolean()
                    ? Blocks.TORCH.getDefaultState()
                    : Blocks.LANTERN.getDefaultState();
            case AUTUMN -> random.nextBoolean()
                    ? Blocks.PUMPKIN.getDefaultState()
                    : Blocks.HAY_BLOCK.getDefaultState();
            case WINTER -> random.nextBoolean()
                    ? Blocks.SNOW.getDefaultState()
                    : Blocks.LANTERN.getDefaultState();
        };
    }

    private static void shareSeasonalGoods(ServerWorld world, List<VillagerEntity> villagers, Season season) {
        if (villagers.isEmpty()) {
            return;
        }

        Random random = world.getRandom();
        int giftCount = Math.min(3, villagers.size());
        for (int i = 0; i < giftCount; i++) {
            VillagerEntity villager = villagers.get(random.nextInt(villagers.size()));
            ItemStack gift = giftFor(season, random);
            ItemEntity entity = new ItemEntity(world, villager.getX(), villager.getY() + 0.5, villager.getZ(), gift);
            entity.setPickupDelay(20);
            world.spawnEntity(entity);
        }
    }

    private static ItemStack giftFor(Season season, Random random) {
        return switch (season) {
            case SPRING -> new ItemStack(random.nextBoolean() ? Items.BEETROOT_SEEDS : Items.WHEAT_SEEDS, 2 + random.nextInt(3));
            case SUMMER -> new ItemStack(random.nextBoolean() ? Items.MELON_SLICE : Items.SWEET_BERRIES, 2 + random.nextInt(3));
            case AUTUMN -> new ItemStack(random.nextBoolean() ? Items.BREAD : Items.PUMPKIN_PIE, 1 + random.nextInt(2));
            case WINTER -> new ItemStack(random.nextBoolean() ? Items.BAKED_POTATO : Items.COOKIE, 1 + random.nextInt(2));
        };
    }
}
