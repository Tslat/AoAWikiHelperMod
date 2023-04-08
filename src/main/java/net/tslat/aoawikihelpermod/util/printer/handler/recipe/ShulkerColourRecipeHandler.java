package net.tslat.aoawikihelpermod.util.printer.handler.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ShulkerColourRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public ShulkerColourRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
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
				ForgeRegistries.BLOCKS.getKey(Blocks.SHULKER_BOX),
				Tags.Items.DYES.location()
		);
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(ForgeRegistries.BLOCKS.getKey(Blocks.SHULKER_BOX));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);

		ingredientsHandler.addIngredient(ObjectHelper.getItemName(Blocks.SHULKER_BOX), "minecraft");
		ingredientsHandler.addIngredient(StringUtil.toTitleCase(Tags.Items.DYES.location().getPath()), "minecraft");
		ingredientsHandler.addOutput(new ItemStack(Blocks.SHULKER_BOX));
		String targetItemName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler, false);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
