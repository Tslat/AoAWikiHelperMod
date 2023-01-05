package net.tslat.aoawikihelpermod.util;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.tslat.aoa3.client.ClientOperations;
import net.tslat.aoa3.content.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.content.item.weapon.bow.BaseBow;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import net.tslat.aoa3.content.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.printers.TablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WikiTemplateHelper {
	public static String tooltip(String text, String tooltip) {
		return makeWikiTemplateObject("tooltip", true, text, tooltip);
	}

	public static String makeWikiTemplateObject(String type, boolean singleLine, String... entries) {
		if (singleLine) {
			StringBuilder builder = new StringBuilder("{{");

			builder.append(type);

			for (String str : entries) {
				builder.append("|");
				builder.append(str);
			}

			builder.append("}}");

			return builder.toString();
		}
		else {
			ArrayList<String> lines = new ArrayList<String>();

			lines.add("{{" + type);


			for (String str : entries) {
				lines.add("|" + str);
			}

			lines.add("}}");

			return TablePrintHelper.combineLines(lines);
		}
	}

	public static String makeCraftingTemplate(ArrayList<String> ingredients, RecipePrintHandler.PrintableIngredient output, boolean shapeless) {
		int extraLines = 1;

		if (shapeless)
			extraLines++;

		if (output.count > 1)
			extraLines++;

		String[] lines = new String[ingredients.size() + extraLines];
		int i = 0;

		while (i < ingredients.size()) {
			lines[i] = ingredients.get(i);
			i++;
		}

		lines[i] = "output=" + output.formattedName;

		if (output.count > 1)
			lines[++i] = "amount=" + output.count;

		if (shapeless)
			lines[++i] = "shapeless=1";

		return makeWikiTemplateObject("Crafting", false, lines);
	}

	public static String makeSmeltingTemplate(String input, String output) {
		return makeSmeltingTemplate(input, null, output, null);
	}

	public static String makeSmeltingTemplate(String input, @Nullable String inputImage, String output, @Nullable String outputImage) {
		List<String> lines = new ArrayList<>();

		lines.add("input=" + input);

		if (inputImage != null)
			lines.add("inputimage=" + inputImage);

		lines.add("output=" + output);

		if (outputImage != null)
			lines.add("outputimage=" + outputImage);

		return makeWikiTemplateObject("Smelting", false, lines.toArray(new String[0]));
	}

	public static String makeInfusionTemplate(ArrayList<String> ingredients, RecipePrintHandler.PrintableIngredient input, RecipePrintHandler.PrintableIngredient output) {
		int lineCountMod = input.count == 0 ? 2 : 3;

		if (output.count > 1)
			lineCountMod++;

		if (input.imageName != null)
			lineCountMod++;

		String[] lines = new String[ingredients.size() + lineCountMod];
		int i = 0;

		if (input != RecipePrintHandler.PrintableIngredient.EMPTY) {
			if (input.imageName != null) {
				lines[i++] = "inputimage=" + input.imageName;
				lines[i++] = "input=" + input.imageName.substring(0, input.imageName.lastIndexOf("."));
			}
			else {
				lines[i++] = "input=" + input.formattedName;
			}
		}

		for (String ingredient : ingredients) {
			lines[i] = ingredient;
			i++;
		}

		i = output.count > 1 ? 3 : 2;

		lines[lines.length - i--] = "output=" + (output.formattedName.isEmpty() ? "Air" : output.formattedName);

		if (output.count > 1)
			lines[lines.length - i--] = "amount=" + output.count;

		lines[lines.length - i] = "shapeless=1";

		return makeWikiTemplateObject("Infusion", false, lines);
	}

	public static String makeBlockInfoboxTemplate(Block block) {
		List<String> params = new ObjectArrayList<>();
		Item item = Item.byBlock(block);
		ItemStack stack = item == Items.AIR ? null : new ItemStack(item);

		params.add("name=" + ObjectHelper.getBlockName(block));
		params.add("image=" + ObjectHelper.getBlockName(block) + ".png");
		params.add("id=" + RegistryUtil.getId(block));
		params.add("hardness=" + block.defaultDestroyTime());
		params.add("blastresistance=" + block.getExplosionResistance());
		params.add("transparent=" + ClientHelper.isRenderTransparent(block));
		params.add("flammable=" + ObjectHelper.getBlockFlammability(block));

		String luminance = ObjectHelper.getBlockLuminosity(block);

		if (luminance != null)
			params.add("luminance=" + luminance);

		String harvestLevel = ObjectHelper.getBlockHarvestTag(block);

		if (harvestLevel != null)
			params.add("harvestlevel=" + harvestLevel);

		String toolType = ObjectHelper.getBlockToolTag(block);

		if (toolType != null)
			params.add("tool=" + toolType);

		if (stack != null) {
			params.add("stackable=" + (stack.isStackable() ? "Yes (" + stack.getMaxStackSize() + ")" : "No"));
			params.add("raritycolor=" + StringUtil.toTitleCase(stack.getRarity().toString()));
		}

		params.add("versionadded=");

		return makeWikiTemplateObject("BlockInfo", false, params.toArray(new String[0]));
	}

	public static String makeItemInfoboxTemplate(Item item) {
		List<String> params = new ObjectArrayList<>();
		ItemStack stack = new ItemStack(item);

		params.add("name=" + ObjectHelper.getItemName(item));
		params.add("image=" + ObjectHelper.getItemName(item) + ".png");
		params.add("id=" + RegistryUtil.getId(item));

		String ammo = ObjectHelper.getItemAmmoType(item);

		if (ammo != null)
			params.add("ammo=" + ammo);

		Multimap<Attribute, AttributeModifier> attributes = ObjectHelper.getAttributesForItem(item);

		if (attributes.containsKey(Attributes.ATTACK_DAMAGE))
			params.add("damage=" + NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ATTACK_DAMAGE, attributes.values()), 2));

		if (attributes.containsKey(Attributes.ATTACK_SPEED)) {
			if (attributes.containsKey(Attributes.ATTACK_DAMAGE)) {
				params.add("attackspeed=" + NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ATTACK_SPEED, attributes.values()) + 4, 2) + "/sec");
			}
			else {
				params.add("unholstertime=" + NumberUtil.roundToNthDecimalPlace(1 / ((float)ObjectHelper.getAttributeValue(Attributes.ATTACK_SPEED, attributes.values()) + 4), 2) + "s");
			}
		}

		if (attributes.containsKey(Attributes.ATTACK_KNOCKBACK))
			params.add("knockback=" + NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ATTACK_KNOCKBACK, attributes.values()), 2));

		if (attributes.containsKey(Attributes.ARMOR))
			params.add("armor=" + NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ARMOR, attributes.values()), 2));

		if (attributes.containsKey(Attributes.ARMOR))
			params.add("armortoughness=" + NumberUtil.roundToNthDecimalPlace((float)ObjectHelper.getAttributeValue(Attributes.ARMOR_TOUGHNESS, attributes.values()), 2));

		if (stack.isStackable()) {
			params.add("stackable=Yes (" + stack.getMaxStackSize() + ")");
		}
		else {
			params.add("durability=" + stack.getMaxDamage());
			params.add("stackable=No");
		}

		FoodProperties foodProperties = stack.getFoodProperties(ClientOperations.getPlayer());

		if (foodProperties != null) {
			params.add("hunger=" + foodProperties.getNutrition());
			params.add("saturation=Up to " + foodProperties.getSaturationModifier() * foodProperties.getNutrition() + " (" + NumberUtil.roundToNthDecimalPlace(foodProperties.getSaturationModifier(), 2) + ")");
		}

		if (item instanceof TieredItem tieredItem) {
			Tier tier = tieredItem.getTier();

			params.add("efficiency=" + NumberUtil.roundToNthDecimalPlace(tier.getSpeed(), 2));

			if (tier.getTag() != null)
				params.add("harvestlevel=" + tier.getTag().location().toString());
		}

		if (item instanceof BaseGun gun && !(gun instanceof BaseThrownWeapon)) {
			params.add("firerate=" + NumberUtil.roundToNthDecimalPlace(20f / gun.getFiringDelay(), 2) + "/sec");
			params.add("firetype=" + (gun.isFullAutomatic() ? "Fully-Automatic" : "Semi-Automatic"));
		}
		else if (item instanceof BaseBlaster blaster) {
			params.add("firerate=" + NumberUtil.roundToNthDecimalPlace(20f / blaster.getFiringDelay(), 2) + "/sec");
			params.add("firetype=Fully-Automatic");
		}
		else if (item instanceof BaseBow bow) {
			params.add("drawspeed=" + NumberUtil.roundToNthDecimalPlace(1f / bow.getDrawSpeedMultiplier(), 2) + "/s");
		}

		params.add("raritycolor=" + StringUtil.toTitleCase(stack.getRarity().toString()));

		return makeWikiTemplateObject("ItemInfo", false, params.toArray(new String[0]));
	}
}
