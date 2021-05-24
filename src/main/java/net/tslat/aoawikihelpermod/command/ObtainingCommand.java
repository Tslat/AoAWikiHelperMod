package net.tslat.aoawikihelpermod.command;

import com.google.common.collect.HashMultimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.library.misc.MutableSupplier;
import net.tslat.aoawikihelpermod.dataskimmers.LootTablesSkimmer;
import net.tslat.aoawikihelpermod.dataskimmers.RecipesSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.LootTablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.RecipePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class ObtainingCommand implements Command<CommandSource> {
	private static final ObtainingCommand CMD = new ObtainingCommand();

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("obtaining").executes(CMD);

		builder.then(Commands.argument("id", ItemArgument.item()).executes(ObtainingCommand::printUsages));

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

	private static void checkRecipeSources(Item item, CommandSource commandSource) {
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
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void checkTradeSources(Item item, CommandSource commandSource) {

	}

	private static void checkLootTableSources(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		String baseFileName = "Obtaining - " + itemName;
		Collection<ResourceLocation> resultingLootTables = LootTablesSkimmer.TABLES_BY_LOOT.get(item.getRegistryName());

		if (!resultingLootTables.isEmpty()) {
			String fileName = baseFileName + " - Loot Tables";

			try (LootTablePrintHelper printHelper = LootTablePrintHelper.open(fileName)) {
				printHelper.withClipboardOutput(clipboardContent);

				for (ResourceLocation id : resultingLootTables) {
					printHelper.printTable(id, LootTablesSkimmer.TABLE_PRINTERS.get(id));
				}

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Obtaining", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		}
	}

	private static int printUsages(CommandContext<CommandSource> cmd) {
		Item item = ItemArgument.getItem(cmd, "id").getItem();
		CommandSource source = cmd.getSource();

		WikiHelperCommand.info(cmd.getSource(), "Obtaining", "Searching for sources of '" + item.getRegistryName() + "'...");

		checkRecipeSources(item, source);
		checkLootTableSources(item, source);
		checkTradeSources(item, source); // TODO

		// TODO further usages

		return 1;
	}
}
