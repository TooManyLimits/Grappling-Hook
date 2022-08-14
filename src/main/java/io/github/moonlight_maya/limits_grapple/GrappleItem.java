package io.github.moonlight_maya.limits_grapple;

import io.github.moonlight_maya.limits_grapple.physics.GrapplePhysics;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class GrappleItem extends Item {
	public GrappleItem(Settings settings) {
		super(settings);
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 30 * 20;
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		NbtCompound tag = stack.getOrCreateNbt();
		if (tag.contains("X")) {
			Vec3d anchor = new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
			int newStability = GrapplePhysics.handlePhysics(user, anchor, getMaxUseTime(stack) - remainingUseTicks, tag.getInt("Stability"));
			if (newStability <= 0)
				clearEndpoint(stack);
			else
				tag.putInt("Stability", newStability);
		}
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
//		user.playSound(SoundEvents.BLOCK_BARREL_CLOSE, 1.0f, 0.6f);
//		user.playSound(SoundEvents.BLOCK_BARREL_CLOSE, 1.0f, 0.9f);
//		user.playSound(SoundEvents.BLOCK_BARREL_CLOSE, 1.0f, 1.2f);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
		playerEntity.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 0.6f);
		playerEntity.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 0.9f);
		playerEntity.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 1.2f);

		if (!world.isClient) {
			ItemStack grappleItem = playerEntity.getStackInHand(hand);
			float maxRange = 64f;
			Vec3d startVec = playerEntity.getEyePos();
			Vec3d endVec = playerEntity.getRotationVector().multiply(maxRange).add(startVec);
			BlockHitResult result = world.raycast(new RaycastContext(startVec, endVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, playerEntity));
			if (result.getType() == HitResult.Type.MISS) {
				clearEndpoint(grappleItem);
			} else {
				storeEndpoint(grappleItem, result.getPos());
			}

		}

		return ItemUsage.consumeHeldItem(world, playerEntity, hand);
	}

	private static void storeEndpoint(ItemStack stack, Vec3d endPoint) {
		NbtCompound tag = stack.getOrCreateNbt();
		tag.putDouble("X", endPoint.x);
		tag.putDouble("Y", endPoint.y);
		tag.putDouble("Z", endPoint.z);
		tag.putInt("Stability", GrapplePhysics.DEFAULT_STABILITY);
	}

	private static void clearEndpoint(ItemStack stack) {
		NbtCompound tag = stack.getOrCreateNbt();
		tag.remove("X");
		tag.remove("Y");
		tag.remove("Z");
		tag.remove("Stability");
	}
}
