package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.loot.functions.EnchantRandomly;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class EnchantRandomlyFunctionHelper extends LootFunctionHelper<EnchantRandomly> {
	@Nonnull
	@Override
	public String getDescription(EnchantRandomly function) {
		ArrayList<String> enchants = new ArrayList<String>();

		for (Enchantment enchant : function.enchantments) {
			enchants.add(ObjectHelper.getEnchantmentName(enchant, 0));
		}

		return "will be enchanted with:<br/>" + FormattingHelper.listToString(enchants, false);
	}
}
