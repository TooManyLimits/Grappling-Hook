package io.github.moonlight_maya.limits_grapple;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.*;

public class RenderingUtils {

	public static Vec3f getTransformedAnchorThirdPerson(AbstractClientPlayerEntity playerEntity, Vec3d anchor, boolean left) {
		float tickDelta = MinecraftClient.getInstance().getTickDelta();
		anchor = anchor.subtract(playerEntity.getLerpedPos(tickDelta));
		Vec3f transformedAnchor = new Vec3f(anchor);
		float entityYaw = getTheH(playerEntity);
		transformedAnchor.transform(new Matrix3f(Vec3f.POSITIVE_Y.getDegreesQuaternion(entityYaw)));
		float leaningPitch = playerEntity.getLeaningPitch(tickDelta);
		if (leaningPitch > 0) {
			float pitchMod = playerEntity.isTouchingWater() ? playerEntity.getPitch(tickDelta) : 0;
			Quaternion quat = Vec3f.NEGATIVE_X.getDegreesQuaternion(leaningPitch * (90 + pitchMod));
			transformedAnchor.transform(new Matrix3f(quat));
			if (playerEntity.isInSwimmingPose())
				transformedAnchor.add(0, 0.875f, -0.3f*0.875f);
		}
		//transformedAnchor is now in player space.
		Vec3f playerSpacePivot = new Vec3f((left ? 1 : -1) * 5*0.875f/16f, (playerEntity.isInSneakingPose() ? 18.8f : 22) * 0.875f / 16, 0);
		transformedAnchor.subtract(playerSpacePivot); //TransformedAnchor is now relative to the player space pivot.
		return transformedAnchor;
	}

	//get that H!
	//local variable from LivingEntityRenderer$render()
	private static float getTheH(LivingEntity livingEntity) {
		float g = MinecraftClient.getInstance().getTickDelta();
		float h = MathHelper.lerpAngleDegrees(g, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
		float j = MathHelper.lerpAngleDegrees(g, livingEntity.prevHeadYaw, livingEntity.headYaw);
		float k;
		float l;
		if (livingEntity.hasVehicle() && livingEntity.getVehicle() instanceof LivingEntity livingEntity2) {
			h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
			k = j - h;
			l = MathHelper.wrapDegrees(k);
			if (l < -85.0F) {
				l = -85.0F;
			}
			if (l >= 85.0F) {
				l = 85.0F;
			}
			h = j - l;
			if (l * l > 2500.0F) {
				h += l * 0.2F;
			}
		}
		return h;
	}

	public static Vec3f transformWorldToView(Vec3d worldPos) {
		MinecraftClient client = MinecraftClient.getInstance();
		Camera camera = client.gameRenderer.getCamera();
		Matrix3f cameraMat = new Matrix3f(camera.getRotation());
		cameraMat.invert();
		Vec3f result = new Vec3f(worldPos.subtract(camera.getPos()));
		result.transform(cameraMat);
		return result;
	}

	public static void renderChainsBasic(double distance, MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay) {
		matrices.translate(0, -distance, 0);
//		matrices.scale(1, (float) distance, 1);
//		MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(Blocks.CHAIN.getDefaultState(), matrices, vcp, light, overlay);

		for (int i=0;i<distance-1; i++) {
			matrices.translate(0, 1, 0);
			MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(Blocks.CHAIN.getDefaultState(), matrices, vcp, light, overlay);
		}
	}


}
