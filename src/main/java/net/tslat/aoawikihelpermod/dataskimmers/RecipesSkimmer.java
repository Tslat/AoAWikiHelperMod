package net.tslat.aoawikihelpermod.dataskimmers;

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
import net.tslat.aoa3.common.container.recipe.InfusionRecipe;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.printers.handlers.*;
import net.tslat.aoawikihelpermod.util.printers.handlers.recipe.*;
import net.tslat.aoawikihelpermod.util.printers.handlers.recipe.immersivenegineering.IEClocheRecipeHandler;
import net.tslat.aoawikihelpermod.util.printers.handlers.recipe.thermalexpansion.*;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class RecipesSkimmer extends JsonReloadListener {
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
		RECIPE_HANDLERS.put("minecraft:crafting_special_firework_rocket", FireworkRecipeHandler::new);
		RECIPE_HANDLERS.put("minecraft:crafting_special_shulkerboxcoloring", ShulkerColourRecipeHandler::new);
		RECIPE_HANDLERS.put("minecraft:crafting_special_suspiciousstew", SuspiciousStewRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:upgrade_kit", UpgradeKitRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:infusion", InfusionRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:trophy", TrophyRecipeHandler::new);

		RECIPE_HANDLERS.put("thermal:tree_extractor", TETreeExtractorRecipeHandler::new);
		RECIPE_HANDLERS.put("thermal:sawmill", TESawmillRecipeHandler::new);
		RECIPE_HANDLERS.put("thermal:pulverizer", TEPulverizerRecipeHandler::new);
		RECIPE_HANDLERS.put("thermal:chiller", TEChillerRecipeHandler::new);
		RECIPE_HANDLERS.put("thermal:insolator", TEInsolatorRecipeHandler::new);

		RECIPE_HANDLERS.put("immersiveengineering:cloche", IEClocheRecipeHandler::new);
	}

	public RecipesSkimmer() {
		super(AoAWikiHelperMod.GSON, "recipes");
	}

	public static void registerRecipeHandler(String recipeType, RecipePrintHandler.Factory handlerFactory) {
		RECIPE_HANDLERS.put(recipeType, handlerFactory);

		if (RECIPE_PRINTERS.isEmpty())
			AoAWikiHelperMod.LOGGER.log(Level.WARN, "Recipe handler registered after data loading. This will result in recipes being missed. Register recipe handler in constructor of addon");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsonMap, IResourceManager resourceManager, IProfiler profiler) {
		RECIPE_PRINTERS.clear();
		RECIPES_BY_INGREDIENT.clear();
		RECIPES_BY_OUTPUT.clear();

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
					AoAWikiHelperMod.LOGGER.log(Level.WARN, "Unknown recipe found: " + id + ", using only json format.");
				}

				String recipeType = JSONUtils.getAsString(json.getAsJsonObject(), "type");
				RecipePrintHandler.Factory factory = RECIPE_HANDLERS.get(recipeType);

				if (factory == null) {
					AoAWikiHelperMod.LOGGER.log(Level.INFO, "No recipe handler found for recipe of type '" + recipeType + "' for recipe: " + id);

					continue;
				}

				RecipePrintHandler recipePrintHandler = factory.create(id, json.getAsJsonObject(), recipe);

				if (recipe != null) {
					populateIngredientsByRecipe(id, recipe);
				}
				else {
					populateIngredientsByHandler(id, recipePrintHandler);
				}

				RECIPE_PRINTERS.put(id, recipePrintHandler);
			}
			catch (Exception ex) {
				AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Failed recipe skim for: " + id + ", skipping recipe.", ex);
			}
		}
	}

	private void populateIngredientsByRecipe(ResourceLocation id, IRecipe<?> recipe) {
		for (Ingredient ingredient : recipe.getIngredients()) {
			for (ItemStack stack : ingredient.getItems()) {
				RECIPES_BY_INGREDIENT.put(stack.getItem().getRegistryName(), id);
			}

			if (recipe instanceof InfusionRecipe && !((InfusionRecipe)recipe).isEnchanting())
				RECIPES_BY_INGREDIENT.put(((InfusionRecipe)recipe).getRecipeInput().getItem().getRegistryName(), id);
		}

		RECIPES_BY_OUTPUT.put(recipe.getResultItem().getItem().getRegistryName(), id);
	}

	private void populateIngredientsByHandler(ResourceLocation id, RecipePrintHandler recipePrintHandler) {
		for (ResourceLocation ingredient : recipePrintHandler.getIngredientsForLookup()) {
			RECIPES_BY_INGREDIENT.put(ingredient, id);
		}

		for (ResourceLocation output : recipePrintHandler.getOutputsForLookup()) {
			RECIPES_BY_OUTPUT.put(output, id);
		}
	}

	@Override
	public String getName() {
		return "Recipes Skimmer";
	}
}
