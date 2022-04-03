package net.tslat.aoawikihelpermod.util.loottable.condition;


import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;

import javax.annotation.Nonnull;

public class KilledByPlayerConditionHelper extends LootConditionHelper<LootItemKilledByPlayerCondition> {
	@Nonnull
	@Override
	public String getDescription(LootItemKilledByPlayerCondition condition) {
		return "if the target was killed by a player";
	}
}
