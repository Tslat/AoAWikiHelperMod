package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.loot.functions.EnchantWithLevels;

import javax.annotation.Nonnull;

public class EnchantWithLevelsFunctionHelper extends LootFunctionHelper<EnchantWithLevels> {
	@Nonnull
	@Override
	public String getDescription(EnchantWithLevels function) {
		return "will be enchanted with random enchantments";
	}
}
