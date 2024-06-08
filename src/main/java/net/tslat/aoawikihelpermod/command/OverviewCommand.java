package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.tslat.aoa3.common.registration.item.AoATools;
import net.tslat.aoa3.common.registration.item.AoAWeapons;
import net.tslat.aoa3.content.item.tool.axe.BaseAxe;
import net.tslat.aoa3.content.item.tool.hoe.BaseHoe;
import net.tslat.aoa3.content.item.tool.pickaxe.BasePickaxe;
import net.tslat.aoa3.content.item.tool.shovel.BaseShovel;
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
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printer.TablePrintHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OverviewCommand implements Command<CommandSourceStack> {
	private static final OverviewCommand CMD = new OverviewCommand();

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("overview").executes(CMD);

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
		builder.then(Commands.literal("hoes").executes(OverviewCommand::printHoes));
		builder.then(Commands.literal("thrownWeapons").executes(OverviewCommand::printThrownWeapons));

		return builder;
	}

	protected String commandName() {
		return "Overview";
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print overviews for various categories of data in AoA");

		return 1;
	}

	private static int printAxes(CommandContext<CommandSourceStack> cmd) {
		List<Item> axes = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseAxe);
		String fileName = "Overview - Axes";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Attack Speed", "Efficiency", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : axes) {
				AxeItem axe = (AxeItem)item;
				String itemName = ObjectHelper.getItemName(axe);
				float damage = (float)ObjectHelper.getAttributeValueFromItem(axe, Attributes.ATTACK_DAMAGE);
				String attackSpeed = NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValueFromItem(axe, Attributes.ATTACK_SPEED) + 4, 2) + "/sec";
				String efficiency = NumberUtil.roundToNthDecimalPlace(axe.getTier().getSpeed(), 2);
				String durability = String.valueOf(axe.getTier().getUses());
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(axe, AoATools.LIMONITE_AXE.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						FormattingHelper.healthValue(damage),
						attackSpeed,
						efficiency,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printBlasters(CommandContext<CommandSourceStack> cmd) {
		List<Item> blasters = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseBlaster);
		String fileName = "Overview - Blasters";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Unholster Time", "Fire Rate", "Spirit Cost", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : blasters) {
				BaseBlaster blaster = (BaseBlaster)item;
				ItemStack stack = blaster.getDefaultInstance();
				String itemName = ObjectHelper.getItemName(blaster);
				String damage = FormattingHelper.healthValue((float)blaster.getBlasterDamage(stack));
				String unholsterTime = NumberUtil.roundToNthDecimalPlace(1 / ((float)ObjectHelper.getAttributeValueFromItem(blaster, Attributes.ATTACK_SPEED) + 4), 2) + "s";
				String fireRate = NumberUtil.roundToNthDecimalPlace(20 / (float)blaster.getTicksBetweenShots(stack), 2) + "/sec";
				String energyCost = NumberUtil.roundToNthDecimalPlace(blaster.getBaseEnergyCost(stack), 2);
				String durability = String.valueOf(blaster.getMaxDamage(new ItemStack(blaster)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(blaster, AoAWeapons.BONE_BLASTER.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName, 64) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						unholsterTime,
						fireRate,
						energyCost,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printBows(CommandContext<CommandSourceStack> cmd) {
		List<Item> bows = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseBow);
		String fileName = "Overview - Bows";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Average Damage (Full Charge)", "Draw Time", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : bows) {
				BaseBow bow = (BaseBow)item;
				String itemName = ObjectHelper.getItemName(bow);
				String damage = FormattingHelper.healthValue(bow.getBowDamage());
				String drawTime = NumberUtil.roundToNthDecimalPlace(1 / bow.getDrawSpeedMultiplier(), 2) + "s";
				String durability = String.valueOf(bow.getMaxDamage(new ItemStack(bow)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(bow, AoAWeapons.ALACRITY_BOW.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						drawTime,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printCannons(CommandContext<CommandSourceStack> cmd) {
		List<Item> cannons = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseCannon);
		String fileName = "Overview - Cannons";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Unholster Time", "Fire Rate", "Recoil", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : cannons) {
				BaseCannon cannon = (BaseCannon)item;
				ItemStack stack = cannon.getDefaultInstance();
				String itemName = ObjectHelper.getItemName(cannon);
				String damage = FormattingHelper.healthValue(cannon.getGunDamage(stack));
				String unholsterTime = NumberUtil.roundToNthDecimalPlace(1 / ((float)ObjectHelper.getAttributeValueFromItem(cannon, Attributes.ATTACK_SPEED) + 4), 2) + "s";
				String fireRate = NumberUtil.roundToNthDecimalPlace(20 / (float)cannon.getTicksBetweenShots(stack), 2) + "/sec";
				String recoil = NumberUtil.roundToNthDecimalPlace(cannon.getRecoilForShot(stack, (LivingEntity)cmd.getSource().getEntity()), 2);
				String durability = String.valueOf(cannon.getMaxDamage(new ItemStack(cannon)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(cannon, AoAWeapons.MINI_CANNON.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						unholsterTime,
						fireRate,
						recoil,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printCrossbows(CommandContext<CommandSourceStack> cmd) {
		List<Item> crossbows = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseCrossbow);
		String fileName = "Overview - Crossbows";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : crossbows) {
				BaseCrossbow crossbow = (BaseCrossbow)item;
				ItemStack stack = crossbow.getDefaultInstance();
				String itemName = ObjectHelper.getItemName(crossbow);
				String damage = FormattingHelper.healthValue((float)crossbow.getCrossbowDamage(stack));
				String durability = String.valueOf(crossbow.getMaxDamage(new ItemStack(crossbow)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(crossbow, AoAWeapons.TROLLS_CROSSBOW.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printGreatblades(CommandContext<CommandSourceStack> cmd) {
		List<Item> greatblades = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseGreatblade);
		String fileName = "Overview - Greatblades";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Attack Speed", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : greatblades) {
				BaseGreatblade greatblade = (BaseGreatblade)item;
				ItemStack stack = greatblade.getDefaultInstance();
				String itemName = ObjectHelper.getItemName(greatblade);
				String damage = FormattingHelper.healthValue(greatblade.getBaseDamage(stack));
				String attackSpeed = NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValueFromItem(greatblade, Attributes.ATTACK_SPEED) + 4, 2) + "/sec";
				String durability = String.valueOf(greatblade.getMaxDamage(new ItemStack(greatblade)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(greatblade, AoAWeapons.ROYAL_GREATBLADE.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName, 64) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						attackSpeed,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printGuns(CommandContext<CommandSourceStack> cmd) {
		List<Item> guns = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseGun && !(item instanceof BaseCannon) && !(item instanceof BaseSniper) && !(item instanceof BaseShotgun) && !(item instanceof BaseThrownWeapon));
		String fileName = "Overview - Guns";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Unholster Time", "Fire Rate", "Recoil", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : guns) {
				BaseGun gun = (BaseGun)item;
				ItemStack stack = gun.getDefaultInstance();
				String itemName = ObjectHelper.getItemName(item);
				String damage = FormattingHelper.healthValue(gun.getGunDamage(stack));
				String unholsterTime = NumberUtil.roundToNthDecimalPlace(1 / ((float)ObjectHelper.getAttributeValueFromItem(gun, Attributes.ATTACK_SPEED) + 4), 2) + "s";
				String fireRate = NumberUtil.roundToNthDecimalPlace(20 / (float)gun.getTicksBetweenShots(stack), 2) + "/sec";
				String recoil = NumberUtil.roundToNthDecimalPlace(gun.getRecoilForShot(gun.getDefaultInstance(), (LivingEntity)cmd.getSource().getEntity()), 2);
				String durability = String.valueOf(gun.getMaxDamage(new ItemStack(gun)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(gun, AoAWeapons.SQUAD_GUN.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName, 64) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						unholsterTime,
						fireRate,
						recoil,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printMauls(CommandContext<CommandSourceStack> cmd) {
		List<Item> mauls = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseMaul);
		String fileName = "Overview - Mauls";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Attack Speed", "Knockback (Approx)", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : mauls) {
				BaseMaul maul = (BaseMaul)item;
				String itemName = ObjectHelper.getItemName(maul);
				String damage = FormattingHelper.healthValue(maul.getAttackDamage());
				String attackSpeed = NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValueFromItem(maul, Attributes.ATTACK_SPEED) + 4, 2) + "/sec";
				String knockback = "+" + NumberUtil.roundToNthDecimalPlace((float)maul.getBaseKnockback(), 2);
				String durability = String.valueOf(maul.getMaxDamage(new ItemStack(maul)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(maul, AoAWeapons.CORALSTONE_MAUL.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName, 64) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						attackSpeed,
						knockback,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printPickaxes(CommandContext<CommandSourceStack> cmd) {
		List<Item> pickaxes = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BasePickaxe);
		String fileName = "Overview - Pickaxes";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Attack Speed", "Efficiency", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : pickaxes) {
				PickaxeItem pickaxe = (PickaxeItem)item;
				String itemName = ObjectHelper.getItemName(pickaxe);
				float damage = (float)ObjectHelper.getAttributeValueFromItem(pickaxe, Attributes.ATTACK_DAMAGE);
				String attackSpeed = NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValueFromItem(pickaxe, Attributes.ATTACK_SPEED) + 4, 2) + "/sec";
				String efficiency = NumberUtil.roundToNthDecimalPlace(pickaxe.getTier().getSpeed(), 2);
				String durability = String.valueOf(pickaxe.getTier().getUses());
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(pickaxe, AoATools.LIMONITE_PICKAXE.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						FormattingHelper.healthValue(damage),
						attackSpeed,
						efficiency,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printShotguns(CommandContext<CommandSourceStack> cmd) {
		List<Item> shotguns = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseShotgun);
		String fileName = "Overview - Shotguns";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage (Per Pellet)", "Pellets", "Unholster Time", "Fire Rate", "Recoil", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : shotguns) {
				BaseShotgun shotgun = (BaseShotgun)item;
				ItemStack stack = shotgun.getDefaultInstance();
				String itemName = ObjectHelper.getItemName(shotgun);
				String damage = FormattingHelper.healthValue(shotgun.getGunDamage(stack)) + " x" + shotgun.getPelletCount(stack);
				String pellets = String.valueOf(shotgun.getPelletCount(stack));
				String unholsterTime = NumberUtil.roundToNthDecimalPlace(1 / ((float)ObjectHelper.getAttributeValueFromItem(shotgun, Attributes.ATTACK_SPEED) + 4), 2) + "s";
				String fireRate = NumberUtil.roundToNthDecimalPlace(20 / (float)shotgun.getTicksBetweenShots(stack), 2) + "/sec";
				String recoil = NumberUtil.roundToNthDecimalPlace(shotgun.getRecoilForShot(shotgun.getDefaultInstance(), (LivingEntity)cmd.getSource().getEntity()), 2);
				String durability = String.valueOf(shotgun.getMaxDamage(new ItemStack(shotgun)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(shotgun, AoAWeapons.MINI_CANNON.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName, 64) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						pellets,
						unholsterTime,
						fireRate,
						recoil,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printShovels(CommandContext<CommandSourceStack> cmd) {
		List<Item> shovels = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseShovel);
		String fileName = "Overview - Shovels";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Attack Speed", "Efficiency", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : shovels) {
				ShovelItem shovel = (ShovelItem)item;
				String itemName = ObjectHelper.getItemName(shovel);
				float damage = (float)ObjectHelper.getAttributeValueFromItem(shovel, Attributes.ATTACK_DAMAGE);
				String attackSpeed = NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValueFromItem(shovel, Attributes.ATTACK_SPEED) + 4, 2) + "/sec";
				String efficiency = NumberUtil.roundToNthDecimalPlace(shovel.getTier().getSpeed(), 2);
				String durability = String.valueOf(shovel.getTier().getUses());
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(shovel, AoATools.LIMONITE_SHOVEL.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						FormattingHelper.healthValue(damage),
						attackSpeed,
						efficiency,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printSnipers(CommandContext<CommandSourceStack> cmd) {
		List<Item> snipers = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseSniper);
		String fileName = "Overview - Snipers";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Unholster Time", "Fire Rate", "Recoil", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : snipers) {
				BaseSniper sniper = (BaseSniper)item;
				ItemStack stack = sniper.getDefaultInstance();
				String itemName = ObjectHelper.getItemName(sniper);
				String damage = FormattingHelper.healthValue(sniper.getGunDamage(stack));
				String unholsterTime = NumberUtil.roundToNthDecimalPlace(1 / ((float)ObjectHelper.getAttributeValueFromItem(sniper, Attributes.ATTACK_SPEED) + 4), 2) + "s";
				String fireRate = NumberUtil.roundToNthDecimalPlace(20 / (float)sniper.getTicksBetweenShots(stack), 2) + "/sec";
				String recoil = NumberUtil.roundToNthDecimalPlace(sniper.getRecoilForShot(sniper.getDefaultInstance(), (LivingEntity)cmd.getSource().getEntity()) * 0.25f, 2);
				String durability = String.valueOf(sniper.getMaxDamage(new ItemStack(sniper)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(sniper, AoAWeapons.MINI_CANNON.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName, 64) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						unholsterTime,
						fireRate,
						recoil,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printStaves(CommandContext<CommandSourceStack> cmd) {
		List<Item> staves = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseStaff);
		String fileName = "Overview - Staves";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Runes", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : staves) {
				BaseStaff<?> staff = (BaseStaff<?>)item;
				String itemName = ObjectHelper.getItemName(staff);
				ArrayList<String> runeArray = new ArrayList<String>();
				String durability = String.valueOf(staff.getMaxDamage(new ItemStack(staff)));
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(staff, AoAWeapons.MINI_CANNON.get());


				for (Object2IntMap.Entry<Item> runeEntry : staff.runeCost().runeCosts().object2IntEntrySet()) {
					String name = ObjectHelper.getItemName(runeEntry.getKey());

					runeArray.add(runeEntry.getIntValue() + "x " + FormattingHelper.createImageBlock(name) + " " + FormattingHelper.createLinkableText(name, runeEntry.getIntValue() > 1 , true));
				}

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName, 64) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						FormattingHelper.listToString(runeArray, false),
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printSwords(CommandContext<CommandSourceStack> cmd) {
		List<Item> swords = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseSword);
		String fileName = "Overview - Swords";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Attack Speed", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : swords) {
				BaseSword sword = (BaseSword)item;
				String itemName = ObjectHelper.getItemName(item);
				float damage = (float)ObjectHelper.getAttributeValueFromItem(item, Attributes.ATTACK_DAMAGE);
				String attackSpeed = NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValueFromItem(item, Attributes.ATTACK_SPEED) + 4, 2) + "/sec";
				String durability = String.valueOf(sword.getTier().getUses());
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(item, AoAWeapons.LIMONITE_SWORD.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						FormattingHelper.healthValue(damage),
						attackSpeed,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printHoes(CommandContext<CommandSourceStack> cmd) {
		List<Item> hoes = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseHoe);
		String fileName = "Overview - Hoes";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Attack Speed", "Durability", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : hoes) {
				BaseHoe sword = (BaseHoe)item;
				String itemName = ObjectHelper.getItemName(item);
				float damage = (float)ObjectHelper.getAttributeValueFromItem(item, Attributes.ATTACK_DAMAGE);
				String attackSpeed = NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValueFromItem(item, Attributes.ATTACK_SPEED) + 4, 2) + "/sec";
				String durability = String.valueOf(sword.getTier().getUses());
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(item, AoATools.LIMONITE_HOE.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						FormattingHelper.healthValue(damage),
						attackSpeed,
						durability,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}

	private static int printThrownWeapons(CommandContext<CommandSourceStack> cmd) {
		List<Item> thrownWeapons = ObjectHelper.scrapeRegistryForItems(item -> item instanceof BaseThrownWeapon);
		String fileName = "Overview - Thrown Weapons";
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Name", "Damage", "Throw Rate", "Effects")) {
			printHelper.defaultFullPageTableProperties();
			printHelper.withProperty("class", "sortable");
			printHelper.withClipboardOutput(clipboardContent);

			for (Item item : thrownWeapons) {
				BaseThrownWeapon thrownWeapon = (BaseThrownWeapon)item;
				ItemStack stack = thrownWeapon.getDefaultInstance();
				String itemName = ObjectHelper.getItemName(item);
				String damage = FormattingHelper.healthValue(thrownWeapon.getGunDamage(stack));
				String fireRate = NumberUtil.roundToNthDecimalPlace(20 / (float)thrownWeapon.getTicksBetweenShots(stack), 2) + "/sec";
				String tooltip = ObjectHelper.attemptToExtractItemSpecificEffects(thrownWeapon, AoAWeapons.SQUAD_GUN.get());

				printHelper.entry(
						FormattingHelper.createImageBlock(itemName) + " " + FormattingHelper.bold(FormattingHelper.createLinkableText(itemName, false, true)),
						damage,
						fireRate,
						tooltip);
			}

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(cmd.getSource(), "Overview", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));

		return 1;
	}
}
