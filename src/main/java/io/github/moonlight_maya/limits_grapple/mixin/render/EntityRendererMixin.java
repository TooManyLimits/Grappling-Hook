package io.github.moonlight_maya.limits_grapple.mixin.render;

import io.github.moonlight_maya.limits_grapple.GrappleMod;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	public void forceRenderWhileHolding(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof PlayerEntity player)
			if (player.isUsingItem() && player.getActiveItem().isOf(GrappleMod.GRAPPLE_ITEM))
				cir.setReturnValue(true);
	}

}
