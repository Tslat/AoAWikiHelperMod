package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;

import javax.annotation.Nonnull;

public class EnchantWithLevelsFunctionHelper extends LootFunctionHelper<EnchantWithLevelsFunction> {
	@Nonnull
	@Override
	public String getDescription(EnchantWithLevelsFunction function) {
		return "will be enchanted with random enchantments";
	}
}
