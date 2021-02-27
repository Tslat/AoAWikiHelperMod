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
import net.tslat.aoa3.item.misc.RuneItem;
import net.tslat.aoa3.item.weapon.staff.BaseStaff;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandPrintStavesOverview extends CommandBase {
	@Override
	public String getName() {
		return "printstavesoverview";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printstavesoverview [clipboard] - Prints out all AoA staves data to file. Optionally copy contents to clipboard.";
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
			List<BaseStaff> staves = new ArrayList<BaseStaff>();

			for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
				if (item instanceof BaseStaff)
					staves.add((BaseStaff)item);
			}

			staves = staves.stream().sorted(Comparator.comparing(staff -> staff.getItemStackDisplayName(new ItemStack(staff)))).collect(Collectors.toList());

			data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
			data.add("|- style=\"background-color:#eee\" | data-sort-type=number |");
			data.add("! Name !! Runes !! Durability !! Effects");
			data.add("|-");

			for (BaseStaff staff : staves) {
				ItemStack staffStack = new ItemStack(staff);
				String name = staff.getItemStackDisplayName(staffStack);
				StringBuilder runesLineBuilder = new StringBuilder();

				for (Map.Entry<RuneItem, Integer> runeEntry : staff.getRunes().entrySet()) {
					String runeName = runeEntry.getKey().getItemStackDisplayName(new ItemStack(runeEntry.getKey()));

					runesLineBuilder.append("<br/>");
					runesLineBuilder.append(runeEntry.getValue()).append("x ").append("[[File:").append(runeName).append(".png|link=]] [[").append(runeName);

					if (runeEntry.getValue() > 1)
						runesLineBuilder.append("|").append(runeName).append("s");

					runesLineBuilder.append("]]");
				}


				data.add("| [[File:" + name + ".png|64px|link=]] '''[[" + name + "]]''' || " + runesLineBuilder.toString().substring(5) + " || " + staff.getMaxDamage(staffStack) + " || ");
				data.add("|-");
			}

			data.add("|}");
			CategoryTableWriter.writeData("Staves", data, sender, copyToClipboard);
		}
	}
}
