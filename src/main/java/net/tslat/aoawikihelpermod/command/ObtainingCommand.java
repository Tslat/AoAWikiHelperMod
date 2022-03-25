package net.tslat.aoawikihelpermod.command;

import com.google.common.collect.HashMultimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoawikihelpermod.dataskimmers.*;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.*;
import net.tslat.aoawikihelpermod.util.printers.handlers.HaulingTablePrintHandler;
import net.tslat.aoawikihelpermod.util.printers.handlers.MerchantTradePrintHandler;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class ObtainingCommand implements Command<CommandSource> {
	private static final ObtainingCommand CMD = new ObtainingCommand();

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("obtaining").executes(CMD);

		builder.then(Commands.argument("id", ItemArgument.item()).executes(ObtainingCommand::printSources));

		return builder;
	}

	protected String commandName() {
		return "Obtaining";
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print files relating to places a specific item is obtained.");

		return 1;
	}

	private static boolean checkRecipeSources(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		String baseFileName = "Obtaining - " + itemName;
		Collection<ResourceLocation> resultingRecipes = RecipesSkimmer.RECIPES_BY_OUTPUT.get(item.getRegistryName());

		try {
			if (!resultingRecipes.isEmpty()) {
				HashMultimap<String, RecipePrintHandler> sortedRecipes = HashMultimap.create();

				for (ResourceLocation id : resultingRecipes) {
					RecipePrintHandler handler = RecipesSkimmer.RECIPE_PRINTERS.get(id);

					sortedRecipes.put(handler.getTableGroup(), handler);
				}

				for (String type : sortedRecipes.keySet()) {
					ArrayList<RecipePrintHandler> recipeHandlers = new ArrayList<RecipePrintHandler>(sortedRecipes.get(type));

					if (recipeHandlers.isEmpty())
						continue;

					String fileName = baseFileName + " - " + type;
					recipeHandlers.sort(Comparator.comparing(handler -> handler.getRecipeId().toString()));

					try (RecipePrintHelper printHelper = RecipePrintHelper.open(fileName, recipeHandlers.get(0))) {
						printHelper.withProperty("class", "wikitable");
						printHelper.withClipboardOutput(clipboardContent);

						for (RecipePrintHandler handler : recipeHandlers) {
							printHelper.entry(handler.toTableEntry(item));
						}

						outputFile = printHelper.getOutputFile();
					}

					WikiHelperCommand.success(commandSource, "Obtaining", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
				}

				return true;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}

	private static boolean checkTradeSources(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		Set<MerchantTradePrintHandler> trades = MerchantsSkimmer.TRADES_BY_ITEM.get(item.getRegistryName());

		if (!trades.isEmpty()) {
			String fileName = "Obtaining - " + itemName + " - Merchants";

			try (TradesPrintHelper printHelper = TradesPrintHelper.open(fileName, false)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.withProperty("class", "wikitable");
				printHelper.handleTradeList(trades);

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Obtaining", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

			return true;
		}

		return false;
	}

	private static boolean checkLogStrippingSources(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		String stripInfo = item instanceof BlockItem ? BlockDataSkimmer.get(((BlockItem)item).getBlock()).getStrippableBlockDescription() : null;

		if (stripInfo != null) {
			String fileName = "Obtaining - " + itemName + " - Log Stripping";

			try (PrintHelper printHelper = PrintHelper.open(fileName)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.write(stripInfo);

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Obtaining", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

			return true;
		}

		return false;
	}

	private static boolean checkHaulingSources(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		String fileName = "Obtaining - " + itemName + " - Hauling";
		Collection<ResourceLocation> haulingTables = HaulingFishTableSkimmer.TABLES_BY_LOOT.get(item.getRegistryName());

		if (!haulingTables.isEmpty()) {
			ArrayList<HaulingTablePrintHandler> sortedTables = new ArrayList<HaulingTablePrintHandler>();

			for (ResourceLocation id : haulingTables) {
				HaulingTablePrintHandler handler = HaulingFishTableSkimmer.TABLE_PRINTERS.get(id);

				sortedTables.add(handler);
			}

			sortedTables.sort(Comparator.comparing(handler -> handler.getTableId().toString()));

			try (HaulingTablePrintHelper printHelper = HaulingTablePrintHelper.open(fileName, false)) {
				printHelper.withClipboardOutput(clipboardContent);

				for (ResourceLocation id : haulingTables) {
					printHelper.printTable(id, HaulingFishTableSkimmer.TABLE_PRINTERS.get(id));
				}

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Obtaining", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

			return true;
		}

		return false;
	}

	private static boolean checkLootTableSources(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		String baseFileName = "Obtaining - " + itemName;
		Collection<ResourceLocation> resultingLootTables = LootTablesSkimmer.TABLES_BY_LOOT.get(item.getRegistryName());

		if (!resultingLootTables.isEmpty()) {
			String fileName = baseFileName + " - Loot Tables";

			try (LootTablePrintHelper printHelper = LootTablePrintHelper.open(fileName, false)) {
				printHelper.withClipboardOutput(clipboardContent);

				for (ResourceLocation id : resultingLootTables) {
					printHelper.printTable(id, LootTablesSkimmer.TABLE_PRINTERS.get(id));
				}

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Obtaining", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

			return true;
		}

		return false;
	}

	private static int printSources(CommandContext<CommandSource> cmd) {
		Item item = ItemArgument.getItem(cmd, "id").getItem();
		CommandSource source = cmd.getSource();
		boolean success;

		WikiHelperCommand.info(cmd.getSource(), "Obtaining", "Searching for sources of '" + item.getRegistryName() + "'...");

		success = checkRecipeSources(item, source);
		success |= checkLootTableSources(item, source);
		success |= checkTradeSources(item, source);
		success |= checkLogStrippingSources(item, source);
		success |= checkHaulingSources(item, source);

		// TODO further usages

		if (!success)
			WikiHelperCommand.info(cmd.getSource(), "Obtaining", "No source information found.");

		return 1;
	}
}
