package net.tslat.aoawikihelpermod.dataskimmers;

import com.google.common.collect.HashMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.common.registration.AoAItems;
import net.tslat.aoa3.common.registration.AoAWeapons;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemMiscUsageSkimmer {
	private static final HashMap<ResourceLocation, String> REPAIRS_BY_ITEM = new HashMap<ResourceLocation, String>();
	private static final HashMap<ResourceLocation, String> FUEL_BY_ITEM = new HashMap<ResourceLocation, String>();
	private static final HashMap<ResourceLocation, String> LOG_STRIPPING_BY_SOURCE = new HashMap<ResourceLocation, String>();
	private static final HashMap<ResourceLocation, String> LOG_STRIPPING_BY_STRIPPED_BLOCK = new HashMap<ResourceLocation, String>();

	public static void init() {
		REPAIRS_BY_ITEM.clear();
		FUEL_BY_ITEM.clear();
		LOG_STRIPPING_BY_SOURCE.clear();
		LOG_STRIPPING_BY_STRIPPED_BLOCK.clear();

		HashMultimap<Item, Item> repairablesMap = HashMultimap.create();
		HashMap<Item, Integer> fuelsMap = new HashMap<Item, Integer>();
		HashMap<Block, Block> logStripMap = new HashMap<Block, Block>();

		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			checkRepairableItem(repairablesMap, item);
			checkFuelItem(fuelsMap, item);
		}

		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			checkLogStripBlock(logStripMap, block);
		}

		repairablesMap.putAll(AoAWeapons.SHYREGEM_BOW.get(), Arrays.asList(AoAItems.SHYRESTONE_INGOT.get(), AoAItems.SHYREGEM.get()));
		repairablesMap.putAll(AoAWeapons.CRYSTAL_CARVER.get(), Arrays.stream(Ingredient.of(Tags.Items.GEMS).getItems()).map(ItemStack::getItem).collect(Collectors.toList()));

		prepRepairDescriptions(repairablesMap);
		prepFuelDescriptions(fuelsMap);
		prepLogStripDescriptions(logStripMap);
	}

	private static void checkLogStripBlock(HashMap<Block, Block> logStripMap, Block block) {
		BlockState strippedBlock = AxeItem.getAxeStrippingState(block.defaultBlockState());

		if (strippedBlock != null)
			logStripMap.put(block, strippedBlock.getBlock());
	}

	private static void checkRepairableItem(HashMultimap<Item, Item> repairablesMap, Item item) {
		if (item instanceof TieredItem) {
			IItemTier tier = ((TieredItem)item).getTier();

			for (ItemStack stack : tier.getRepairIngredient().getItems()) {
				repairablesMap.put(stack.getItem(), item);
			}
		}
	}

	private static void checkFuelItem(HashMap<Item, Integer> fuelsMap, Item item) {
		int burnTime = ForgeHooks.getBurnTime(new ItemStack(item));

		if (burnTime > 0)
			fuelsMap.put(item, burnTime);
	}

	private static void prepLogStripDescriptions(HashMap<Block, Block> logStrippingMap) {
		for (Map.Entry<Block, Block> entry : logStrippingMap.entrySet()) {
			StringBuilder builder = new StringBuilder();
			String sourceBlock = entry.getKey().getName().getString();
			String strippedBlock = entry.getValue().getName().getString();

			builder.append(FormattingHelper.lazyPluralise(sourceBlock));
			builder.append(" can be stripped into a ");
			builder.append(FormattingHelper.createLinkableText(strippedBlock, false, entry.getValue().getRegistryName().getNamespace().equals("minecraft"), true));
			builder.append(" by using an axe on it.");

			LOG_STRIPPING_BY_SOURCE.put(entry.getKey().getRegistryName(), builder.toString());

			builder = new StringBuilder();

			builder.append(strippedBlock);
			builder.append(" can be made by stripping a ");
			builder.append(FormattingHelper.createLinkableText(sourceBlock, false, entry.getKey().getRegistryName().getNamespace().equals("minecraft"), true));
			builder.append(" by using an axe on it");

			LOG_STRIPPING_BY_STRIPPED_BLOCK.put(entry.getValue().getRegistryName(), builder.toString());
		}
	}

	private static void prepFuelDescriptions(HashMap<Item, Integer> fuelsMap) {
		for (Map.Entry<Item, Integer> entry : fuelsMap.entrySet()) {
			StringBuilder builder = new StringBuilder();
			int duration = entry.getValue();

			builder.append(ObjectHelper.getItemName(entry.getKey()));
			builder.append(" can be used in a ");
			builder.append(FormattingHelper.createLinkableItem(Blocks.FURNACE, false, true));
			builder.append(" to provide ");
			builder.append(FormattingHelper.getTimeFromTicks(duration));
			builder.append(" fuel time (smelting up to ");
			builder.append(duration / 200);
			builder.append(" items).");

			FUEL_BY_ITEM.put(entry.getKey().getRegistryName(), builder.toString());
		}
	}

	private static void prepRepairDescriptions(HashMultimap<Item, Item> repairablesMap) {
		for (Item item : repairablesMap.keySet()) {
			Set<Item> repairItems = repairablesMap.get(item);
			StringBuilder builder = new StringBuilder();

			builder.append(FormattingHelper.lazyPluralise(ObjectHelper.getItemName(item)));
			builder.append(" can be used to repair ");

			int i = 0;

			for (Item repairable : repairItems) {
				if (i > 0) {
					builder.append(", ");

					if (i == repairItems.size() - 1)
						builder.append(" and ");
				}

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

	@Nullable
	public static String getFuelDescription(ResourceLocation itemId) {
		return FUEL_BY_ITEM.get(itemId);
	}

	@Nullable
	public static String getStrippedBlockDescription(ResourceLocation itemId) {
		return LOG_STRIPPING_BY_STRIPPED_BLOCK.get(itemId);
	}

	@Nullable
	public static String getStrippableBlockDescription(ResourceLocation itemId) {
		return LOG_STRIPPING_BY_SOURCE.get(itemId);
	}
}
