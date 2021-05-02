package net.tslat.aoawikihelpermod.util;

import net.tslat.aoawikihelpermod.util.printers.TablePrintHelper;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;

public class WikiTemplateHelper {
	public static String makeWikiTemplateObject(String type, String... entries) {
		ArrayList<String> lines = new ArrayList<String>();

		lines.add("{{" + type);

		for (String str : entries) {
			lines.add("|" + str);
		}

		lines.add("}}");

		return TablePrintHelper.combineLines(lines);
	}

	public static String makeCraftingTemplate(ArrayList<String> ingredients, Triple<Integer, String, String> output, boolean shapeless) {
		int extraLines = 1;

		if (shapeless)
			extraLines++;

		if (output.getLeft() > 1)
			extraLines++;

		String[] lines = new String[ingredients.size() + extraLines];
		int i = 0;

		while (i < ingredients.size()) {
			lines[i] = ingredients.get(i);
			i++;
		}

		lines[i] = "output=" + output.getRight();

		if (output.getLeft() > 1)
			lines[++i] = "amount=" + output.getLeft();

		if (shapeless)
			lines[++i] = "shapeless=1";

		return makeWikiTemplateObject("Crafting", lines);
	}

	public static String makeSmeltingTemplate(String input, String output) {
		return makeWikiTemplateObject("Smelting", "input=" + input, "output=" + output);
	}

	public static String makeInfusionTemplate(ArrayList<String> ingredients, String input, String output) {
		String[] lines = new String[ingredients.size() + (input.isEmpty() ? 2 : 3)];
		int i = 0;

		if (!input.isEmpty()) {
			i = 1;
			lines[0] = "input=" + input;
		}

		for (String ingredient : ingredients) {
			lines[i] = ingredient;
			i++;
		}

		lines[lines.length - 2] = "output=" + output;
		lines[lines.length - 1] = "shapeless=1";

		return makeWikiTemplateObject("Infusion", lines);
	}
}
