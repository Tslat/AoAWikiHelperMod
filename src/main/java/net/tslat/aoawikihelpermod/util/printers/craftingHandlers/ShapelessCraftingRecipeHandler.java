package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ShapelessCraftingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final ICraftingRecipe recipe;

	private String[] printout = null;

	public ShapelessCraftingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (ICraftingRecipe)recipe;
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

		JsonArray ingredients = JSONUtils.getAsJsonArray(rawRecipe, "ingredients");
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(ingredients.size());

		for (JsonElement ele : ingredients) {
			ingredientsHandler.addIngredient(ele.getAsJsonObject());
		}

		ingredientsHandler.addOutput(rawRecipe.getAsJsonObject("result"));

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
