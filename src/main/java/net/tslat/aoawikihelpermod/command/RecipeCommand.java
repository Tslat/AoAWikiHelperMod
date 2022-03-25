package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.dataskimmers.RecipesSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.printers.RecipePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import java.io.File;

public class RecipeCommand implements Command<CommandSource> {
	private static final RecipeCommand CMD = new RecipeCommand();
	private static final SuggestionProvider<CommandSource> SUGGESTION_PROVIDER = SuggestionProviders.register(new ResourceLocation(AoAWikiHelperMod.MOD_ID, "raw_recipes"), (context, builder) -> ISuggestionProvider.suggestResource(RecipesSkimmer.RECIPE_PRINTERS.keySet().stream(), builder));

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("recipe").executes(CMD);

		builder.then(Commands.argument("recipe_id", ResourceLocationArgument.id()).suggests(SUGGESTION_PROVIDER).executes(RecipeCommand::printUsages));

		return builder;
	}

	protected String commandName() {
		return "Recipe";
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out a specific recipe in its relevant template format.");

		return 1;
	}

	private static int printUsages(CommandContext<CommandSource> cmd) {
		try {
			ResourceLocation id = ResourceLocationArgument.getId(cmd, "recipe_id");
			RecipePrintHandler recipeHandler = RecipesSkimmer.RECIPE_PRINTERS.get(id);

			if (recipeHandler == null) {
				WikiHelperCommand.error(cmd.getSource(), "Recipe", "Invalid recipe ID: '" + id + "'");

				return 1;
			}

			File outputFile;
			MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
			String fileName = "Recipe - ";
			String pathName = id.getPath();

			if (pathName.contains("\\")) {
				pathName = StringUtil.toTitleCase(pathName.replaceAll("\\\\", " - "));
			}
			else if (pathName.contains("/")) {
				pathName = StringUtil.toTitleCase(pathName.replaceAll("/", " - "));
			}

			fileName = fileName + pathName;

			try (RecipePrintHelper printHelper = RecipePrintHelper.open(fileName, recipeHandler)) {
				printHelper.withProperty("class", "wikitable");
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.entry(recipeHandler.toTableEntry(null));

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(cmd.getSource(), "Recipe", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		}
		catch (Exception ex) {
			WikiHelperCommand.error(cmd.getSource(), "Recipe", "Error encountered while printing recipe, see log for details");

			ex.printStackTrace();
		}

		return 1;
	}
}
