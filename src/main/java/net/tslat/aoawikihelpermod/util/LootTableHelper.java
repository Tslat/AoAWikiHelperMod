package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.*;
import net.minecraft.loot.functions.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tags.ITag;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.tslat.aoa3.common.registration.AoABlocks;
import net.tslat.aoa3.loottable.condition.HoldingItem;
import net.tslat.aoa3.loottable.condition.PlayerHasLevel;
import net.tslat.aoa3.loottable.condition.PlayerHasResource;
import net.tslat.aoa3.loottable.condition.PlayerHasTribute;
import net.tslat.aoa3.loottable.function.EnchantSpecific;
import net.tslat.aoa3.loottable.function.GrantSkillXp;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoa3.util.constant.Deities;
import net.tslat.aoa3.util.constant.Resources;
import net.tslat.aoa3.util.constant.Skills;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;
import java.util.*;

public class LootTableHelper {
	private static final Field lootTablePoolsField;
	private static final Field lootPoolEntriesField;
	private static final Field lootPoolConditionsField;

	static {
		lootTablePoolsField = ObfuscationReflectionHelper.findField(LootTable.class, "field_186466_c");
		lootPoolEntriesField = ObfuscationReflectionHelper.findField(LootPool.class, "field_186453_a");
		lootPoolConditionsField = ObfuscationReflectionHelper.findField(LootPool.class, "field_186454_b");
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

	// This could easily have its descriptions expanded, depending on how in-detail you want to go
	public static String getConditionsDescription(String target, Collection<ILootCondition> conditions) {
		if (conditions.isEmpty())
			return "";

		StringBuilder builder = new StringBuilder();

		for (ILootCondition condition : conditions) {
			if (builder.length() > 0)
				builder.append(", and<br/>");

			if (condition instanceof BlockStateProperty) {
				builder.append("This ").append(target).append(" will only roll if the target block meets certain conditions");
			}
			else if (condition instanceof DamageSourceProperties) {
				builder.append("This ").append(target).append(" will only roll if the damage source meets certain conditions");
			}
			else if (condition instanceof EntityHasProperty || condition instanceof EntityHasScore) {
				LootContext.EntityTarget entityTarget = condition instanceof EntityHasProperty ? ((EntityHasProperty)condition).entityTarget : ((EntityHasScore)condition).entityTarget;

				builder.append("This ").append(target);

				switch (entityTarget) {
					case THIS:
						builder.append(" will only roll if the target entity meets certain conditions");
						break;
					case KILLER:
						builder.append(" will only roll if the attacking entity meets certain conditions");
						break;
					case DIRECT_KILLER:
						builder.append(" will only roll if the directly killing entity meets certain conditions");
						break;
					case KILLER_PLAYER:
						builder.append(" will only roll if the killer is a player, and meets certain conditions");
						break;
					default:
						break;
				}
			}
			else if (condition instanceof HoldingItem) {
				builder.append("This ").append(target);

				HoldingItem holdingItemCondition = (HoldingItem)condition;
				ItemPredicate predicate = holdingItemCondition.getPredicate();
				LootContext.EntityTarget entityTarget = holdingItemCondition.getTarget();
				Hand hand = holdingItemCondition.getHand();

				String handParticle = hand == null ? "held item" : (hand == Hand.MAIN_HAND ? "mainhand item" : "offhand item");
				String heldItemParticle;

				if (predicate.item != null) {
					heldItemParticle = "is " + FormattingHelper.createLinkableItem(predicate.item, false, true);
				}
				else if (predicate.tag instanceof ITag.INamedTag) {
					heldItemParticle = "is anything tagged as " + FormattingHelper.createLinkableTag(((ITag.INamedTag<?>)predicate.tag).getName().toString());
				}
				else {
					heldItemParticle = "meets certain conditions";
				}

				switch (entityTarget) {
					case THIS:
						builder.append(" will only roll if the target entity's ").append(handParticle).append(" ").append(heldItemParticle);
						break;
					case KILLER:
						builder.append(" will only roll if the attacking entity's ").append(handParticle).append(" ").append(heldItemParticle);
						break;
					case DIRECT_KILLER:
						builder.append(" will only roll if the directly killing entity's ").append(handParticle).append(" ").append(heldItemParticle);
						break;
					case KILLER_PLAYER:
						builder.append(" will only roll if the killer is a player, and if their ").append(handParticle).append(" ").append(heldItemParticle);
						break;
					default:
						break;
				}
			}
			else if (condition instanceof Inverted) {
				String conditionLine = getConditionsDescription(target, Collections.singletonList(((Inverted)condition).term)).replace(".", "");

				builder.append(conditionLine.replace("will only roll", "won't roll"));
			}
			else if (condition instanceof KilledByPlayer) {
				builder.append("This ").append(target).append(" will only roll if the target was killed by a player");
			}
			else if (condition instanceof LocationCheck) {
				builder.append("This ").append(target).append(" will only roll if the source of the drops is from a specific position");
			}
			else if (condition instanceof MatchTool) {
				ItemPredicate predicate = ((MatchTool)condition).predicate;

				String heldItemParticle2;

				if (predicate.item != null) {
					heldItemParticle2 = "is " + FormattingHelper.createLinkableItem(predicate.item, false, true);
				}
				else if (predicate.tag instanceof ITag.INamedTag) {
					heldItemParticle2 = "is anything tagged as " + FormattingHelper.createLinkableTag(((ITag.INamedTag<?>)predicate.tag).getName().toString());
				}
				else {
					heldItemParticle2 = "meets certain conditions";
				}

				builder.append("This ").append(target).append(" will only roll if the tool used ").append(heldItemParticle2);
			}
			else if (condition instanceof PlayerHasLevel) {
				Skills skill = ((PlayerHasLevel)condition).getSkill();
				int level = ((PlayerHasLevel)condition).getLevel();

				builder.append("This ").append(target).append(" will only roll if the player has at least level ").append(level).append(" ").append(StringUtil.toTitleCase(skill.toString()));
			}
			else if (condition instanceof PlayerHasResource) {
				Resources resource = ((PlayerHasResource)condition).getResource();
				float amount = ((PlayerHasResource)condition).getAmount();

				builder.append("This ").append(target).append(" will only roll if the player has at least ").append(NumberUtil.roundToNthDecimalPlace(amount, 2)).append(" ").append(FormattingHelper.createLinkableText(StringUtil.toTitleCase(resource.toString()), false, false, true));
			}
			else if (condition instanceof PlayerHasTribute) {
				Deities deity = ((PlayerHasTribute)condition).getDeity();
				int amount = ((PlayerHasTribute)condition).getAmount();

				builder.append("This ").append(target).append(" will only roll if the player has at least ").append(amount).append(" tribute for ").append(FormattingHelper.createLinkableText(StringUtil.toTitleCase(deity.toString()), false, false, true));
			}
			else if (condition instanceof RandomChance) {
				float chance = ((RandomChance)condition).probability;

				builder.append("This ").append(target).append(" will only roll if a fixed random chance check is passed, with a chance of ").append(NumberUtil.roundToNthDecimalPlace(chance, 3)).append("%");
			}
			else if (condition instanceof RandomChanceWithLooting) {
				float chance = ((RandomChanceWithLooting)condition).percent;
				float lootingMod = ((RandomChanceWithLooting)condition).lootingMultiplier;

				builder.append("This ").append(target).append(" will only roll if a fixed random chance check is passed, with a chance of ").append(NumberUtil.roundToNthDecimalPlace(chance, 3)).append("%")
						.append(", with an extra ").append(NumberUtil.roundToNthDecimalPlace(chance, 3)).append(" per looting level");
			}
			else if (condition instanceof TableBonus) {
				Enchantment enchant = ((TableBonus)condition).enchantment;

				builder.append("This ").append(target).append(" will only roll if a chance check is passed, depending on the level of ").append(FormattingHelper.createLinkableText(ObjectHelper.getEnchantmentName(enchant, 0), false, enchant.getRegistryName().getNamespace().equals("minecraft"), true)).append(" used");
			}
			else if (condition instanceof WeatherCheck) {
				Boolean isRaining = ((WeatherCheck)condition).isRaining;
				Boolean isThundering = ((WeatherCheck)condition).isThundering;

				if (isRaining != null || isThundering != null) {
					builder.append("This ").append(target).append(" will only roll if ");

					if (isRaining != null) {
						if (isRaining) {
							builder.append("it is raining");
						}
						else {
							builder.append("it isn't raining");
						}
					}

					if (isThundering != null) {
						if (isThundering) {
							if (isRaining != null) {
								builder.append(", and it is thundering");
							}
							else {
								builder.append("is it thundering");
							}
						}
						else {
							if (isRaining != null) {
								builder.append(", and it isn't thundering");
							}
							else {
								builder.append("it isn't thundering");
							}
						}
					}
				}
			}
		}

		if (builder.length() > 0)
			builder.append(".");

		String[] line = builder.toString().split("<br/>");
		StringBuilder reBuilder = new StringBuilder(line[0]);

		for (int i = 1; i < line.length; i++) {
			reBuilder.append("<br/>").append("and if").append(line[i].substring(line[i].indexOf("will only roll if") + 17));
		}

		return reBuilder.toString();
	}

	// This could easily have its descriptions expanded, depending on how in-detail you want to go
	public static String getFunctionsDescription(String target, Collection<ILootFunction> functions) {
		if (functions.isEmpty())
			return "";

		StringBuilder builder = new StringBuilder();

		for (ILootFunction function : functions) {
			if (function instanceof SetCount || function instanceof LootingEnchantBonus) {
				continue;
			}

			if (builder.length() > 0)
				builder.append(", and<br/>");

			if (function instanceof ApplyBonus) {
				Enchantment enchant = ((ApplyBonus)function).enchantment;

				builder.append("This ").append(target).append(" will vary in quantity depending on the level of ").append(FormattingHelper.createLinkableText(ObjectHelper.getEnchantmentName(enchant, 0), false, false, true)).append(" used");
			}
			else if (function instanceof EnchantWithLevels) {
				builder.append("This ").append(target).append(" will be enchanted with random enchantments");
			}
			else if (function instanceof EnchantRandomly) {
				ArrayList<String> enchants = new ArrayList<String>();

				for (Enchantment enchant : ((EnchantRandomly)function).enchantments) {
					enchants.add(ObjectHelper.getEnchantmentName(enchant, 0));
				}

				builder.append("This ").append(target).append(" will be enchanted with:<br/>").append(FormattingHelper.listToString(enchants, false));
			}
			else if (function instanceof EnchantSpecific) {
				Map<Enchantment, Integer> enchants = ((EnchantSpecific)function).getEnchantments();
				ArrayList<String> enchantNames = new ArrayList<String>();

				for (Map.Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
					enchantNames.add(ObjectHelper.getEnchantmentName(enchant.getKey(), enchant.getValue()));
				}

				builder.append("This ").append(target).append(" will be enchanted with:<br/>").append(FormattingHelper.listToString(enchantNames, false));
			}
			else if (function instanceof LimitCount) {
				builder.append("This ").append(target).append(" will have its amount capped to a specific amount");
			}
			else if (function instanceof GrantSkillXp) {
				Skills skill = ((GrantSkillXp)function).getSkill();
				float xp = ((GrantSkillXp)function).getXp();

				builder.append("This ").append(target).append(" will additionally grant ").append(NumberUtil.roundToNthDecimalPlace(xp, 2)).append(" ").append(FormattingHelper.createLinkableText(StringUtil.toTitleCase(skill.toString()), false, false, true)).append(" xp");
			}
			else if (function instanceof Smelt) {
				builder.append("This ").append(target).append(" will be converted into its smelted/cooked version");
			}
		}

		if (builder.length() > 0)
			builder.append(".");

		String[] line = builder.toString().split(", and<br/>");
		StringBuilder reBuilder = new StringBuilder(line[0]);

		for (int i = 1; i < line.length; i++) {
			reBuilder.append("<br/>").append("and will").append(line[i].substring(line[i].indexOf("This " + target + " will") + 10 + target.length()));
		}

		return reBuilder.toString();
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

		//if (entry.item.getRegistryName().getNamespace().equals("minecraft"))
		//	entryBuilder.append("mcw:"); Not redirecting mcw links anymore

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
			if (entry.quality > 0) {
				entryNotesBuilder.append("<br/>").append("Chance is increased with each level of luck");
			}
			else {
				entryNotesBuilder.append("<br/>").append("Chance is decreased with each level of luck");
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

			//if (item.getRegistryName().getNamespace().equals("minecraft"))
			//	entryBuilder.append("mcw:"); Not redirecting mcw links anymore

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
