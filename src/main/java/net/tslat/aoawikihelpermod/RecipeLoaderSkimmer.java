package net.tslat.aoawikihelpermod;

import com.google.common.collect.HashMultimap;
import com.google.gson.JsonElement;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.printers.craftingHandlers.*;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class RecipeLoaderSkimmer extends JsonReloadListener {
	private static final HashMap<String, RecipePrintHandler.Factory> RECIPE_HANDLERS = new HashMap<String, RecipePrintHandler.Factory>();
	public static final HashMap<ResourceLocation, RecipePrintHandler> RECIPE_PRINTERS = new HashMap<ResourceLocation, RecipePrintHandler>();
	public static final HashMultimap<ResourceLocation, ResourceLocation> RECIPES_BY_INGREDIENT = HashMultimap.create();
	public static final HashMultimap<ResourceLocation, ResourceLocation> RECIPES_BY_OUTPUT = HashMultimap.create();

	static {
		RECIPE_HANDLERS.put("minecraft:crafting_shaped", ShapedCraftingRecipeHandler::new);
		RECIPE_HANDLERS.put("minecraft:crafting_shapeless", ShapelessCraftingRecipeHandler::new);
		RECIPE_HANDLERS.put("minecraft:smelting", SmeltingRecipeHandler::new);
		RECIPE_HANDLERS.put("minecraft:blasting", BlastingRecipeHandler::new);
		RECIPE_HANDLERS.put("minecraft:smoking", SmokingRecipeHandler::new);
		RECIPE_HANDLERS.put("minecraft:campfire_cooking", CampfireRecipeHandler::new);
		RECIPE_HANDLERS.put("minecraft:stonecutting", StonecuttingRecipeHandler::new);
		RECIPE_HANDLERS.put("minecraft:smithing", SmithingRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:upgrade_kit", UpgradeKitRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:infusion", InfusionRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:trophy", TrophyRecipeHandler::new);
	}

	public RecipeLoaderSkimmer() {
		super(AoAWikiHelperMod.GSON, "recipes");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsonMap, IResourceManager resourceManager, IProfiler profiler) {
		for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
			ResourceLocation id = entry.getKey();
			JsonElement json = entry.getValue();

			if (id.getPath().startsWith("_") || !json.isJsonObject())
				continue;

			try {
				IRecipe<?> recipe = null;

				try {
					recipe = RecipeManager.fromJson(id, json.getAsJsonObject());
				}
				catch (Exception ex) {
					AoAWikiHelperMod.LOGGER.log(Level.WARN, "Invalid recipe found: " + id + ", using only json format.");
				}

				String recipeType = JSONUtils.getAsString(json.getAsJsonObject(), "type");
				RecipePrintHandler.Factory factory = RECIPE_HANDLERS.get(recipeType);

				if (factory == null) {
					AoAWikiHelperMod.LOGGER.log(Level.INFO, "No recipe handler found for recipe of type '" + recipeType + "' for recipe: " + id);

					continue;
				}

				if (recipe != null) {
					populateIngredientsByRecipe(id, recipe);
				}
				else {
					populateIngredientsByJson(id, json);
				}

				RECIPE_PRINTERS.put(id, factory.create(id, json.getAsJsonObject(), recipe));
			}
			catch (Exception ex) {
				AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Failed recipe skim for: " + id + ", skipping recipe.");
			}
		}
	}

	private void populateIngredientsByRecipe(ResourceLocation id, IRecipe<?> recipe) {
		for (Ingredient ingredient : recipe.getIngredients()) {
			for (ItemStack stack : ingredient.getItems()) {
				RECIPES_BY_INGREDIENT.put(stack.getItem().getRegistryName(), id);
			}
		}

		RECIPES_BY_OUTPUT.put(recipe.getResultItem().getItem().getRegistryName(), id);
	}

	private void populateIngredientsByJson(ResourceLocation id, JsonElement recipe) {

	}

	@Override
	public String getName() {
		return "Recipe Skimmer";
	}
}
