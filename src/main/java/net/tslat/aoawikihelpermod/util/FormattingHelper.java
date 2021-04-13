package net.tslat.aoawikihelpermod.util;

import com.ibm.icu.text.PluralFormat;
import net.minecraft.util.IItemProvider;

public class FormattingHelper {
	public static String createImageBlock(String name) {
		return createImageBlock(name, 32);
	}

	public static String createImageBlock(String name, int size) {
		return "[[File:" + name + ".png|" + size + "px|link=]]";
	}

	public static String createObjectBlock(IItemProvider object, boolean shouldLink) {
		return createObjectBlock(ObjectHelper.getItemName(object.asItem()), object.asItem().getRegistryName().getNamespace().equals("minecraft"), shouldLink);
	}

	public static String createObjectBlock(String text, boolean isVanilla, boolean shouldLink) {
		StringBuilder builder = new StringBuilder(shouldLink ? "[[" : "");
		String pluralName = lazyPluralise(text);

		if (isVanilla) {
			builder.append("mcw:");
			builder.append(text);
			builder.append("|");
		}
		else if (shouldLink && !pluralName.equals(text)) {
			builder.append(text);
			builder.append("|");
		}

		builder.append(pluralName);

		if (shouldLink)
			builder.append("]]");

		return builder.toString();
	}

	public static String lazyPluralise(String text) {
		return !text.endsWith("s") && !text.endsWith("y") ? text.endsWith("x") || text.endsWith("o") ? text + "es" : text + "s" : text;
	}
}
