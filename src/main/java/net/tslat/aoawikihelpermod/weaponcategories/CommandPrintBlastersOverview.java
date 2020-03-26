package net.tslat.aoawikihelpermod.weaponcategories;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tslat.aoa3.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.library.misc.AoAAttributes;
import net.tslat.aoa3.utils.ItemUtil;
import net.tslat.aoa3.utils.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandPrintBlastersOverview extends CommandBase {
	@Override
	public String getName() {
		return "printblastersoverview";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printblastersoverview [clipboard] - Prints out all AoA blasters data to file. Optionally copy contents to clipboard.";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();

		if (!(sender instanceof EntityPlayer)) {
			sender.sendMessage(new TextComponentString("This command can only be done ingame for accuracy."));

			return;
		}

		if (!world.isRemote) {
			boolean copyToClipboard = args.length > 0 && args[0].equalsIgnoreCase("clipboard");

			if (copyToClipboard && server.isDedicatedServer()) {
				sender.sendMessage(new TextComponentString("Can't copy contents of file to clipboard on dedicated servers, skipping."));
				copyToClipboard = false;
			}

			List<String> data = new ArrayList<String>();
			List<BaseBlaster> blasters = new ArrayList<BaseBlaster>();

			for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
				if (item instanceof BaseBlaster)
					blasters.add((BaseBlaster)item);
			}

			blasters = blasters.stream().sorted(Comparator.comparing(blaster -> blaster.getItemStackDisplayName(new ItemStack(blaster)))).collect(Collectors.toList());

			data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
			data.add("|- style=\"background-color:#eee\" | data-sort-type=number |");
			data.add("! Name !! Damage !! Unholster time !! Fire rate !! Energy cost !! Durability !! Effects");
			data.add("|-");

			for (BaseBlaster blaster : blasters) {
				ItemStack blasterStack = new ItemStack(blaster);
				String name = blaster.getItemStackDisplayName(blasterStack);
				String unholsterTime = StringUtil.roundToNthDecimalPlace(1 / (float)(ItemUtil.getStackAttributeValue(blasterStack, SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.VANILLA_ATTACK_SPEED)), 2) + "s";
				String firingSpeed = (2000 / blaster.getFiringDelay()) / (double)100 + "/sec";

				data.add("| [[File:" + name + ".png|64px|link=]] '''[[" + name + "]]''' || {{hp|" + StringUtil.roundToNthDecimalPlace((float)blaster.getDamage(), 1) + "}} || " + unholsterTime + " || " + firingSpeed + " || " + StringUtil.roundToNthDecimalPlace(blaster.getEnergyCost(), 2) + " || " + blaster.getMaxDamage(blasterStack) + " || ");
				data.add("|-");
			}

			data.add("|}");
			CategoryTableWriter.writeData("Blasters", data, sender, copyToClipboard);
		}
	}
}
