package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.tslat.aoa3.util.NumberUtil;

import javax.annotation.Nonnull;

public class RandomChanceConditionHelper extends LootConditionHelper<LootItemRandomChanceCondition> {
	@Nonnull
	@Override
	public String getDescription(LootItemRandomChanceCondition condition) {
		return "if a fixed random chance check is passed, with a chance of " + NumberUtil.roundToNthDecimalPlace(condition.probability, 3) + "%";
	}
}
