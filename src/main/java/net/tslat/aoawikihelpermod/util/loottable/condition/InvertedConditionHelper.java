package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.tslat.aoawikihelpermod.util.LootTableHelper;

import javax.annotation.Nonnull;

public class InvertedConditionHelper extends LootConditionHelper<InvertedLootItemCondition> {
	@Nonnull
	@Override
	public String getDescription(InvertedLootItemCondition condition) {
		String termDescription = LootTableHelper.getConditionDescription(condition.term());

		if (termDescription.length() < 3)
			return "";

		return "if the following check fails: \n:" + termDescription.substring(3).replaceAll("\n:", "\n::");
	}
}
