package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.Comparators;
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
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ObjectHelper {
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
		List<ITextComponent> itemTooltip = new ArrayList<ITextComponent>();
		List<ITextComponent> controlItemTooltip = new ArrayList<ITextComponent>();
		StringBuilder builder = new StringBuilder();

		item.appendHoverText(new ItemStack(item), null, itemTooltip, ITooltipFlag.TooltipFlags.NORMAL);

		if (controlItem != null)
			controlItem.appendHoverText(new ItemStack(controlItem), null, controlItemTooltip, ITooltipFlag.TooltipFlags.NORMAL);

		for (ITextComponent text : itemTooltip) {
			String line = text.getString();
			boolean matching = false;

			for (ITextComponent controlText : controlItemTooltip) {
				if (line.equals(controlText.getString())) {
					matching = true;
					break;
				}
			}

			if (!matching) {
				if (builder.length() > 0)
					builder.append("<br/>");

				builder.append(text.getString());
			}
		}

		return builder.toString();
	}
}
