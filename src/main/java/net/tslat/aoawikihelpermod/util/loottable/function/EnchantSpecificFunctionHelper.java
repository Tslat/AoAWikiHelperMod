package net.tslat.aoawikihelpermod.util.loottable.function;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
		ItemEnchantments enchants = function.getEnchantments();
		ArrayList<String> enchantNames = new ArrayList<>();

		for (Object2IntMap.Entry<Holder<Enchantment>> enchant : enchants.entrySet()) {
			enchantNames.add(ObjectHelper.getEnchantmentName(enchant.getKey().value(), enchant.getIntValue()));
		}

		return "will be enchanted with:<br/>" + FormattingHelper.listToString(enchantNames, false);
	}
}
