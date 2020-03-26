package net.tslat.aoawikihelpermod.weaponcategories;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tslat.aoa3.item.tool.shovel.BaseShovel;
import net.tslat.aoa3.utils.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandPrintShovelsOverview extends CommandBase {
	@Override
	public String getName() {
		return "printshovelsoverview";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printshovelsoverview [clipboard] - Prints out all AoA shovels data to file. Optionally copy contents to clipboard.";
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
			List<BaseShovel> shovels = new ArrayList<BaseShovel>();

			for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
				if (item instanceof BaseShovel)
					shovels.add((BaseShovel)item);
			}

			shovels = shovels.stream().sorted(Comparator.comparing(shovel -> shovel.getItemStackDisplayName(new ItemStack(shovel)))).collect(Collectors.toList());

			data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
			data.add("|- style=\"background-color:#eee\" | data-sort-type=number |");
			data.add("! Name !! Damage !! Efficiency !! Durability !! Effects");
			data.add("|-");

			for (BaseShovel shovel : shovels) {
				ItemStack axeStack = new ItemStack(shovel);
				String name = shovel.getItemStackDisplayName(axeStack);
				float efficiency = ObfuscationReflectionHelper.getPrivateValue(ItemTool.class, shovel, "field_77864_a");
				float damage = ObfuscationReflectionHelper.getPrivateValue(ItemTool.class, shovel, "field_77865_bY");

				data.add("| [[File:" + name + ".png|32px|link=]] '''[[" + name + "]]''' || {{hp|" + StringUtil.roundToNthDecimalPlace(damage, 1) + "}} || " + StringUtil.roundToNthDecimalPlace(efficiency, 1) + " || " + shovel.getMaxDamage(axeStack) + " || ");
				data.add("|-");
			}

			data.add("|}");
			CategoryTableWriter.writeData("Shovels", data, sender, copyToClipboard);
		}
	}
}
