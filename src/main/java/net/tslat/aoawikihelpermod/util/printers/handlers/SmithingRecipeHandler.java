package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SmithingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final SmithingRecipe recipe;

	private String[] printout = null;

	public SmithingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (SmithingRecipe)recipe;
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

	@Nullable
	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<ResourceLocation>(2);
		ResourceLocation base = ObjectHelper.getIngredientItemId(this.rawRecipe.get("base"));
		ResourceLocation addition = ObjectHelper.getIngredientItemId(this.rawRecipe.get("addition"));

		if (base != null)
			ingredients.add(base);

		if (addition != null)
			ingredients.add(addition);

		return ingredients.isEmpty() ? null : ingredients;
	}

	@Nullable
	@Override
	public ResourceLocation getOutputForLookup() {
		return ObjectHelper.getIngredientItemId(this.rawRecipe.get("result"));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (printout != null)
			return printout;

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		Pair<String, String> input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("base"));
		Pair<String, String> material = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("addition"));
		Triple<Integer, String, String> output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));

		this.printout = new String[3];
		this.printout[0] = FormattingHelper.createImageBlock(Blocks.SMITHING_TABLE) + " " + FormattingHelper.createLinkableItem(Blocks.SMITHING_TABLE, false, true);
		this.printout[1] = FormattingHelper.createImageBlock(input.getSecond()) + " " + FormattingHelper.createLinkableText(input.getSecond(), false, input.getFirst().equals("minecraft"), !input.getSecond().equals(targetName)) + " + " + FormattingHelper.createImageBlock(input.getSecond()) + " " + FormattingHelper.createLinkableText(material.getSecond(), false, material.getFirst().equals("minecraft"), !material.getSecond().equals(targetName));
		this.printout[2] = FormattingHelper.createImageBlock(output.getRight()) + " " + FormattingHelper.createLinkableText(output.getRight(), false, output.getMiddle().equals("minecraft"), !output.getRight().equals(targetName));

		return printout;
	}
}
