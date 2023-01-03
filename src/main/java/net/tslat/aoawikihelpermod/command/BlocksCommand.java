package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.dataskimmers.BlockDataSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import net.tslat.aoawikihelpermod.util.printers.TablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.BlockDataPrintHandler;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class BlocksCommand implements Command<CommandSourceStack> {
	private static final BlocksCommand CMD = new BlocksCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("blocks").executes(CMD);

		builder.then(Commands.argument("id", ResourceLocationArgument.id())
				.suggests(BLOCK_PROVIDER.getProvider())
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("states").executes(BlocksCommand::printStates))
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("tags").executes(BlocksCommand::printTags)));

		return builder;
	}

	protected String commandName() {
		return "Blocks";
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out data related to a given block.");

		return 1;
	}

	private static int printStates(CommandContext<CommandSourceStack> cmd) {
		Block block = ForgeRegistries.BLOCKS.getDelegateOrThrow(cmd.getArgument("id", ResourceLocation.class)).get();
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

	private static int printTags(CommandContext<CommandSourceStack> cmd) {
		Block block = ForgeRegistries.BLOCKS.getDelegateOrThrow(cmd.getArgument("id", ResourceLocation.class)).get();
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

	public static final BlockProvider BLOCK_PROVIDER = new BlockProvider();
	public static final class BlockProvider {
		private SuggestionProvider<CommandSourceStack> provider = null;
		public BlockProvider(){
			this.getProvider();
		}

		public SuggestionProvider<CommandSourceStack> getProvider() {
			if(provider != null)return this.provider;
			this.provider = SuggestionProviders.register(
					new ResourceLocation(AoAWikiHelperMod.MOD_ID, "blocks"),
					(context, suggestionBuilder) -> SharedSuggestionProvider.suggestResource(
							ForgeRegistries.BLOCKS.getKeys().stream(),
							suggestionBuilder,
							loc -> loc,
							(id) -> Component.translatable(Util.makeDescriptionId("block", id))
					)
			);
			return this.provider;
		}
	}
}
