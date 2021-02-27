package net.tslat.aoawikihelpermod.recipes;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandPrintInfusionEnchants extends CommandBase {
	@Override
	public String getName() {
		return "printinfusionenchants";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printinfusionenchants [clipboard] - Prints out all current imbuing recipes for the Infusion wiki page";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();

		if (!world.isRemote) {
			boolean copyToClipboard = args.length > 0 && args[0].equalsIgnoreCase("clipboard");

			if (copyToClipboard && server.isDedicatedServer()) {
				sender.sendMessage(new TextComponentString("Can't copy contents of file to clipboard on dedicated servers, skipping."));
				copyToClipboard = false;
			}

			InfusionEnchantsWriter.printImbuingEntries(sender, copyToClipboard);
		}
	}
}
