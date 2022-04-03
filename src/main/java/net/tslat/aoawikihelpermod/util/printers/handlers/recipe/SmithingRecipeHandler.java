package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Blocks;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;
import org.apache.commons.lang3.tuple.Triple;

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
		Pair<String, String> input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("base"));
		Pair<String, String> material = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("addition"));
		Triple<Integer, String, String> output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));

		String[] printData = new String[3];
		printData[0] = FormattingHelper.createImageBlock(Blocks.SMITHING_TABLE) + " " + FormattingHelper.createLinkableItem(Blocks.SMITHING_TABLE, false, true);
		printData[1] = FormattingHelper.createImageBlock(input.getSecond()) + " " + FormattingHelper.createLinkableText(input.getSecond(), false, input.getFirst().equals("minecraft"), !input.getSecond().equals(targetName)) + " + " + FormattingHelper.createImageBlock(input.getSecond()) + " " + FormattingHelper.createLinkableText(material.getSecond(), false, material.getFirst().equals("minecraft"), !material.getSecond().equals(targetName));
		printData[2] = FormattingHelper.createImageBlock(output.getRight()) + " " + FormattingHelper.createLinkableText(output.getRight(), false, output.getMiddle().equals("minecraft"), !output.getRight().equals(targetName));

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
