package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.tslat.aoa3.util.NumberUtil;

import javax.annotation.Nonnull;

public class RandomChanceWithLootingConditionHelper extends LootConditionHelper<LootItemRandomChanceWithLootingCondition> {
	@Nonnull
	@Override
	public String getDescription(LootItemRandomChanceWithLootingCondition condition) {
		float chance = condition.percent;
		float lootingMod = condition.lootingMultiplier;

		return "if a fixed random chance check is passed, with a chance of " + NumberUtil.roundToNthDecimalPlace(chance * 100, 3) + "%, with an extra " + NumberUtil.roundToNthDecimalPlace(lootingMod * 100, 3) + " per looting level";
	}
}
