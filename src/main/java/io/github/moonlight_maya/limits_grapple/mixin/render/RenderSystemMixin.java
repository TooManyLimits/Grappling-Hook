package io.github.moonlight_maya.limits_grapple.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moonlight_maya.limits_grapple.GrappleMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

	@Inject(method = "clear", at=@At("HEAD"), cancellable = true, remap = false)
	private static void limits_grapple$perhapsNotClear(int i, boolean bl, CallbackInfo ci) {
		if (GrappleMod.DISABLING_RENDERSYSTEM_CLEAR)
			ci.cancel();
	}

}
