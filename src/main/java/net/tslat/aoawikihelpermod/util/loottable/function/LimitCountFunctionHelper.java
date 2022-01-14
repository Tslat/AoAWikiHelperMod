package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.loot.functions.LimitCount;

import javax.annotation.Nonnull;

public class LimitCountFunctionHelper extends LootFunctionHelper<LimitCount> {
	@Nonnull
	@Override
	public String getDescription(LimitCount function) {
		return "will have its amount capped to a specific amount";
	}
}
