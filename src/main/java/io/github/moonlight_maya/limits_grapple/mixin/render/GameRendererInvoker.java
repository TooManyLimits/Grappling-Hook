package io.github.moonlight_maya.limits_grapple.mixin.render;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
	@Invoker("getFov")
	double limits_grapple$getFov(Camera camera, float tickDelta, boolean changingFov);
}