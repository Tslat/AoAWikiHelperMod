package net.tslat.aoawikihelpermod.dataskimmers;

import com.google.common.collect.HashMultimap;
import net.minecraft.block.Blocks;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.common.registration.AoAItems;
import net.tslat.aoa3.common.registration.AoAWeapons;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class RepairablesSkimmer {
	private static final HashMap<ResourceLocation, String> REPAIRS_BY_ITEM = new HashMap<ResourceLocation, String>();

	public static void init() {
		HashMultimap<Item, Item> repairablesMap = HashMultimap.create();

		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			if (item instanceof TieredItem) {
				IItemTier tier = ((TieredItem)item).getTier();

				for (ItemStack stack : tier.getRepairIngredient().getItems()) {
					repairablesMap.put(stack.getItem(), item);
				}
			}
		}

		repairablesMap.putAll(AoAWeapons.SHYREGEM_BOW.get(), Arrays.asList(AoAItems.SHYRESTONE_INGOT.get(), AoAItems.SHYREGEM.get()));
		repairablesMap.putAll(AoAWeapons.CRYSTAL_CARVER.get(), Arrays.stream(Ingredient.of(Tags.Items.GEMS).getItems()).map(ItemStack::getItem).collect(Collectors.toList()));

		prepRepairDescriptions(repairablesMap);
	}

	private static void prepRepairDescriptions(HashMultimap<Item, Item> repairablesMap) {
		for (Item item : repairablesMap.keySet()) {
			Set<Item> repairItems = repairablesMap.get(item);
			StringBuilder builder = new StringBuilder();

			builder.append(FormattingHelper.lazyPluralise(ObjectHelper.getItemName(item)));
			builder.append(" can be used to repair ");

			int i = 0;

			for (Item repairable : repairItems) {
				if (i > 0)
					builder.append(", ");

				if (i == repairItems.size() - 1)
					builder.append(" and ");

				builder.append(FormattingHelper.createLinkableItem(repairable, true, true));

				i++;
			}

			builder.append(" on an ");
			builder.append(FormattingHelper.createLinkableItem(Blocks.ANVIL, false, true));
			REPAIRS_BY_ITEM.put(item.getRegistryName(), builder.toString());
		}
	}

	@Nullable
	public static String getRepairDescription(ResourceLocation itemId) {
		return REPAIRS_BY_ITEM.get(itemId);
	}
}
