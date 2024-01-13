package net.tslat.aoawikihelpermod.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.*;
import net.tslat.aoa3.util.NumberUtil;

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
		return createLinkableText(ObjectHelper.getItemName(object.asItem()), pluralise, shouldLink);
	}

	public static String createLinkableTag(String tag, Object sampleObjectFromTagTypeRegistry) {
		Registry registryForObject = ObjectHelper.getRegistryForObject(sampleObjectFromTagTypeRegistry);
		ResourceLocation registryId = registryForObject.getKey(sampleObjectFromTagTypeRegistry);

		return tooltip(tag, "Any " + registryId.getPath().replaceAll("_", "") + " tagged as " + tag) + " ([[Tags#" + registryId + ":" + tag + "|Tag]])";
	}

	public static String createTagIngredientDescription(String tag, Object sampleObjectFromTagTypeRegistry) {
		Registry registryForObject = ObjectHelper.getRegistryForObject(sampleObjectFromTagTypeRegistry);
		ResourceLocation registryId = registryForObject.getKey(sampleObjectFromTagTypeRegistry);

		return "Any " + registryId.getPath().replaceAll("_", "") + " tagged as " + "[[Tags#" + registryId + ":" + tag + "|" + tag + "]]";
	}

	public static String createLinkableText(String text, boolean pluralise) {
		return createLinkableText(text, pluralise, true);
	}

	public static String createLinkableText(String text, boolean pluralise, boolean shouldLink) {
		return createLinkableText(text, pluralise, shouldLink, null);
	}

	public static String createLinkableText(String text, boolean pluralise, boolean shouldLink, @Nullable String customDisplayText) {
		StringBuilder builder = new StringBuilder(shouldLink ? "[[" : "");
		String pluralName = pluralise ? lazyPluralise(text) : text;

		if (customDisplayText != null || (shouldLink && !pluralName.equals(text))) {
			builder.append(text);
			builder.append("|");

			text = customDisplayText != null ? customDisplayText : pluralName;
		}

		builder.append(text);

		if (shouldLink)
			builder.append("]]");

		return builder.toString();
	}

	public static String lazyPluralise(String text) {
		return !text.endsWith("s") && !text.endsWith("y") ? text.endsWith("x") || text.endsWith("o") ? text + "es" : text + "s" : text;
	}

	public static MutableComponent generateResultMessage(File file, String linkName, @Nullable String clipboardContent) {
		String fileUrl = file.getAbsolutePath().replace("\\", "/");
		MutableComponent component = Component.literal("Generated data file: ")
				.append(Component.literal(linkName).withStyle(style -> style.withColor(ChatFormatting.BLUE)
						.withUnderlined(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, fileUrl))));

		if (clipboardContent != null) {
				component
						.append(Component.literal(" "))
						.append(Component.literal("(Copy)").withStyle(style -> style.withColor(ChatFormatting.BLUE)
								.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, clipboardContent))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy contents of file to clipboard")))));
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
			return NumberUtil.roundToNthDecimalPlace(((ConstantValue)range).value(), 2);

		if (range instanceof BinomialDistributionGenerator)
			return "0+";

		if (range instanceof UniformGenerator randomRange) {
			String min = getStringFromRange(randomRange.min());
			String max = getStringFromRange(randomRange.max());

			if (!randomRange.min().equals(randomRange.max())) {
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
			String val = "Scoreboard value of '" + scoreboardValue.score() + "'";

			if (scoreboardValue.scale() != 0)
				val += " * " + NumberUtil.roundToNthDecimalPlace(scoreboardValue.scale(), 3);

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
