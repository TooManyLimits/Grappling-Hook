package io.github.moonlight_maya.limits_grapple;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import javax.swing.text.Style;

public class GrappleItem extends Item {
	public GrappleItem(Settings settings) {
		super(settings);
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 10_000_000;
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		NbtCompound tag = stack.getOrCreateNbt();
		//If the item is not active, or we missed the shot, then do not affect the player.
		if (!tag.getBoolean("Active") || !tag.getBoolean("Hit"))
			return;

		int ticksElapsed = getMaxUseTime(stack) - remainingUseTicks;
		if (world.isClient && user instanceof ClientPlayerEntity cpe)
			affectClientPlayer(cpe, tag, ticksElapsed);

		//Decide if we want to break the grapple
		Vec3d diff = new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")).subtract(user.getEyePos());
		double dotProd = diff.normalize().dotProduct(user.getRotationVector());
		if (dotProd < -0.4)
			disconnectGrapple(user, stack);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		onStoppedUsing(stack, world, user, 0);
		return stack;
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		disconnectGrapple(user, stack);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
		//Perform raycast
		ItemStack grappleItem = playerEntity.getStackInHand(hand);
		double maxRange = 64;
		Vec3d startVec = playerEntity.getEyePos();
		Vec3d diffVec = playerEntity.getRotationVector().multiply(maxRange);
		Vec3d endVec = startVec.add(diffVec);
		BlockHitResult result = world.raycast(new RaycastContext(startVec, endVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, playerEntity));

		//Check for dual wield behavior
		if (playerEntity.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND).isOf(GrappleMod.GRAPPLE_ITEM)) {
			boolean isThisInRightHand = (hand == Hand.MAIN_HAND) == (playerEntity.getMainArm() == Arm.RIGHT);

			Vec3d playerVel;
			if (world.isClient)
				playerVel = playerEntity.getVelocity();
			else
				playerVel = ServerPlayerVelocityHelper.getVelocity((ServerPlayerEntity) playerEntity);

			boolean swingingRight = playerVel.crossProduct(diffVec).y > 0; //swinging around the right side of something, so we should use left hand

			if (isThisInRightHand == swingingRight)
				return TypedActionResult.pass(grappleItem);
		}

		fireGrapple(playerEntity, grappleItem, result.getPos(), result.getType() != HitResult.Type.MISS);

		return ItemUsage.consumeHeldItem(world, playerEntity, hand);
	}

	public static final double ACCEL = 0.1;// * 3;
	public static final double JERK = 0.03 / 20;
	public static final double LOOK_COMPONENT = 0.15;// * 3;
	public static final double INPUT_COMPONENT = 0.25;// * 3;
	public static final double MAX_SPEED = 2.0;// * 3;
	public static final double GRAPPLE_FIRE_SPEED = 10.0;
	private static void affectClientPlayer(ClientPlayerEntity clientPlayerEntity, NbtCompound stackTag, int ticksElapsed) {
		//Get some useful vectors:
		Vec3d anchorPoint = new Vec3d(stackTag.getDouble("X"), stackTag.getDouble("Y"), stackTag.getDouble("Z"));
		Vec3d playerPos = clientPlayerEntity.getEyePos();

		//Determine if the grapple has been launched far enough yet to hit the endpoint and start pulling.
		ticksElapsed -= playerPos.distanceTo(anchorPoint) / GRAPPLE_FIRE_SPEED - 1;
		if (ticksElapsed <= 0)
			return;

		//Unit vector from player towards anchor.
		Vec3d pullVector = anchorPoint.subtract(playerPos).normalize();
		//Unit vector in the direction the player is looking.
		Vec3d lookVec = clientPlayerEntity.getRotationVector();
		//Unit vector pointing right from the player's point of view.
		double yaw = Math.toRadians(clientPlayerEntity.getYaw());
		Vec3d rightVec = new Vec3d(Math.cos(yaw), 0, Math.sin(yaw));

		//Strength of the pull force
		double pullForce = ACCEL + JERK * ticksElapsed;

		//Get velocity, add each component, scaled in the proper ways.
		Vec3d vel = clientPlayerEntity.getVelocity()
				.add(pullVector.multiply(pullForce))
				.add(lookVec.multiply(LOOK_COMPONENT))
				.add(lookVec.multiply(INPUT_COMPONENT * clientPlayerEntity.input.forwardMovement))
				.add(rightVec.multiply(INPUT_COMPONENT * clientPlayerEntity.input.sidewaysMovement));

		//Clamp velocity to MAX_SPEED
		double clamped = Math.min(vel.length(), MAX_SPEED);
		vel = vel.normalize().multiply(clamped);
		clientPlayerEntity.setVelocity(vel);
	}

	private static void fireGrapple(LivingEntity entity, ItemStack stack, Vec3d endPoint, boolean hit) {
		//Throw this in here until i can make it work nicely without hitting anything
		if (!hit)
			return;

		entity.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 0.6f);
		entity.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 0.9f);
		entity.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 1.2f);

		NbtCompound tag = stack.getOrCreateNbt();
		tag.putDouble("X", endPoint.x);
		tag.putDouble("Y", endPoint.y);
		tag.putDouble("Z", endPoint.z);
		tag.putBoolean("Active", true);
		tag.putBoolean("Hit", hit);
	}

	private static void disconnectGrapple(LivingEntity entity, ItemStack stack) {
		entity.playSound(SoundEvents.BLOCK_BARREL_CLOSE, 1.0f, 2f);

		NbtCompound tag = stack.getOrCreateNbt();
		tag.putBoolean("Active", false);
	}
}
