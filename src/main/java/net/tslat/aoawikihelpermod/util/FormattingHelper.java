package net.tslat.aoawikihelpermod.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.*;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class FormattingHelper {
	public static String bold(String text) {
		return "'''" + text + "'''";
	}

	public static String tooltip(String text, String tooltip) {
		return "{{tooltip|" + text + "|" + tooltip + "}}";
	}

	public static String healthValue(float value) {
		return "{{hp|" + NumberUtil.roundToNthDecimalPlace(value, 2) + "}}";
	}

	public static String createImageBlock(ItemLike item) {
		return createImageBlock(ObjectHelper.getItemName(item));
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

	public static String createLinkableItem(ItemLike object, boolean pluralise, boolean shouldLink) {
		return createLinkableText(ObjectHelper.getItemName(object.asItem()), pluralise, object.asItem().getRegistryName().getNamespace().equals("minecraft"), shouldLink);
	}

	public static String createLinkableTag(String tag) {
		StringBuilder builder = new StringBuilder("[[");
		ResourceLocation tagId = new ResourceLocation(tag);
		String tagName = StringUtil.toTitleCase(tagId.getPath());

		//if (tagId.getNamespace().equals("minecraft"))
		//	builder.append("mcw:"); Not redirecting mcw links anymore

		builder.append(tagName);
		builder.append("|");
		builder.append(lazyPluralise(tagName));
		builder.append(" (Any)]]");

		return tooltip(builder.toString(), "Any item in tag collection");
	}

	public static String createLinkableText(String text, boolean pluralise, boolean isVanilla, boolean shouldLink) {
		StringBuilder builder = new StringBuilder(shouldLink ? "[[" : "");
		String pluralName = pluralise ? lazyPluralise(text) : text;

		if (shouldLink && !pluralName.equals(text)) {
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

	public static MutableComponent generateResultMessage(File file, String linkName, @Nullable String clipboardContent) {
		String fileUrl = file.getAbsolutePath().replace("\\", "/");
		MutableComponent component = new TextComponent("Generated data file: ")
				.append(new TextComponent(linkName).withStyle(style -> style.withColor(ChatFormatting.BLUE)
						.setUnderlined(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, fileUrl))));

		if (clipboardContent != null) {
				component
						.append(new TextComponent(" "))
						.append(new TextComponent("(Copy)").withStyle(style -> style.withColor(ChatFormatting.BLUE)
								.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, clipboardContent))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Copy contents of file to clipboard")))));
		}

		return component;
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

	public static Optional<Float> tryParseFloat(String value) {
		try {
			return Optional.of(Float.parseFloat(value));
		}
		catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

	public static String getStringFromRange(NumberProvider range) {
		if (range instanceof ConstantValue)
			return String.valueOf(((ConstantValue)range).value);

		if (range instanceof BinomialDistributionGenerator)
			return "0+";

		if (range instanceof UniformGenerator randomRange) {
			String min = getStringFromRange(randomRange.min);
			String max = getStringFromRange(randomRange.max);

			if (!randomRange.min.equals(randomRange.max)) {
				Optional<Float> minValue = tryParseFloat(min);
				Optional<Float> maxValue = tryParseFloat(max);

				return (minValue.isPresent() ? NumberUtil.roundToNthDecimalPlace(minValue.get(), 3) : min) + "-" + (maxValue.isPresent() ? NumberUtil.roundToNthDecimalPlace(maxValue.get(), 3) : max);
			}
			else {
				Optional<Float> value = tryParseFloat(min);

				if (value.isPresent())
					return NumberUtil.roundToNthDecimalPlace(value.get(), 3);

				return min;
			}
		}

		if (range instanceof ScoreboardValue scoreboardValue) {
			String val = "Scoreboard value of '" + scoreboardValue.score + "'";

			if (scoreboardValue.scale != 0)
				val += " * " + NumberUtil.roundToNthDecimalPlace(scoreboardValue.scale, 3);

			return val;
		}

		return "1";
	}

	public static String getTimeFromTicks(int ticks) {
		StringBuilder builder = new StringBuilder();

		if (ticks > 1200) {
			builder.append(ticks / 1200).append("m");

			if (ticks % 1200 != 0)
				builder.append(", ").append(NumberUtil.roundToNthDecimalPlace((ticks % 1200) / 20f, 2)).append("s");
		}
		else {
			builder.append(NumberUtil.roundToNthDecimalPlace(ticks % 1200 / 20f, 2)).append("s");
		}

		return builder.toString();
	}
}
