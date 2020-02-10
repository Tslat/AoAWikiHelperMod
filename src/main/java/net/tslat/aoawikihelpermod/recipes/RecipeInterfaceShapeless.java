package net.tslat.aoawikihelpermod.recipes;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.ArrayList;

public class RecipeInterfaceShapeless extends IRecipeInterface {
	public RecipeInterfaceShapeless(IRecipe recipe, JsonObject json) {
		super(recipe, json);
	}

	@Override
	protected String recipeGroup() {
		return "CraftingShapeless";
	}

	@Override
	protected void populateIngredientArrays() {
		JsonContext context = new JsonContext("aoa3");

		for (JsonElement entry : JsonUtils.getJsonArray(json, "ingredients")) {
			String entryType = context.appendModId(JsonUtils.getString(entry.getAsJsonObject(), "type", "minecraft:item"));
			ItemStack matchingStack = CraftingHelper.getIngredient(entry, context).getMatchingStacks()[0];
			String entryName = matchingStack.getDisplayName();
			String oreDictName = entryType.equals("forge:ore_dict") ? JsonUtils.getString(entry.getAsJsonObject(), "ore") : null;

			if (matchingStack.getItem().getRegistryName().getResourceDomain().equals("minecraft"))
				entryName = "mcw:" + entryName;

			addIngredient(entryName, oreDictName);
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
	protected boolean matchAdditionalIngredients(ItemStack targetStack) {
		return false;
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

		lines.add("|shapeless=1");

		return lines;
	}
}
