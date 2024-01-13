package net.tslat.aoawikihelpermod.util.printer.handler;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.neoforge.common.CommonHooks;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.TagUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemDataPrintHandler {
	private final Item item;

	private ArrayList<Item> repairableItems = null;

	private String repairIngredientPrintout = null;
	private String fuelPrintout = null;
	private String[] tagsPrintout = null;
	private String composterPrintout = null;

	private Level level = null;

	public ItemDataPrintHandler(Item item) {
		this.item = item;
	}

	public ItemDataPrintHandler withLevel(Level level) {
		this.level = level;

		return this;
	}

	public Level getLevel() {
		if (this.level == null)
			throw new IllegalStateException("No level provided for ItemDataPrintHandler!");

		return this.level;
	}

	public void addRepairableItems(Item... itemsToRepair) {
		if (repairableItems == null)
			repairableItems = new ArrayList<Item>();

		Collections.addAll(repairableItems, itemsToRepair);
	}

	public String[] getTagsPrintout() {
		if (tagsPrintout != null)
			return tagsPrintout;

		List<ResourceLocation> tags = getTags();

		if (tags.isEmpty()) {
			this.tagsPrintout = new String[] {ObjectHelper.getItemName(item) + " has no tags."};
		}
		else {
			StringBuilder builder = new StringBuilder();

			builder.append("=== Tags: ===<br/>");
			builder.append(FormattingHelper.listToString(tags.stream().map((id) -> "* " + id.toString()).collect(Collectors.toList()), false));
			builder.append("<br/><br/>");

			String[] lines = builder.toString().split("<br/>");

			this.tagsPrintout = new String[lines.length + 1];
			this.tagsPrintout[0] = ObjectHelper.getItemName(item) + " has the following tags:<br/>";

			System.arraycopy(lines, 0, this.tagsPrintout, 1, lines.length);
		}

		return this.tagsPrintout;
	}

	@Nullable
	public String getRepairIngredientPrintout() {
		if (repairableItems == null)
			return null;

		if (repairIngredientPrintout != null)
			return repairIngredientPrintout;

		StringBuilder builder = new StringBuilder();

		builder.append(FormattingHelper.lazyPluralise(ObjectHelper.getItemName(item)));
		builder.append(" can be used to repair ");

		int i = 0;

		for (Item repairable : repairableItems) {
			if (i > 0) {
				builder.append(", ");

				if (i == repairableItems.size() - 1)
					builder.append("and ");
			}

			builder.append(FormattingHelper.createLinkableItem(repairable, true, true));

			i++;
		}

		builder.append(" on an ");
		builder.append(FormattingHelper.createLinkableItem(Blocks.ANVIL, false, true));

		repairIngredientPrintout = builder.toString();

		return repairIngredientPrintout;
	}

	@Nullable
	public String getFuelPrintout() {
		if (fuelPrintout != null)
			return fuelPrintout.isEmpty() ? null : repairIngredientPrintout;

		int burnTime = CommonHooks.getBurnTime(item.getDefaultInstance(), null);

		if (burnTime <= 0) {
			fuelPrintout = "";
		}
		else {
			StringBuilder builder = new StringBuilder(ObjectHelper.getItemName(item));

			builder.append(" can be used as a fuel source to provide ");
			builder.append(FormattingHelper.getTimeFromTicks(burnTime));
			builder.append(" fuel time (smelting/cooking up to ");
			builder.append(burnTime / 200);
			builder.append(" items).");

			fuelPrintout = builder.toString();
		}

		return getFuelPrintout();
	}

	@Nullable
	public String getComposterPrintout() {
		if (composterPrintout != null)
			return composterPrintout;

		if (ComposterBlock.COMPOSTABLES.containsKey(item))
			this.composterPrintout = ObjectHelper.getItemName(item) + " can be placed in a " + FormattingHelper.createLinkableText(ObjectHelper.getBlockName(Blocks.COMPOSTER), false, true) + " for a " + NumberUtil.roundToNthDecimalPlace(ComposterBlock.COMPOSTABLES.getFloat(item), 2) + "% chance to raise the compost level.";

		return composterPrintout;
	}

	public boolean hasTags() {
		return getTags() != null;
	}

	private List<ResourceLocation> getTags() {
		return TagUtil.getAllTagsFor(Registries.ITEM, this.item, getLevel()).map(TagKey::location).toList();
	}
}
