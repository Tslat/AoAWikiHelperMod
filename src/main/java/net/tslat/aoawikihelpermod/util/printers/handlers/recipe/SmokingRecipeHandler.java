package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SmokingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final SmokingRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public SmokingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (SmokingRecipe)recipe;
	}

	@Override
	public String getTableGroup() {
		return "Smoking";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public boolean isPlainTextPrintout() {
		return true;
	}

	@Override
	public String[] getColumnTitles() {
		return null;
	}

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ResourceLocation id = ObjectHelper.getIngredientItemId(this.rawRecipe.get("ingredient"));

		return id == null ? Collections.emptyList() : Collections.singletonList(id);
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
		Pair<String, String> input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("ingredient"));
		Triple<Integer, String, String> output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));
		float xp = JSONUtils.getAsFloat(rawRecipe, "experience", 0);
		int cookingTime = JSONUtils.getAsInt(rawRecipe, "cookingtime", 100);

		String[] printData = new String[] {
				FormattingHelper.createLinkableText(input.getSecond(), true, input.getFirst().equals("minecraft"), !input.getSecond().equals(targetName)) +
						" can be processed in a " +
						FormattingHelper.createLinkableItem(Blocks.SMOKER, false, true) +
						" to produce " +
						output.getLeft() +
						" " +
						FormattingHelper.createLinkableText(output.getRight(), output.getLeft() > 1, output.getMiddle().equals("minecraft"), !output.getRight().equals(targetName)) +
						" and " +
						NumberUtil.roundToNthDecimalPlace(xp, 1) +
						"xp, taking " +
						NumberUtil.roundToNthDecimalPlace(cookingTime / 20f, 2) +
						" seconds."
		};

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
