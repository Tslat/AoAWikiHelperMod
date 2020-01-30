package net.tslat.aoawikihelpermod.recipes;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecipeInterfaceShaped extends IRecipeInterface {
	public RecipeInterfaceShaped(IRecipe recipe, JsonObject json) {
		super(recipe, json);
	}

	@Override
	protected void populateIngredientArrays() {
		JsonContext context = new JsonContext("aoa3");

		HashMap<String, IRecipeInterfaceIngredient> interfaceIngredientMap = new HashMap<String, IRecipeInterfaceIngredient>();

		for (Map.Entry<String, JsonElement> keyEntry : JsonUtils.getJsonObject(json, "key").entrySet()) {
			JsonObject value;

			if (keyEntry.getValue().isJsonArray()) {
				value = keyEntry.getValue().getAsJsonArray().get(0).getAsJsonObject();
			}
			else {
				value = keyEntry.getValue().getAsJsonObject();
			}

			String entryType = context.appendModId(JsonUtils.getString(value, "type", "minecraft:item"));
			ItemStack matchingStack = CraftingHelper.getIngredient(value, context).getMatchingStacks()[0];
			String entryName = matchingStack.getDisplayName();
			String oreDictName = entryType.equals("forge:ore_dict") ? JsonUtils.getString(value, "ore") : null;

			if (matchingStack.getItem().getRegistryName().getResourceDomain().equals("minecraft"))
				entryName = "mcw:" + entryName;

			IRecipeInterfaceIngredient interfaceIngredient = new IRecipeInterfaceIngredient(entryName, oreDictName);

			interfaceIngredientMap.put(keyEntry.getKey(), interfaceIngredient);
		}

		JsonArray patternArray = JsonUtils.getJsonArray(json, "pattern");
		int maxWidth = 1;
		int patternIndex = 0;

		ingredientNameBySlot.clear();

		for (int i = 0; i < 9; i++) {
			ingredientNameBySlot.add("");
		}

		for (int i = 0; i < patternArray.size(); i++) {
			String line = patternArray.get(i).getAsString();
			maxWidth = Math.max(maxWidth, line.length());
			char[] charArray = line.toCharArray();

			charArray:
			for (char x : charArray) {
				if (x != ' ') {
					IRecipeInterfaceIngredient ing = interfaceIngredientMap.get(String.valueOf(x));

					ingredientNameBySlot.set(patternIndex, ing.getIngredientName().replace("mcw:", ""));

					for (IRecipeInterfaceIngredient ing2 : ingredientArray) {
						if (ing2.matchingIngredient(ing)) {
							ing2.increment();

							patternIndex++;

							continue charArray;
						}
					}

					ingredientArray.add(ing);
				}

				patternIndex++;
			}

			patternIndex += 3 - line.length();
		}

		if (maxWidth == 1) {
			ingredientNameBySlot.set(7, ingredientNameBySlot.get(6));
			ingredientNameBySlot.set(4, ingredientNameBySlot.get(3));
			ingredientNameBySlot.set(1, ingredientNameBySlot.get(0));

			ingredientNameBySlot.set(6, "");
			ingredientNameBySlot.set(3, "");
			ingredientNameBySlot.set(0, "");
		}
	}

	@Override
	protected String buildIngredientSummaryLine(ItemStack targetStack) {
		StringBuilder builder = new StringBuilder();

		for (IRecipeInterfaceIngredient ing : ingredientArray) {
			String ingredientName = ing.getIngredientName();
			String mcwSafeName = ingredientName.replace("mcw:", "");

			builder.append(" +<br/> ");
			builder.append(ing.getCount());
			builder.append(" ");

			if (ing.getOreDictName() != null) {
				builder.append("{{tooltip|");
				builder.append(mcwSafeName);
				builder.append("|Ore Dictionary: '");
				builder.append(ing.getOreDictName());
				builder.append("'}}");
			}
			else if (!targetStack.getDisplayName().equals(mcwSafeName)) {
				builder.append("[[");

				if (ingredientName.contains("mcw:")) {
					builder.append(ingredientName);
					builder.append("|");
				}

				builder.append(mcwSafeName);

				if (ing.getCount() > 1 && (!ingredientName.endsWith("s") && !ingredientName.endsWith("y"))) {
					String pluralSuffix = ingredientName.endsWith("x") ? "es" : "s";

					if (ingredientName.contains("mcw:")) {
						builder.append("]]");
						builder.append(pluralSuffix);
					}
					else {
						builder.append("|");
						builder.append(mcwSafeName);
						builder.append(pluralSuffix);
						builder.append("]]");
					}
				}
				else {
					builder.append("]]");
				}
			}
			else {
				builder.append(mcwSafeName);

				if (ing.getCount() > 1 && (!ingredientName.endsWith("s") && !ingredientName.endsWith("y")))
					builder.append(ingredientName.endsWith("x") ? "es" : "s");
			}
		}

		return builder.toString().substring(8);
	}

	@Override
	protected String buildWikiTableHeadingsLine(ArrayListMultimap<String, IRecipe> matchedRecipes) {
		return "! Item !! Ingredients !! Recipe";
	}

	@Override
	protected String getWikiTemplateName() {
		return "Crafting";
	}

	@Override
	protected ArrayList<String> buildAdditionalTemplateLines(ItemStack targetStack, boolean printImageLines) {
		ArrayList<String> lines = new ArrayList<String>();
		ItemStack output = recipe.getRecipeOutput();

		for (int i = 0; i < ingredientNameBySlot.size(); i++) {
			String slotName = ingredientNameBySlot.get(i);

			lines.add("|" + slotPrefixes[i] + "=" + slotName);

			if (printImageLines && !slotName.equals(""))
				lines.add("|" + slotPrefixes[i] + "image=" + slotName + ".png");
		}

		lines.add("|output=" + output.getDisplayName());

		if (printImageLines)
			lines.add("|outputimage=" + output.getDisplayName() + ".png");

		if (output.getCount() > 1)
			lines.add("|amount=" + output.getCount());

		return lines;
	}
}
