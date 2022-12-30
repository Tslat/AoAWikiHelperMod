package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.infoboxes.BlockInfoboxPrintHelper;
import net.tslat.aoawikihelpermod.util.printers.infoboxes.EntityInfoboxPrintHelper;
import net.tslat.aoawikihelpermod.util.printers.infoboxes.ItemInfoboxPrintHelper;

import java.io.File;

public class InfoboxCommand implements Command<CommandSourceStack> {
	private static final InfoboxCommand CMD = new InfoboxCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("infobox").executes(CMD);

		builder.then(
				Commands.literal("block")
						.then(
								Commands.argument("id", BlocksCommand.BlockArgument.block())
										.executes(InfoboxCommand::printBlockInfobox)
						)
		);
		builder.then(
				Commands.literal("item")
						.then(
								Commands.argument("id", ItemsCommand.ItemArgument.item())
										.executes(InfoboxCommand::printItemInfobox)
						)
		);
		builder.then(
				Commands.literal("entity")
						.then(
								Commands.argument("id", ResourceLocationArgument.id())
										.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
										.executes(InfoboxCommand::printEntityInfobox)
						)
		);
		ItemsCommand.ITEM_CATEGORY_PROVIDERS.forEach(provider -> {
			try {
				builder.then(
						Commands.literal(provider.getCategoryName())
								.then(
										Commands.argument("id", ResourceLocationArgument.id())
												.suggests(provider.getProvider())
												.executes(InfoboxCommand::printItemInfobox)
								)
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		return builder;
	}

	protected String commandName() {
		return "Infobox";
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Prints wiki infoboxes. Currently only supports blocks.");

		return 1;
	}

	private static int printBlockInfobox(CommandContext<CommandSourceStack> cmd) {
		Block block = BlocksCommand.BlockArgument.getBlock(cmd, "id").getBlock();
		CommandSourceStack source = cmd.getSource();
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;

		WikiHelperCommand.info(cmd.getSource(), "Infobox", "Printing block infobox for '" + ForgeRegistries.BLOCKS.getKey(block) + "'...");
		String fileName = "Block Infobox - " + ObjectHelper.getBlockName(block);

		try (BlockInfoboxPrintHelper printHelper = BlockInfoboxPrintHelper.open(fileName)) {
			printHelper.withClipboardOutput(clipboardContent);
			printHelper.printBlockInfobox(block);

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(source, "Infobox", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		return 1;
	}

	private static int printItemInfobox(CommandContext<CommandSourceStack> cmd) {
		Item item = ForgeRegistries.ITEMS.getDelegateOrThrow(cmd.getArgument("id", ResourceLocation.class)).get();
		CommandSourceStack source = cmd.getSource();
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;

		WikiHelperCommand.info(cmd.getSource(), "Infobox", "Printing item infobox for '" + ForgeRegistries.ITEMS.getKey(item) + "'...");
		String fileName = "Item Infobox - " + ObjectHelper.getItemName(item);

		try (ItemInfoboxPrintHelper printHelper = ItemInfoboxPrintHelper.open(fileName)) {
			printHelper.withClipboardOutput(clipboardContent);
			printHelper.printItemInfobox(item, (LivingEntity) cmd.getSource().getEntity());

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(source, "Infobox", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		return 1;
	}

	private static int printEntityInfobox(CommandContext<CommandSourceStack> cmd) {
		ResourceLocation id = ResourceLocationArgument.getId(cmd, "id");
		EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(id);
		CommandSourceStack source = cmd.getSource();

		if (entity == null || (!id.toString().equals("minecraft:pig") && entity == EntityType.PIG)) {
			WikiHelperCommand.warn(cmd.getSource(), "LootTable", "Invalid entity id '" + id + "'");

			return 1;
		}

		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;

		WikiHelperCommand.info(cmd.getSource(), "Infobox", "Printing entity infobox for '" + ForgeRegistries.ENTITY_TYPES.getKey(entity) + "'...");
		String fileName = "Entity Infobox - " + ObjectHelper.getEntityName(entity);

		try (EntityInfoboxPrintHelper printHelper = EntityInfoboxPrintHelper.open(fileName)) {
			printHelper.withClipboardOutput(clipboardContent);
			printHelper.printEntityInfobox(entity, cmd.getSource().getEntity());

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(source, "Infobox", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		return 1;
	}
}
