package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.Inverted;
import net.tslat.aoawikihelpermod.util.LootTableHelper;

import javax.annotation.Nonnull;

public class InvertedConditionHelper extends LootConditionHelper<Inverted> {
	@Nonnull
	@Override
	public String getDescription(Inverted condition) {
		String termDescription = LootTableHelper.getConditionDescription(condition.term);

		if (termDescription.length() < 3)
			return "";

		return "if the following check fails: \n:" + termDescription.substring(3).replaceAll("\n:", "\n::");
	}
}
