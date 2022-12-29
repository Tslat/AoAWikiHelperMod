package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2BooleanAVLTreeMap;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.tslat.aoa3.content.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.content.item.weapon.bow.BaseBow;
import net.tslat.aoa3.content.item.weapon.cannon.BaseCannon;
import net.tslat.aoa3.content.item.weapon.crossbow.BaseCrossbow;
import net.tslat.aoa3.content.item.weapon.greatblade.BaseGreatblade;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import net.tslat.aoa3.content.item.weapon.maul.BaseMaul;
import net.tslat.aoa3.content.item.weapon.shotgun.BaseShotgun;
import net.tslat.aoa3.content.item.weapon.sniper.BaseSniper;
import net.tslat.aoa3.content.item.weapon.staff.BaseStaff;
import net.tslat.aoa3.content.item.weapon.sword.BaseSword;
import net.tslat.aoa3.content.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.content.item.weapon.vulcane.BaseVulcane;
import net.tslat.aoa3.util.LocaleUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.dataskimmers.TagDataSkimmer;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;
import net.tslat.aoawikihelpermod.util.printers.handlers.TagCategoryPrintHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectHelper {
	private static final Object2BooleanAVLTreeMap<ResourceKey<? extends Registry<?>>> REGISTRY_KEYS = new Object2BooleanAVLTreeMap<>();
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
		return ForgeRegistries.ENTITY_TYPES.getValues().stream().filter(filter).collect(Collectors.toList());
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

	public static String getSampleElementForTag(ResourceLocation id) {
		for (ResourceLocation registryId : TagDataSkimmer.tagTypes()) {
			TagCategoryPrintHandler tagHandler = TagDataSkimmer.get(registryId);
			Object sampleElement = tagHandler.getSampleElement(id);

			if (sampleElement == null)
				continue;

			return getNameFunctionForUnknownObject(sampleElement).apply(sampleElement);
		}

		return "Air";
	}

	public static RecipePrintHandler.PrintableIngredient getIngredientName(JsonObject obj) {
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
			RecipePrintHandler.PrintableIngredient ingredient = new RecipePrintHandler.PrintableIngredient(ownerId, ingredientName);

			ingredient.setCustomImageName(getSampleElementForTag(new ResourceLocation(ingredientName)) + ".png");

			return ingredient;
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
		return ForgeRegistries.ENTITY_TYPES.getValue(id) != EntityType.PIG;
	}

	public static String getItemName(ItemLike item) {
		ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item.asItem());
		EntityType<?> matchingEntity = ForgeRegistries.ENTITY_TYPES.getValue(itemId);
		String suffix = "";

		if (matchingEntity != EntityType.PIG) {
			try {
				Entity testInstance = matchingEntity.create(FakeWorld.INSTANCE.get());

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

	public static String getItemNameFromItem(Item item){
		return item.getName(new ItemStack(item)).getString();
	}

	public static String getBlockName(Block block) {
		if (block.asItem() != Items.AIR)
			return getItemName(block);

		return StringUtil.toTitleCase(ForgeRegistries.BLOCKS.getKey(block).getPath());
	}

	public static String getBiomeName(ResourceKey<Biome> biome) {
		return getBiomeName(biome.location());
	}

	public static String getBiomeName(ResourceLocation biomeId) {
		String key = "biome." + biomeId.getNamespace() + "." + biomeId.getPath();
		String name = LocaleUtil.getLocaleString(key);

		if (name.equals(key))
			return StringUtil.toTitleCase(biomeId.getPath());

		return name;
	}

	public static String getEntityName(EntityType<?> entityType) {
		ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
		String suffix = isItem(id) ? " (entity)" : "";

		return LocaleUtil.getLocaleMessage("entity." + id.getNamespace() + "." + id.getPath()).getString() + suffix;
	}

	public static String getFluidName(Fluid fluid) {
		if (isBlock(ForgeRegistries.FLUIDS.getKey(fluid)))
			return getBlockName(ForgeRegistries.BLOCKS.getValue(ForgeRegistries.FLUIDS.getKey(fluid)));

		return StringUtil.toTitleCase(ForgeRegistries.FLUIDS.getKey(fluid).getPath());
	}

	public static String getEnchantmentName(Enchantment enchant, int level) {
		if (level <= 0)
			return Component.translatable(enchant.getDescriptionId()).getString();

		return enchant.getFullname(level).getString();
	}

	public static RecipePrintHandler.PrintableIngredient getFormattedItemDetails(ResourceLocation id) {
		Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);

		return new RecipePrintHandler.PrintableIngredient(id.getNamespace(), item == null ? StringUtil.toTitleCase(id.getPath()) : ObjectHelper.getItemName(item));
	}

	public static RecipePrintHandler.PrintableIngredient getStackDetailsFromJson(JsonElement element) {
		int count = 1;
		RecipePrintHandler.PrintableIngredient ingredient;

		if (element.isJsonObject()) {
			JsonObject obj = (JsonObject)element;

			if (obj.has("count"))
				count = obj.get("count").getAsInt();

			ingredient = getIngredientName(obj);
		}
		else {
			ingredient = getFormattedItemDetails(new ResourceLocation(element.getAsString()));
		}

		ingredient.count = count;

		return ingredient;
	}

	public static String attemptToExtractItemSpecificEffects(Item item, @Nullable Item controlItem) {
		MutableComponent dummyComponent = Component.literal("");

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

	public static Object2BooleanAVLTreeMap<ResourceKey<? extends Registry<?>>> getAllRegistries() {
		if (!REGISTRY_KEYS.isEmpty())
			return REGISTRY_KEYS;

		for (ResourceKey<? extends Registry<?>> key : ((BiMap<ResourceLocation, ForgeRegistry<Object>>)ObfuscationReflectionHelper.getPrivateValue(RegistryManager.class, RegistryManager.ACTIVE, "registries")).values().stream().map(ForgeRegistry::getRegistryKey).sorted().toList()) {
			REGISTRY_KEYS.put(key, false);
		}

		for (ResourceKey<? extends Registry<?>> key : ServerLifecycleHooks.getCurrentServer().registryAccess().registries().map(RegistryAccess.RegistryEntry::key).sorted().toList()) {
			REGISTRY_KEYS.put(key, true);
		}

		return REGISTRY_KEYS;
	}

	public static Either<Registry<?>, IForgeRegistry<?>> getRegistry(ResourceKey<? extends Registry<?>> key) {
		return getAllRegistries().getBoolean(key) ? Either.left(ServerLifecycleHooks.getCurrentServer().registryAccess().registry(key).get()) : Either.right(RegistryManager.ACTIVE.getRegistry(key.location()));
	}

	@Nullable
	public static Either<Registry<?>, IForgeRegistry<?>> getRegistryForObject(Object object) {
		for (ResourceKey<? extends Registry<?>> key : getAllRegistries().keySet()) {
			Either<Registry<? extends Object>, IForgeRegistry<?>> dynamicOrFixedRegistry = getRegistry(key);

			if (dynamicOrFixedRegistry.left().isPresent()) {
				Registry registry = dynamicOrFixedRegistry.left().get();
				ResourceLocation id = registry.getKey(object);

				if (id != null && (!(registry instanceof DefaultedRegistry<?> defaultedRegistry) || defaultedRegistry.getDefaultKey() != id))
					return Either.left(registry);
			}
			else if (dynamicOrFixedRegistry.right().isPresent()) {
				if (((IForgeRegistry)dynamicOrFixedRegistry.right().get()).containsValue(object))
					return Either.right(dynamicOrFixedRegistry.right().get());
			}
		}

		return null;
	}

	public static Function<Object, String> getNameFunctionForUnknownObject(Object entry) {
		Function<Object, String> namingFunction;

		if (entry instanceof Item) {
			namingFunction = item -> ObjectHelper.getItemName((Item)item);
		}
		else if (entry instanceof Block) {
			namingFunction = block -> ObjectHelper.getBlockName((Block)block);
		}
		else if (entry instanceof EntityType) {
			namingFunction = entityType -> ObjectHelper.getEntityName((EntityType<?>)entityType);
		}
		else if (entry instanceof Biome) {
			namingFunction = biome -> ObjectHelper.getBiomeName(ServerLifecycleHooks.getCurrentServer().registryAccess().registry(Registries.BIOME).get().getKey((Biome)biome));
		}
		else if (entry instanceof Enchantment) {
			namingFunction = enchant -> ObjectHelper.getEnchantmentName((Enchantment)enchant, 0);
		}
		else if (entry instanceof Fluid) {
			namingFunction = fluid -> ObjectHelper.getFluidName((Fluid)fluid);
		}
		else {
			namingFunction = unknownObject -> {
				Either<Registry<?>, IForgeRegistry<?>> registry = getRegistryForObject(unknownObject);

				if (registry == null)
					return "???";

				if (registry.left().isPresent())
					return ((Registry)registry.left().get()).getKey(unknownObject).toString();

				if (registry.right().isPresent())
					return ((IForgeRegistry)registry.right().get()).getKey(unknownObject).toString();

				return "???";
			};
		}

		return namingFunction;
	}
}
