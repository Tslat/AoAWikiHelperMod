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
import net.tslat.aoa3.item.tool.pickaxe.BasePickaxe;
import net.tslat.aoa3.utils.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandPrintPickaxesOverview extends CommandBase {
	@Override
	public String getName() {
		return "printpickaxesoverview";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printpickaxesoverview [clipboard] - Prints out all AoA pickaxes data to file. Optionally copy contents to clipboard.";
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
			List<BasePickaxe> pickaxes = new ArrayList<BasePickaxe>();

			for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
				if (item instanceof BasePickaxe)
					pickaxes.add((BasePickaxe)item);
			}

			pickaxes = pickaxes.stream().sorted(Comparator.comparing(pickaxe -> pickaxe.getItemStackDisplayName(new ItemStack(pickaxe)))).collect(Collectors.toList());

			data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
			data.add("|- style=\"background-color:#eee\"");
			data.add("! Name !! data-sort-type=number | Damage !! Efficiency !! Durability !! Effects");
			data.add("|-");

			for (BasePickaxe pickaxe : pickaxes) {
				ItemStack pickaxeStack = new ItemStack(pickaxe);
				String name = pickaxe.getItemStackDisplayName(pickaxeStack);
				float efficiency = ObfuscationReflectionHelper.getPrivateValue(ItemTool.class, pickaxe, "field_77864_a");
				float damage = ObfuscationReflectionHelper.getPrivateValue(ItemTool.class, pickaxe, "field_77865_bY");

				data.add("| [[File:" + name + ".png|32px|link=]] '''[[" + name + "]]''' || {{hp|" + StringUtil.roundToNthDecimalPlace(damage, 1) + "}} || " + StringUtil.roundToNthDecimalPlace(efficiency, 1) + " || " + pickaxe.getMaxDamage(pickaxeStack) + " || ");
				data.add("|-");
			}

			data.add("|}");
			CategoryTableWriter.writeData("Pickaxes", data, sender, copyToClipboard);
		}
	}
}
