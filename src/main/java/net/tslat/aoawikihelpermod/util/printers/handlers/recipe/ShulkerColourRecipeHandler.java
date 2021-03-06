package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ShulkerColourRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public ShulkerColourRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
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
				Blocks.SHULKER_BOX.getRegistryName(),
				Tags.Items.DYES.getName()
		);
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(Blocks.SHULKER_BOX.getRegistryName());
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);

		ingredientsHandler.addIngredient(ObjectHelper.getItemName(Blocks.SHULKER_BOX), "minecraft");
		ingredientsHandler.addIngredient(StringUtil.toTitleCase(Tags.Items.DYES.getName().getPath()), "minecraft");
		ingredientsHandler.addOutput(new ItemStack(Blocks.SHULKER_BOX));
		String targetItemName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler.getIngredientsWithSlots(), ingredientsHandler.getOutput(), false);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
