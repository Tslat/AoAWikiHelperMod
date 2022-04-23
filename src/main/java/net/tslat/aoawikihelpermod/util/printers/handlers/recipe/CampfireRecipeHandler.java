package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CampfireRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final CampfireCookingRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<>();

	public CampfireRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (CampfireCookingRecipe)recipe;
	}

	@Override
	public String getTableGroup() {
		return "Campfire";
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
		PrintableIngredient input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("ingredient"));
		PrintableIngredient output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));
		float xp = GsonHelper.getAsFloat(rawRecipe, "experience", 0);
		int cookingTime = GsonHelper.getAsInt(rawRecipe, "cookingtime", 100);

		String[] printData = new String[] {
				FormattingHelper.createLinkableText(input.formattedName, true, !input.matches(targetName)) +
						" can be cooked on a " +
						FormattingHelper.createLinkableItem(Blocks.CAMPFIRE, false, true) +
						" to produce " +
						output.count +
						" " +
						FormattingHelper.createLinkableText(output.formattedName, output.count > 1, !output.matches(targetName)) +
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
