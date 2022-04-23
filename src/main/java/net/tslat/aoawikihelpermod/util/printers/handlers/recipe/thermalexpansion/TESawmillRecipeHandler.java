package net.tslat.aoawikihelpermod.util.printers.handlers.recipe.thermalexpansion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TESawmillRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;
	private final JsonObject rawRecipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public TESawmillRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
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

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		return Collections.singletonList(ObjectHelper.getIngredientItemId(this.rawRecipe.get("ingredient")));
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		if (this.rawRecipe.get("result").isJsonObject()) {
			return Collections.singletonList(ObjectHelper.getIngredientItemId(this.rawRecipe.get("result")));
		}
		else if (this.rawRecipe.get("result").isJsonArray()) {
			ArrayList<ResourceLocation> outputs = new ArrayList<ResourceLocation>();

			for (JsonElement ele : this.rawRecipe.get("result").getAsJsonArray()) {
				outputs.add(ObjectHelper.getIngredientItemId(ele));
			}

			return outputs;
		}
		else {
			return Collections.emptyList();
		}
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		PrintableIngredient input = ObjectHelper.getIngredientName(GsonHelper.getAsJsonObject(this.rawRecipe, "input"));
		int energy = GsonHelper.getAsInt(this.rawRecipe, "energy", 2000);
		List<PrintableIngredient> result;

		if (this.rawRecipe.get("result").isJsonObject()) {
			result = Collections.singletonList(ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result")));
		}
		else if (this.rawRecipe.get("result").isJsonArray()) {
			JsonArray resultArray = this.rawRecipe.get("result").getAsJsonArray();
			result = new ArrayList<>();

			for (JsonElement ele : resultArray) {
				result.add(ObjectHelper.getStackDetailsFromJson(ele));
			}
		}
		else {
			result = Collections.emptyList();
		}

		StringBuilder resultBuilder = new StringBuilder();

		for (PrintableIngredient ingredient : result) {
			if (resultBuilder.length() > 0)
				resultBuilder.append(" +<br/>");

			resultBuilder.append(FormattingHelper.createImageBlock(ingredient.formattedName))
					.append(" ")
					.append(ingredient.count)
					.append(" ")
					.append(FormattingHelper.createLinkableText(ingredient.formattedName, ingredient.count > 1, ingredient.isVanilla(), !ingredient.matches(targetName)));
		}

		String[] printData = new String[3];
		printData[0] = FormattingHelper.createImageBlock(input.formattedName) + " " + FormattingHelper.createLinkableText(input.formattedName, false, input.isVanilla(), !input.matches(targetName));
		printData[1] = String.valueOf(energy);
		printData[2] = resultBuilder.toString();

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
