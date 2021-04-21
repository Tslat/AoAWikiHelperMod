package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.common.container.recipe.TrophyRecipe;

import javax.annotation.Nullable;

public class TrophyRecipeHandler extends RecipePrintHandler {
	private final JsonObject rawRecipe;
	private final TrophyRecipe recipe;

	private String[] printout;

	public TrophyRecipeHandler(JsonObject rawRecipe, IRecipe<?> recipe) {
		this.rawRecipe = rawRecipe;
		this.recipe = (TrophyRecipe)recipe;
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (printout != null)
			return printout;



		return printout;
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
