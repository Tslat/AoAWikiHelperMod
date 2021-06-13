package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShapelessCraftingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final ICraftingRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

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
	public String[] getColumnTitles() {
		return new String[] {"Item", "Ingredients", "Recipe"};
	}

	@Nullable
	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<ResourceLocation>();

		for (JsonElement element : JSONUtils.getAsJsonArray(rawRecipe, "ingredients")) {
			ResourceLocation id = ObjectHelper.getIngredientItemId(element);

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

		JsonArray ingredients = JSONUtils.getAsJsonArray(rawRecipe, "ingredients");
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(ingredients.size());

		for (JsonElement ele : ingredients) {
			ingredientsHandler.addIngredient(ele);
		}

		ingredientsHandler.addOutput(rawRecipe.getAsJsonObject("result"));

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler.getIngredientsWithSlots(), ingredientsHandler.getOutput(), true);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
