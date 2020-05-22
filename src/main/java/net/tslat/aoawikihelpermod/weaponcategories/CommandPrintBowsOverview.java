package net.tslat.aoawikihelpermod.weaponcategories;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tslat.aoa3.item.weapon.bow.BaseBow;
import net.tslat.aoa3.utils.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandPrintBowsOverview extends CommandBase {
	@Override
	public String getName() {
		return "printbowsoverview";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printbowsoverview [clipboard] - Prints out all AoA bows data to file. Optionally copy contents to clipboard.";
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
			List<BaseBow> bows = new ArrayList<BaseBow>();

			for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
				if (item instanceof BaseBow)
					bows.add((BaseBow)item);
			}

			bows = bows.stream().sorted(Comparator.comparing(bow -> bow.getItemStackDisplayName(new ItemStack(bow)))).collect(Collectors.toList());

			data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
			data.add("|- style=\"background-color:#eee\"");
			data.add("! Name !! data-sort-type=number | Damage !! Draw time !! Durability !! Effects");
			data.add("|-");

			for (BaseBow bow : bows) {
				ItemStack bowStack = new ItemStack(bow);
				String name = bow.getItemStackDisplayName(bowStack);
				String drawTime = (((int)(72000 / bow.getDrawSpeedMultiplier()) / 720) / (double)100) + "s";

				data.add("| [[File:" + name + ".png|32px|link=]] '''[[" + name + "]]''' || {{hp|" + StringUtil.roundToNthDecimalPlace((float)bow.getDamage(), 1) + "}} || " + drawTime + " || " + bow.getMaxDamage(bowStack) + " || ");
				data.add("|-");
			}

			data.add("|}");
			CategoryTableWriter.writeData("Bows", data, sender, copyToClipboard);
		}
	}
}
