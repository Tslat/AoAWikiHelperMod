package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.tslat.aoa3.common.registration.AoATools;
import net.tslat.aoa3.item.tool.axe.EmberstoneAxe;
import net.tslat.aoa3.library.misc.MutableSupplier;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.PrintHelper;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class OverviewCommand implements Command<CommandSource> {
	private static final OverviewCommand CMD = new OverviewCommand();

	public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("overview").executes(CMD);

		builder.then(Commands.literal("axes").executes(OverviewCommand::printAxes));
		builder.then(Commands.literal("blasters").executes(OverviewCommand::printBlasters));
		builder.then(Commands.literal("bows").executes(OverviewCommand::printBows));
		builder.then(Commands.literal("cannons").executes(OverviewCommand::printCannons));
		builder.then(Commands.literal("crossbows").executes(OverviewCommand::printCrossbows));
		builder.then(Commands.literal("greatblades").executes(OverviewCommand::printGreatblades));
		builder.then(Commands.literal("guns").executes(OverviewCommand::printGuns));
		builder.then(Commands.literal("mauls").executes(OverviewCommand::printMauls));
		builder.then(Commands.literal("pickaxes").executes(OverviewCommand::printPickaxes));
		builder.then(Commands.literal("shotguns").executes(OverviewCommand::printShotguns));
		builder.then(Commands.literal("shovels").executes(OverviewCommand::printShovels));
		builder.then(Commands.literal("snipers").executes(OverviewCommand::printSnipers));
		builder.then(Commands.literal("staves").executes(OverviewCommand::printStaves));
		builder.then(Commands.literal("swords").executes(OverviewCommand::printSwords));
		builder.then(Commands.literal("thrownWeapons").executes(OverviewCommand::printThrownWeapons));

		return builder;
	}

	protected String commandName() {
		return "Overview";
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print overviews for various categories of data in AoA");

		return 1;
	}

	private static int printAxes(CommandContext<CommandSource> cmd) {
		List<Item> axes = ObjectHelper.scrapeRegistryForItems(item -> item.getRegistryName().getNamespace().equals("aoa3") && item instanceof AxeItem);
		String fileName = "Overview - Axes";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (PrintHelper.TablePrintHelper printHelper = PrintHelper.TablePrintHelper.open(fileName, "Name", "Damage", "Attack Speed", "Harvest Level", "Efficiency", "Durability", "Effects")) {
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : axes) {
				AxeItem axe = (AxeItem)item;
				String itemName = ObjectHelper.getItemName(item);
				float damage = (float)ObjectHelper.getAttributeFromItem(item, Attributes.ATTACK_DAMAGE);
				String attackSpeed = NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeFromItem(item, Attributes.ATTACK_SPEED) + 4, 2);
				String efficiency = NumberUtil.roundToNthDecimalPlace(axe.getTier().getSpeed(), 2);
				String durability = String.valueOf(axe.getTier().getUses());
				String harvestLevel = String.valueOf(axe.getTier().getLevel());
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(item, AoATools.AMETHYST_AXE.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createObjectBlock(itemName, false, false,true)),
						FormattingHelper.healthValue(damage),
						attackSpeed,
						harvestLevel,
						efficiency,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printBlasters(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printBows(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printCannons(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printCrossbows(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printGreatblades(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printGuns(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printMauls(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printPickaxes(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printShotguns(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printShovels(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printSnipers(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printStaves(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printSwords(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}

	private static int printThrownWeapons(CommandContext<CommandSource> cmd) throws CommandSyntaxException {


		return 1;
	}
}
