package net.tslat.aoawikihelpermod.util.printers.handlers.thermalexpansion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TETreeExtractorRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;
	private final JsonObject rawRecipe;

	private String[] printout;

	public TETreeExtractorRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
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

	@Nullable
	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<ResourceLocation>();

		if (this.rawRecipe.has("trunk"))
			ingredients.add(ObjectHelper.getIngredientItemId(this.rawRecipe.get("trunk")));

		if (this.rawRecipe.has("leaves"))
			ingredients.add(ObjectHelper.getIngredientItemId(this.rawRecipe.get("leaves")));

		return ingredients.isEmpty() ? null : ingredients;
	}

	@Nullable
	@Override
	public ResourceLocation getOutputForLookup() {
		JsonElement result = this.rawRecipe.get("result");

		if (!result.isJsonObject() || !result.getAsJsonObject().has("item"))
			return null;

		return ObjectHelper.getIngredientItemId(result.getAsJsonObject().get("item"));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printout != null)
			return this.printout;

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		Pair<String, String> trunk = null;
		Pair<String, String> leaves = null;
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

			output = output + ObjectHelper.getFormattedItemDetails(new ResourceLocation(resultObj.get("fluid").getAsString())).getSecond();
		}
		else {
			output = 1000 + ObjectHelper.getFormattedItemDetails(new ResourceLocation(result.getAsString())).getSecond();
		}

		this.printout = new String[3];
		this.printout[0] = trunk == null ? "" : FormattingHelper.createImageBlock(trunk.getSecond()) + " " + FormattingHelper.createLinkableText(trunk.getSecond(), false, trunk.getFirst().equals("minecraft"), !trunk.getSecond().equals(targetName));
		this.printout[1] = leaves == null ? "" : FormattingHelper.createImageBlock(leaves.getSecond()) + " " + FormattingHelper.createLinkableText(leaves.getSecond(), false, leaves.getFirst().equals("minecraft"), !leaves.getSecond().equals(targetName));
		this.printout[2] = output;

		return this.printout;
	}
}
