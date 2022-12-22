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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoawikihelpermod.dataskimmers.ItemDataSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.ItemDataPrintHandler;

import java.io.File;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.concurrent.CompletableFuture;
import java.util.Collection;

public class ItemsCommand implements Command<CommandSourceStack> {
	private static final ItemsCommand CMD = new ItemsCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext buildContext) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("items").executes(CMD);

		builder.then(Commands.argument("item", ItemArgument.item(buildContext))
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("tags").executes(ItemsCommand::printTags)));

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

	private static int printTags(CommandContext<CommandSourceStack> cmd) {
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

	public static class ItemInput implements Predicate<Item> {
		private final Item item;

		public ItemInput(Item item) {
			this.item = item;
		}

		public Item getItem() {
			return this.item;
		}

		public boolean test(Item item) {
			return item == this.item;
		}
	}

	public static class ItemArgument implements ArgumentType<ItemsCommand.ItemInput> {
		private static final Collection<String> EXAMPLES = Arrays.asList("sword", "minecraft:torch", "aoa3:realmstone");

		public static ItemsCommand.ItemArgument item() {
			return new ItemsCommand.ItemArgument();
		}

		public static ItemsCommand.ItemArgument item(CommandBuildContext buildContext) {
			return item();
		}

		@Override
		public ItemsCommand.ItemInput parse(StringReader reader) throws CommandSyntaxException {
			ResourceLocation id = ResourceLocation.read(reader);
			Item item = BuiltInRegistries.ITEM.getOptional(id).orElseThrow(() -> ItemParser.ERROR_UNKNOWN_ITEM.createWithContext(reader, id.toString()));

			return new ItemsCommand.ItemInput(item);
		}

		public static ItemsCommand.ItemInput getItem(CommandContext<?> context, String argumentName) {
			return context.getArgument(argumentName, ItemsCommand.ItemInput.class);
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			StringReader reader = new StringReader(builder.getInput());

			reader.setCursor(builder.getStart());

			return SharedSuggestionProvider.suggestResource(ForgeRegistries.ITEMS.getKeys(), builder.createOffset(reader.getCursor()));
		}

		@Override
		public Collection<String> getExamples() {
			return EXAMPLES;
		}
	}
}
