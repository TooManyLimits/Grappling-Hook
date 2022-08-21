package io.github.moonlight_maya.limits_grapple.item;

import io.github.moonlight_maya.limits_grapple.GrappleMod;
import io.github.moonlight_maya.limits_grapple.ServerPlayerVelocityHelper;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class GrappleItem extends Item implements FabricItem {
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
		if (!world.isClient && ticksElapsed == 8) { //Only decay durability after fired for 8 ticks
			final Hand useHand = user.getStackInHand(Hand.MAIN_HAND) == stack ? Hand.MAIN_HAND : Hand.OFF_HAND;
			stack.damage(1, user, p -> p.sendToolBreakStatus(useHand));
		}
		if (world.isClient && user instanceof ClientPlayerEntity cpe)
			affectClientPlayer(cpe, stack, ticksElapsed);

		//Decide if we want to break the grapple
		Vec3d diff = new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")).subtract(user.getEyePos());
		double dotProd = diff.normalize().dotProduct(user.getRotationVector());
		if (dotProd < -0.4)
			disconnectGrapple(user, stack);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return super.isEnchantable(stack);
	}

	@Override
	public int getEnchantability() {
		return ToolMaterials.DIAMOND.getEnchantability();
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

	public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
		return false;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
		ItemStack grappleItem = playerEntity.getStackInHand(hand);

		BlockHitResult result = GrappleItem.raycast(playerEntity, grappleItem);

		//Check for dual wield behavior
		boolean isDualWield = playerEntity.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND).isOf(GrappleMod.GRAPPLE_ITEM);
		if (isDualWield && hand == Hand.MAIN_HAND) {
			boolean isThisInRightHand = playerEntity.getMainArm() == Arm.RIGHT;

			Vec3d playerVel;
			if (world.isClient)
				playerVel = playerEntity.getVelocity();
			else
				playerVel = ServerPlayerVelocityHelper.getVelocity((ServerPlayerEntity) playerEntity);

			boolean swingingRight = playerVel.crossProduct(playerEntity.getRotationVector()).y > 0; //swinging around the right side of something, so we should use left hand

			if (isThisInRightHand == swingingRight) {
				ItemStack offHandItem = playerEntity.getOffHandStack();
				BlockHitResult offHandWouldHit = GrappleItem.raycast(playerEntity, offHandItem);
				if (offHandWouldHit.getType() != HitResult.Type.MISS && !world.getBlockState(offHandWouldHit.getBlockPos()).isIn(GrappleMod.NO_GRAPPLE_BLOCKS)) {

					return TypedActionResult.pass(grappleItem);
				}

			}
		}

		boolean hit = result.getType() != HitResult.Type.MISS && !world.getBlockState(result.getBlockPos()).isIn(GrappleMod.NO_GRAPPLE_BLOCKS);
		if (hit || !isDualWield || hand == Hand.OFF_HAND) {
			fireGrapple(playerEntity, grappleItem, result.getPos(), hit);
			return ItemUsage.consumeHeldItem(world, playerEntity, hand);
		}

		return TypedActionResult.pass(grappleItem);
	}

	public static BlockHitResult raycast(PlayerEntity user, ItemStack grappleItem) {
		//Get raycast range
		double range = RANGE_BASE + RANGE_PER_LEVEL * EnchantmentHelper.getLevel(GrappleMod.RANGE_ENCHANTMENT, grappleItem);
		range = Math.min(range, 108); //cap it at the max value, so /give'd grapples don't try to raycast absurd distance.

		//Perform raycast
		Vec3d startVec = user.getEyePos();
		Vec3d diffVec = user.getRotationVector().multiply(range);
		Vec3d endVec = startVec.add(diffVec);
		return user.world.raycast(new RaycastContext(startVec, endVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
	}

	public static final double RANGE_BASE = 48.0;
	public static final double RANGE_PER_LEVEL = 12.0;
	public static final double FIRE_SPEED_BASE = 6.0;//10.0;
	public static final double FIRE_SPEED_PER_LEVEL = 1.0;//2.5; //uses range enchant

	public static final double ACCEL_BASE = 0.1;
	public static final double ACCEL_PER_LEVEL = 0.04;
	public static final double JERK_BASE = 0.025 / 20;
	public static final double JERK_PER_LEVEL = 0.01 / 20;

	public static final double MAX_SPEED_BASE = 1.5;
	public static final double MAX_SPEED_PER_LEVEL = 0.3;

	public static final double LOOK_COMPONENT_BASE = 0.1;
	public static final double LOOK_COMPONENT_PER_LEVEL = 0.06;
	public static final double INPUT_COMPONENT_BASE = 0.175;
	public static final double INPUT_COMPONENT_PER_LEVEL = 0.1;


	private static void affectClientPlayer(ClientPlayerEntity clientPlayerEntity, ItemStack stack, int ticksElapsed) {
		NbtCompound stackTag = stack.getOrCreateNbt();

		//Get some useful vectors:
		Vec3d anchorPoint = new Vec3d(stackTag.getDouble("X"), stackTag.getDouble("Y"), stackTag.getDouble("Z"));
		Vec3d playerPos = clientPlayerEntity.getEyePos();

		//Determine if the grapple has been launched far enough yet to hit the endpoint and start pulling.
		double fireSpeed = FIRE_SPEED_BASE + FIRE_SPEED_PER_LEVEL * EnchantmentHelper.getLevel(GrappleMod.RANGE_ENCHANTMENT, stack);
		ticksElapsed -= playerPos.distanceTo(anchorPoint) / fireSpeed - 1;
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
		int enchLevel = EnchantmentHelper.getLevel(GrappleMod.ACCELERATION_ENCHANTMENT, stack);
		double accel = ACCEL_BASE + ACCEL_PER_LEVEL * enchLevel;
		double jerk = JERK_BASE + JERK_PER_LEVEL * enchLevel;
		double pullForce = accel + jerk * ticksElapsed;
		double lookComponent = LOOK_COMPONENT_BASE + LOOK_COMPONENT_PER_LEVEL * enchLevel;
		double inputComponent = INPUT_COMPONENT_BASE + INPUT_COMPONENT_PER_LEVEL * enchLevel;

		//Get velocity, add each component, scaled in the proper ways.
		Vec3d vel = clientPlayerEntity.getVelocity()
				.add(pullVector.multiply(pullForce))
				.add(lookVec.multiply(lookComponent + inputComponent * clientPlayerEntity.input.forwardMovement))
				.add(rightVec.multiply(inputComponent * clientPlayerEntity.input.sidewaysMovement));

		//Clamp velocity to MAX_SPEED
		double maxSpeed = MAX_SPEED_BASE + MAX_SPEED_PER_LEVEL * EnchantmentHelper.getLevel(GrappleMod.MAX_SPEED_ENCHANTMENT, stack);
		double clamped = Math.min(vel.length(), maxSpeed);
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
