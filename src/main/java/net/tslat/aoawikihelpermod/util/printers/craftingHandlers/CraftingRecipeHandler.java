package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CraftingRecipeHandler extends RecipePrintHandler {
	private final JsonObject rawRecipe;
	private final ICraftingRecipe recipe;

	private final RecipeIngredientsHandler ingredientsHandler;

	private String[] printout = null;

	public CraftingRecipeHandler(JsonObject rawRecipe, IRecipe<?> recipe) {
		this.rawRecipe = rawRecipe;
		this.recipe = (ICraftingRecipe)recipe;
		this.ingredientsHandler = new RecipeIngredientsHandler(this.recipe.getIngredients().size());
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		return recipe.getSerializer() == IRecipeSerializer.SHAPED_RECIPE ? compileShapedRecipe(targetItem) : compileShapelessRecipe(targetItem);
	}

	private String[] compileShapelessRecipe(@Nullable Item targetItem) {
		if (this.printout != null)
			return this.printout;

		for (JsonElement ele : JSONUtils.getAsJsonArray(rawRecipe, "ingredients")) {
			ingredientsHandler.addIngredient(ele.getAsJsonObject());
		}

		ingredientsHandler.addOutput(rawRecipe.getAsJsonObject("result"));

		this.printout = new String[3];
		this.printout[0] = ingredientsHandler.getOutputFormatted(targetItem);
		this.printout[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		this.printout[2] = makeCraftingTemplate(targetItem);

		return this.printout;
	}

	private String[] compileShapedRecipe(@Nullable Item targetItem) {
		if (this.printout != null)
			return this.printout;

		HashMap<String, Pair<String, String>> ingredientMap = new HashMap<String, Pair<String, String>>();

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



		this.printout = new String[3];
		this.printout[0] = ingredientsHandler.getOutputFormatted(targetItem);
		this.printout[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		this.printout[2] = makeCraftingTemplate(targetItem);

		return this.printout;
	}

	@Override
	public String getTableGroup() {
		return "Crafting";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipe.getId();
	}

	protected String makeCraftingTemplate(@Nullable Item targetItem) {
		ArrayList<String> slottedIngredients = ingredientsHandler.getIngredientsWithSlots();
		String[] lines = new String[slottedIngredients.size() + 1];

		for (int i = 0; i < slottedIngredients.size(); i++) {
			lines[i] = slottedIngredients.get(i);
		}

		lines[lines.length - 1] = "output=" + ingredientsHandler.getOutputFormatted(targetItem);

		return FormattingHelper.makeWikiTemplateObject("crafting", lines);
	}
}
