package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class EnchantRandomlyFunctionHelper extends LootFunctionHelper<EnchantRandomlyFunction> {
	@Nonnull
	@Override
	public String getDescription(EnchantRandomlyFunction function) {
		ArrayList<String> enchants = new ArrayList<String>();

		for (Holder<Enchantment> enchant : function.enchantments.get()) {
			enchants.add(ObjectHelper.getEnchantmentName(enchant.value(), 0));
		}

		return "will be enchanted with:<br/>" + FormattingHelper.listToString(enchants, false);
	}
}
