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
import java.util.List;

public class TESawmillRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;
	private final JsonObject rawRecipe;

	private String[] printout;

	public TESawmillRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
	}

	@Override
	public String getTableGroup() {
		return "TE Sawmill";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] getColumnTitles() {
		return new String[] {"Input", "Energy", "Output"};
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
		if (this.printout != null)
			return this.printout;

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		Pair<String, String> input = ObjectHelper.getIngredientName(JSONUtils.getAsJsonObject(this.rawRecipe, "input"));
		int energy = JSONUtils.getAsInt(this.rawRecipe, "energy", 2000);
		Triple<Integer, String, String> result = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));

		this.printout = new String[3];
		this.printout[0] = FormattingHelper.createImageBlock(input.getSecond()) + " " + FormattingHelper.createLinkableText(input.getSecond(), false, input.getFirst().equals("minecraft"), !input.getSecond().equals(targetName));
		this.printout[1] = String.valueOf(energy);
		this.printout[2] = FormattingHelper.createImageBlock(result.getRight()) + " " + result.getLeft() + " " + FormattingHelper.createLinkableText(result.getRight(), result.getLeft() > 1, result.getMiddle().equals("minecraft"), !result.getRight().equals(targetName));

		return this.printout;
	}
}
