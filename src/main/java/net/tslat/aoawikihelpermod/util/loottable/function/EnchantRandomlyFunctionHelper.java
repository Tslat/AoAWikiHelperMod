package net.tslat.aoawikihelpermod.util.loottable.function;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.tslat.aoa3.util.WorldUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Stream;

public class EnchantRandomlyFunctionHelper extends LootFunctionHelper<EnchantRandomlyFunction> {
	@Nonnull
	@Override
	public String getDescription(EnchantRandomlyFunction function) {
		ArrayList<String> enchants = new ArrayList<String>();

		function.options
				.map(HolderSet::stream)
				.orElseGet(() -> WorldUtil.getServer().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().map(Function.identity()))
				.forEach(enchant -> enchants.add(ObjectHelper.getEnchantmentName(enchant, 0)));

		return "will be enchanted with:<br/>" + FormattingHelper.listToString(enchants, false);
	}
}
