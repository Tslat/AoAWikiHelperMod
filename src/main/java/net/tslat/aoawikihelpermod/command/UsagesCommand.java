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
import net.tslat.aoawikihelpermod.RecipeLoaderSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.TablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.craftingHandlers.RecipePrintHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class UsagesCommand implements Command<CommandSource> {
	private static final UsagesCommand CMD = new UsagesCommand();

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("usages").executes(CMD);

		builder.then(Commands.argument("id", ItemArgument.item())).executes(UsagesCommand::printUsages);

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

	private static int printUsages(CommandContext<CommandSource> cmd) {
		Item item = ItemArgument.getItem(cmd, "id").getItem();
		String itemName = ObjectHelper.getItemName(item);
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		String baseFileName = "Usages - " + itemName;

		Collection<ResourceLocation> containingRecipes = RecipeLoaderSkimmer.RECIPES_BY_INGREDIENT.get(item.getRegistryName());

		if (!containingRecipes.isEmpty()) {
			HashMultimap<String, RecipePrintHandler> sortedRecipes = HashMultimap.create();

			for (ResourceLocation id : containingRecipes) {
				RecipePrintHandler handler = RecipeLoaderSkimmer.RECIPE_PRINTERS.get(id);

				sortedRecipes.put(handler.getTableGroup(), handler);
			}

			for (String type : sortedRecipes.keySet()) {
				ArrayList<RecipePrintHandler> recipeHandlers = new ArrayList<RecipePrintHandler>(sortedRecipes.get(type));
				String fileName = baseFileName + " - " + type;
				recipeHandlers.sort(Comparator.comparing(handler -> handler.getRecipeId().toString()));

				try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Ingredients", "Recipe")) {
					printHelper.withProperty("class", "wikitable");
					printHelper.withClipboardOutput(clipboardContent);

					for (RecipePrintHandler handler : recipeHandlers) {
						printHelper.entry(handler.toTableEntry(item));
					}

					outputFile = printHelper.getOutputFile();
				}

				WikiHelperCommand.success(cmd.getSource(), "Usages", FormattingHelper.generateResultMessage(outputFile, baseFileName, clipboardContent.get()));
			}
		}

		// TODO further usages

		return 1;
	}
}
