package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.common.registration.AoAAttributes;
import net.tslat.aoa3.content.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.content.item.weapon.bow.BaseBow;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import net.tslat.aoa3.content.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printer.TablePrintHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.List;

public class WikiTemplateHelper {
	public static class Template {
		private final String name;
		private List<Pair<String, String>> entries = new ObjectArrayList<>();

		public Template(String name) {
			this.name = name;
		}

		public Template entry(String key, String value) {
			this.entries.add(Pair.of(key, value));

			return this;
		}

		public Template optionalEntry(String key, @Nullable String value) {
			if (value == null || value.isEmpty())
				return this;

			return entry(key, value);
		}

		public String[] getLines() {
			String[] lines = new String[this.entries.size() + 2];

			lines[0] = "{{" + this.name;

			for (int i = 0; i < this.entries.size(); i++) {
				Pair<String, String> entry = this.entries.get(i);

				lines[i + 1] = "|" + entry.getFirst() + "=" + entry.getSecond();
			}

			lines[lines.length - 1] = "}}";

			return lines;
		}

		public String getPrintout() {
			return TablePrintHelper.combineLines(getLines());
		}
	}

	public static String tooltip(String text, String tooltip) {
		return makeInlineTemplate("tooltip", true, text, tooltip);
	}

	public static String makeInlineTemplate(String type, boolean singleLine, String... entries) {
		StringBuilder builder = new StringBuilder("{{");

		builder.append(type);

		for (String str : entries) {
			builder.append("|");
			builder.append(str);
		}

		builder.append("}}");

		return builder.toString();
	}

	public static String makeCraftingTemplate(RecipePrintHandler.RecipeIngredientsHandler ingredientsHandler, boolean shapeless) {
		Template template = new Template("Crafting");
		RecipePrintHandler.PrintableIngredient output = ingredientsHandler.getOutput();

		ingredientsHandler.addIngredientsToWikiTemplate(template);
		template.entry("output", output.formattedName)
				.optionalEntry("amount", output.count <= 1 ? null : String.valueOf(output.count))
				.optionalEntry("shapeless", shapeless ? null : "1");

		return template.getPrintout();
	}

	public static String makeSmeltingTemplate(RecipePrintHandler.PrintableIngredient input, RecipePrintHandler.PrintableIngredient output) {
		Template template = new Template("Smelting");

		template.entry("input", input.formattedName)
				.optionalEntry("inputimage", input.imageName)
				.entry("output", output.formattedName)
				.optionalEntry("outputimage", output.imageName);

		return template.getPrintout();
	}

	public static String makeInfusionTemplate(RecipePrintHandler.RecipeIngredientsHandler ingredientsHandler, RecipePrintHandler.PrintableIngredient input) {
		Template template = new Template("Infusion");
		RecipePrintHandler.PrintableIngredient output = ingredientsHandler.getOutput();

		if (input != RecipePrintHandler.PrintableIngredient.EMPTY) {
			if (input.imageName != null) {
				template.entry("inputimage", input.imageName)
						.entry("input", input.imageName.substring(0, input.imageName.lastIndexOf(".")));
			}
			else {
				template.entry("input", input.formattedName);
			}
		}

		ingredientsHandler.addIngredientsToWikiTemplate(template);
		template.entry("output", output == RecipePrintHandler.PrintableIngredient.EMPTY ? "Air" : output.formattedName)
				.optionalEntry("amount", output.count <= 1 ? null : String.valueOf(output.count))
				.entry("shapeless", "1");

		return template.getPrintout();
	}

