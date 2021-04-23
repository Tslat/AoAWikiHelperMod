package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

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



		return this.printout;
	}

	private String[] compileShapedRecipe(@Nullable Item targetItem) {
		if (this.printout != null)
			return this.printout;



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
}
