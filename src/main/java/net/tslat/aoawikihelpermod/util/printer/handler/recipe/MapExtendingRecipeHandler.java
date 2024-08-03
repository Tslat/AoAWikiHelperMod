package net.tslat.aoawikihelpermod.util.printer.handler.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MapExtendingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public MapExtendingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
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
		return Arrays.asList(
				RegistryUtil.getId(Items.PAPER),
				RegistryUtil.getId(Items.FILLED_MAP)
		);
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(RegistryUtil.getId(Items.MAP));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		HashMap<String, PrintableIngredient> ingredientMap = new HashMap<>();

		ingredientMap.put(" ", new PrintableIngredient("", ""));
		ingredientMap.put("#", ObjectHelper.getFormattedItemDetails(RegistryUtil.getId(Items.PAPER)));
		ingredientMap.put("x", ObjectHelper.getFormattedItemDetails(RegistryUtil.getId(Items.FILLED_MAP)));

		String[] pattern = new String[] {"###", "#x#", "###"};
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);

		for (int x = 0; x < pattern.length; x++) {
			for (int y = 0; y < pattern[x].length(); y++) {
				String key = pattern[x].substring(y, y + 1);
				PrintableIngredient ingredient = ingredientMap.get(key);

				ingredientsHandler.addIngredient(ingredient, y + 3 * x);
			}
		}

		ingredientsHandler.addOutput(Items.MAP.getDefaultInstance());

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler, false);

		this.printoutData.put(targetItem, printData);


		return printData;
	}
}
