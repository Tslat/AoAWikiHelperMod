package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nullable;
import java.util.HashMap;

public class CraftingRecipeHandler extends RecipePrintHandler {
	private final JsonObject rawRecipe;
	private final IRecipe<?> recipe;



	public CraftingRecipeHandler(JsonObject rawRecipe, IRecipe<?> recipe) {
		this.rawRecipe = rawRecipe;
		this.recipe = recipe;
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		return recipe.getSerializer() == IRecipeSerializer.SHAPED_RECIPE ? compileShapedRecipe(targetItem) : compileShapelessRecipe(targetItem);
	}

	private String[] compileShapelessRecipe(@Nullable Item targetItem) {
		ItemStack result = recipe.getResultItem();
		String output = FormattingHelper.bold(FormattingHelper.createLinkableItem(result, result.getItem() != targetItem));
		HashMap<String, Integer> ingredientsMap = new HashMap<String, Integer>();


	}

	private String[] compileShapedRecipe(@Nullable Item targetItem) {

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
