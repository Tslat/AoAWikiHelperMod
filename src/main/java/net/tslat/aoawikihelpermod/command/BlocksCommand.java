package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.tslat.aoa3.util.misc.MutableSupplier;
import net.tslat.aoawikihelpermod.dataskimmers.BlockDataSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import net.tslat.aoawikihelpermod.util.printers.TablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.recipe.BlockDataPrintHandler;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class BlocksCommand implements Command<CommandSource> {
	private static final BlocksCommand CMD = new BlocksCommand();

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("blocks").executes(CMD);

		builder.then(Commands.argument("block", BlockArgument.block())
				.then(LiteralArgumentBuilder.<CommandSource>literal("states").executes(BlocksCommand::printStates))
				.then(LiteralArgumentBuilder.<CommandSource>literal("tags").executes(BlocksCommand::printTags)));

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

	private static int printStates(CommandContext<CommandSource> cmd) {
		Block block = BlockArgument.getBlock(cmd, "block").getBlock();
		String blockName = ObjectHelper.getBlockName(block);
		File outputFile;
		String fileName = "Blockstates - " + blockName;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		BlockDataPrintHandler printHandler = BlockDataSkimmer.get(block);

		if (printHandler == null) {
			WikiHelperCommand.error(cmd.getSource(), "Blocks", "Unable to find compiled block data for block: " + blockName);

			return 1;
		}

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "State", "Allowed values", "Description")) {
			printHelper.withClipboardOutput(clipboardContent);
			printHelper.withProperty("class", "wikitable");

			for (String[] entry : printHandler.getStatePrintout()) {
				printHelper.entry(entry);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Blocks", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printTags(CommandContext<CommandSource> cmd) {
		Block block = BlockArgument.getBlock(cmd, "block").getBlock();
		String blockName = ObjectHelper.getBlockName(block);
		File outputFile;
		String fileName = "Block Tags - " + blockName;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		BlockDataPrintHandler printHandler = BlockDataSkimmer.get(block);

		if (printHandler == null) {
			WikiHelperCommand.error(cmd.getSource(), "Blocks", "Unable to find compiled block data for block: " + blockName);

			return 1;
		}

		try (PrintHelper printHelper = PrintHelper.open(fileName)) {
			printHelper.withClipboardOutput(clipboardContent);

			for (String line : printHandler.getTagsPrintout()) {
				printHelper.write(line);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Blocks", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	public static class BlockInput implements Predicate<Block> {
		private final Block block;

		public BlockInput(Block block) {
			this.block = block;
		}

		public Block getBlock() {
			return this.block;
		}

		public boolean test(Block bl) {
			return bl == block;
		}
	}

	public static class BlockArgument implements ArgumentType<BlockInput> {
		private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:coal_ore", "aoa3:white_shyre_bricks_wall");

		public static BlockArgument block() {
			return new BlockArgument();
		}

		@Override
		public BlockInput parse(StringReader reader) throws CommandSyntaxException {
			ResourceLocation id = ResourceLocation.read(reader);
			Block block = Registry.BLOCK.getOptional(id).orElseThrow(() -> ItemParser.ERROR_UNKNOWN_ITEM.createWithContext(reader, id.toString()));

			return new BlockInput(block);
		}

		 public static BlockInput getBlock(CommandContext<?> context, String argumentName) {
			return context.getArgument(argumentName, BlockInput.class);
		 }

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			StringReader reader = new StringReader(builder.getInput());

			reader.setCursor(builder.getStart());

			return ISuggestionProvider.suggestResource(Registry.BLOCK.keySet(), builder.createOffset(reader.getCursor()));
		}

		@Override
		public Collection<String> getExamples() {
			return EXAMPLES;
		}
	}
}
