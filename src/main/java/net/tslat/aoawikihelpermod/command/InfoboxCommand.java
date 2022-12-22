package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.infoboxes.BlockInfoboxPrintHelper;

import java.io.File;

public class InfoboxCommand implements Command<CommandSourceStack> {
	private static final InfoboxCommand CMD = new InfoboxCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("infobox").executes(CMD);

		// Only supports blocks for now
		builder.then(
				Commands.literal("block")
						.then(Commands.argument("id", BlocksCommand.BlockArgument.block()))
		).executes(InfoboxCommand::printBlockInfobox);
		builder.then(
				Commands.literal("item")
						.then(Commands.argument("id", BlocksCommand.BlockArgument.block()))
		).executes(InfoboxCommand::printBlockInfobox);

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
}
