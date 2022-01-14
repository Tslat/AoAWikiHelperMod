package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.loot.functions.ApplyBonus;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nonnull;

public class ApplyBonusFunctionHelper extends LootFunctionHelper<ApplyBonus> {
	@Nonnull
	@Override
	public String getDescription(ApplyBonus function) {
		return "will vary in quantity depending on the level of " + FormattingHelper.createLinkableText(ObjectHelper.getEnchantmentName(function.enchantment, 0), false, false, true) + " used";
	}
}
