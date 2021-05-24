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
import net.tslat.aoawikihelpermod.dataskimmers.RecipesSkimmer;
import net.tslat.aoawikihelpermod.dataskimmers.RepairablesSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import net.tslat.aoawikihelpermod.util.printers.RecipePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

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

	private static void checkRecipeUsages(Item item, CommandSource commandSource) {
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
			}
			else {
				WikiHelperCommand.info(commandSource, "Usages", "No supported usages found for " + itemName);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void checkRepairUsages(Item item, CommandSource commandSource) {
		String itemName = ObjectHelper.getItemName(item);
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		String repairInfo = RepairablesSkimmer.getRepairDescription(item.getRegistryName());

		if (repairInfo != null) {
			String fileName = "Usages - " + itemName + " - Repairing";

			try (PrintHelper printHelper = PrintHelper.open(fileName)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.write(repairInfo);
				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(commandSource, "Usages", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		}
	}

	private static void checkTradeUsages(Item item, CommandSource commandSource) {}

	private static int printUsages(CommandContext<CommandSource> cmd) {
		Item item = ItemArgument.getItem(cmd, "id").getItem();
		CommandSource source = cmd.getSource();

		WikiHelperCommand.info(cmd.getSource(), "Usages", "Searching for usages of '" + item.getRegistryName() + "'...");

		checkRecipeUsages(item, source);
		checkRepairUsages(item, source);
		checkTradeUsages(item, source); // TODO

		// TODO further usages
		// Fuels?

		return 1;
	}
}
