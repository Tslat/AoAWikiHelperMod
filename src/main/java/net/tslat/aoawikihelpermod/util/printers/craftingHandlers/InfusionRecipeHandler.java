package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class InfusionRecipeHandler extends RecipePrintHandler {
	private final JsonObject rawRecipe;
	private final IRecipe<?> recipe;

	public InfusionRecipeHandler(JsonObject rawRecipe, IRecipe<?> recipe) {
		this.rawRecipe = rawRecipe;
		this.recipe = recipe;
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		return new String[0];
	}

	@Override
	public String getTableGroup() {
		return "Infusion";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipe.getId();
	}
}
