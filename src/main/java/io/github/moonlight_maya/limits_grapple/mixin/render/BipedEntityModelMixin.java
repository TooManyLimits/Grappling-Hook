package io.github.moonlight_maya.limits_grapple.mixin.render;

import io.github.moonlight_maya.limits_grapple.GrappleMod;
import io.github.moonlight_maya.limits_grapple.RenderingUtils;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Arm;
import net.minecraft.util.math.*;
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
	@Shadow
	public boolean sneaking;
	@Shadow
	public float leaningPitch;
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
					Vec3f transformedAnchor = RenderingUtils.getTransformedAnchorThirdPerson(playerEntity, anchor, left);
					transformedAnchor.normalize();
					armPart.yaw = (float) (Math.atan2(transformedAnchor.getZ(), transformedAnchor.getX()) - Math.PI / 2);
					armPart.pitch = (float) (-Math.asin(transformedAnchor.getY()) - Math.PI / 2);
				} else {
					armPart.yaw = head.yaw;
					armPart.pitch = head.pitch - 1.5f;
				}
				if (sneaking)
					armPart.pitch -= 0.4f;
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
