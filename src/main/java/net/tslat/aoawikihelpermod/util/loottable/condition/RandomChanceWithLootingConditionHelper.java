package net.tslat.aoawikihelpermod.util.loottable.condition;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nonnull;

public class RandomChanceWithLootingConditionHelper extends LootConditionHelper<LootItemRandomChanceWithEnchantedBonusCondition> {
	@Nonnull
	@Override
	public String getDescription(LootItemRandomChanceWithEnchantedBonusCondition condition) {
		float chance = condition.unenchantedChance();
		String lootingMod = FormattingHelper.getStringFromEnchantmentRange(condition.enchantedChance());

		return "if a fixed random chance check is passed, with a chance of " + NumberUtil.roundToNthDecimalPlace(chance * 100, 3) + "% if unenchanted, or " + lootingMod + "if enchanted with " + ObjectHelper.getEnchantmentName(condition.enchantment(), 0);
	}
}
