package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.loot.conditions.RandomChance;
import net.tslat.aoa3.util.NumberUtil;

import javax.annotation.Nonnull;

public class RandomChanceConditionHelper extends LootConditionHelper<RandomChance> {
	@Nonnull
	@Override
	public String getDescription(RandomChance condition) {
		return "if a fixed random chance check is passed, with a chance of " + NumberUtil.roundToNthDecimalPlace(condition.probability, 3) + "%";
	}
}
