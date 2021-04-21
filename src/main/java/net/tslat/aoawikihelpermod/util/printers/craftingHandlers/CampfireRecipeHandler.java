package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nullable;

public class CampfireRecipeHandler extends RecipePrintHandler {
	private final JsonObject rawRecipe;
	private final CampfireCookingRecipe recipe;

	private String[] printout = null;

	public CampfireRecipeHandler(JsonObject rawRecipe, IRecipe<?> recipe) {
		this.rawRecipe = rawRecipe;
		this.recipe = (CampfireCookingRecipe)recipe;
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
		String campfireName = FormattingHelper.createLinkableItem(Blocks.CAMPFIRE, false, true);
		this.printout = new String[] {
				inputName + " can be cooked on a " +
						campfireName +
						" to produce " +
						recipe.getResultItem().getCount() +
						outputName +
						" and " +
						NumberUtil.roundToNthDecimalPlace(recipe.getExperience(), 1) +
						"xp, taking " +
						NumberUtil.roundToNthDecimalPlace(recipe.getCookingTime() / 20f, 2) +
						" seconds."
		};

		return printout;
	}

	@Override
	public String getTableGroup() {
		return "Campfire";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipe.getId();
	}
}
