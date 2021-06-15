package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoa3.util.misc.MutableSupplier;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.dataskimmers.LootTablesSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.printers.LootTablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.LootTablePrintHandler;

import java.io.File;

public class LootTableCommand implements Command<CommandSource> {
	private static final LootTableCommand CMD = new LootTableCommand();
	private static final SuggestionProvider<CommandSource> SUGGESTION_PROVIDER = SuggestionProviders.register(new ResourceLocation(AoAWikiHelperMod.MOD_ID, "loot_tables"), (context, builder) -> ISuggestionProvider.suggestResource(LootTablesSkimmer.TABLE_PRINTERS.keySet().stream(), builder));

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("loottable").executes(CMD);

		builder.then(Commands.literal("table").then(Commands.argument("loot_table", ResourceLocationArgument.id()).suggests(SUGGESTION_PROVIDER).executes(cmd -> printTable(cmd, ResourceLocationArgument.getId(cmd, "loot_table")))));
		builder.then(Commands.literal("entity").then(Commands.argument("entity", ResourceLocationArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(LootTableCommand::printEntityTable)));
		builder.then(Commands.literal("block").then(Commands.argument("block", ResourceLocationArgument.id()).executes(LootTableCommand::printBlockTable)));

		return builder;
	}

	protected String commandName() {
		return "LootTable";
	}

	private static int printBlockTable(CommandContext<CommandSource> cmd) {
		ResourceLocation id = ResourceLocationArgument.getId(cmd, "block");
		Block block = ForgeRegistries.BLOCKS.getValue(id);

		if (block == null) {
			WikiHelperCommand.warn(cmd.getSource(), "LootTable", "Invalid block ID '" + id + "'");

			return 1;
		}
		else if (block.getLootTable() == null) {
			WikiHelperCommand.warn(cmd.getSource(), "LootTable", "Block '" + block.getRegistryName() + "' has no attached loot table");

			return 1;
		}

		return printTable(cmd, block.getLootTable());
	}

	private static int printEntityTable(CommandContext<CommandSource> cmd) {
		ResourceLocation id = ResourceLocationArgument.getId(cmd, "entity");
		EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(id);

		if (entity == null || (!id.toString().equals("minecraft:pig") && entity == EntityType.PIG)) {
			WikiHelperCommand.warn(cmd.getSource(), "LootTable", "Invalid entity id '" + id + "'");

			return 1;
		}

		return printTable(cmd, entity.getDefaultLootTable());
	}

	private static int printTable(CommandContext<CommandSource> cmd, ResourceLocation tableId) {
		try {
			LootTablePrintHandler printHandler = LootTablesSkimmer.TABLE_PRINTERS.get(tableId);

			if (printHandler == null) {
				WikiHelperCommand.error(cmd.getSource(), "LootTable", "Invalid loot table ID: '" + tableId + "'");

				return 1;
			}

			File outputFile;
			MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
			String fileName = "LootTable - ";
			String pathName = tableId.getPath();

			if (pathName.contains("\\")) {
				pathName = StringUtil.toTitleCase(pathName.replaceAll("\\\\", " - "));
			}
			else if (pathName.contains("/")) {
				pathName = StringUtil.toTitleCase(pathName.replaceAll("/", " - "));
			}

			fileName = fileName + pathName;

			try (LootTablePrintHelper printHelper = LootTablePrintHelper.open(fileName, true)) {
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.printTable(tableId, printHandler);

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(cmd.getSource(), "LootTable", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		}
		catch (Exception ex) {
			WikiHelperCommand.error(cmd.getSource(), "LootTable", "Error encountered while printing loot table, see log for details");

			ex.printStackTrace();
		}

		return 1;
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out a specific loot table in its relevant template format");

		return 1;
	}
}
