package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SmeltingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final Recipe<?> recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public SmeltingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
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
		PrintableIngredient input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("ingredient"));
		PrintableIngredient output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));
		float xp = GsonHelper.getAsFloat(rawRecipe, "experience", 0);
		int cookingTime = GsonHelper.getAsInt(rawRecipe, "cookingtime", 200);

		String[] printData = new String[5];
		printData[0] = (output.count > 1 ? output.count + " " : "") + FormattingHelper.createLinkableText(output.formattedName, output.count > 1, !output.matches(targetName));
		printData[1] = NumberUtil.roundToNthDecimalPlace(cookingTime / 20f, 2);
		printData[2] = NumberUtil.roundToNthDecimalPlace(xp, 1);
		printData[3] = FormattingHelper.createLinkableText(input.formattedName, false, !input.matches(targetName));
		printData[4] = WikiTemplateHelper.makeSmeltingTemplate(input.formattedName, output.formattedName);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
