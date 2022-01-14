package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.tslat.aoa3.common.registration.AoAItems;
import net.tslat.aoa3.common.registration.AoAWeapons;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import java.util.Arrays;

public class ItemMiscUsageSkimmer {
	public static void init() {
		prepRepairableItems();
	}

	private static void prepRepairableItems() {
		ItemDataSkimmer.get(AoAWeapons.SHYREGEM_BOW.get()).addRepairableItems(AoAItems.SHYRESTONE_INGOT.get(), AoAItems.SHYREGEM.get());
		ItemDataSkimmer.get(AoAWeapons.CRYSTAL_CARVER.get()).addRepairableItems(Arrays.stream(Ingredient.of(Tags.Items.GEMS).getItems()).map(ItemStack::getItem).toArray(Item[]::new));

		for (Item item : ObjectHelper.scrapeRegistryForItems(item -> item instanceof TieredItem)) {
			IItemTier tier = ((TieredItem)item).getTier();

			for (ItemStack stack : tier.getRepairIngredient().getItems()) {
				ItemDataSkimmer.get(stack.getItem()).addRepairableItems(item);
			}
		}
	}
}
