package net.tslat.aoawikihelpermod.util.printer.handler.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.tslat.aoa3.content.recipe.ToolInteractionRecipe;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ToolInteractionRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;
	private final JsonObject rawRecipe;
	@Nullable
	private final ToolInteractionRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public ToolInteractionRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (ToolInteractionRecipe) recipe;
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
		return new String[] {"Item", "Ingredients", "Recipe", "Notes"};
	}

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		List<ResourceLocation> ingredients = new ObjectArrayList<>();

		for (JsonElement element : GsonHelper.getAsJsonArray(rawRecipe, "ingredients")) {
			ResourceLocation id = ObjectHelper.getIngredientItemId(element);

			if (id != null)
				ingredients.add(id);
		}

		return ingredients.isEmpty() ? List.of() : ingredients;
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(ObjectHelper.getIngredientItemId(this.rawRecipe.get("result")));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		JsonArray ingredients = GsonHelper.getAsJsonArray(this.rawRecipe, "ingredients");
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);
		PrintableIngredient tool = ObjectHelper.getIngredientName(GsonHelper.getAsJsonObject(this.rawRecipe, "tool"));
		for (JsonElement ele : ingredients) {
			ingredientsHandler.addIngredient(ele);
		}

		ingredientsHandler.addOutput(this.rawRecipe.getAsJsonObject("result"));

		String[] printData = new String[4];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler, true);
		printData[3] = "One point of durability is removed from the " + tool.formattedName + " per craft.";

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
