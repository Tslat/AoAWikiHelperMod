package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.util.LocaleUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectHelper {
	private static final ArrayList<Pattern> TOOLTIP_BLACKLIST = new ArrayList<Pattern>();

	static {
		TOOLTIP_BLACKLIST.add(Pattern.compile("^[\\d|\\.]+ \\w+ damage$"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Firing rate: .*?"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Ammo: .*?"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Consumes [\\d|\\.]+ \\w+$"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^[\\d|\\.]+ Average \\w+ damage$"));
		TOOLTIP_BLACKLIST.add(Pattern.compile("^Runes required:.*?"));
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
		return item.getAttributeModifiers(EquipmentSlot.MAINHAND, new ItemStack(item));
	}

	public static double getAttributeFromItem(Item item, Attribute attribute) {
		Multimap<Attribute, AttributeModifier> attributes = getAttributesForItem(item);

		if (!attributes.containsKey(attribute))
			return 0d;

		return getAttributeValue(attribute, attributes.get(attribute));
	}

	public static double getAttributeValue(Attribute attribute, Collection<AttributeModifier> modifiers) {
		AttributeInstance instance = new AttributeInstance(attribute, consumer -> {});

		for (AttributeModifier modifier : modifiers) {
			if (!instance.hasModifier(modifier))
				instance.addTransientModifier(modifier);
		}

		double value = instance.getValue() - attribute.getDefaultValue(); // Remove due to the way instanceless attributes are calculated

		if (attribute == Attributes.ATTACK_DAMAGE) {
			value++;

			if (value < 0)
				value++;
		}

		return value;
	}

	public static <T extends Object, U extends Comparable<? super U>> ArrayList<T> sortCollection(Collection<T> collection, Function<T, U> sortFunction) {
		return (ArrayList<T>)collection.stream().sorted(Comparator.comparing(sortFunction)).collect(Collectors.toList());
	}

	public static Pair<String, String> getIngredientName(JsonObject obj) {
		if ((obj.has("item") && obj.has("tag")) || (!obj.has("item") && !obj.has("tag")))
			throw new JsonParseException("Invalidly formatted ingredient, unable to proceed.");

		String ingredientName;
		String ownerId;

		if (obj.has("item")) {
			return getFormattedItemDetails(new ResourceLocation(GsonHelper.getAsString(obj, "item")));
		}
		else if (obj.has("tag")) {
			ingredientName = GsonHelper.getAsString(obj, "tag");

			if (!ingredientName.contains(":"))
				ingredientName = "minecraft:" + ingredientName;

			ownerId = ingredientName.split(":")[0];

			return new Pair<String, String>(ownerId, ingredientName);
		}
		else {
			throw new JsonParseException("Invalidly formatted ingredient, unable to proceed.");
		}
	}

	@Nullable
	public static ResourceLocation getIngredientItemId(JsonElement element) {
		if (element.isJsonObject()) {
			JsonObject obj = element.getAsJsonObject();

			if (obj.has("tag"))
				return null;

			if (obj.has("item")) {
				return new ResourceLocation(GsonHelper.getAsString(obj, "item"));
			}
			else {
				throw new JsonParseException("Invalidly formatted ingredient, unable to proceed.");
			}
		}
		else {
			return new ResourceLocation(element.getAsString());
		}
	}

	public static boolean isItem(ResourceLocation id) {
		return ForgeRegistries.ITEMS.getValue(id) != Items.AIR;
	}

	public static boolean isBlock(ResourceLocation id) {
		return ForgeRegistries.BLOCKS.getValue(id) != Blocks.AIR;
	}

	public static boolean isEntity(ResourceLocation id) {
		return ForgeRegistries.ENTITIES.getValue(id) != EntityType.PIG;
	}

	public static String getItemName(ItemLike item) {
		ResourceLocation itemId = item.asItem().getRegistryName();
		EntityType<?> matchingEntity = ForgeRegistries.ENTITIES.getValue(itemId);
		String suffix = "";

		if (matchingEntity != EntityType.PIG) {
			try {
				Entity testInstance = matchingEntity.create(FakeWorld.INSTANCE);

				if (testInstance != null) {
					if (testInstance instanceof LivingEntity)
						suffix = " (item)";

					testInstance.discard();
				}
			}
			catch (Exception ignored) {}
		}

		return new ItemStack(item).getHoverName().getString() + suffix;
	}

	public static String getBlockName(Block block) {
		if (block.asItem() != Items.AIR)
			return getItemName(block);

		return StringUtil.toTitleCase(block.getRegistryName().getPath());
	}

	public static String getBiomeName(ResourceKey<Biome> biome) {
		ResourceLocation biomeId = biome.location();

		if (biomeId.getNamespace().equals(AdventOfAscension.MOD_ID))
			return StringUtil.toTitleCase(biomeId.getPath());

		return biomeId.toString();
	}

	public static String getEntityName(EntityType<?> entityType) {
		ResourceLocation id = entityType.getRegistryName();
		String suffix = isItem(id) ? " (entity)" : "";

		return LocaleUtil.getLocaleMessage("entity." + id.getNamespace() + "." + id.getPath()).getString() + suffix;
	}

	public static String getBiomeCategoryName(Biome.BiomeCategory category) {
		return StringUtil.toTitleCase(category.getName());
	}

	public static String getEnchantmentName(Enchantment enchant, int level) {
		if (level <= 0)
			return new TranslatableComponent(enchant.getDescriptionId()).getString();

		return enchant.getFullname(level).getString();
	}

	public static Pair<String, String> getFormattedItemDetails(ResourceLocation id) {
		Item item = Registry.ITEM.getOptional(id).orElse(null);

		return new Pair<String, String>(id.getNamespace(), item == null ? StringUtil.toTitleCase(id.getPath()) : ObjectHelper.getItemName(item));
	}

	public static Triple<Integer, String, String> getStackDetailsFromJson(JsonElement element) {
		Pair<String, String> itemName;
		int count = 1;

		if (element.isJsonObject()) {
			JsonObject obj = (JsonObject)element;

			if (obj.has("count"))
				count = obj.get("count").getAsInt();

			itemName = getIngredientName(obj);
		}
		else {
			itemName = getFormattedItemDetails(new ResourceLocation(element.getAsString()));
		}

		return Triple.of(count, itemName.getFirst(), itemName.getSecond());
	}

	public static String attemptToExtractItemSpecificEffects(Item item, @Nullable Item controlItem) {
		TextComponent dummyComponent = new TextComponent("");

		List<Component> itemTooltip = new ArrayList<Component>();
		List<Component> controlItemTooltip = new ArrayList<Component>();
		StringBuilder builder = new StringBuilder();

		itemTooltip.add(dummyComponent);
		itemTooltip.add(dummyComponent);
		controlItemTooltip.add(dummyComponent);
		controlItemTooltip.add(dummyComponent);

		ClientHelper.collectTooltipLines(item, itemTooltip, false);

		if (controlItem != null)
			ClientHelper.collectTooltipLines(controlItem, controlItemTooltip, false);

		tooltipLoop:
		for (Component text : itemTooltip) {
			String line = text.getString();

			if (line.isEmpty())
				continue;

			for (Pattern pattern : TOOLTIP_BLACKLIST) {
				if (pattern.matcher(line).matches())
					continue tooltipLoop;
			}

			for (Component controlText : controlItemTooltip) {
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
