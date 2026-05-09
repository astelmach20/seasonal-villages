package com.codex.seasonalvillages;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class VillageAlmanacItem extends Item {
    public VillageAlmanacItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            VillageSnapshot snapshot = SeasonalVillageManager.snapshot(serverWorld, user.getBlockPos());
            user.sendMessage(Text.literal(snapshot.summary()), false);
        }

        return TypedActionResult.success(stack);
    }
}
