package net.tslat.aoawikihelpermod.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;

import java.io.File;
import java.util.List;

public class FormattingHelper {
	public static String bold(String text) {
		return "'''" + text + "'''";
	}

	public static String tooltip(String text, String tooltip) {
		return "{{tooltip|" + text + "|" + tooltip + "}}";
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

	public static String createLinkableItem(ItemStack object, boolean shouldLink) {
		return createLinkableItem(object.getItem(), object.getCount() > 1, shouldLink);
	}

	public static String createLinkableItem(IItemProvider object, boolean pluralise, boolean shouldLink) {
		return createLinkableItem(ObjectHelper.getItemName(object.asItem()), pluralise, object.asItem().getRegistryName().getNamespace().equals("minecraft"), shouldLink);
	}

	public static String createLinkableTag(ITag.INamedTag<Item> tag) {
		StringBuilder builder = new StringBuilder("[[");
		String tagName = StringUtil.toTitleCase(tag.getName().getPath());

		if (tag.getName().getNamespace().equals("minecraft"))
			builder.append("mcw:");

		builder.append(tagName);
		builder.append("|");
		builder.append(lazyPluralise(tagName));
		builder.append(" (Any)]]");

		return tooltip(builder.toString(), "Any item in tag collection");
	}

	public static String createLinkableItem(String text, boolean pluralise, boolean isVanilla, boolean shouldLink) {
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
				.append(new StringTextComponent(linkName).withStyle(style -> style.withColor(TextFormatting.BLUE)
						.setUnderlined(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, fileUrl))))
				.append(new StringTextComponent(" "))
				.append(new StringTextComponent("(Copy)").withStyle(style -> style.withColor(TextFormatting.BLUE)
						.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, clipboardContent))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Copy contents of file to clipboard")))));
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
