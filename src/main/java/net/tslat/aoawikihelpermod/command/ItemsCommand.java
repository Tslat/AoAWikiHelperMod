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
import it.unimi.dsi.fastutil.ints.IntIterators;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
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
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.dataskimmers.ItemDataSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.ItemDataPrintHandler;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ItemsCommand implements Command<CommandSourceStack> {
	private static final ItemsCommand CMD = new ItemsCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext buildContext) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("items").executes(CMD);

		builder.then(Commands.argument("item", net.minecraft.commands.arguments.item.ItemArgument.item(buildContext))
				.suggests(ITEM_PROVIDER.getProvider())
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
		Item item = ForgeRegistries.ITEMS.getDelegateOrThrow(cmd.getArgument("id", ResourceLocation.class)).get();
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

	public static final class ItemCategoryProvider {
		private final String categoryName;
		private final Class<? extends Item> classType;

		private SuggestionProvider<CommandSourceStack> provider = null;

		public ItemCategoryProvider(String categoryName, Class<? extends Item> classType) {
			this.classType = classType;
			this.categoryName = categoryName;
			this.getProvider();
		}

		public String getCategoryName() {
			return this.categoryName;
		}

		public Predicate<ResourceLocation> isIdOfType() {
			return id -> {
				Item item = ForgeRegistries.ITEMS.getDelegateOrThrow(id).get();
				if (this.classType.isInstance(item)) {
					return true;
				}
				return false;
			};
		}

		public SuggestionProvider<CommandSourceStack> getProvider() {
			if (provider != null) return this.provider;
			this.provider = SuggestionProviders.register(
					new ResourceLocation(AoAWikiHelperMod.MOD_ID, "item_" + categoryName),
					(context, suggestionBuilder) -> SharedSuggestionProvider.suggestResource(
							ForgeRegistries.ITEMS.getKeys().stream().filter(this.isIdOfType()),
							suggestionBuilder,
							loc -> loc,
							(id) -> Component.translatable(Util.makeDescriptionId("item", id))
					)
			);
			return this.provider;
		}
	}

	//because we filter ForgeRegistry this will only contain AoA (or other mod) items
	public static final ItemCategoryProvider ITEM_PROVIDER = new ItemCategoryProvider("item", Item.class);
	public static final List<ItemCategoryProvider> ITEM_CATEGORY_PROVIDERS = List.of(
			new ItemCategoryProvider("blaster", BaseBlaster.class),
			new ItemCategoryProvider("bow", BaseBow.class),
			new ItemCategoryProvider("cannon", BaseCannon.class),
			new ItemCategoryProvider("crossbow", BaseCrossbow.class),
			new ItemCategoryProvider("greatblade", BaseGreatblade.class),
			new ItemCategoryProvider("gun", BaseGun.class),
			new ItemCategoryProvider("maul", BaseMaul.class),
			new ItemCategoryProvider("shotgun", BaseShotgun.class),
			new ItemCategoryProvider("sniper", BaseSniper.class),
			new ItemCategoryProvider("staff", BaseStaff.class),
			new ItemCategoryProvider("sword", BaseSword.class),
			new ItemCategoryProvider("thrown", BaseThrownWeapon.class),
			new ItemCategoryProvider("vulcane", BaseVulcane.class)
	);
}
