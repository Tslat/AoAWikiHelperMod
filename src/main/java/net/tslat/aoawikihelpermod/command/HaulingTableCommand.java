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
import net.tslat.aoawikihelpermod.dataskimmers.HaulingFishTableSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.printers.HaulingTablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.HaulingTablePrintHandler;

import java.io.File;

public class HaulingTableCommand implements Command<CommandSource> {
	private static final HaulingTableCommand CMD = new HaulingTableCommand();
	private static final SuggestionProvider<CommandSource> SUGGESTION_PROVIDER = SuggestionProviders.register(new ResourceLocation(AoAWikiHelperMod.MOD_ID, "hauling_tables"), (context, builder) -> ISuggestionProvider.suggestResource(HaulingFishTableSkimmer.TABLE_PRINTERS.keySet().stream(), builder));

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("haulingtable").executes(CMD);

		builder.then(Commands.argument("id", ResourceLocationArgument.id()).suggests(SUGGESTION_PROVIDER).executes(cmd -> printTable(cmd, ResourceLocationArgument.getId(cmd, "id"))));

		return builder;
	}

	protected String commandName() {
		return "HaulingTable";
	}

	private static int printTable(CommandContext<CommandSource> cmd, ResourceLocation tableId) {
		try {
			HaulingTablePrintHandler printHandler = HaulingFishTableSkimmer.TABLE_PRINTERS.get(tableId);

			if (printHandler == null) {
				WikiHelperCommand.error(cmd.getSource(), "HaulingTable", "Invalid Hauling table ID: '" + tableId + "'");

				return 1;
			}

			File outputFile;
			MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
			String fileName = "HaulingTable - ";
			String pathName = tableId.getPath();

			if (pathName.contains("\\")) {
				pathName = StringUtil.toTitleCase(pathName.replaceAll("\\\\", " - "));
			}
			else if (pathName.contains("/")) {
				pathName = StringUtil.toTitleCase(pathName.replaceAll("/", " - "));
			}

			fileName = fileName + pathName;

			try (HaulingTablePrintHelper printHelper = HaulingTablePrintHelper.open(fileName, true)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.printTable(tableId, printHandler);

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(cmd.getSource(), "HaulingTable", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		}
		catch (Exception ex) {
			WikiHelperCommand.error(cmd.getSource(), "HaulingTable", "Error encountered while printing Hauling table, see log for details");

			ex.printStackTrace();
		}

		return 1;
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out a specific Hauling table in its relevant template format");

		return 1;
	}
}
