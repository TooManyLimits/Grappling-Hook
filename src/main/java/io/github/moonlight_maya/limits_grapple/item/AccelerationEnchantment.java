package io.github.moonlight_maya.limits_grapple.item;

import io.github.moonlight_maya.limits_grapple.GrappleMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class AccelerationEnchantment extends Enchantment {
	public AccelerationEnchantment() {
		super(Rarity.UNCOMMON, EnchantmentTarget.BREAKABLE, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
	}

	@Override
	public boolean isAcceptableItem(ItemStack stack) {
		return stack.isOf(GrappleMod.GRAPPLE_ITEM);
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getMinPower(int level) {
		return 8 + (level-1) * 9;
	}

	@Override
	public int getMaxPower(int level) {
		return 999;
	}
}
