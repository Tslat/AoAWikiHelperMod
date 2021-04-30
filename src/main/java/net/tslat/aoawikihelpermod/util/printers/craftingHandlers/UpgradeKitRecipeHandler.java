package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.common.container.recipe.UpgradeKitRecipe;
import net.tslat.aoa3.common.registration.AoABlocks;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nullable;

public class UpgradeKitRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final UpgradeKitRecipe recipe;

	private String[] printout = null;

	public UpgradeKitRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (UpgradeKitRecipe)recipe;
	}

	@Override
	public String getTableGroup() {
		return "Divine Station";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printout != null)
			return this.printout;
		// TODO convert to raw recipe
		this.printout = new String[3];
		Item input = recipe.getIngredients().get(0).getItems()[0].getItem();
		Item upgradeItem = recipe.getIngredients().get(1).getItems()[1].getItem();
		Item output = recipe.getResultItem().getItem();

		this.printout[0] = FormattingHelper.createImageBlock(AoABlocks.DIVINE_STATION.get()) + " " + FormattingHelper.createLinkableItem(AoABlocks.DIVINE_STATION.get(), false, true);
		this.printout[1] = FormattingHelper.createImageBlock(input) + " " + FormattingHelper.createLinkableItem(input, false, input != targetItem) + " + " + FormattingHelper.createImageBlock(upgradeItem) + " " + FormattingHelper.createLinkableItem(upgradeItem, false, upgradeItem != targetItem);
		this.printout[2] = FormattingHelper.createImageBlock(output) + " " + FormattingHelper.createLinkableItem(recipe.getResultItem(), output != targetItem);

		return this.printout;
	}
}
