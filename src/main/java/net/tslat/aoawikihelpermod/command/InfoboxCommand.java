package net.tslat.aoawikihelpermod.command;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.infoboxes.BlockInfoboxPrintHelper;
import net.tslat.aoawikihelpermod.util.printers.infoboxes.ItemInfoboxPrintHelper;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

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
		ItemsCommand.ITEM_ARGUMENT_CLASSES.forEach((key, value) -> {
			try {
				builder.then(
						Commands.literal(key)
								.then(
										Commands.argument("id", value.getConstructor().newInstance())
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
		Item item = ItemsCommand.ItemArgument.getItem(cmd, "id").getItem();
		CommandSourceStack source = cmd.getSource();
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
		File outputFile;

		WikiHelperCommand.info(cmd.getSource(), "Infobox", "Printing item infobox for '" + ForgeRegistries.ITEMS.getKey(item) + "'...");
		String fileName = "Item Infobox - " + ObjectHelper.getItemName(item);

		try (ItemInfoboxPrintHelper printHelper = ItemInfoboxPrintHelper.open(fileName)) {
			printHelper.withClipboardOutput(clipboardContent);
			printHelper.printItemInfobox(item, cmd.getSource().getEntity());

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(source, "Infobox", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		return 1;
	}
}
