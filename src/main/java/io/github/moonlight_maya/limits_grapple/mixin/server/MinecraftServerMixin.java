package io.github.moonlight_maya.limits_grapple.mixin.server;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disable fly kicking lmao
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(method = "isFlightEnabled", at=@At("RETURN"), cancellable = true)
	public void limits_grapple$isFlightEnabled(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}
}
