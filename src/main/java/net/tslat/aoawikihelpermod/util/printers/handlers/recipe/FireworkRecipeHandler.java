package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FireworkRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final HashMap<Item, String[]> printoutData = new HashMap<>();

	public FireworkRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
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
				ForgeRegistries.ITEMS.getKey(Items.PAPER),
				ForgeRegistries.ITEMS.getKey(Items.GUNPOWDER),
				ForgeRegistries.ITEMS.getKey(Items.FIREWORK_STAR)
		);
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(ForgeRegistries.ITEMS.getKey(Items.FIREWORK_ROCKET));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(9);

		ingredientsHandler.addIngredient(ObjectHelper.getItemName(Items.PAPER), "minecraft");
		ingredientsHandler.addIngredient(ObjectHelper.getItemName(Items.GUNPOWDER), "minecraft");
		ingredientsHandler.addIngredient(ObjectHelper.getItemName(Items.FIREWORK_STAR), "minecraft");
		ingredientsHandler.addOutput(new ItemStack(Items.FIREWORK_ROCKET));
		String targetItemName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);

		String[] printData = new String[3];
		printData[0] = ingredientsHandler.getFormattedOutput(targetItem);
		printData[1] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeCraftingTemplate(ingredientsHandler.getIngredientsWithSlots(), ingredientsHandler.getOutput(), false);

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
