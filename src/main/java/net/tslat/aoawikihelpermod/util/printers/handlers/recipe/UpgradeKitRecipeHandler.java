package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.tslat.aoa3.common.registration.AoABlocks;
import net.tslat.aoa3.content.recipe.UpgradeKitRecipe;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class UpgradeKitRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final UpgradeKitRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public UpgradeKitRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
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

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<ResourceLocation>(2);
		ResourceLocation input = ObjectHelper.getIngredientItemId(this.rawRecipe.get("input"));
		ResourceLocation upgradeKit = ObjectHelper.getIngredientItemId(this.rawRecipe.get("upgrade_kit"));

		if (input != null)
			ingredients.add(input);

		if (upgradeKit != null)
			ingredients.add(upgradeKit);

		return ingredients.isEmpty() ? Collections.emptyList() : ingredients;
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(ObjectHelper.getIngredientItemId(this.rawRecipe.get("result")));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		PrintableIngredient input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("input"));
		PrintableIngredient upgradeKit = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("upgrade_kit"));
		PrintableIngredient output = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("result"));

		String[] printData = new String[3];
		printData[0] = FormattingHelper.createImageBlock(AoABlocks.DIVINE_STATION.get()) + " " + FormattingHelper.createLinkableItem(AoABlocks.DIVINE_STATION.get(), false, true);
		printData[1] = FormattingHelper.createImageBlock(input.formattedName) + " " + FormattingHelper.createLinkableText(input.formattedName, false, !input.matches(targetName)) + " + " + FormattingHelper.createImageBlock(upgradeKit.formattedName) + " " + FormattingHelper.createLinkableText(upgradeKit.formattedName, false, !upgradeKit.matches(targetName));
		printData[2] = FormattingHelper.createImageBlock(output.formattedName) + " " + FormattingHelper.createLinkableText(output.formattedName, false, !output.matches(targetName));

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
