package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Blocks;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SmithingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final UpgradeRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<>();

	public SmithingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (UpgradeRecipe)recipe;
	}

	@Override
	public String getTableGroup() {
		return "Smithing";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] getColumnTitles() {
		return null;
	}

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<>(2);
		ResourceLocation base = ObjectHelper.getIngredientItemId(this.rawRecipe.get("base"));
		ResourceLocation addition = ObjectHelper.getIngredientItemId(this.rawRecipe.get("addition"));

		if (base != null)
			ingredients.add(base);

		if (addition != null)
			ingredients.add(addition);

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
		PrintableIngredient input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("base"));
		PrintableIngredient material = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("addition"));
		PrintableIngredient output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));

		String[] printData = new String[3];
		printData[0] = FormattingHelper.createImageBlock(Blocks.SMITHING_TABLE) + " " + FormattingHelper.createLinkableItem(Blocks.SMITHING_TABLE, false, true);
		printData[1] = FormattingHelper.createImageBlock(input.formattedName) + " " + FormattingHelper.createLinkableText(input.formattedName, false, input.isVanilla(), !input.matches(targetName)) + " + " + FormattingHelper.createImageBlock(input.formattedName) + " " + FormattingHelper.createLinkableText(material.formattedName, false, material.isVanilla(), !material.matches(targetName));
		printData[2] = FormattingHelper.createImageBlock(output.formattedName) + " " + FormattingHelper.createLinkableText(output.formattedName, false, output.isVanilla(), !output.matches(targetName));

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
