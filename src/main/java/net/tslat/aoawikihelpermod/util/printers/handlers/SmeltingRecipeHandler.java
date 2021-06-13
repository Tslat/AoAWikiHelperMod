package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SmeltingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final IRecipe<?> recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public SmeltingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = recipe;
	}

	@Override
	public String getTableGroup() {
		return "Smelting";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] getColumnTitles() {
		return new String[] {"Item", "Smelt Time (Seconds)", "XP", "Ingredients", "Recipe"};
	}

	@Nullable
	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ResourceLocation id = ObjectHelper.getIngredientItemId(this.rawRecipe.get("ingredient"));

		return id == null ? null : Collections.singletonList(id);
	}

	@Nullable
	@Override
	public ResourceLocation getOutputForLookup() {
		return ObjectHelper.getIngredientItemId(this.rawRecipe.get("result"));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		Pair<String, String> input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("ingredient"));
		Triple<Integer, String, String> output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));
		float xp = JSONUtils.getAsFloat(rawRecipe, "experience", 0);
		int cookingTime = JSONUtils.getAsInt(rawRecipe, "cookingtime", 200);

		String[] printData = new String[5];
		printData[0] = (output.getLeft() > 1 ? output.getLeft() + " " : "") + FormattingHelper.createLinkableText(output.getRight(), output.getLeft() > 1, output.getMiddle().equals("minecraft"), !output.getRight().equals(targetName));
		printData[1] = NumberUtil.roundToNthDecimalPlace(cookingTime / 20f, 2);
		printData[2] = NumberUtil.roundToNthDecimalPlace(xp, 1);
		printData[3] = FormattingHelper.createLinkableText(input.getSecond(), false, input.getFirst().equals("minecraft"), !input.getSecond().equals(targetName));
		printData[4] = WikiTemplateHelper.makeSmeltingTemplate(input.getSecond(), output.getRight());

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
