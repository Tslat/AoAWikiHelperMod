package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.*;
import net.minecraft.loot.functions.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tags.ITag;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.tslat.aoa3.common.registration.AoABlocks;
import net.tslat.aoa3.content.loottable.condition.HoldingItem;
import net.tslat.aoa3.content.loottable.condition.PlayerHasLevel;
import net.tslat.aoa3.content.loottable.condition.PlayerHasResource;
import net.tslat.aoa3.content.loottable.function.EnchantSpecific;
import net.tslat.aoa3.content.loottable.function.GrantSkillXp;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.loottable.condition.*;
import net.tslat.aoawikihelpermod.util.loottable.function.*;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class LootTableHelper {
	private static final HashMap<Class<? extends ILootCondition>, LootConditionHelper<? extends ILootCondition>> CONDITION_DESCRIPTORS = new HashMap<Class<? extends ILootCondition>, LootConditionHelper<? extends ILootCondition>>();
	private static final HashMap<Class<? extends ILootFunction>, LootFunctionHelper<? extends ILootFunction>> FUNCTION_DESCRIPTORS = new HashMap<Class<? extends ILootFunction>, LootFunctionHelper<? extends ILootFunction>>();
	private static final Field lootTablePoolsField;
	private static final Field lootPoolEntriesField;
	private static final Field lootPoolConditionsField;

	static {
		lootTablePoolsField = ObfuscationReflectionHelper.findField(LootTable.class, "field_186466_c");
		lootPoolEntriesField = ObfuscationReflectionHelper.findField(LootPool.class, "field_186453_a");
		lootPoolConditionsField = ObfuscationReflectionHelper.findField(LootPool.class, "field_186454_b");
	}

	public static void init() {
		CONDITION_DESCRIPTORS.put(Alternative.class, new AlternativeConditionHelper());
		CONDITION_DESCRIPTORS.put(BlockStateProperty.class, new BlockStatePropertyConditionHelper());
		CONDITION_DESCRIPTORS.put(DamageSourceProperties.class, new DamageSourcePropertiesConditionHelper());
		CONDITION_DESCRIPTORS.put(EntityHasProperty.class, new EntityHasPropertyConditionHelper());
		CONDITION_DESCRIPTORS.put(EntityHasScore.class, new EntityHasScoreConditionHelper());
		CONDITION_DESCRIPTORS.put(HoldingItem.class, new HoldingItemConditionHelper());
		CONDITION_DESCRIPTORS.put(Inverted.class, new InvertedConditionHelper());
		CONDITION_DESCRIPTORS.put(KilledByPlayer.class, new KilledByPlayerConditionHelper());
		CONDITION_DESCRIPTORS.put(LocationCheck.class, new LocationCheckConditionHelper());
		CONDITION_DESCRIPTORS.put(MatchTool.class, new MatchToolConditionHelper());
		CONDITION_DESCRIPTORS.put(PlayerHasLevel.class, new PlayerHasLevelConditionHelper());
		CONDITION_DESCRIPTORS.put(PlayerHasResource.class, new PlayerHasResourceConditionHelper());
		CONDITION_DESCRIPTORS.put(RandomChance.class, new RandomChanceConditionHelper());
		CONDITION_DESCRIPTORS.put(RandomChanceWithLooting.class, new RandomChanceWithLootingConditionHelper());
		CONDITION_DESCRIPTORS.put(TableBonus.class, new TableBonusConditionHelper());
		CONDITION_DESCRIPTORS.put(WeatherCheck.class, new WeatherCheckConditionHelper());

		FUNCTION_DESCRIPTORS.put(ApplyBonus.class, new ApplyBonusFunctionHelper());
		FUNCTION_DESCRIPTORS.put(EnchantWithLevels.class, new EnchantWithLevelsFunctionHelper());
		FUNCTION_DESCRIPTORS.put(EnchantRandomly.class, new EnchantRandomlyFunctionHelper());
		FUNCTION_DESCRIPTORS.put(EnchantSpecific.class, new EnchantSpecificFunctionHelper());
		FUNCTION_DESCRIPTORS.put(LimitCount.class, new LimitCountFunctionHelper());
		FUNCTION_DESCRIPTORS.put(GrantSkillXp.class, new GrantSkillXpFunctionHelper());
		FUNCTION_DESCRIPTORS.put(Smelt.class, new SmeltFunctionHelper());
	}

	public static List<LootPool> getPools(LootTable table) {
		try {
			return (List<LootPool>)lootTablePoolsField.get(table);
		}
		catch (Exception ex) {
			AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Failed to get loot pools from reflected pools field.");
		}

		return ImmutableList.of();
	}

	public static List<LootEntry> getLootEntries(LootPool pool) {
		try {
			return (List<LootEntry>)lootPoolEntriesField.get(pool);
		}
		catch (Exception ex) {
			AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Failed to get loot entries from reflected entries field.");
		}

		return ImmutableList.of();
	}

	public static List<ILootCondition> getConditions(LootPool pool) {
		try {
			return (List<ILootCondition>)lootPoolConditionsField.get(pool);
		}
		catch (Exception ex) {
			AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Failed to get loot pool conditions from reflected conditions field.");
		}

		return ImmutableList.of();
	}

	@Nonnull
	public static String getConditionDescription(ILootCondition lootCondition) {
		if (!CONDITION_DESCRIPTORS.containsKey(lootCondition.getClass()))
			return "";

		return CONDITION_DESCRIPTORS.get(lootCondition.getClass()).getDescriptor(lootCondition);
	}

	@Nonnull
	public static String getFunctionDescription(ILootFunction lootFunction) {
		if (!FUNCTION_DESCRIPTORS.containsKey(lootFunction.getClass()))
			return "";

		return FUNCTION_DESCRIPTORS.get(lootFunction.getClass()).getDescriptor(lootFunction);
	}

	public static String getConditionsDescription(String target, Collection<ILootCondition> conditions) {
		if (conditions.isEmpty())
			return "";

		StringBuilder builder = new StringBuilder("This ").append(target).append(" will only roll ");
		int initialLength = builder.length();
		ILootCondition[] conditionArray = conditions.toArray(new ILootCondition[0]);

		for (int i = 0; i < conditionArray.length; i++) {
			String conditionDescription = getConditionDescription(conditionArray[i]);

			if (conditionDescription.isEmpty())
				continue;

			if (i > 0) {
				if (builder.charAt(builder.length() - 1) == '\n') {
					builder.append("and ");
				}
				else {
					builder.append(", and<br/>");
				}
			}

			builder.append(conditionDescription);
		}

		if (builder.length() > initialLength) {
			builder.append(".");
		}
		else {
			builder.setLength(0);
		}

		return builder.toString();
	}

	public static String getFunctionsDescription(String target, Collection<ILootFunction> functions) {
		if (functions.isEmpty())
			return "";

		StringBuilder builder = new StringBuilder("This ").append(target).append(" ");
		int initialLength = builder.length();
		ILootFunction[] functionsArray = functions.toArray(new ILootFunction[0]);

		for (int i = 0; i < functionsArray.length; i++) {
			ILootFunction function = functionsArray[i];

			if (function instanceof SetCount || function instanceof LootingEnchantBonus)
				continue;

			String functionDescription = getFunctionDescription(function);

			if (functionDescription.isEmpty())
				continue;

			if (i > 0)
				builder.append(", and<br/>");

			builder.append(functionDescription);
		}

		if (builder.length() > initialLength) {
			builder.append(".");
		}
		else {
			builder.setLength(0);
		}

		return builder.toString();
	}

	public static String getLootEntryLine(int poolIndex, LootEntry entry, List<ILootCondition> conditions) {
		if (entry instanceof EmptyLootEntry)
			return getEmptyLootEntryLine(poolIndex, (EmptyLootEntry)entry, conditions, Arrays.asList(((EmptyLootEntry)entry).functions));

		if (entry instanceof ItemLootEntry)
			return getItemLootEntryLine(poolIndex, (ItemLootEntry)entry, conditions, Arrays.asList(((ItemLootEntry)entry).functions));

		if (entry instanceof TagLootEntry)
			return getTagLootEntryLine(poolIndex, (TagLootEntry)entry, conditions, Arrays.asList(((TagLootEntry)entry).functions));

		if (entry instanceof TableLootEntry)
			return getTableLootEntryLine(poolIndex, (TableLootEntry)entry, conditions, Arrays.asList(((TableLootEntry)entry).functions));

		return "";
	}

	private static String getEmptyLootEntryLine(int poolIndex, EmptyLootEntry entry, List<ILootCondition> conditions, List<ILootFunction> functions) {
		StringBuilder entryNotesBuilder = new StringBuilder();
		StringBuilder entryBuilder = new StringBuilder("group:" + poolIndex + "; item:");
		String looting = getLootingString(functions);

		entryBuilder.append("Nothing; image:none;");
		entryBuilder.append(" weight:").append(entry.weight).append(";");
		entryBuilder.append(" quantity:-;");

		if (!looting.isEmpty())
			entryBuilder.append(" looting:").append(looting).append(";");

		if (entry.quality != 0) {
			if (entry.quality > 0) {
				entryNotesBuilder.append("Chance is increased with each level of luck");
			}
			else {
				entryNotesBuilder.append("Chance is decreased with each level of luck");
			}
		}

		if (!conditions.isEmpty()) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getConditionsDescription("entry", conditions));
		}

		if (!functions.isEmpty()) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getFunctionsDescription("entry", functions));
		}

		if (entryNotesBuilder.length() > 0)
			entryBuilder.append(" notes:").append(entryNotesBuilder);

		return entryBuilder.toString();
	}

	private static String getItemLootEntryLine(int poolIndex, ItemLootEntry entry, List<ILootCondition> conditions, List<ILootFunction> functions) {
		StringBuilder entryNotesBuilder = new StringBuilder();
		StringBuilder entryBuilder = new StringBuilder("group:" + poolIndex + "; item:");
		String looting = getLootingString(functions);
		String itemName = ObjectHelper.getItemName(entry.item);

		if (entry.item instanceof PotionItem) {
			for (ILootFunction function : entry.functions) {
				if (function instanceof SetNBT) {
					ItemStack potionStack = new ItemStack(entry.item);

					((SetNBT)function).run(potionStack, null);
					entryBuilder.append(potionStack.getHoverName().getString()).append("; image:").append(ObjectHelper.getItemName(entry.item)).append(".png;");

					List<EffectInstance> effects = PotionUtils.getMobEffects(potionStack);

					if (!effects.isEmpty()) {
						entryNotesBuilder.append("<br/>Effects:");

						for (EffectInstance effect : effects) {
							entryNotesBuilder.append("<br/>").append(effect.getEffect().getDisplayName().getString()).append(" ").append(effect.getAmplifier() + 1);
							entryNotesBuilder.append(" (").append(FormattingHelper.getTimeFromTicks(effect.getDuration())).append(")");
						}
					}

					break;
				}
			}
		}
		else if (entry.item == AoABlocks.TROPHY.get().asItem()) {
			for (ILootFunction function : entry.functions) {
				if (function instanceof SetNBT) {
					ItemStack trophyStack = new ItemStack(entry.item);

					((SetNBT)function).run(trophyStack, null);
					entryBuilder.append(trophyStack.getHoverName().getString()).append("; image:").append(ObjectHelper.getItemName(entry.item)).append(".png;");

					break;
				}
			}
		}
		else {
			entryBuilder.append(itemName).append(";");
		}

		entryBuilder.append(" weight:").append(entry.weight).append(";");
		entryBuilder.append(" quantity:").append(getQuantityString(functions)).append(";");

		if (!looting.isEmpty())
			entryBuilder.append(" looting:").append(looting).append(";");

		if (entry.quality != 0) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			if (entry.quality > 0) {
				entryNotesBuilder.append("Chance is increased with each level of luck");
			}
			else {
				entryNotesBuilder.append("Chance is decreased with each level of luck");
			}
		}

		if (!conditions.isEmpty()) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getConditionsDescription("entry", conditions));
		}

		if (!functions.isEmpty()) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getFunctionsDescription("entry", functions));
		}

		if (entryNotesBuilder.length() > 0)
			entryBuilder.append(" notes:").append(entryNotesBuilder);

		return entryBuilder.toString();
	}

	private static String getTagLootEntryLine(int poolIndex, TagLootEntry entry, List<ILootCondition> conditions, List<ILootFunction> functions) {
		StringBuilder entryNotesBuilder = new StringBuilder();
		StringBuilder entryBuilder = new StringBuilder("group:" + poolIndex + "; item:");
		String looting = getLootingString(functions);
		List<Item> tagItems = entry.tag.getValues();

		if (tagItems.isEmpty()) {
			entryBuilder.append("Varies; image:none;");
		}
		else {
			Item item = tagItems.get(0);

			entryBuilder.append(ObjectHelper.getItemName(item)).append(";");
		}

		entryBuilder.append(" weight:").append(entry.weight).append(";");
		entryBuilder.append(" quantity:").append(getQuantityString(functions)).append(";");

		if (!looting.isEmpty())
			entryBuilder.append(" looting:").append(looting).append(";");

		if (entry.tag instanceof ITag.INamedTag) {
			entryNotesBuilder.append("Randomly selects item from anything tagged as ").append(FormattingHelper.createLinkableTag(((ITag.INamedTag<?>)entry.tag).getName().toString()));
		}
		else {
			entryNotesBuilder.append("Randomly selects item from anything with same tag");
		}

		if (entry.quality != 0) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			if (entry.quality > 0) {
				entryNotesBuilder.append("Chance is increased with each level of luck");
			}
			else {
				entryNotesBuilder.append("Chance is decreased with each level of luck");
			}
		}

		if (!conditions.isEmpty()) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getConditionsDescription("entry", conditions));
		}

		if (!functions.isEmpty()) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getFunctionsDescription("entry", functions));
		}

		if (entryNotesBuilder.length() > 0)
			entryBuilder.append(" notes:").append(entryNotesBuilder);

		return entryBuilder.toString();
	}

	private static String getTableLootEntryLine(int poolIndex, TableLootEntry entry, List<ILootCondition> conditions, List<ILootFunction> functions) {
		StringBuilder entryNotesBuilder = new StringBuilder();
		StringBuilder entryBuilder = new StringBuilder("group:" + poolIndex + "; image:none; item:");
		String looting = getLootingString(functions);
		String tableName = entry.name.getPath();

		if (tableName.contains("\\")) {
			tableName = StringUtil.toTitleCase(tableName.substring(tableName.indexOf("\\") + 1));
		}
		else if (tableName.contains("/")) {
			tableName = StringUtil.toTitleCase(tableName.substring(tableName.indexOf("/") + 1));
		}

		entryBuilder.append(tableName);

		if (!tableName.endsWith("Table")) {
			entryBuilder.append(" Table;");
		}
		else {
			entryBuilder.append(";");
		}

		entryBuilder.append(" weight:").append(entry.weight).append(";");
		entryBuilder.append(" quantity:").append(getQuantityString(functions)).append(";");

		if (!looting.isEmpty())
			entryBuilder.append(" looting:").append(looting).append(";");

		if (entry.quality != 0) {
			if (entry.quality > 0) {
				entryNotesBuilder.append("Chance is increased with each level of luck");
			}
			else {
				entryNotesBuilder.append("Chance is decreased with each level of luck");
			}
		}

		if (!conditions.isEmpty()) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getConditionsDescription("entry", conditions));
		}

		if (!functions.isEmpty()) {
			if (entryNotesBuilder.length() > 0)
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getFunctionsDescription("entry", functions));
		}

		if (entryNotesBuilder.length() > 0)
			entryBuilder.append(" notes:").append(entryNotesBuilder);

		return entryBuilder.toString();
	}

	private static String getQuantityString(List<ILootFunction> functions) {
		for (ILootFunction function : functions) {
			if (function instanceof SetCount)
				return FormattingHelper.getStringFromRange(((SetCount)function).value);
		}

		return "1";
	}

	private static String getLootingString(List<ILootFunction> functions) {
		for (ILootFunction function : functions) {
			if (function instanceof LootingEnchantBonus) {
				LootingEnchantBonus bonus = (LootingEnchantBonus)function;

				if (bonus.value.getMin() != bonus.value.getMax()) {
					if (bonus.limit > 0 && bonus.value.getMin() > bonus.limit)
						return String.valueOf(bonus.limit);

					return bonus.value.getMin() + "-" + (bonus.limit > 0 ? Math.min(bonus.limit, bonus.value.getMax()) : bonus.value.getMax());
				}
				else {
					if (bonus.limit > 0 && bonus.value.getMax() > bonus.limit)
						return String.valueOf(bonus.limit);
				}
			}
		}

		return "";
	}
}
