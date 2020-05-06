package net.tslat.aoawikihelpermod.dataprintouts;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.tslat.aoa3.utils.ConfigurationUtil;
import scala.actors.threadpool.Arrays;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class CommandPrintStructuresData extends CommandBase {
	@Override
	public String getName() {
		return "printstructuresdata";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printstructuresdata [clipboard] - Prints out all AoA structures defaults to file. Must be done with a new config. Optionally copy contents to clipboard.";
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
			data.add("! Structure !! Default Value !! Description");
			data.add("|-");

			try {
				Field[] subCategories = ConfigurationUtil.StructureConfig.class.getDeclaredFields();
				HashMap<Field, Object> structureFields = new HashMap<Field, Object>();

				for (Field field : subCategories) {
					Object value = field.get(null);

					if (value.getClass().getSimpleName().contains("SubCategory"))
						Arrays.asList(value.getClass().getDeclaredFields()).forEach(e1 -> structureFields.put((Field)e1, value));
				}

				for (Map.Entry<Field, Object> configField : structureFields.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getName())).collect(Collectors.toList())) {
					int configValue = configField.getKey().getInt(configField.getValue());

					data.add("| [[" + nameToCapitals(configField.getKey().getName()) + "]] || " + configValue + " || ");
					data.add("|-");
				}
			}
			catch (Exception e) {
				sender.sendMessage(new TextComponentString("An error occured while trying to process this command, see the log file for details."));

				e.printStackTrace();
			}

			data.add("|}");

			DataPrintoutWriter.writeData("Structures", data, sender, copyToClipboard);
		}
	}

	private String nameToCapitals(String name) {
		StringBuilder builder = new StringBuilder();
		name = name.replace("SpawnChance", "");

		builder.append(Character.toUpperCase(name.charAt(0)));

		for (char c : name.substring(1).toCharArray()) {
			if (Character.isUpperCase(c))
				builder.append(" ");

			builder.append(c);
		}

		return builder.toString();
	}
}
