package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;

import javax.annotation.Nonnull;

public class SmeltFunctionHelper extends LootFunctionHelper<SmeltItemFunction> {
	@Nonnull
	@Override
	public String getDescription(SmeltItemFunction function) {
		return "will be converted into its smelted/cooked version on drop if possible";
	}
}
