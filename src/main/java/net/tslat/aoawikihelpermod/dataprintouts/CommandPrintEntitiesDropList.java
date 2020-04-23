package net.tslat.aoawikihelpermod.dataprintouts;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandPrintEntitiesDropList extends CommandBase {
	@Override
	public String getName() {
		return "printentitiesdroplist";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printentitiesdroplist Optional:[clipboard] - Prints out the list of entities that drops a given held item. Optionally copy contents to clipboard.";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();

		if (!world.isRemote) {
			if (!(sender instanceof EntityPlayer)) {
				sender.sendMessage(new TextComponentString("This command cannot be used from console"));

				return;
			}

			EntityPlayer pl = (EntityPlayer)sender;
			ItemStack targetStack = pl.getHeldItemMainhand();

			if (targetStack.isEmpty()) {
				sender.sendMessage(new TextComponentString("You're not holding anything!"));

				return;
			}
			else if (!targetStack.getItem().getRegistryName().getResourceDomain().equals("aoa3")) {
				sender.sendMessage(new TextComponentString("The item you are holding is not from AoA! You are holding: " + targetStack.getDisplayName() + " (" + targetStack.getItem().getRegistryName().toString() + ")"));

				return;
			}

			boolean copyToClipboard = args.length > 0 && args[0].equalsIgnoreCase("clipboard");

			if (copyToClipboard && server.isDedicatedServer()) {
				sender.sendMessage(new TextComponentString("Can't copy contents of file to clipboard on dedicated servers, skipping."));
				copyToClipboard = false;
			}

			DataPrintoutWriter.writeItemEntityDropsList(sender, targetStack, copyToClipboard);
		}
	}
}
