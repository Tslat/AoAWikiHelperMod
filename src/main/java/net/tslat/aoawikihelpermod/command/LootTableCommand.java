package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.dataskimmers.LootTablesSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.printers.LootTablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.LootTablePrintHandler;

import java.io.File;

public class LootTableCommand implements Command<CommandSourceStack> {
	private static final LootTableCommand CMD = new LootTableCommand();
	private static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = SuggestionProviders.register(new ResourceLocation(AoAWikiHelperMod.MOD_ID, "loot_tables"), (context, builder) -> SharedSuggestionProvider.suggestResource(LootTablesSkimmer.TABLE_PRINTERS.keySet().stream(), builder));

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("loottable").executes(CMD);

		builder.then(Commands.literal("table").then(Commands.argument("loot_table", ResourceLocationArgument.id()).suggests(SUGGESTION_PROVIDER).executes(cmd -> printTable(cmd, ResourceLocationArgument.getId(cmd, "loot_table")))));
		builder.then(Commands.literal("entity").then(Commands.argument("entity", ResourceLocationArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(LootTableCommand::printEntityTable)));
		builder.then(Commands.literal("block").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(BlocksCommand.BLOCK_PROVIDER.getProvider()).executes(LootTableCommand::printBlockTable)));

		return builder;
	}

	protected String commandName() {
		return "LootTable";
	}

	private static int printBlockTable(CommandContext<CommandSourceStack> cmd) {
		Block block = ForgeRegistries.BLOCKS.getDelegateOrThrow(cmd.getArgument("id", ResourceLocation.class)).get();

		if (block.getLootTable() == null) {
			WikiHelperCommand.warn(cmd.getSource(), "LootTable", "Block '" + ForgeRegistries.BLOCKS.getKey(block) + "' has no attached loot table");

			return 1;
		}

		return printTable(cmd, block.getLootTable());
	}

	private static int printEntityTable(CommandContext<CommandSourceStack> cmd) {
		ResourceLocation id = ResourceLocationArgument.getId(cmd, "entity");
		EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(id);

		if (entity == null || (!id.toString().equals("minecraft:pig") && entity == EntityType.PIG)) {
			WikiHelperCommand.warn(cmd.getSource(), "LootTable", "Invalid entity id '" + id + "'");

			return 1;
		}

		return printTable(cmd, entity.getDefaultLootTable());
	}

	private static int printTable(CommandContext<CommandSourceStack> cmd, ResourceLocation tableId) {
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
	public int run(CommandContext<CommandSourceStack> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out a specific loot table in its relevant template format");

		return 1;
	}
}
