package net.tslat.aoawikihelpermod.dataprintouts;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.tslat.aoa3.utils.skills.HunterUtil;

import java.util.*;
import java.util.stream.Collectors;

public class CommandPrintHunterCreatureData extends CommandBase {
	@Override
	public String getName() {
		return "printhuntercreaturedata";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printhuntercreaturedata [clipboard] - Prints out all known hunter creature data to file. Must be done with a new config. Optionally copy contents to clipboard.";
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

			List<String> data = new ArrayList<String>();

			data.add("{|class=\"wikitable\"");
			data.add("|-");
			data.add("! Mob !! Mob ID !! Default Level !! Default XP");
			data.add("|-");

			HashMap<Class<? extends EntityLivingBase>, Tuple<Integer, Float>> hunterCreatureMap = ObfuscationReflectionHelper.getPrivateValue(HunterUtil.class, null, "hunterCreatureMap");

			for (Map.Entry<Class<? extends EntityLivingBase>, Tuple<Integer, Float>> entry : hunterCreatureMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getSimpleName())).collect(Collectors.toList())) {
				Entity entity = EntityList.newEntity(entry.getKey(), world);

				if (entity == null) {
					System.out.println("Unable to find entity instance with class: " + entry.getKey().getSimpleName());

					continue;
				}

				data.add("| [[" + entity.getDisplayName().getUnformattedText() + "]] || " + EntityRegistry.getEntry(entry.getKey()).getRegistryName().toString() + " || " + entry.getValue().getFirst() + " || " + entry.getValue().getSecond());
				data.add("|-");
			}

			data.add("|}");

			DataPrintoutWriter.writeData("Hunter Mobs", data, sender, copyToClipboard);
		}
	}
}
