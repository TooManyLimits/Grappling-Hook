package io.github.moonlight_maya.limits_grapple.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

/**
 * Mixin class by LemmaEOF
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

	private static ThreadLocal<Enchantment> ench = new ThreadLocal<>();

	/**
	 * @author LemmaEOF
	 */
	@Inject(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;isTreasure()Z"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private static void localEnchantment(int power, ItemStack stack, boolean allowTreasure, CallbackInfoReturnable info, List<EnchantmentLevelEntry> ret, Item item, boolean isBook, Iterator<Enchantment> itr, Enchantment enchantment) {
		ench.set(enchantment);
	}

	/**
	 * @author LemmaEOF
	 * @reason defer to the enchantment's isAcceptableItem so we can use our own
	 */
	@Redirect(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentTarget;isAcceptableItem(Lnet/minecraft/item/Item;)Z"))
	private static boolean redirectEnchantmentList(EnchantmentTarget target, Item item, int level, ItemStack stack, boolean allowTreasure) {
		if (ench.get() == null) return target.isAcceptableItem(item);
		return ench.get().isAcceptableItem(stack);
	}

}
