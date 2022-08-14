package io.github.moonlight_maya.limits_grapple;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class GrappleMod implements ModInitializer {

	public static final String MODID = "limits_grapple";

	public static final Item GRAPPLE_ITEM = new GrappleItem(new QuiltItemSettings().group(ItemGroup.TOOLS).maxCount(1));

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.ITEM, new Identifier(MODID, "grappling_hook"), GRAPPLE_ITEM);
	}
}
