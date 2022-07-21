package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;

import javax.annotation.Nonnull;

public class SmeltFunctionHelper extends LootFunctionHelper<SmeltItemFunction> {
	@Nonnull
	@Override
	public String getDescription(SmeltItemFunction function) {
		return "will be changed into its smelted/cooked form if the mob is killed while burning.";
	}
}
