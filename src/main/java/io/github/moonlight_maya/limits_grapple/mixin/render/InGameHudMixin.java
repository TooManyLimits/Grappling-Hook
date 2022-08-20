package io.github.moonlight_maya.limits_grapple.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tessellator;
import io.github.moonlight_maya.limits_grapple.GrappleMod;
import io.github.moonlight_maya.limits_grapple.item.GrappleItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper {
	@Shadow
	private int scaledWidth;
	@Shadow
	private int scaledHeight;
	private static final Identifier CROSSHAIR_TEXTURE = new Identifier(GrappleMod.MODID, "textures/crosshair_indicator.png");
	private static final int CROSSHAIR_TEXTURE_WIDTH = 20;
	private static final int CROSSHAIR_TEXTURE_HEIGHT = 20;

	private static final Vector4f HIT_COLOR = new Vector4f(0.2f, 0.2f, 1.0f, 1.0f);
	private static final Vector4f MISS_COLOR = new Vector4f(0.6f, 0.6f, 0.3f, 1.0f);

	@Inject(method = "renderCrosshair", at = @At("HEAD"))
	public void limits_grapple$renderCrosshairIndicator(MatrixStack matrices, CallbackInfo ci) {

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options.getPerspective() != Perspective.FIRST_PERSON)
			return;

		Entity entity = client.getCameraEntity();
		if (entity instanceof PlayerEntity player) {

			matrices.push();
			Arm mainArm = player.getMainArm();
			ItemStack rightStack = mainArm == Arm.RIGHT ? player.getMainHandStack() : player.getOffHandStack();
			ItemStack leftStack = mainArm == Arm.LEFT ? player.getMainHandStack() : player.getOffHandStack();

			boolean rightGrapple = rightStack.isOf(GrappleMod.GRAPPLE_ITEM);
			boolean leftGrapple = leftStack.isOf(GrappleMod.GRAPPLE_ITEM);

			if (rightGrapple || leftGrapple) {
				//Set render state
				RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
				RenderSystem.setShaderTexture(0, CROSSHAIR_TEXTURE);
				RenderSystem.disableBlend();

				if (rightGrapple && leftGrapple) {
					drawHitResult(matrices, GrappleItem.raycast(player, rightStack), false);
					drawHitResult(matrices, GrappleItem.raycast(player, leftStack), true);
				} else {
					ItemStack grappleStack = rightGrapple ? rightStack : leftStack;
					BlockHitResult hitResult = GrappleItem.raycast(player, grappleStack);
					drawHitResult(matrices, hitResult, false);
					drawHitResult(matrices, hitResult, true);
				}

				//Restore state
				RenderSystem.setShaderColor(1, 1, 1, 1);
				RenderSystem.enableBlend();
				RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
			}
			matrices.pop();
		}

	}

	private void drawHitResult(MatrixStack matrices, BlockHitResult hitResult, boolean left) {
		//Set color
		if (hitResult.getType() != HitResult.Type.MISS)
			RenderSystem.setShaderColor(HIT_COLOR.getX(), HIT_COLOR.getY(), HIT_COLOR.getZ(), HIT_COLOR.getW());
		else
			RenderSystem.setShaderColor(MISS_COLOR.getX(), MISS_COLOR.getY(), MISS_COLOR.getZ(), MISS_COLOR.getW());

		//Get vars
		int x = (scaledWidth - CROSSHAIR_TEXTURE_WIDTH) / 2;
		int y = (scaledHeight - CROSSHAIR_TEXTURE_HEIGHT) / 2;
		int v = left ? CROSSHAIR_TEXTURE_HEIGHT : 0;

		drawTexture(matrices, x, y, 0, v, CROSSHAIR_TEXTURE_WIDTH, CROSSHAIR_TEXTURE_HEIGHT, CROSSHAIR_TEXTURE_WIDTH, CROSSHAIR_TEXTURE_HEIGHT * 2);
	}





}
