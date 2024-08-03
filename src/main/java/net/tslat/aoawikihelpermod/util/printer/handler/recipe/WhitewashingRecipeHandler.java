package net.tslat.aoawikihelpermod.util.printer.handler.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.tslat.aoa3.common.registration.block.AoABlocks;
import net.tslat.aoa3.content.recipe.WhitewashingRecipe;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class WhitewashingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final WhitewashingRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public WhitewashingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (WhitewashingRecipe)recipe;
	}

	@Override
	public String getTableGroup() {
		return "Whitewashing";
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
		ResourceLocation washingMaterial = ObjectHelper.getIngredientItemId(this.rawRecipe.get("washing_material"));

		if (input != null)
			ingredients.add(input);

		if (washingMaterial != null)
			ingredients.add(washingMaterial);

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
		PrintableIngredient washingMaterial = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("washing_material"));
		PrintableIngredient output = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("result"));

		String[] printData = new String[3];
		printData[0] = FormattingHelper.createImageBlock(AoABlocks.WHITEWASHING_TABLE.get()) + " " + FormattingHelper.createLinkableItem(AoABlocks.WHITEWASHING_TABLE.get(), false, true);
		printData[1] = FormattingHelper.createImageBlock(input.formattedName) + " " + FormattingHelper.createLinkableText(input.formattedName, false, !input.matches(targetName)) + " + " + FormattingHelper.createImageBlock(washingMaterial.formattedName) + " " + FormattingHelper.createLinkableText(washingMaterial.formattedName, false, !washingMaterial.matches(targetName));
		printData[2] = FormattingHelper.createImageBlock(output.formattedName) + " " + FormattingHelper.createLinkableText(output.formattedName, false, !output.matches(targetName));

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
