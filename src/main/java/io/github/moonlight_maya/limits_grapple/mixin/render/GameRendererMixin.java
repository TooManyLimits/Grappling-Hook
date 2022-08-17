package io.github.moonlight_maya.limits_grapple.mixin.render;

import io.github.moonlight_maya.limits_grapple.GrappleMod;
import io.github.moonlight_maya.limits_grapple.GrappleModClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Inject(method = "renderWorld", at=@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", shift = At.Shift.BEFORE))
	public void limits_grapple$perhapsDisableDepthClear(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
		Entity cameraOwner = MinecraftClient.getInstance().getCameraEntity();
		if (cameraOwner instanceof AbstractClientPlayerEntity playerEntity) {
			if (playerEntity.isUsingItem() && playerEntity.getActiveItem().isOf(GrappleMod.GRAPPLE_ITEM)) {
				GrappleModClient.DISABLE_DEPTH_CLEAR_FLAG = true;
			}
		}
	}

	@Inject(method = "renderWorld", at=@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", shift = At.Shift.AFTER))
	public void limits_grapple$reEnableDepthClear(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
		GrappleModClient.DISABLE_DEPTH_CLEAR_FLAG = false;
	}

}
