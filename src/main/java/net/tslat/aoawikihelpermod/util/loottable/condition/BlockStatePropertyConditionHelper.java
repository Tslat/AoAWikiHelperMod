package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import javax.annotation.Nonnull;

public class BlockStatePropertyConditionHelper extends LootConditionHelper<LootItemBlockStatePropertyCondition> {
	@Nonnull
	@Override
	public String getDescription(LootItemBlockStatePropertyCondition condition) {
		return "if the target block meets certain conditions";
	}
}
