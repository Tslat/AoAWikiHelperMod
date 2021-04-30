package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShapedCraftingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final ShapedRecipe recipe;

	private String[] printout = null;

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
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printout != null)
			return this.printout;

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
		int height = pattern.length;
		int width = pattern[0].length();
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(width * height);

		for (int x = 0; x < pattern.length; x++) {
			for (int y = 0; y < pattern[x].length(); y++) {
				String key = pattern[x].substring(y, y + 1);
				Pair<String, String> ingredient = ingredientMap.get(key);

				ingredientsHandler.addIngredient(ingredient.getFirst(), ingredient.getSecond(), y + width * x);
			}
		}

		this.printout = new String[3];
		this.printout[0] = ingredientsHandler.getOutputFormatted(targetItem);
		this.printout[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		this.printout[2] = makeCraftingTemplate(ingredientsHandler, targetItem);

		return this.printout;
	}

	protected String makeCraftingTemplate(RecipeIngredientsHandler ingredientsHandler, @Nullable Item targetItem) {
		ArrayList<String> slottedIngredients = ingredientsHandler.getIngredientsWithSlots();
		String[] lines = new String[slottedIngredients.size() + 1];

		for (int i = 0; i < slottedIngredients.size(); i++) {
			lines[i] = slottedIngredients.get(i);
		}

		lines[lines.length - 1] = "output=" + ingredientsHandler.getOutputFormatted(targetItem);

		return FormattingHelper.makeWikiTemplateObject("crafting", lines);
	}
}
