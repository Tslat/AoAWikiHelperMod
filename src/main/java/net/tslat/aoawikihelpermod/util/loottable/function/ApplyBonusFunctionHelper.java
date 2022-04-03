package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nonnull;

public class ApplyBonusFunctionHelper extends LootFunctionHelper<ApplyBonusCount> {
	@Nonnull
	@Override
	public String getDescription(ApplyBonusCount function) {
		return "will vary in quantity depending on the level of " + FormattingHelper.createLinkableText(ObjectHelper.getEnchantmentName(function.enchantment, 0), false, false, true) + " used";
	}
}
