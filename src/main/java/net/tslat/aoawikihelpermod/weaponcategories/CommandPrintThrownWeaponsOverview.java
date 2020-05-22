package net.tslat.aoawikihelpermod.weaponcategories;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tslat.aoa3.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.utils.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandPrintThrownWeaponsOverview extends CommandBase {
	@Override
	public String getName() {
		return "printthrownweaponsoverview";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printthrownweaponsoverview [clipboard] - Prints out all AoA thrown weapons data to file. Optionally copy contents to clipboard.";
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
			List<BaseThrownWeapon> thrownWeapons = new ArrayList<BaseThrownWeapon>();

			for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
				if (item instanceof BaseThrownWeapon)
					thrownWeapons.add((BaseThrownWeapon)item);
			}

			thrownWeapons = thrownWeapons.stream().sorted(Comparator.comparing(thrownWeapon -> thrownWeapon.getItemStackDisplayName(new ItemStack(thrownWeapon)))).collect(Collectors.toList());

			data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
			data.add("|- style=\"background-color:#eee\"");
			data.add("! Name !! data-sort-type=number | Damage !! Throw rate !! Effects");
			data.add("|-");

			for (BaseThrownWeapon thrownWeapon : thrownWeapons) {
				ItemStack thrownWeaponStack = new ItemStack(thrownWeapon);
				String name = thrownWeapon.getItemStackDisplayName(thrownWeaponStack);
				String firingRate = StringUtil.roundToNthDecimalPlace((2000 / (int)ObfuscationReflectionHelper.getPrivateValue(BaseThrownWeapon.class, thrownWeapon, "firingDelay")) / (float)100, 2) + "/sec";
				double damage = ObfuscationReflectionHelper.getPrivateValue(BaseThrownWeapon.class, thrownWeapon, "dmg");

				data.add("| [[File:" + name + ".png|32px|link=]] '''[[" + name + "]]''' || {{hp|" + StringUtil.roundToNthDecimalPlace((float)damage, 1) + "}} || " + firingRate + " || ");
				data.add("|-");
			}

			data.add("|}");
			CategoryTableWriter.writeData("Thrown Weapons", data, sender, copyToClipboard);
		}
	}
}
