package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.loot.functions.Smelt;

import javax.annotation.Nonnull;

public class SmeltFunctionHelper extends LootFunctionHelper<Smelt> {
	@Nonnull
	@Override
	public String getDescription(Smelt function) {
		return "will be converted into its smelted/cooked version on drop if possible";
	}
}
