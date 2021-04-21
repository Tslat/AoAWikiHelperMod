package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class SmithingRecipeHandler extends RecipePrintHandler {
	private final JsonObject rawRecipe;
	private final SmithingRecipe recipe;

	private String[] printout = null;

	public SmithingRecipeHandler(JsonObject rawRecipe, IRecipe<?> recipe) {
		this.rawRecipe = rawRecipe;
		this.recipe = (SmithingRecipe)recipe;
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (printout != null)
			return printout;

		return new String[0];
	}

	@Override
	public String getTableGroup() {
		return "Smithing";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipe.getId();
	}
}
