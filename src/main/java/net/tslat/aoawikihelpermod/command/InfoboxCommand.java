package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printer.PrintHelper;

import java.io.File;
import java.io.IOException;

public class InfoboxCommand implements Command<CommandSourceStack> {
	private static final InfoboxCommand CMD = new InfoboxCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("infobox").executes(CMD);

		builder.then(Commands.literal("block")
				.then(Commands.argument("id", ResourceLocationArgument.id())
						.suggests(BlocksCommand.BLOCK_PROVIDER.getProvider())
						.executes(InfoboxCommand::printBlockInfobox)));
		builder.then(Commands.literal("item")
				.then(Commands.argument("id", ResourceLocationArgument.id())
						.suggests(ItemsCommand.ITEM_PROVIDER.getProvider())
						.executes(InfoboxCommand::printItemInfobox)));
		builder.then(Commands.literal("entity")
				.then(Commands.argument("id", ResourceLocationArgument.id())
						.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
						.executes(InfoboxCommand::printEntityInfobox)));

		ItemsCommand.ITEM_CATEGORY_PROVIDERS.forEach(provider ->
				builder.then(Commands.literal(provider.getCategoryName())
						.then(Commands.argument("id", ResourceLocationArgument.id())
								.suggests(provider.getProvider())
								.executes(InfoboxCommand::printItemInfobox))));

		return builder;
	}

	protected String commandName() {
		return "Infobox";
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Prints wiki-template infoboxes for objects");

		return 1;
	}

	private static int printBlockInfobox(CommandContext<CommandSourceStack> cmd) {
		ResourceLocation id = ResourceLocationArgument.getId(cmd, "id");
		Block block = BuiltInRegistries.BLOCK.get(id);
		CommandSourceStack source = cmd.getSource();
		MutableSupplier<String> clipboardContent = new MutableSupplier<>(null);
		File outputFile;
		String fileName = "Block Infobox - " + ObjectHelper.getBlockName(block);

		WikiHelperCommand.info(cmd.getSource(), "Infobox", "Printing block infobox for '" + id + "'...");

		try (PrintHelper printHelper = PrintHelper.open(fileName)) {
			printHelper.withClipboardOutput(clipboardContent);

			for (String line : WikiTemplateHelper.makeBlockInfoboxTemplate(block, source.getLevel()).getLines()) {
				printHelper.write(line);
			}

			outputFile = printHelper.getOutputFile();
		}
		catch (IOException ex) {
			WikiHelperCommand.error(source, "Infobox", "Error generating print helper for block. Check log for more info");
			ex.printStackTrace();

			return 0;
		}

		WikiHelperCommand.success(source, "Infobox", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printItemInfobox(CommandContext<CommandSourceStack> cmd) {
		ResourceLocation id = ResourceLocationArgument.getId(cmd, "id");
		Item item = BuiltInRegistries.ITEM.get(id);
		CommandSourceStack source = cmd.getSource();
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		String fileName = "Item Infobox - " + ObjectHelper.getItemName(item);

		WikiHelperCommand.info(cmd.getSource(), "Infobox", "Printing item infobox for '" + id + "'...");

		try (PrintHelper printHelper = PrintHelper.open(fileName)) {
			printHelper.withClipboardOutput(clipboardContent);

			for (String line : WikiTemplateHelper.makeItemInfoboxTemplate(item).getLines()) {
				printHelper.write(line);
			}

			outputFile = printHelper.getOutputFile();
		}
		catch (IOException ex) {
			WikiHelperCommand.error(source, "Infobox", "Error generating print helper for item. Check log for more info");
			ex.printStackTrace();

			return 0;
		}

		WikiHelperCommand.success(source, "Infobox", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printEntityInfobox(CommandContext<CommandSourceStack> cmd) {
		ResourceLocation id = ResourceLocationArgument.getId(cmd, "id");
		EntityType<?> entity = BuiltInRegistries.ENTITY_TYPE.get(id);
		CommandSourceStack source = cmd.getSource();
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;
		String fileName = "Entity Infobox - " + ObjectHelper.getEntityName(entity);

		if (entity == null || (!id.toString().equals("minecraft:pig") && entity == EntityType.PIG)) {
			WikiHelperCommand.warn(cmd.getSource(), "LootTable", "Invalid entity id '" + id + "'");

			return 1;
		}

		WikiHelperCommand.info(cmd.getSource(), "Infobox", "Printing entity infobox for '" + id + "'...");

		try (PrintHelper printHelper = PrintHelper.open(fileName)) {
			printHelper.withClipboardOutput(clipboardContent);

			for (String line : WikiTemplateHelper.makeEntityInfoboxTemplate(entity, source.getLevel()).getLines()) {
				printHelper.write(line);
			}

			outputFile = printHelper.getOutputFile();
		}
		catch (IOException ex) {
			WikiHelperCommand.error(source, "Infobox", "Error generating print helper for entity. Check log for more info");
			ex.printStackTrace();

			return 0;
		}

		WikiHelperCommand.success(source, "Infobox", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}
}
