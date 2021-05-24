package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.common.container.recipe.UpgradeKitRecipe;
import net.tslat.aoa3.common.registration.AoABlocks;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
	public String[] getColumnTitles() {
		return new String[] {"Block", "Ingredients", "Item"};
	}

	@Nullable
	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<ResourceLocation>(2);
		ResourceLocation input = ObjectHelper.getIngredientItemId(this.rawRecipe.get("input"));
		ResourceLocation upgradeKit = ObjectHelper.getIngredientItemId(this.rawRecipe.get("upgrade_kit"));

		if (input != null)
			ingredients.add(input);

		if (upgradeKit != null)
			ingredients.add(upgradeKit);

		return ingredients.isEmpty() ? null : ingredients;
	}

	@Nullable
	@Override
	public ResourceLocation getOutputForLookup() {
		return ObjectHelper.getIngredientItemId(this.rawRecipe.get("result"));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printout != null)
			return this.printout;

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		Pair<String, String> input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("input"));
		Pair<String, String> upgradeKit = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("upgrade_kit"));
		Pair<String, String> output = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("result"));

		this.printout = new String[3];
		this.printout[0] = FormattingHelper.createImageBlock(AoABlocks.DIVINE_STATION.get()) + " " + FormattingHelper.createLinkableItem(AoABlocks.DIVINE_STATION.get(), false, true);
		this.printout[1] = FormattingHelper.createImageBlock(input.getSecond()) + " " + FormattingHelper.createLinkableText(input.getSecond(), false, input.getFirst().equals("minecraft"), !input.getSecond().equals(targetName)) + " + " + FormattingHelper.createImageBlock(upgradeKit.getSecond()) + " " + FormattingHelper.createLinkableText(upgradeKit.getSecond(), false, upgradeKit.getFirst().equals("minecraft"), !upgradeKit.getSecond().equals(targetName));
		this.printout[2] = FormattingHelper.createImageBlock(output.getSecond()) + " " + FormattingHelper.createLinkableText(output.getSecond(), false, output.getFirst().equals("minecraft"), !output.getSecond().equals(targetName));

		return this.printout;
	}
}
