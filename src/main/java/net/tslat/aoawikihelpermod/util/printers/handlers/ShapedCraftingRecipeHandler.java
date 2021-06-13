package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapedCraftingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final ShapedRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public ShapedCraftingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (ShapedRecipe)recipe;
	}

	@Override
	public String getTableGroup() {
		return "Crafting";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] getColumnTitles() {
		return new String[] {"Item", "Ingredients", "Recipe"};
	}

	@Nullable
	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<ResourceLocation>();

		for (Map.Entry<String, JsonElement> key : JSONUtils.getAsJsonObject(rawRecipe, "key").entrySet()) {
			JsonObject value;

			if (key.getValue().isJsonArray()) {
				value = key.getValue().getAsJsonArray().get(0).getAsJsonObject();
			}
			else {
				value = key.getValue().getAsJsonObject();
			}

			ResourceLocation id = ObjectHelper.getIngredientItemId(value);

			if (id != null)
				ingredients.add(id);
		}

		return ingredients.isEmpty() ? null : ingredients;
	}

	@Nullable
	@Override
	public ResourceLocation getOutputForLookup() {
		return ObjectHelper.getIngredientItemId(this.rawRecipe.get("result"));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		HashMap<String, Pair<String, String>> ingredientMap = new HashMap<String, Pair<String, String>>();

		ingredientMap.put(" ", new Pair<String, String>("", ""));

		for (Map.Entry<String, JsonElement> key : JSONUtils.getAsJsonObject(rawRecipe, "key").entrySet()) {
			JsonObject value;

			if (key.getValue().isJsonArray()) {
				value = key.getValue().getAsJsonArray().get(0).getAsJsonObject();
			}
			else {
				value = key.getValue().getAsJsonObject();
			}

			ingredientMap.put(key.getKey(), ObjectHelper.getIngredientName(value));
		}

		String[] pattern = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(JSONUtils.getAsJsonArray(this.rawRecipe, "pattern")));
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);

		for (int x = 0; x < pattern.length; x++) {
			for (int y = 0; y < pattern[x].length(); y++) {
				String key = pattern[x].substring(y, y + 1);
				Pair<String, String> ingredient = ingredientMap.get(key);

				ingredientsHandler.addIngredient(ingredient.getSecond(), ingredient.getFirst(), y + 3 * x);
			}
		}

		ingredientsHandler.addOutput(rawRecipe.getAsJsonObject("result"));

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler.getIngredientsWithSlots(), ingredientsHandler.getOutput(), false);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
