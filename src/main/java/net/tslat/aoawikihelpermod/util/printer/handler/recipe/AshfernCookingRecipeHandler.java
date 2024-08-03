package net.tslat.aoawikihelpermod.util.printer.handler.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.tslat.aoa3.common.registration.item.AoAItems;
import net.tslat.aoa3.content.recipe.AshfernCookingRecipe;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AshfernCookingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;
	private final JsonObject rawRecipe;
	@Nullable
	private final AshfernCookingRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public AshfernCookingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (AshfernCookingRecipe) recipe;
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

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		// List all food?
		return Collections.singletonList(AoAItems.ASHFERN.getId());
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		// List all food?
		return List.of();
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(2);

		ingredientsHandler.addIngredient(ObjectHelper.getFormattedItemDetails(AoAItems.ASHFERN.getId()), 0);
		ingredientsHandler.addIngredient(new PrintableIngredient("", "Any cookable food").setCustomImageName("Raw Beef.png"), 1);
		ingredientsHandler.addOutput(new PrintableIngredient("Cooked version of the food").skipLink().setCustomImageName("Steak.png"));

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getOutput().formattedName;
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler, true);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
