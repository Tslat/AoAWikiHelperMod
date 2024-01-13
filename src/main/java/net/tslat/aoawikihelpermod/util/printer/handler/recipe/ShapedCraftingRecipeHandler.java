package net.tslat.aoawikihelpermod.util.printer.handler.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.*;

public class ShapedCraftingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final ShapedRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public ShapedCraftingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (ShapedRecipe)recipe;
	}

	@Override
	public String getTableGroup() {
		return "Crafting";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] getColumnTitles() {
		return new String[] {"Item", "Ingredients", "Recipe"};
	}

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<ResourceLocation>();

		for (Map.Entry<String, JsonElement> key : GsonHelper.getAsJsonObject(rawRecipe, "key").entrySet()) {
			JsonObject value;

			if (key.getValue().isJsonArray()) {
				value = key.getValue().getAsJsonArray().get(0).getAsJsonObject();
			}
			else {
				value = key.getValue().getAsJsonObject();
			}

			ResourceLocation id = ObjectHelper.getIngredientItemId(value);

			if (id != null)
				ingredients.add(id);
		}

		return ingredients.isEmpty() ? Collections.emptyList() : ingredients;
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(ObjectHelper.getIngredientItemId(this.rawRecipe.get("result")));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		HashMap<String, PrintableIngredient> ingredientMap = new HashMap<>();

		ingredientMap.put(" ", new PrintableIngredient("", ""));

		for (Map.Entry<String, JsonElement> key : GsonHelper.getAsJsonObject(rawRecipe, "key").entrySet()) {
			JsonObject value;

			if (key.getValue().isJsonArray()) {
				value = key.getValue().getAsJsonArray().get(0).getAsJsonObject();
			}
			else {
				value = key.getValue().getAsJsonObject();
			}

			ingredientMap.put(key.getKey(), ObjectHelper.getIngredientName(value));
		}

		String[] pattern = ShapedRecipePattern.shrink(ShapedRecipePattern.Data.PATTERN_CODEC.decode(JsonOps.INSTANCE, GsonHelper.getAsJsonArray(this.rawRecipe, "pattern")).result().orElseThrow().getFirst());
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);

		for (int x = 0; x < pattern.length; x++) {
			for (int y = 0; y < pattern[x].length(); y++) {
				String key = pattern[x].substring(y, y + 1);
				PrintableIngredient ingredient = ingredientMap.get(key);

				ingredientsHandler.addIngredient(ingredient, y + 3 * x);
			}
		}

		ingredientsHandler.addOutput(rawRecipe.getAsJsonObject("result"));

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler, false);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
