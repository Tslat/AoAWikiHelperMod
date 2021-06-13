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
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoa3.util.misc.MutableSupplier;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.dataskimmers.RecipesSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.printers.RecipePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import java.io.File;
import java.util.stream.Stream;

public class StructuresCommand implements Command<CommandSource> {
	private static final StructuresCommand CMD = new StructuresCommand();
	private static final SuggestionProvider<CommandSource> SUGGESTION_PROVIDER = SuggestionProviders.register(new ResourceLocation(AoAWikiHelperMod.MOD_ID, "structure_pieces"), (context, builder) -> ISuggestionProvider.suggestResource(Stream.of(
			new ResourceLocation(AdventOfAscension.MOD_ID, "structures/abyss/abyssal_lotto_hut/abyssal_lotto_hut"),
			new ResourceLocation(AdventOfAscension.MOD_ID, "structures/shyrelands/decorations/ruined_arch"),
			new ResourceLocation("minecraft", "structures/ruined_portal/giant_portal_1"),
			new ResourceLocation("minecraft", "structures/igloo/bottom"),
			new ResourceLocation("minecraft", "structures/bastion/bridge/bridge_pieces/bridge")), builder));

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("structure").executes(CMD);

		builder.then(Commands.argument("structure_piece_id", ResourceLocationArgument.id()).suggests(SUGGESTION_PROVIDER).executes(StructuresCommand::printStructurePiece));

		return builder;
	}

	protected String commandName() {
		return "Structure";
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out a structure details for specific pieces or template pools.");

		return 1;
	}

	private static int printStructurePiece(CommandContext<CommandSource> cmd) {
		try {
			ResourceLocation id = ResourceLocationArgument.getId(cmd, "recipe_id");
			RecipePrintHandler recipeHandler = RecipesSkimmer.RECIPE_PRINTERS.get(id);

			if (recipeHandler == null) {
				WikiHelperCommand.error(cmd.getSource(), "Recipe", "Invalid recipe ID: '" + id + "'");

				return 1;
			}

			File outputFile;
			MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
			String fileName = "Recipe - " + StringUtil.toTitleCase(id.getPath());

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
