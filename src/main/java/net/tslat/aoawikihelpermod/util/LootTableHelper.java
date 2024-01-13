package net.tslat.aoawikihelpermod.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.tslat.aoa3.common.registration.block.AoABlocks;
import net.tslat.aoa3.content.loottable.condition.PlayerHasLevel;
import net.tslat.aoa3.content.loottable.condition.PlayerHasResource;
import net.tslat.aoa3.content.loottable.condition.WearingOrHoldingItem;
import net.tslat.aoa3.content.loottable.function.EnchantSpecific;
import net.tslat.aoa3.content.loottable.function.GrantSkillXp;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.loottable.condition.*;
import net.tslat.aoawikihelpermod.util.loottable.function.*;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class LootTableHelper {
	private static final HashMap<Class<? extends LootItemCondition>, LootConditionHelper<? extends LootItemCondition>> CONDITION_DESCRIPTORS = new HashMap<>();
	private static final HashMap<Class<? extends LootItemFunction>, LootFunctionHelper<? extends LootItemFunction>> FUNCTION_DESCRIPTORS = new HashMap<>();

	public static void init() {
		CONDITION_DESCRIPTORS.put(AnyOfCondition.class, new AnyOfConditionHelper());
		CONDITION_DESCRIPTORS.put(LootItemBlockStatePropertyCondition.class, new BlockStatePropertyConditionHelper());
		CONDITION_DESCRIPTORS.put(DamageSourceCondition.class, new DamageSourcePropertiesConditionHelper());
		CONDITION_DESCRIPTORS.put(LootItemEntityPropertyCondition.class, new EntityHasPropertyConditionHelper());
		CONDITION_DESCRIPTORS.put(EntityHasScoreCondition.class, new EntityHasScoreConditionHelper());
		CONDITION_DESCRIPTORS.put(WearingOrHoldingItem.class, new WearingOrHoldingItemConditionHelper());
		CONDITION_DESCRIPTORS.put(InvertedLootItemCondition.class, new InvertedConditionHelper());
		CONDITION_DESCRIPTORS.put(LootItemKilledByPlayerCondition.class, new KilledByPlayerConditionHelper());
		CONDITION_DESCRIPTORS.put(LocationCheck.class, new LocationCheckConditionHelper());
		CONDITION_DESCRIPTORS.put(MatchTool.class, new MatchToolConditionHelper());
		CONDITION_DESCRIPTORS.put(PlayerHasLevel.class, new PlayerHasLevelConditionHelper());
		CONDITION_DESCRIPTORS.put(PlayerHasResource.class, new PlayerHasResourceConditionHelper());
		CONDITION_DESCRIPTORS.put(LootItemRandomChanceCondition.class, new RandomChanceConditionHelper());
		CONDITION_DESCRIPTORS.put(LootItemRandomChanceWithLootingCondition.class, new RandomChanceWithLootingConditionHelper());
		CONDITION_DESCRIPTORS.put(BonusLevelTableCondition.class, new TableBonusConditionHelper());
		CONDITION_DESCRIPTORS.put(WeatherCheck.class, new WeatherCheckConditionHelper());

		FUNCTION_DESCRIPTORS.put(ApplyBonusCount.class, new ApplyBonusFunctionHelper());
		FUNCTION_DESCRIPTORS.put(EnchantWithLevelsFunction.class, new EnchantWithLevelsFunctionHelper());
		FUNCTION_DESCRIPTORS.put(EnchantRandomlyFunction.class, new EnchantRandomlyFunctionHelper());
		FUNCTION_DESCRIPTORS.put(EnchantSpecific.class, new EnchantSpecificFunctionHelper());
		FUNCTION_DESCRIPTORS.put(LimitCount.class, new LimitCountFunctionHelper());
		FUNCTION_DESCRIPTORS.put(GrantSkillXp.class, new GrantSkillXpFunctionHelper());
		FUNCTION_DESCRIPTORS.put(SmeltItemFunction.class, new SmeltFunctionHelper());
	}

	@Nonnull
	public static String getConditionDescription(LootItemCondition lootCondition) {
		if (!CONDITION_DESCRIPTORS.containsKey(lootCondition.getClass()))
			return "";

		return CONDITION_DESCRIPTORS.get(lootCondition.getClass()).getDescriptor(lootCondition);
	}

	@Nonnull
	public static String getFunctionDescription(LootItemFunction lootFunction) {
		if (!FUNCTION_DESCRIPTORS.containsKey(lootFunction.getClass()))
			return "";

		return FUNCTION_DESCRIPTORS.get(lootFunction.getClass()).getDescriptor(lootFunction);
	}

	public static String getConditionsDescription(String target, Collection<LootItemCondition> conditions) {
		if (conditions.isEmpty())
			return "";

		StringBuilder builder = new StringBuilder("This ").append(target).append(" will only roll ");
		int initialLength = builder.length();
		LootItemCondition[] conditionArray = conditions.toArray(new LootItemCondition[0]);

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

	public static String getFunctionsDescription(String target, Collection<LootItemFunction> functions) {
		if (functions.isEmpty())
			return "";

		StringBuilder builder = new StringBuilder("This ").append(target).append(" ");
		int initialLength = builder.length();
		LootItemFunction[] functionsArray = functions.toArray(new LootItemFunction[0]);
		int index = 0;

		for (LootItemFunction function : functionsArray) {
			if (function instanceof SetItemCountFunction || function instanceof LootingEnchantFunction)
				continue;

			String functionDescription = getFunctionDescription(function);

			if (functionDescription.isEmpty())
				continue;

			if (index++ > 0)
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

	public static String getLootEntryLine(int poolIndex, LootPoolEntryContainer entry, List<LootItemCondition> conditions) {
		if (entry instanceof EmptyLootItem emptyLoot)
			return getEmptyLootEntryLine(poolIndex, emptyLoot, conditions, emptyLoot.functions);

		if (entry instanceof LootItem itemLoot)
			return getItemLootEntryLine(poolIndex, itemLoot, conditions, itemLoot.functions);

		if (entry instanceof TagEntry tagLoot)
			return getTagLootEntryLine(poolIndex, tagLoot, conditions, tagLoot.functions);

		if (entry instanceof LootTableReference tableLoot)
			return getTableLootEntryLine(poolIndex, tableLoot, conditions, tableLoot.functions);

		if (entry instanceof EntryGroup groupLoot)
			return getCollectionLootEntryLine(poolIndex, groupLoot, conditions, List.of());

		return "UNKNOWN LOOT ENTRY TYPE: " + entry.getClass();
	}

	private static String getCollectionLootEntryLine(int poolIndex, EntryGroup entry, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
		StringBuilder entryBuilder = new StringBuilder("group:" + poolIndex + "; item:");

		entryBuilder.append("A bunch of stuff - do this manually until you figure out what format you want this in; image:none;");

		return entryBuilder.toString();
	}

	private static String getEmptyLootEntryLine(int poolIndex, EmptyLootItem entry, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
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
			if (!entryNotesBuilder.isEmpty())
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getConditionsDescription("entry", conditions));
		}

		if (!functions.isEmpty()) {
			if (!entryNotesBuilder.isEmpty())
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getFunctionsDescription("entry", functions));
		}

		if (!entryNotesBuilder.isEmpty())
			entryBuilder.append(" notes:").append(entryNotesBuilder);

		return entryBuilder.toString();
	}

	private static String getItemLootEntryLine(int poolIndex, LootItem entry, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
		StringBuilder entryNotesBuilder = new StringBuilder();
		StringBuilder entryBuilder = new StringBuilder("group:" + poolIndex + "; item:");
		String looting = getLootingString(functions);
		String itemName = ObjectHelper.getItemName(entry.item.value());

		if (entry.item instanceof PotionItem) {
			for (LootItemFunction function : entry.functions) {
				if (function instanceof SetNbtFunction) {
					ItemStack potionStack = new ItemStack(entry.item);

					((SetNbtFunction)function).run(potionStack, null);
					entryBuilder.append(potionStack.getHoverName().getString()).append("; image:").append(ObjectHelper.getItemName(entry.item.value())).append(".png;");

					List<MobEffectInstance> effects = PotionUtils.getMobEffects(potionStack);

					if (!effects.isEmpty()) {
						entryNotesBuilder.append("<br/>Effects:");

						for (MobEffectInstance effect : effects) {
							entryNotesBuilder.append("<br/>").append(effect.getEffect().getDisplayName().getString()).append(" ").append(effect.getAmplifier() + 1);
							entryNotesBuilder.append(" (").append(FormattingHelper.getTimeFromTicks(effect.getDuration())).append(")");
						}
					}

					break;
				}
			}
		}
		else if (entry.item == AoABlocks.TROPHY.get().asItem()) {
			for (LootItemFunction function : entry.functions) {
				if (function instanceof SetNbtFunction) {
					ItemStack trophyStack = new ItemStack(entry.item);

					((SetNbtFunction)function).run(trophyStack, null);
					entryBuilder.append(trophyStack.getHoverName().getString()).append("; image:").append(ObjectHelper.getItemName(entry.item.value())).append(".png;");

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
			if (!entryNotesBuilder.isEmpty())
				entryNotesBuilder.append("<br/>");

			if (entry.quality > 0) {
				entryNotesBuilder.append("Chance is increased with each level of luck");
			}
			else {
				entryNotesBuilder.append("Chance is decreased with each level of luck");
			}
		}

		if (!conditions.isEmpty()) {
			if (!entryNotesBuilder.isEmpty())
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getConditionsDescription("entry", conditions));
		}

		if (!functions.isEmpty()) {
			if (!entryNotesBuilder.isEmpty())
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getFunctionsDescription("entry", functions));
		}

		if (!entryNotesBuilder.isEmpty())
			entryBuilder.append(" notes:").append(entryNotesBuilder);

		return entryBuilder.toString();
	}

	private static String getTagLootEntryLine(int poolIndex, TagEntry entry, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
		StringBuilder entryNotesBuilder = new StringBuilder();
		StringBuilder entryBuilder = new StringBuilder("group:" + poolIndex + "; item:");
		String looting = getLootingString(functions);
		List<Item> tagItems = BuiltInRegistries.ITEM.getTag(entry.tag).stream().<Item>mapMulti((set, adder) -> set.stream().map(Holder::value).toList().forEach(adder)).toList();

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

		entryNotesBuilder.append("Randomly selects item from anything tagged as ").append(FormattingHelper.createLinkableTag(entry.tag.location().toString(), Items.STONE));

		if (entry.quality != 0) {
			if (!entryNotesBuilder.isEmpty())
				entryNotesBuilder.append("<br/>");

			if (entry.quality > 0) {
				entryNotesBuilder.append("Chance is increased with each level of luck");
			}
			else {
				entryNotesBuilder.append("Chance is decreased with each level of luck");
			}
		}

		if (!conditions.isEmpty()) {
			if (!entryNotesBuilder.isEmpty())
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

	private static String getTableLootEntryLine(int poolIndex, LootTableReference entry, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
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
			if (!entryNotesBuilder.isEmpty())
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getConditionsDescription("entry", conditions));
		}

		if (!functions.isEmpty()) {
			if (!entryNotesBuilder.isEmpty())
				entryNotesBuilder.append("<br/>");

			entryNotesBuilder.append(LootTableHelper.getFunctionsDescription("entry", functions));
		}

		if (!entryNotesBuilder.isEmpty())
			entryBuilder.append(" notes:").append(entryNotesBuilder);

		return entryBuilder.toString();
	}

	private static String getQuantityString(List<LootItemFunction> functions) {
		for (LootItemFunction function : functions) {
			if (function instanceof SetItemCountFunction)
				return FormattingHelper.getStringFromRange(((SetItemCountFunction)function).value);
		}

		return "1";
	}

	private static String getLootingString(List<LootItemFunction> functions) {
		for (LootItemFunction function : functions) {
			if (function instanceof LootingEnchantFunction bonus)
				return FormattingHelper.getStringFromRange(bonus.value);
		}

		return "";
	}
}
