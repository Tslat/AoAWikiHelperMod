package net.tslat.aoawikihelpermod.util;

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

	public static String makeInfusionTemplate(ArrayList<String> ingredients, String input, RecipePrintHandler.PrintableIngredient output) {
		int lineCountMod = input.isEmpty() ? 2 : 3;

		if (output.count > 1)
			lineCountMod++;

		String[] lines = new String[ingredients.size() + lineCountMod];
		int i = 0;

		if (!input.isEmpty()) {
			i = 1;
			lines[0] = "input=" + input;
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
}
