package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.BlockStateProperty;

import javax.annotation.Nonnull;

public class BlockStatePropertyConditionHelper extends LootConditionHelper<BlockStateProperty> {
	@Nonnull
	@Override
	public String getDescription(BlockStateProperty condition) {
		return "if the target block meets certain conditions";
	}
}
