package io.github.moonlight_maya.limits_grapple.mixin.render;

import io.github.moonlight_maya.limits_grapple.GrappleMod;
import io.github.moonlight_maya.limits_grapple.GrappleModClient;
import io.github.moonlight_maya.limits_grapple.RenderingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

	@Shadow
	public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

	@Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at=@At("HEAD"))
	public void limits_grapple$storeCurrentRenderedPlayer(LivingEntity entity, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (entity instanceof AbstractClientPlayerEntity acpe)
			if (acpe.getActiveItem().isOf(GrappleMod.GRAPPLE_ITEM))
				GrappleModClient.currentRenderedPlayerEntity = acpe;
	}

	@Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at=@At("RETURN"))
	public void limits_grapple$restoreCurrentRenderedPlayer(LivingEntity entity, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		GrappleModClient.currentRenderedPlayerEntity = null;
	}

	@Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at=@At("HEAD"))
	public void limits_grapple$negateFirstPersonItemSway(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
		float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
		float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
		matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion((player.getYaw(tickDelta) - i) * 0.1F));
		matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion((player.getPitch(tickDelta) - h) * 0.1F));
	}


	@Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
	public void limits_grapple$setupFirstPersonArmRotation(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (!item.isOf(GrappleMod.GRAPPLE_ITEM))
			return;
		NbtCompound tag = item.getOrCreateNbt();
		if (!tag.getBoolean("Active"))
			return;

		matrices.push();
		Vec3d anchor = new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
		Vec3f transformedAnchor = RenderingUtils.transformWorldToView(anchor);

		//Stolen from applyEquipmentOffset
		boolean leftHand = (player.getMainArm() == Arm.LEFT) == (hand == Hand.MAIN_HAND);
		int i = leftHand ? -1 : 1;
		Vec3f diff = new Vec3f(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.7200000286102295f);
		Vec3f diffScaled = diff.copy();
		float scaleFactor = RenderingUtils.getSizeMultiplier(player, tickDelta);
		diffScaled.multiplyComponentwise(scaleFactor, -scaleFactor, scaleFactor);
		transformedAnchor.add(diffScaled);

		transformedAnchor.normalize();
		float pitchOffset = (float) (Math.asin(transformedAnchor.getY()));
		float yawOffset = (float) (Math.atan2(transformedAnchor.getX(), transformedAnchor.getZ()));


//		matrices.scale(scaleFactor, scaleFactor, scaleFactor);
		//Following translation is to move the item from the center of the screen
		//to the left or right side, depending on the hand holding it. This part
		//does not need to take the scale into account.
		matrices.translate(diff.getX(), diff.getY(), diff.getZ());
		matrices.translate(i*-1.0/16, 3.0/16, 0);
		matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(yawOffset));
		matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(pitchOffset));

		renderItem(player, item, leftHand ? ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND : ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND, leftHand, matrices, vertexConsumers, light);

		matrices.pop();

		ci.cancel();

	}

}
