package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nullable;

public class StonecuttingRecipeHandler extends RecipePrintHandler {
	private final JsonObject rawRecipe;
	private final StonecuttingRecipe recipe;

	private String[] printout = null;

	public StonecuttingRecipeHandler(JsonObject rawRecipe, IRecipe<?> recipe) {
		this.rawRecipe = rawRecipe;
		this.recipe = (StonecuttingRecipe)recipe;
	}

	@Override
	public boolean isPlainTextPrintout() {
		return true;
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (printout != null)
			return printout;

		Item input = recipe.getIngredients().get(0).getItems()[0].getItem();
		String inputName = FormattingHelper.createLinkableItem(input, false, input != targetItem);
		String outputName = FormattingHelper.createLinkableItem(recipe.getResultItem(), recipe.getResultItem().getItem() != targetItem);
		String stonecutterName = FormattingHelper.createLinkableItem(Blocks.STONECUTTER, false, true);
		this.printout = new String[] {
				inputName + " can be processed in a " +
						stonecutterName +
						" to produce " +
						recipe.getResultItem().getCount() +
						outputName +
						"."
		};

		return printout;
	}

	@Override
	public String getTableGroup() {
		return "Stonecutting";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipe.getId();
	}
}