	public static Template makeBlockInfoboxTemplate(Block block, Level level) {
		Template template = new Template("BlockInfo");
		ItemStack stack = block.asItem().getDefaultInstance();

		return template.entry("name", ObjectHelper.getBlockName(block))
				.entry("image", ObjectHelper.getBlockName(block) + ".png")
				.entry("imgsize", "150px")
				.entry("id", RegistryUtil.getId(block).toString())
				.entry("hardness", NumberUtil.roundToNthDecimalPlace(block.defaultDestroyTime(), 1))
				.entry("blastresistance", NumberUtil.roundToNthDecimalPlace(block.getExplosionResistance(), 1))
				.entry("transparent", ClientHelper.isRenderTransparent(block).toString())
				.entry("flammable", ObjectHelper.getBlockFlammability(block))
				.optionalEntry("luminance", ObjectHelper.getBlockLuminosity(block))
				.optionalEntry("harvestlevel", ObjectHelper.getBlockHarvestTag(block, level))
				.optionalEntry("tool", ObjectHelper.getBlockToolTag(block, level))
				.optionalEntry("stackable", stack.isEmpty() ? null : (stack.isStackable() ? "Yes (" + stack.getMaxStackSize() + ")" : "No"))
				.optionalEntry("raritycolor", stack.isEmpty() ? null : StringUtil.toTitleCase(stack.getRarity().name()))
				.entry("versionadded", "");
	}

