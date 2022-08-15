package io.github.moonlight_maya.limits_grapple;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class ChainRenderer {

	public static void renderChains(double distance, MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay) {
		matrices.translate(0, -distance, 0);
		for (int i=0;i<distance-1; i++) {
			matrices.translate(0, 1, 0);
			MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(Blocks.CHAIN.getDefaultState(), matrices, vcp, light, overlay);
		}
	}


}
