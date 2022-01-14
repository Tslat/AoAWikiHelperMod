package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.item.Item;
import net.tslat.aoa3.util.misc.MutableSupplier;
import net.tslat.aoawikihelpermod.dataskimmers.ItemDataSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.ItemDataPrintHandler;

import java.io.File;

public class ItemsCommand implements Command<CommandSource> {
	private static final ItemsCommand CMD = new ItemsCommand();

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("items").executes(CMD);

		builder.then(Commands.argument("item", ItemArgument.item())
				.then(LiteralArgumentBuilder.<CommandSource>literal("tags").executes(ItemsCommand::printTags)));

		return builder;
	}

	protected String commandName() {
		return "Blocks";
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out data related to a given block.");

		return 1;
	}

	private static int printTags(CommandContext<CommandSource> cmd) {
		Item item = ItemArgument.getItem(cmd, "item").getItem();
		String itemName = ObjectHelper.getItemName(item);
		File outputFile;
		String fileName = "Item Tags - " + itemName;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		ItemDataPrintHandler printHandler = ItemDataSkimmer.get(item);

		if (printHandler == null) {
			WikiHelperCommand.error(cmd.getSource(), "Items", "Unable to find compiled item data for item: " + itemName);

			return 1;
		}

		try (PrintHelper printHelper = PrintHelper.open(fileName)) {
			printHelper.withClipboardOutput(clipboardContent);

			for (String line : printHandler.getTagsPrintout()) {
				printHelper.write(line);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Items", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}
}
