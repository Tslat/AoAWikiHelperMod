package net.tslat.aoawikihelpermod.util.printers.handlers.thermalexpansion;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TEInsolatorRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;
	private final JsonObject rawRecipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public TEInsolatorRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
	}

	@Override
	public String getTableGroup() {
		return "TE Insolator";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] getColumnTitles() {
		return new String[] {"Input", "Resources", "Output"};
	}

	@Nullable
	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		return Collections.singletonList(ObjectHelper.getIngredientItemId(this.rawRecipe.get("input")));
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
		Pair<String, String> input = ObjectHelper.getIngredientName(JSONUtils.getAsJsonObject(this.rawRecipe, "input"));
		int water = JSONUtils.getAsInt(this.rawRecipe, "water", 500);
		int energy = JSONUtils.getAsInt(this.rawRecipe, "energy", 20000);
		Triple<Integer, String, String> result = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));

		String[] printData = new String[3];
		printData[0] = FormattingHelper.createImageBlock(input.getSecond()) + " " + FormattingHelper.createLinkableText(input.getSecond(), false, input.getFirst().equals("minecraft"), !input.getSecond().equals(targetName));
		printData[1] = water + " Water + " + energy + " Energy";
		printData[2] = FormattingHelper.createImageBlock(result.getRight()) + " " + result.getLeft() + " " + FormattingHelper.createLinkableText(result.getRight(), result.getLeft() > 1, result.getMiddle().equals("minecraft"), !result.getRight().equals(targetName));

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
