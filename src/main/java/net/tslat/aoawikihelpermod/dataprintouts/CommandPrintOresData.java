package net.tslat.aoawikihelpermod.dataprintouts;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.tslat.aoa3.utils.ConfigurationUtil;
import net.tslat.aoa3.utils.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CommandPrintOresData extends CommandBase {
	@Override
	public String getName() {
		return "printoresdata";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printoresdata [clipboard] - Prints out all known ore generation data to file. Must be done with a new config. Optionally copy contents to clipboard.";
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

			try {
				Field[] subCategories = ConfigurationUtil.OreConfig.class.getDeclaredFields();

				for (Field field : subCategories) {
					Object value = field.get(null);
					Class<?> clazz = value.getClass();
					String categoryName = StringUtil.capitaliseFirstLetter(clazz.getSimpleName() + " Ore");
					Annotation[] annotations = field.getDeclaredAnnotations();

					for (Annotation annotation : annotations) {
						if (annotation.annotationType() == Config.LangKey.class) {
							categoryName = StringUtil.getLocaleString(((Config.LangKey)annotation).value());
							System.out.println(categoryName);
							break;
						}
					}

					data.add("=== [[" + categoryName + "]] ===");
					data.add("{|class=\"wikitable\"");
					data.add("|-");
					data.add("! Config Option !! Allowed Values !! Default Value !! Description");
					data.add("|-");

					Field[] subCategoryFields = clazz.getDeclaredFields();

					for (Field subCategoryField : subCategoryFields) {
						data.add("|" + fieldTypeToStringPrefix(subCategoryField.getType()) + subCategoryField.getName() + " || " + oreConfigFieldToValueDesc(value, subCategoryField));
						data.add("|-");
					}

					data.add("|}");
					data.add("");
				}
			}
			catch (Exception e) {
				sender.sendMessage(new TextComponentString("An error occured while trying to process this command, see the log file for details."));

				e.printStackTrace();
			}

			DataPrintoutWriter.writeData("Ores", data, sender, copyToClipboard);
		}
	}

	private String oreConfigFieldToValueDesc(Object category, Field field) {
		Annotation[] annotations = field.getDeclaredAnnotations();
		Class<?> fieldType = field.getType();

		if (fieldType == int.class || fieldType == Integer.class) {
			int min = 0;
			int max = 0;
			String desc = "";
			int value = 0;

			try {
				value = field.getInt(category);
			}
			catch (Exception e) {
				System.out.println("Encountered error while determining default config value for " + category.getClass().getSimpleName() + ": " + field.getName() + ". Defaulting to 0, but you should probably check this.");
				e.printStackTrace();
			}

			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == Config.RangeInt.class) {
					Config.RangeInt rangeAnnotation = (Config.RangeInt)annotation;
					min = rangeAnnotation.min();
					max = rangeAnnotation.max();
				}
				else if (annotation.annotationType() == Config.Comment.class) {
					desc = ((Config.Comment)annotation).value()[0];
				}
			}

			return "Any number between " + min + " (inclusive) and " + max + " (exclusive) || " + value + " || " + desc;
		}
		else if (fieldType == double.class || fieldType == Double.class) {
			double min = 0;
			double max = 0;
			String desc = "";
			double value = 0;

			try {
				value = field.getDouble(category);
			}
			catch (Exception e) {
				System.out.println("Encountered error while determining default config value for " + category.getClass().getSimpleName() + ": " + field.getName() + ". Defaulting to 0, but you should probably check this.");
			}

			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == Config.RangeDouble.class) {
					Config.RangeDouble rangeAnnotation = (Config.RangeDouble)annotation;
					min = rangeAnnotation.min();
					max = rangeAnnotation.max();
				}
				else if (annotation.annotationType() == Config.Comment.class) {
					desc = ((Config.Comment)annotation).value()[0];
				}
			}

			return "Any number between " + min + " (inclusive) and " + max + " (exclusive) || " + value + " || " + desc;
		}

		return "";
	}

	private String fieldTypeToStringPrefix(Class<?> fieldType) {
		if (fieldType == int.class || fieldType == Integer.class)
			return "I:";

		if (fieldType == double.class || fieldType == Double.class)
			return "D:";

		return "";
	}
}