	public static Template makeItemInfoboxTemplate(Item item) {
		Template template = new Template("ItemInfo");
		Multimap<Attribute, AttributeModifier> attributes = ObjectHelper.getAttributesForItem(item);
		ItemStack stack = item.getDefaultInstance();
		FoodProperties foodProperties = stack.getFoodProperties(null);

		template.entry("name", ObjectHelper.getItemName(item))
				.entry("image", ObjectHelper.getItemName(item) + ".png")
				.entry("id", RegistryUtil.getId(item).toString())
				.optionalEntry("ammo", ObjectHelper.getItemAmmoType(item))
				.optionalEntry("damage", !attributes.containsKey(Attributes.ATTACK_DAMAGE) ? null : NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ATTACK_DAMAGE, attributes.values()), 2))
				.optionalEntry("attackspeed", !attributes.containsKey(Attributes.ATTACK_SPEED) || !attributes.containsKey(Attributes.ATTACK_DAMAGE) ? null : NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ATTACK_SPEED, attributes.values()) + 4, 2) + "/sec")
				.optionalEntry("unholstertime", !attributes.containsKey(Attributes.ATTACK_SPEED) || attributes.containsKey(Attributes.ATTACK_DAMAGE) ? null : NumberUtil.roundToNthDecimalPlace(1 / ((float)ObjectHelper.getAttributeValue(Attributes.ATTACK_SPEED, attributes.values()) + 4), 2) + "s")
				.optionalEntry("knockback", !attributes.containsKey(Attributes.ATTACK_KNOCKBACK) ? null : NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ATTACK_KNOCKBACK, attributes.values()), 2))
				.optionalEntry("armor", !attributes.containsKey(Attributes.ARMOR) ? null : NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ARMOR, attributes.values()), 2))
				.optionalEntry("armortoughness", !attributes.containsKey(Attributes.ARMOR_TOUGHNESS) ? null : NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ARMOR_TOUGHNESS, attributes.values()), 2))
				.entry("stackable", stack.isStackable() ? "Yes (" + stack.getMaxStackSize() + ")" : "No")
				.optionalEntry("durability", stack.isStackable() ? null : String.valueOf(stack.getMaxDamage()))
				.optionalEntry("hunger", foodProperties == null ? null : NumberUtil.roundToNthDecimalPlace(foodProperties.getNutrition(), 1))
				.optionalEntry("saturation", foodProperties == null ? null : "Up to " + foodProperties.getSaturationModifier() * foodProperties.getNutrition() + " (" + NumberUtil.roundToNthDecimalPlace(foodProperties.getSaturationModifier(), 2) + ")");

		if (item instanceof TieredItem tieredItem) {
			Tier tier = tieredItem.getTier();

			template.entry("efficiency", NumberUtil.roundToNthDecimalPlace(tier.getSpeed(), 2))
					.optionalEntry("harvestlevel", tier.getTag() == null ? null : tier.getTag().location().toString());
		}

		if (item instanceof BaseGun gun && !(gun instanceof BaseThrownWeapon)) {
			template.entry("firerate", NumberUtil.roundToNthDecimalPlace(20f / gun.getFiringDelay(), 2) + "/sec")
					.entry("firetype", gun.isFullAutomatic() ? "Fully-Automatic" : "Semi-Automatic");
		}
		else if (item instanceof BaseBlaster blaster) {
			template.entry("firerate", NumberUtil.roundToNthDecimalPlace(20f / blaster.getFiringDelay(), 2) + "/sec")
					.entry("firetype", "Fully-Automatic");
		}
		else if (item instanceof BaseBow bow) {
			template.entry("drawspeed", NumberUtil.roundToNthDecimalPlace(1f / bow.getDrawSpeedMultiplier(), 2) + "/s");
		}

		template.entry("raritycolor", StringUtil.toTitleCase(stack.getRarity().name()));

		return template;
	}

	public static Template makeEntityInfoboxTemplate(EntityType<?> entity, ServerLevel level) {
		Template template = new Template("EntityInfo");
		Entity instance = entity.create(FakeWorld.INSTANCE.get());
		LivingEntity livingInstance = instance instanceof LivingEntity livingEntity ? livingEntity : null;

		if (instance instanceof Mob mob)
			ForgeEventFactory.onFinalizeSpawn(mob, level, new DifficultyInstance(Difficulty.HARD, 0, 0, 0), MobSpawnType.NATURAL, null, null);

		String meleeStrength = getRoundedAttributeValue(livingInstance, Attributes.ATTACK_DAMAGE);

		template.entry("name", ObjectHelper.getEntityName(entity))
				.entry("image", ObjectHelper.getEntityName(entity) + ".png")
				.entry("noimage", "")
				.entry("image2", "")
				.optionalEntry("health", getRoundedAttributeValue(livingInstance, Attributes.MAX_HEALTH))
				.entry("specialhealth", "")
				.entry("size", "'''Width''': " + entity.getWidth() + " blocks <br> '''Height''': " + entity.getHeight() + " blocks")
				.optionalEntry("damage", meleeStrength != null ? meleeStrength : getRoundedAttributeValue(livingInstance, AoAAttributes.RANGED_ATTACK_DAMAGE.get()))
				.optionalEntry("specialdamage", meleeStrength != null ? getRoundedAttributeValue(livingInstance, AoAAttributes.RANGED_ATTACK_DAMAGE.get()) : null)
				.optionalEntry("armor", getRoundedAttributeValue(livingInstance, Attributes.ARMOR))
				.optionalEntry("armortoughness", getRoundedAttributeValue(livingInstance, Attributes.ARMOR_TOUGHNESS))
				.entry("environment", "")
				.optionalEntry("hostility", instance instanceof Enemy ? "Hostile" : instance instanceof NeutralMob ? "Neutral" : "Passive")
				.optionalEntry("classification", StringUtil.toTitleCase(entity.getCategory().getName()))
				.optionalEntry("xp", livingInstance == null ? null : String.valueOf(livingInstance.getExperienceReward()))
				.optionalEntry("knockbackresist", getRoundedAttributeValue(livingInstance, Attributes.KNOCKBACK_RESISTANCE))
				.entry("id", ForgeRegistries.ENTITY_TYPES.getKey(entity).toString())
				.entry("versionadded", "");

		return template;
	}

	@Nullable
	private static String getRoundedAttributeValue(@Nullable LivingEntity entity, Attribute attribute) {
		if (entity == null)
			return null;

		double value = ObjectHelper.getAttributeFromEntity(entity, attribute);

		return value == 0 ? null : NumberUtil.roundToNthDecimalPlace((float)value, 2);
	}
}
