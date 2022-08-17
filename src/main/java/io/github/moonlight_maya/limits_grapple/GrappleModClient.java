package io.github.moonlight_maya.limits_grapple;

import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class GrappleModClient implements ClientModInitializer {

	public static AbstractClientPlayerEntity currentRenderedPlayerEntity;
    public static boolean DISABLE_DEPTH_CLEAR_FLAG = false;

    @Override
	public void onInitializeClient(ModContainer mod) {
		ModelPredicateProviderRegistry.register(GrappleMod.GRAPPLE_ITEM, new Identifier(GrappleMod.MODID, "shot"), (itemStack, clientWorld, livingEntity, i) -> {
			if (livingEntity == null) return 0.0f;
			if (!itemStack.isOf(GrappleMod.GRAPPLE_ITEM)) return 0.0f;
			return itemStack.getOrCreateNbt().getBoolean("Active") ? 1.0f : 0.0f;
		});
	}
}
