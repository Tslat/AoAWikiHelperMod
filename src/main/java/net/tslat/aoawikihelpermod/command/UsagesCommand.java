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
import net.tslat.aoa3.util.misc.MutableSupplier;
import net.tslat.aoawikihelpermod.dataskimmers.*;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import net.tslat.aoawikihelpermod.util.printers.RecipePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.TradesPrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.MerchantTradePrintHandler;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public class UsagesCommand implements Command<CommandSource> {
	private static final UsagesCommand CMD = new UsagesCommand();

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("usages").executes(CMD);

		builder.then(Commands.argument("id", ItemArgument.item()).executes(UsagesCommand::printUsages));

		return builder;
	}

	protected String commandName() {
		return "Usages";
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print files relating to places a specific item is used.");

		return 1;
	}

	private static boolean checkRecipeUsages(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		String baseFileName = "Usages - " + itemName;
		Collection<ResourceLocation> containingRecipes = RecipesSkimmer.RECIPES_BY_INGREDIENT.get(item.getRegistryName());

		try {
			if (!containingRecipes.isEmpty()) {
				HashMultimap<String, RecipePrintHandler> sortedRecipes = HashMultimap.create();

				for (ResourceLocation id : containingRecipes) {
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

					WikiHelperCommand.success(commandSource, "Usages", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
				}

				return true;
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}

	private static boolean checkRepairUsages(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		String repairInfo = ItemDataSkimmer.get(item).getRepairIngredientPrintout();

		if (repairInfo != null) {
			String fileName = "Usages - " + itemName + " - Repairing";

			try (PrintHelper printHelper = PrintHelper.open(fileName)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.write(repairInfo);
				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Usages", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

			return true;
		}

		return false;
	}

	private static boolean checkFuelUsages(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		String fuelInfo = ItemDataSkimmer.get(item).getFuelPrintout();

		if (fuelInfo != null) {
			String fileName = "Usages - " + itemName + " - Fuels";

			try (PrintHelper printHelper = PrintHelper.open(fileName)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.write(fuelInfo);
				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Usages", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

			return true;
		}

		return false;
	}

	private static boolean checkLogStrippingUsages(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		String strippingInfo = item instanceof BlockItem ? BlockDataSkimmer.get(((BlockItem)item).getBlock()).getStrippedBlockDescription() : null;

		if (strippingInfo != null) {
			String fileName = "Usages - " + itemName + " - Log Stripping";

			try (PrintHelper printHelper = PrintHelper.open(fileName)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.write(strippingInfo);

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Usages", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

			return true;
		}

		return false;
	}

	private static boolean checkComposterUsages(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		String composterInfo = ItemDataSkimmer.get(item).getComposterPrintout();

		if (composterInfo != null) {
			String fileName = "Usages - " + itemName + " - Composter";

			try (PrintHelper printHelper = PrintHelper.open(fileName)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.write(composterInfo);
				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Usages", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

			return true;
		}

		return false;
	}

	private static boolean checkTradeUsages(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		Set<MerchantTradePrintHandler> trades = MerchantsSkimmer.TRADES_BY_COST.get(item.getRegistryName());

		if (!trades.isEmpty()) {
			String fileName = "Usages - " + itemName + " - Merchants";

			try (TradesPrintHelper printHelper = TradesPrintHelper.open(fileName, false)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.withProperty("class", "wikitable");
				printHelper.handleTradeList(trades);

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Usages", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

			return true;
		}

		return false;
	}

	private static int printUsages(CommandContext<CommandSource> cmd) {
		Item item = ItemArgument.getItem(cmd, "id").getItem();
		CommandSource source = cmd.getSource();
		boolean success;

		WikiHelperCommand.info(cmd.getSource(), "Usages", "Searching for usages of '" + item.getRegistryName() + "'...");

		success = checkRecipeUsages(item, source);
		success |= checkRepairUsages(item, source);
		success |= checkTradeUsages(item, source);
		success |= checkFuelUsages(item, source);
		success |= checkLogStrippingUsages(item, source);
		success |= checkComposterUsages(item, source);

		// TODO further usages

		if (!success)
			WikiHelperCommand.info(cmd.getSource(), "Usages", "No usage information found.");

		return 1;
	}
}
