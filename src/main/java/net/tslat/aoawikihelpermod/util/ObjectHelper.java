package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.tslat.aoa3.common.registration.AoARegistries;
import net.tslat.aoa3.content.item.weapon.bow.Slingshot;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import net.tslat.aoa3.content.item.weapon.staff.BaseStaff;
import net.tslat.aoa3.content.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.util.LocaleUtil;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoa3.util.TagUtil;
import net.tslat.aoawikihelpermod.dataskimmers.TagDataSkimmer;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;
import net.tslat.aoawikihelpermod.util.printer.handler.TagCategoryPrintHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		return ObjectHelper.sortCollection(BuiltInRegistries.ITEM.stream().filter(filter).collect(Collectors.toList()), ObjectHelper::getItemName);
	}

	public static List<Block> scrapeRegistryForBlocks(Predicate<Block> filter) {
		return ObjectHelper.sortCollection(BuiltInRegistries.BLOCK.stream().filter(filter).collect(Collectors.toList()), ObjectHelper::getItemName);
	}

	public static List<EntityType<?>> scrapeRegistryForEntities(Predicate<EntityType<?>> filter) {
		return BuiltInRegistries.ENTITY_TYPE.stream().filter(filter).collect(Collectors.toList());
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

	public static double getAttributeFromEntity(LivingEntity entity, Attribute attribute) {
		AttributeInstance instance = entity.getAttribute(attribute);

		return instance == null ? 0d : instance.getValue();
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
		return BuiltInRegistries.ITEM.get(id) != Items.AIR;
	}

	public static boolean isBlock(ResourceLocation id) {
		return BuiltInRegistries.BLOCK.get(id) != Blocks.AIR;
	}

	public static boolean isEntity(ResourceLocation id) {
		return BuiltInRegistries.ENTITY_TYPE.get(id) != EntityType.PIG;
	}

	public static String getItemName(ItemLike item) {
		ResourceLocation itemId = RegistryUtil.getId(item.asItem());
		EntityType<?> matchingEntity = BuiltInRegistries.ENTITY_TYPE.get(itemId);
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

	public static String getBlockName(Block block) {
		if (block.asItem() != Items.AIR)
			return getItemName(block);

		return StringUtil.toTitleCase(RegistryUtil.getId(block).getPath());
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
		ResourceLocation id = RegistryUtil.getId(entityType);
		String suffix = isItem(id) ? " (entity)" : "";

		return LocaleUtil.getLocaleMessage("entity." + id.getNamespace() + "." + id.getPath()).getString() + suffix;
	}

	public static String getFluidName(Fluid fluid) {
		if (isBlock(BuiltInRegistries.FLUID.getKey(fluid)))
			return getBlockName(BuiltInRegistries.BLOCK.get(BuiltInRegistries.FLUID.getKey(fluid)));

		return StringUtil.toTitleCase(BuiltInRegistries.FLUID.getKey(fluid).getPath());
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

		collectTooltipLines(item, itemTooltip, false);

		if (controlItem != null)
			collectTooltipLines(controlItem, controlItemTooltip, false);

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

	public static void collectTooltipLines(Item item, List<Component> baseList, boolean advanced) {
		item.appendHoverText(new ItemStack(item), null, baseList, advanced ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
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

	public static Stream<RegistryAccess.RegistryEntry<?>> getAllRegistries() {
		return ServerLifecycleHooks.getCurrentServer().registryAccess().registries();
	}

	public static <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
		return ServerLifecycleHooks.getCurrentServer().registryAccess().registry(key).get();
	}

	public static <T> Registry<? super T> getRegistryForObject(T object) {
		return (Registry)ServerLifecycleHooks.getCurrentServer().registryAccess().registries().filter(registryEntry -> ((Registry)registryEntry.value()).containsValue(object)).findFirst().map(RegistryAccess.RegistryEntry::value).orElse(null);
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
				Registry registry = getRegistryForObject(unknownObject);

				if (registry == null)
					return "???";

				return registry.getKey(unknownObject).toString();
			};
		}

		return namingFunction;
	}

	public static String getBlockFlammability(Block block) {
		VariableResponse flammable = null;

		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			flammable = VariableResponse.merge(flammable, ((FireBlock)Blocks.FIRE).getBurnOdds(state) > 0);
		}

		if (flammable == VariableResponse.NO)
			flammable = null;

		try {
			for (BlockState state : block.getStateDefinition().getPossibleStates()) {
				flammable = VariableResponse.merge(flammable, state.getFlammability(null, null, null) > 0);
			}
		}
		catch (Exception ex) {
			return VariableResponse.VARIES.toString();
		}

		return flammable.toString();
	}

	@Nullable
	public static String getBlockLuminosity(Block block) {
		int min = 15;
		int max = 0;

		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			int luminosity = 0;

			try {
				luminosity = state.getLightEmission(null, null);
			}
			catch (Exception ex) {
				luminosity = state.getLightEmission();
			}

			min = Math.min(luminosity, min);
			max = Math.max(luminosity, max);
		}

		if (max == 0)
			return null;

		if (min == max)
			return String.valueOf(min);

		return "Varies (" + min + "-" + max + ")";
	}

	@Nullable
	public static String getBlockHarvestTag(Block block, Level level) {
		TagKey<Block> harvestTag = TagUtil.getAllTagsFor(Registries.BLOCK, block, level)
				.filter(tag -> tag.location().getPath().startsWith("needs_") && tag.location().getPath().endsWith("_tool"))
				.findAny()
				.orElse(null);

		if (harvestTag == null)
			return null;

		String tagName = harvestTag.location().toString();

		return StringUtil.toTitleCase(tagName.substring(6, tagName.indexOf("_tool")));
	}

	@Nullable
	public static String getBlockToolTag(Block block, Level level) {
		TagKey<Block> toolTag = TagUtil.getAllTagsFor(Registries.BLOCK, block, level)
				.filter(tag -> tag.location().getPath().startsWith("mineable/"))
				.findAny()
				.orElse(null);

		if (toolTag == null)
			return null;

		return StringUtil.toTitleCase(toolTag.location().toString().substring(9));
	}

	@Nullable
	public static String getItemAmmoType(Item item) {
		ItemStack stack = new ItemStack(item);
		StringBuilder builder = new StringBuilder();

		if (item instanceof BaseStaff<?> staff) {
			for (Map.Entry<Item, Integer> rune : staff.getRunes().entrySet()) {
				if (!builder.isEmpty())
					builder.append(System.lineSeparator());

				builder.append(rune.getValue()).append(" ").append(ObjectHelper.getItemName(rune.getKey()));
			}
		}
		else if (item instanceof BaseGun gun && !(gun instanceof BaseThrownWeapon)) {
			builder.append(ObjectHelper.getItemName(gun.getAmmoItem()));
		}
		else if (item instanceof ProjectileWeaponItem weapon) {
			Predicate<ItemStack> ammoPredicate = weapon.getAllSupportedProjectiles();

			if (ammoPredicate == ProjectileWeaponItem.ARROW_ONLY) {
				builder.append("Arrows");
			}
			else if (ammoPredicate == ProjectileWeaponItem.ARROW_OR_FIREWORK) {
				builder.append("Arrows or Fireworks");
			}
			else if (ammoPredicate == Slingshot.AMMO_PREDICATE) {
				builder.append("Pop Shots or Flint");
			}
			else {
				for (Item registryItem : AoARegistries.ITEMS) {
					if (ammoPredicate.test(new ItemStack(registryItem))) {
						if (!builder.isEmpty())
							builder.append(System.lineSeparator());

						builder.append(ObjectHelper.getItemName(registryItem)).append(", ");
					}
				}
			}
		}

		return builder.isEmpty() ? null : builder.toString();
	}

	public enum VariableResponse {
		YES,
		NO,
		VARIES;

		@Override
		public String toString() {
			return StringUtil.toTitleCase(super.toString());
		}

		public static VariableResponse merge(@Nullable VariableResponse existing, boolean newResponse) {
			if (existing == VARIES || (existing == YES && !newResponse) || (existing == NO && newResponse))
				return VARIES;

			return newResponse ? YES : NO;
		}
	}
}
