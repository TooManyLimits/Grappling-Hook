package io.github.moonlight_maya.limits_grapple;

import io.github.moonlight_maya.limits_grapple.item.AccelerationEnchantment;
import io.github.moonlight_maya.limits_grapple.item.GrappleItem;
import io.github.moonlight_maya.limits_grapple.item.MaxSpeedEnchantment;
import io.github.moonlight_maya.limits_grapple.item.RangeEnchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class GrappleMod implements ModInitializer {

	public static final String MODID = "limits_grapple";
	public static boolean DISABLING_RENDERSYSTEM_CLEAR = false;

	public static final Item GRAPPLE_ITEM = new GrappleItem(new QuiltItemSettings().group(ItemGroup.TRANSPORTATION).maxCount(1).maxDamage(768));

	public static final RangeEnchantment RANGE_ENCHANTMENT = new RangeEnchantment();
	public static final AccelerationEnchantment ACCELERATION_ENCHANTMENT = new AccelerationEnchantment();
	public static final MaxSpeedEnchantment MAX_SPEED_ENCHANTMENT = new MaxSpeedEnchantment();

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.ITEM, new Identifier(MODID, "grappling_hook"), GRAPPLE_ITEM);
		Registry.register(Registry.ENCHANTMENT, new Identifier(MODID, "range"), RANGE_ENCHANTMENT);
		Registry.register(Registry.ENCHANTMENT, new Identifier(MODID, "acceleration"), ACCELERATION_ENCHANTMENT);
		Registry.register(Registry.ENCHANTMENT, new Identifier(MODID, "max_speed"), MAX_SPEED_ENCHANTMENT);
	}

}
