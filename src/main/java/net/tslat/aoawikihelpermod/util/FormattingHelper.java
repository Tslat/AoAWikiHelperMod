package net.tslat.aoawikihelpermod.util;

import com.ibm.icu.text.PluralFormat;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.tslat.aoa3.util.NumberUtil;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FormattingHelper {
	public static String bold(String text) {
		return "'''" + text + "'''";
	}

	public static String healthValue(float value) {
		return "{{hp|" + NumberUtil.roundToNthDecimalPlace(value, 3) + "}}";
	}

	public static String createImageBlock(String name) {
		return createImageBlock(name, 32);
	}

	public static String createImageBlock(String name, int size) {
		return "[[File:" + name + ".png|" + size + "px|link=]]";
	}

	public static String createObjectBlock(ItemStack object, boolean shouldLink) {
		return createObjectBlock(object.getItem(), object.getCount() > 1, shouldLink);
	}

	public static String createObjectBlock(IItemProvider object, boolean pluralise, boolean shouldLink) {
		return createObjectBlock(ObjectHelper.getItemName(object.asItem()), pluralise, object.asItem().getRegistryName().getNamespace().equals("minecraft"), shouldLink);
	}

	public static String createObjectBlock(String text, boolean pluralise, boolean isVanilla, boolean shouldLink) {
		StringBuilder builder = new StringBuilder(shouldLink ? "[[" : "");
		String pluralName = pluralise ? lazyPluralise(text) : text;

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

	public static IFormattableTextComponent generateResultMessage(File file, String linkName, String clipboardContent) {
		String fileUrl = file.getAbsolutePath().replace("\\", "/");

		return new TranslationTextComponent("Generated data file: ")
				.append(new StringTextComponent(linkName).withStyle(style -> style.withColor(TextFormatting.BLUE).setUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, fileUrl))))
				.append(new StringTextComponent(" "))
				.append(new StringTextComponent("(Copy to Clipboard)").withStyle(style -> style.withColor(TextFormatting.BLUE).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, clipboardContent))));
	}

	public static String listToString(List<String> list, boolean nativeLineBreaks) {
		String newLineDelimiter = nativeLineBreaks ? System.lineSeparator() : "<br/>";
		StringBuilder builder = new StringBuilder();

		for (String str : list) {
			if (builder.length() > 0)
				builder.append(newLineDelimiter);

			builder.append(str);
		}

		return builder.toString();
	}
}
