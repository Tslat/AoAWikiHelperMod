package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.common.container.recipe.TrophyRecipe;
import net.tslat.aoa3.common.registration.AoABlocks;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrophyRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;
	private final JsonObject rawRecipe;
	@Nullable
	private final TrophyRecipe recipe;

	private String[] printout;

	public TrophyRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (TrophyRecipe)recipe;
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
		return Collections.singletonList(AoABlocks.TROPHY.getId());
	}

	@Nullable
	@Override
	public ResourceLocation getOutputForLookup() {
		return AoABlocks.GOLD_TROPHY.getId();
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printout != null)
			return this.printout;

		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);
		String trophyName = ObjectHelper.getItemName(AoABlocks.TROPHY.get());

		for (int i = 0; i < 9; i++) {
			ingredientsHandler.addIngredient(trophyName, AdventOfAscension.MOD_ID);
		}

		ingredientsHandler.addOutput(new ItemStack(AoABlocks.GOLD_TROPHY.get()));
		String targetItemName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);

		this.printout = new String[3];
		this.printout[0] = ingredientsHandler.getFormattedOutput(targetItem);
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

		lines[lines.length - 1] = "output=" + ingredientsHandler.getOutput().getRight();

		return WikiTemplateHelper.makeWikiTemplateObject("Crafting", lines);
	}
}
