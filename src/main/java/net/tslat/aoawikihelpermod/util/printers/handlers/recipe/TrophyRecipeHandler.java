package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.common.registration.block.AoABlocks;
import net.tslat.aoa3.content.recipe.TrophyRecipe;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TrophyRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;
	private final JsonObject rawRecipe;
	@Nullable
	private final TrophyRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public TrophyRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
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

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		return Collections.singletonList(AoABlocks.TROPHY.getId());
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(AoABlocks.GOLD_TROPHY.getId());
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);
		String trophyName = ObjectHelper.getItemName(AoABlocks.TROPHY.get());

		for (int i = 0; i < 9; i++) {
			ingredientsHandler.addIngredient(trophyName, AdventOfAscension.MOD_ID);
		}

		ingredientsHandler.addOutput(new ItemStack(AoABlocks.GOLD_TROPHY.get()));
		String targetItemName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler.getIngredientsWithSlots(), ingredientsHandler.getOutput(), false);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
