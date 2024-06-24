package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nonnull;

public class RandomChanceConditionHelper extends LootConditionHelper<LootItemRandomChanceCondition> {
	@Nonnull
	@Override
	public String getDescription(LootItemRandomChanceCondition condition) {
		return "if a fixed random chance check is passed, with a chance of " + FormattingHelper.getStringFromRange(condition.chance()) + "%";
	}
}
