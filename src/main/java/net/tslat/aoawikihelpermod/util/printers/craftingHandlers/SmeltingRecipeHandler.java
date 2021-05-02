package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

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

public class SmeltingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final IRecipe<?> recipe;

	private String[] printout = null;

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

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		//if (this.printout != null)
		//	return this.printout;

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		Pair<String, String> input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("ingredient"));
		Triple<Integer, String, String> output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));
		float xp = JSONUtils.getAsFloat(rawRecipe, "experience", 0);
		int cookingTime = JSONUtils.getAsInt(rawRecipe, "cookingtime", 200);

		this.printout = new String[5];
		this.printout[0] = (output.getLeft() > 1 ? output.getLeft() + " " : "") + FormattingHelper.createLinkableItem(output.getRight(), output.getLeft() > 1, output.getMiddle().equals("minecraft"), !output.getRight().equals(targetName));
		this.printout[1] = NumberUtil.roundToNthDecimalPlace(cookingTime / 20f, 2);
		this.printout[2] = NumberUtil.roundToNthDecimalPlace(xp, 1);
		this.printout[3] = FormattingHelper.createLinkableItem(input.getSecond(), false, input.getFirst().equals("minecraft"), !input.getSecond().equals(targetName));
		this.printout[4] = WikiTemplateHelper.makeSmeltingTemplate(input.getSecond(), output.getRight());

		return this.printout;
	}
}
