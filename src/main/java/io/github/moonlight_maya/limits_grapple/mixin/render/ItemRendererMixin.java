package io.github.moonlight_maya.limits_grapple.mixin.render;

import io.github.moonlight_maya.limits_grapple.GrappleMod;
import io.github.moonlight_maya.limits_grapple.GrappleModClient;
import io.github.moonlight_maya.limits_grapple.RenderingUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

	@Inject(
			method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", shift = At.Shift.AFTER))
	public void limits_grapple$renderChains(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if (!stack.isOf(GrappleMod.GRAPPLE_ITEM)) return;
		NbtCompound tag = stack.getOrCreateNbt();
		if (!tag.getBoolean("Active")) return;
		AbstractClientPlayerEntity cpe = GrappleModClient.currentRenderedPlayerEntity;
		if (cpe == null) return;

		Vec3d anchor = new Vec3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));

		switch (renderMode) {
			case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
				Vec3f transformedAnchor = RenderingUtils.getTransformedAnchorThirdPerson(cpe, anchor, leftHanded);
				double dist = Math.sqrt(transformedAnchor.dot(transformedAnchor));
				matrices.push();
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
				matrices.translate(0, 0, -0.875);
				//Modify distance based on pekhui if needed
				dist /= RenderingUtils.getSizeMultiplier(cpe, MinecraftClient.getInstance().getTickDelta());
//				RenderingUtils.renderChainsBasic(stack, dist, matrices, vertexConsumers, light, overlay);
				RenderingUtils.renderChainsFancy(stack, dist, matrices, vertexConsumers, light, overlay);
				matrices.pop();
			}
			case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
				MinecraftClient client = MinecraftClient.getInstance();
				GameRenderer renderer = client.gameRenderer;
				double fov = ((GameRendererInvoker) renderer).limits_grapple$getFov(renderer.getCamera(), client.getTickDelta(), true);
				renderer.loadProjectionMatrix(renderer.getBasicProjectionMatrix(fov));

				Vec3f transformedAnchor = RenderingUtils.transformWorldToView(anchor);
				double dist = Math.sqrt(transformedAnchor.dot(transformedAnchor));
				matrices.push();
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
				matrices.translate(0, 0, -0.875);
				//Modify distance based on pekhui if needed
				dist /= RenderingUtils.getSizeMultiplier(cpe, MinecraftClient.getInstance().getTickDelta());
//				RenderingUtils.renderChainsBasic(stack, dist, matrices, vertexConsumers, light, overlay);
				RenderingUtils.renderChainsFancy(stack, dist, matrices, vertexConsumers, light, overlay);
				matrices.pop();
			}
		}
	}

	@Inject(
			method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at=@At("RETURN"))
	public void limits_grapple$resetProjectionMatrix(ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if (stack.isOf(GrappleMod.GRAPPLE_ITEM) && (renderMode == ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND || renderMode == ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND)) {
			MinecraftClient client = MinecraftClient.getInstance();
			GameRenderer renderer = client.gameRenderer;
			double fov = ((GameRendererInvoker) renderer).limits_grapple$getFov(renderer.getCamera(), client.getTickDelta(), false);
			if (vertexConsumers instanceof  VertexConsumerProvider.Immediate immediate)
				immediate.draw();
			renderer.loadProjectionMatrix(renderer.getBasicProjectionMatrix(fov));
		}
	}

}
