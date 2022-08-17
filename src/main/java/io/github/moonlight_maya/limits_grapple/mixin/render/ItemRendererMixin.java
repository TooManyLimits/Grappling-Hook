package io.github.moonlight_maya.limits_grapple.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.moonlight_maya.limits_grapple.RenderingUtils;
import io.github.moonlight_maya.limits_grapple.GrappleMod;
import io.github.moonlight_maya.limits_grapple.GrappleModClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
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
public class ItemRendererMixin {


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
				RenderingUtils.renderChains(dist, matrices, vertexConsumers, light, overlay);
				matrices.pop();
			}
			case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
				boolean left = renderMode == ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND;
				Vec3f viewPos = RenderingUtils.transformWorldToView(anchor);
				viewPos.add(left ? -0.5f : 0.5f, 0.5f, -0.5f);

				double dist = Math.sqrt(viewPos.dot(viewPos));
				matrices.push();
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
				matrices.translate(0, 0, -0.875);
				RenderingUtils.renderChains(dist, matrices, vertexConsumers, light, overlay);
				matrices.pop();
			}
		}

	}

}
