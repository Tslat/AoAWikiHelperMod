package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.world.item.enchantment.Enchantment;
import net.tslat.aoa3.content.loottable.function.EnchantSpecific;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

public class EnchantSpecificFunctionHelper extends LootFunctionHelper<EnchantSpecific> {
	@Nonnull
	@Override
	public String getDescription(EnchantSpecific function) {
		Map<Enchantment, Integer> enchants = function.getEnchantments();
		ArrayList<String> enchantNames = new ArrayList<>();

		for (Map.Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
			enchantNames.add(ObjectHelper.getEnchantmentName(enchant.getKey(), enchant.getValue()));
		}

		return "will be enchanted with:<br/>" + FormattingHelper.listToString(enchantNames, false);
	}
}
