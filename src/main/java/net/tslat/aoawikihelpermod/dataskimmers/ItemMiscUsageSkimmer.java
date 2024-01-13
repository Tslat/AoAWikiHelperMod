package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.tslat.aoa3.common.registration.item.AoAItems;
import net.tslat.aoa3.common.registration.item.AoAWeapons;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import java.util.Arrays;

public class ItemMiscUsageSkimmer {
	public static void init() {
		prepRepairableItems();
	}

	private static void prepRepairableItems() {
		ItemDataSkimmer.get(AoAWeapons.SHYREGEM_BOW.get(), null).addRepairableItems(AoAItems.SHYRESTONE_INGOT.get(), AoAItems.SHYREGEM.get());
		ItemDataSkimmer.get(AoAWeapons.CRYSTAL_CARVER.get(), null).addRepairableItems(Arrays.stream(Ingredient.of(Tags.Items.GEMS).getItems()).map(ItemStack::getItem).toArray(Item[]::new));

		for (Item item : ObjectHelper.scrapeRegistryForItems(item -> item instanceof TieredItem)) {
			Tier tier = ((TieredItem)item).getTier();

			for (ItemStack stack : tier.getRepairIngredient().getItems()) {
				ItemDataSkimmer.get(stack.getItem(), null).addRepairableItems(item);
			}
		}
	}
}
