package net.tslat.aoawikihelpermod.util.printers.handlers.recipe.thermalexpansion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
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

public class TETreeExtractorRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;
	private final JsonObject rawRecipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public TETreeExtractorRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
	}

	@Override
	public String getTableGroup() {
		return "TE Tree Extracting";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] getColumnTitles() {
		return new String[] {"Trunk", "Leaves", "Output"};
	}

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<ResourceLocation>();

		if (this.rawRecipe.has("trunk"))
			ingredients.add(ObjectHelper.getIngredientItemId(this.rawRecipe.get("trunk")));

		if (this.rawRecipe.has("leaves"))
			ingredients.add(ObjectHelper.getIngredientItemId(this.rawRecipe.get("leaves")));

		return ingredients.isEmpty() ? Collections.emptyList() : ingredients;
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		JsonElement result = this.rawRecipe.get("result");

		if (!result.isJsonObject() || !result.getAsJsonObject().has("item"))
			return Collections.emptyList();

		return Collections.singletonList(ObjectHelper.getIngredientItemId(result.getAsJsonObject().get("item")));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		PrintableIngredient trunk = null;
		PrintableIngredient leaves = null;
		JsonElement result = this.rawRecipe.get("result");
		String output = "";

		if (this.rawRecipe.has("trunk")) {
			JsonElement element = this.rawRecipe.get("trunk");

			if (element.isJsonObject()) {
				trunk = ObjectHelper.getIngredientName(element.getAsJsonObject());
			}
			else {
				trunk = ObjectHelper.getFormattedItemDetails(new ResourceLocation(this.rawRecipe.get("trunk").getAsString()));
			}
		}

		if (this.rawRecipe.has("leaves")) {
			JsonElement element = this.rawRecipe.get("leaves");

			if (element.isJsonObject()) {
				leaves = ObjectHelper.getIngredientName(element.getAsJsonObject());
			}
			else {
				leaves = ObjectHelper.getFormattedItemDetails(new ResourceLocation(this.rawRecipe.get("leaves").getAsString()));
			}
		}

		if (result.isJsonObject()) {
			JsonObject resultObj = result.getAsJsonObject();

			if (resultObj.has("amount"))
				output = resultObj.get("amount").getAsInt() + " ";

			output = output + ObjectHelper.getFormattedItemDetails(new ResourceLocation(resultObj.get("fluid").getAsString())).formattedName;
		}
		else {
			output = 1000 + ObjectHelper.getFormattedItemDetails(new ResourceLocation(result.getAsString())).formattedName;
		}

		String[] printData = new String[3];
		printData[0] = trunk == null ? "" : FormattingHelper.createImageBlock(trunk.formattedName) + " " + FormattingHelper.createLinkableText(trunk.formattedName, false, trunk.isVanilla(), !trunk.matches(targetName));
		printData[1] = leaves == null ? "" : FormattingHelper.createImageBlock(leaves.formattedName) + " " + FormattingHelper.createLinkableText(leaves.formattedName, false, leaves.isVanilla(), !leaves.matches(targetName));
		printData[2] = output;

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
