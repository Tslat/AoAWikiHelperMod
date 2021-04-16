package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectHelper {
	private static final ArrayList<Pattern> TOOLTIP_BLACKLIST = new ArrayList<Pattern>();

	static {
		TOOLTIP_BLACKLIST.add(Pattern.compile("^[\\d|\\.]+ \\w+ Damage$"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Firing Rate: .*?"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Ammo: .*?"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Consumes [\\d|\\.]+ \\w+$"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^[\\d|\\.]+ Average \\w+ Damage$"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Runes Required: .*?"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^\\d+ \\w+ Runes?"));
	}

	public static List<Item> scrapeRegistryForItems(Predicate<Item> filter) {
		return ObjectHelper.sortCollection(ForgeRegistries.ITEMS.getValues().stream().filter(filter).collect(Collectors.toList()), ObjectHelper::getItemName);
	}

	public static List<Block> scrapeRegistryForBlocks(Predicate<Block> filter) {
		return ObjectHelper.sortCollection(ForgeRegistries.BLOCKS.getValues().stream().filter(filter).collect(Collectors.toList()), ObjectHelper::getItemName);
	}

	public static List<EntityType<?>> scrapeRegistryForEntities(Predicate<EntityType<?>> filter) {
		return ForgeRegistries.ENTITIES.getValues().stream().filter(filter).collect(Collectors.toList());
	}

	public static Multimap<Attribute, AttributeModifier> getAttributesForItem(Item item) {
		return item.getAttributeModifiers(EquipmentSlotType.MAINHAND, new ItemStack(item));
	}

	public static double getAttributeFromItem(Item item, Attribute attribute) {
		Multimap<Attribute, AttributeModifier> attributes = getAttributesForItem(item);

		if (!attributes.containsKey(attribute))
			return 0d;

		return getAttributeValue(attribute, attributes.get(attribute));
	}

	public static double getAttributeValue(Attribute attribute, Collection<AttributeModifier> modifiers) {
		ModifiableAttributeInstance instance = new ModifiableAttributeInstance(attribute, consumer -> {});

		for (AttributeModifier modifier : modifiers) {
			if (!instance.hasModifier(modifier))
				instance.addTransientModifier(modifier);
		}

		double value = instance.getValue() - attribute.getDefaultValue(); // Remove due to the way instanceless attributes are calculated

		if (attribute == Attributes.ATTACK_DAMAGE)
			value = Math.max(0, value - 0.5d);

		return value;
	}

	public static <T extends Object, U extends Comparable<? super U>> ArrayList<T> sortCollection(Collection<T> collection, Function<T, U> sortFunction) {
		return (ArrayList<T>)collection.stream().sorted(Comparator.comparing(sortFunction)).collect(Collectors.toList());
	}

	public static String getItemName(IItemProvider item) {
		return new ItemStack(item).getHoverName().getString();
	}

	public static String attemptToExtractItemSpecificEffects(Item item, @Nullable Item controlItem) {
		StringTextComponent dummyComponent = new StringTextComponent("");

		List<ITextComponent> itemTooltip = new ArrayList<ITextComponent>();
		List<ITextComponent> controlItemTooltip = new ArrayList<ITextComponent>();
		StringBuilder builder = new StringBuilder();

		itemTooltip.add(dummyComponent);
		itemTooltip.add(dummyComponent);
		controlItemTooltip.add(dummyComponent);
		controlItemTooltip.add(dummyComponent);
		item.appendHoverText(new ItemStack(item), null, itemTooltip, ITooltipFlag.TooltipFlags.NORMAL);

		if (controlItem != null)
			controlItem.appendHoverText(new ItemStack(controlItem), null, controlItemTooltip, ITooltipFlag.TooltipFlags.NORMAL);

		tooltipLoop:
		for (ITextComponent text : itemTooltip) {
			String line = text.getString();

			if (line.isEmpty())
				continue;

			for (Pattern pattern : TOOLTIP_BLACKLIST) {
				if (pattern.matcher(line).matches())
					continue tooltipLoop;
			}

			for (ITextComponent controlText : controlItemTooltip) {
				if (areStringsSimilar(line, controlText.getString()))
					continue tooltipLoop;
			}

			if (text != dummyComponent) {
				if (builder.length() > 0)
					builder.append("<br/>");

				builder.append(text.getString());
			}
		}

		return builder.toString();
	}

	public static boolean areStringsSimilar(String str1, String str2) {
		if (str1.equals(str2))
			return true;

		if (Math.abs(str1.length() - str2.length()) / (float)str1.length() > 0.5f)
			return false;

		int matches = 0;

		for (int i = 0; i < str1.length(); i++) {
			if (i >= str2.length())
				break;

			if (str1.charAt(i) != str2.charAt(i))
				continue;

			matches++;
		}

		return matches / (float)str1.length() >= 0.75f;
	}
}
