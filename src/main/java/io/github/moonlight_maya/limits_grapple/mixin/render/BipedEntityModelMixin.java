package io.github.moonlight_maya.limits_grapple.mixin.render;

import io.github.moonlight_maya.limits_grapple.GrappleMod;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> implements ModelWithArms, ModelWithHead {

	@Shadow
	@Final
	public ModelPart rightArm;
	@Shadow
	@Final
	public ModelPart head;
	@Shadow
	public BipedEntityModel.ArmPose rightArmPose;
	@Shadow
	public BipedEntityModel.ArmPose leftArmPose;
	@Shadow
	@Final
	public ModelPart leftArm;
	@Unique
	private BipedEntityModel.ArmPose tempRightArmPose;
	@Unique
	private BipedEntityModel.ArmPose tempLeftArmPose;

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at=@At(value = "INVOKE",target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;animateArms(Lnet/minecraft/entity/LivingEntity;F)V",shift = At.Shift.AFTER))
	public void limits_grapple$temporarilySpyglassifyHands(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		tempRightArmPose = rightArmPose;
		tempLeftArmPose = leftArmPose;
		ItemStack rightHandStack = livingEntity.getMainArm() == Arm.RIGHT ? livingEntity.getMainHandStack() : livingEntity.getOffHandStack();
		ItemStack leftHandStack = livingEntity.getMainArm() == Arm.LEFT ? livingEntity.getMainHandStack() : livingEntity.getOffHandStack();
		if (rightHandStack.isOf(GrappleMod.GRAPPLE_ITEM)) {
			if (rightHandStack.getOrCreateNbt().getBoolean("Active")) {
				rightArmPose = BipedEntityModel.ArmPose.SPYGLASS;
			}
		}
		if (leftHandStack.isOf(GrappleMod.GRAPPLE_ITEM)) {
			if (leftHandStack.getOrCreateNbt().getBoolean("Active")) {
				leftArmPose = BipedEntityModel.ArmPose.SPYGLASS;
			}
		}
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at=@At("RETURN"))
	public void limits_grapple$unSpyglassifyHands(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		rightArmPose = tempRightArmPose;
		leftArmPose = tempLeftArmPose;
	}

	private boolean positionArmForGrapple(T entity, boolean left) {
		if (entity instanceof AbstractClientPlayerEntity playerEntity) {
			Arm arm = left ? Arm.LEFT : Arm.RIGHT;
			ModelPart armPart = left ? leftArm : rightArm;
			ItemStack itemStack = playerEntity.getMainArm() == arm ? playerEntity.getMainHandStack() : playerEntity.getOffHandStack();
			if (itemStack.isOf(GrappleMod.GRAPPLE_ITEM)) {
				NbtCompound tag = itemStack.getOrCreateNbt();
				if (tag.getBoolean("Active")) {
					Vec3d anchor = new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
					float tickDelta = MinecraftClient.getInstance().getTickDelta();
					Vec3d armPivot = playerEntity.getLerpedPos(tickDelta).add(0, 22*0.875/16, 0);
					float entityYaw = (float) Math.toRadians(MathHelper.lerp(tickDelta, playerEntity.prevBodyYaw, playerEntity.bodyYaw));
					Vec3d sideVec = new Vec3d(-Math.cos(entityYaw), 0, -Math.sin(entityYaw));
					if (left) sideVec = sideVec.multiply(-1);
					armPivot = armPivot.add(sideVec.multiply(5*0.875/16));
					Vec3d diffVec = anchor.subtract(armPivot).normalize();
					armPart.yaw = (float) (Math.atan2(diffVec.z, diffVec.x) - Math.PI/2 - entityYaw);
					armPart.pitch = (float) (-Math.asin(diffVec.y) - 1.578f);
				} else {
					armPart.yaw = head.yaw;
					armPart.pitch = head.pitch - 1.5f;
				}
				return true;
			}
		}
		return false;
	}

	@Inject(method = "positionRightArm", at=@At("HEAD"), cancellable = true)
	public void limits_grapple$positionRightArmForGrapple(T entity, CallbackInfo ci) {
		if (positionArmForGrapple(entity, false))
			ci.cancel();
	}

	@Inject(method = "positionLeftArm", at=@At("HEAD"), cancellable = true)
	public void limits_grapple$positionLeftArmForGrapple(T entity, CallbackInfo ci) {
		if (positionArmForGrapple(entity, true))
			ci.cancel();
	}


}
