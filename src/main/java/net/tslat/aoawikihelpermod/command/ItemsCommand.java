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
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.content.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.content.item.weapon.bow.BaseBow;
import net.tslat.aoa3.content.item.weapon.cannon.BaseCannon;
import net.tslat.aoa3.content.item.weapon.crossbow.BaseCrossbow;
import net.tslat.aoa3.content.item.weapon.greatblade.BaseGreatblade;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import net.tslat.aoa3.content.item.weapon.maul.BaseMaul;
import net.tslat.aoa3.content.item.weapon.shotgun.BaseShotgun;
import net.tslat.aoa3.content.item.weapon.sniper.BaseSniper;
import net.tslat.aoa3.content.item.weapon.staff.BaseStaff;
import net.tslat.aoa3.content.item.weapon.sword.BaseSword;
import net.tslat.aoa3.content.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.content.item.weapon.vulcane.BaseVulcane;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoawikihelpermod.dataskimmers.ItemDataSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.ItemDataPrintHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.concurrent.CompletableFuture;
import java.util.Collection;

public class ItemsCommand implements Command<CommandSourceStack> {
	private static final ItemsCommand CMD = new ItemsCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext buildContext) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("items").executes(CMD);

		builder.then(Commands.argument("item", net.minecraft.commands.arguments.item.ItemArgument.item(buildContext))
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
		Item item = net.minecraft.commands.arguments.item.ItemArgument.getItem(cmd, "item").getItem();
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

	public static class ItemArgument implements ArgumentType<ItemInput> {
		private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:wooden_sword", "aoa3:limonite_sword");

		public static ItemArgument item() {
			return new ItemArgument();
		}

		public static ItemArgument item(CommandBuildContext buildContext) {
			return item();
		}

		@Override
		public ItemInput parse(StringReader reader) throws CommandSyntaxException {
			ResourceLocation id = ResourceLocation.read(reader);
			try {
				Item item = ForgeRegistries.ITEMS.getDelegateOrThrow(id).get();
				return new ItemInput(item);
			} catch (Exception e) {
				ItemParser.ERROR_UNKNOWN_ITEM.createWithContext(reader, id.toString());
			}

			return null;
		}

		public static ItemInput getItem(CommandContext<?> context, String argumentName) {
			return context.getArgument(argumentName, ItemInput.class);
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

	public static final Map<String, Class<? extends ItemArgumentByType>> ITEM_ARGUMENT_CLASSES = Map.ofEntries(
			Map.entry("blaster", ItemArgumentTypeBlaster.class),
			Map.entry("bow", ItemArgumentTypeBow.class),
			Map.entry("cannon", ItemArgumentTypeCannon.class),
			Map.entry("crossbow", ItemArgumentTypeCrossbow.class),
			Map.entry("greatblade", ItemArgumentTypeGreatblade.class),
			Map.entry("gun", ItemArgumentTypeGun.class),
			Map.entry("maul", ItemArgumentTypeMaul.class),
			Map.entry("shotgun", ItemArgumentTypeShotgun.class),
			Map.entry("sniper", ItemArgumentTypeSniper.class),
			Map.entry("staff", ItemArgumentTypeStaff.class),
			Map.entry("sword", ItemArgumentTypeSword.class),
			Map.entry("thrown", ItemArgumentTypeThrownWeapon.class),
			Map.entry("vulcane", ItemArgumentTypeVulcane.class)
	);

	public static class ItemArgumentByType implements ArgumentType<ItemInput> {
		ItemArgumentByType(Class<? extends Item> type) {
			this.type = type;
		}

		private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:wooden_sword", "aoa3:limonite_sword");

		private Class<? extends Item> type;
		private ArrayList<ResourceLocation> suggestions = new ArrayList<ResourceLocation>();

		private ArrayList<ResourceLocation> getSuggestions() {
			if (suggestions.size() == 0) {
				for (ResourceLocation key : ForgeRegistries.ITEMS.getKeys()) {
					try {
						Item itemFromId = ForgeRegistries.ITEMS.getDelegateOrThrow(key).get();
						if (type.isInstance(itemFromId)) {
							this.suggestions.add(key);
						}
					} catch (Exception e) {
					}
				}
			}
			return this.suggestions;
		}

		public static ItemArgumentByType item(Class<? extends Item> type) {
			return new ItemArgumentByType(type);
		}

		public static ItemArgumentByType item(CommandBuildContext buildContext, Class<? extends Item> type) {
			return item(type);
		}

		@Override
		public ItemInput parse(StringReader reader) throws CommandSyntaxException {
			ResourceLocation id = ResourceLocation.read(reader);
			try {
				Item item = ForgeRegistries.ITEMS.getDelegateOrThrow(id).get();
				return new ItemInput(item);
			} catch (Exception e) {
				ItemParser.ERROR_UNKNOWN_ITEM.createWithContext(reader, id.toString());
			}

			return null;
		}

		public static ItemInput getItem(CommandContext<?> context, String argumentName) {
			return context.getArgument(argumentName, ItemInput.class);
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			StringReader reader = new StringReader(builder.getInput());

			reader.setCursor(builder.getStart());

			return SharedSuggestionProvider.suggestResource(getSuggestions(), builder.createOffset(reader.getCursor()));
		}

		@Override
		public Collection<String> getExamples() {
			return EXAMPLES;
		}
	}

	public static class ItemArgumentTypeBlaster extends ItemArgumentByType {
		public ItemArgumentTypeBlaster() {
			super(BaseBlaster.class);
		}
	}

	public static class ItemArgumentTypeBow extends ItemArgumentByType {
		public ItemArgumentTypeBow() {
			super(BaseBow.class);
		}
	}

	public static class ItemArgumentTypeCannon extends ItemArgumentByType {
		public ItemArgumentTypeCannon() {
			super(BaseCannon.class);
		}
	}

	public static class ItemArgumentTypeCrossbow extends ItemArgumentByType {
		public ItemArgumentTypeCrossbow() {
			super(BaseCrossbow.class);
		}
	}

	public static class ItemArgumentTypeGreatblade extends ItemArgumentByType {
		public ItemArgumentTypeGreatblade() {
			super(BaseGreatblade.class);
		}
	}

	public static class ItemArgumentTypeGun extends ItemArgumentByType {
		public ItemArgumentTypeGun() {
			super(BaseGun.class);
		}
	}

	public static class ItemArgumentTypeMaul extends ItemArgumentByType {
		public ItemArgumentTypeMaul() {
			super(BaseMaul.class);
		}
	}

	public static class ItemArgumentTypeShotgun extends ItemArgumentByType {
		public ItemArgumentTypeShotgun() {
			super(BaseShotgun.class);
		}
	}

	public static class ItemArgumentTypeSniper extends ItemArgumentByType {
		public ItemArgumentTypeSniper() {
			super(BaseSniper.class);
		}
	}

	public static class ItemArgumentTypeStaff extends ItemArgumentByType {
		public ItemArgumentTypeStaff() {
			super(BaseStaff.class);
		}
	}

	public static class ItemArgumentTypeSword extends ItemArgumentByType {
		public ItemArgumentTypeSword() {
			super(BaseSword.class);
		}
	}

	public static class ItemArgumentTypeThrownWeapon extends ItemArgumentByType {
		public ItemArgumentTypeThrownWeapon() {
			super(BaseThrownWeapon.class);
		}
	}

	public static class ItemArgumentTypeVulcane extends ItemArgumentByType {
		public ItemArgumentTypeVulcane() {
			super(BaseVulcane.class);
		}
	}


}
