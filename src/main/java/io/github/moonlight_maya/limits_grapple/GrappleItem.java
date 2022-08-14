package io.github.moonlight_maya.limits_grapple;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class GrappleItem extends Item {
	public GrappleItem(Settings settings) {
		super(settings);
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 999999999; // :P
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		super.onStoppedUsing(stack, world, user, remainingUseTicks);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
		playerEntity.playSound(SoundEvents.BLOCK_BARREL_OPEN, 1.0f, 0.6f);
		playerEntity.playSound(SoundEvents.BLOCK_BARREL_OPEN, 1.0f, 0.9f);
		playerEntity.playSound(SoundEvents.BLOCK_BARREL_OPEN, 1.0f, 1.2f);
		return ItemUsage.consumeHeldItem(world, playerEntity, hand);
	}
}
