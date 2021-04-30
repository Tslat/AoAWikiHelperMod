package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class SmeltingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final IRecipe<?> recipe;

	public SmeltingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = recipe;
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		return new String[0]; // TODO
	}

	@Override
	public String getTableGroup() {
		return "Smelting";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}
}
