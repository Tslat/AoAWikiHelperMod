package net.tslat.aoawikihelpermod.util.printer.handler.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SuspiciousStewRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public SuspiciousStewRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
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
				RegistryUtil.getId(Items.BROWN_MUSHROOM),
				RegistryUtil.getId(Items.RED_MUSHROOM),
				RegistryUtil.getId(Items.BOWL),
				ItemTags.SMALL_FLOWERS.location()
		);
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(RegistryUtil.getId(Items.SUSPICIOUS_STEW));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);

		ingredientsHandler.addIngredient(ObjectHelper.getItemName(Items.BROWN_MUSHROOM), "minecraft");
		ingredientsHandler.addIngredient(ObjectHelper.getItemName(Items.RED_MUSHROOM), "minecraft");
		ingredientsHandler.addIngredient(ObjectHelper.getItemName(Items.BOWL), "minecraft");
		ingredientsHandler.addIngredient(StringUtil.toTitleCase(ItemTags.SMALL_FLOWERS.location().getPath()), "minecraft");
		ingredientsHandler.addOutput(new ItemStack(Items.SUSPICIOUS_STEW));
		String targetItemName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler, false);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
