package net.tslat.aoawikihelpermod.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.printers.TablePrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.awt.*;
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
}
