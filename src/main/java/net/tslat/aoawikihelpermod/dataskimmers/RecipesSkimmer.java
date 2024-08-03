package net.tslat.aoawikihelpermod.dataskimmers;

import com.google.common.collect.HashMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.tslat.aoa3.content.recipe.InfusionRecipe;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;
import net.tslat.aoawikihelpermod.util.printer.handler.recipe.*;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipesSkimmer extends SimpleJsonResourceReloadListener {
	private static final HashMap<String, RecipePrintHandler.Factory> RECIPE_HANDLERS = new HashMap<String, RecipePrintHandler.Factory>();
	public static final HashMap<ResourceLocation, RecipePrintHandler> RECIPE_PRINTERS = new HashMap<ResourceLocation, RecipePrintHandler>();
	private static final HashMultimap<ResourceLocation, ResourceLocation> RECIPES_BY_INGREDIENT = HashMultimap.create();
	private static final HashMultimap<ResourceLocation, ResourceLocation> RECIPES_BY_OUTPUT = HashMultimap.create();

	private static final List<Runnable> COMPUTE_QUEUE = new ObjectArrayList<>();

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
		RECIPE_HANDLERS.put("minecraft:crafting_special_mapextending", MapExtendingRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:upgrade_kit", UpgradeKitRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:whitewashing", WhitewashingRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:imbuing", ImbuingRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:infusion", InfusionRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:gold_trophy", GoldTrophyRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:ashfern_cooking", AshfernCookingRecipeHandler::new);
		RECIPE_HANDLERS.put("aoa3:tool_interaction", ToolInteractionRecipeHandler::new);
	}

	public static Set<ResourceLocation> getRecipesByOutput(ResourceLocation outputId) {
		if (!COMPUTE_QUEUE.isEmpty()) {
			COMPUTE_QUEUE.forEach(Runnable::run);
			COMPUTE_QUEUE.clear();
		}

		return RECIPES_BY_OUTPUT.get(outputId);
	}

	public static Set<ResourceLocation> getRecipesByIngredient(ResourceLocation outputId) {
		if (!COMPUTE_QUEUE.isEmpty()) {
			COMPUTE_QUEUE.forEach(Runnable::run);
			COMPUTE_QUEUE.clear();
		}

		return RECIPES_BY_INGREDIENT.get(outputId);
	}

	public RecipesSkimmer() {
		super(AoAWikiHelperMod.GSON, "recipe");
	}

	public static void registerRecipeHandler(String recipeType, RecipePrintHandler.Factory handlerFactory) {
		RECIPE_HANDLERS.put(recipeType, handlerFactory);

		if (RECIPE_PRINTERS.isEmpty())
			AoAWikiHelperMod.LOGGER.log(Level.WARN, "Recipe handler registered after data loading. This will result in recipes being missed. Register recipe handler in constructor of addon");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
		RECIPE_PRINTERS.clear();
		RECIPES_BY_INGREDIENT.clear();
		RECIPES_BY_OUTPUT.clear();
		RegistryOps<JsonElement> registryops = this.makeConditionalOps();

		for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
			ResourceLocation id = entry.getKey();
			JsonElement json = entry.getValue();

			if (id.getPath().startsWith("_") || !json.isJsonObject())
				continue;

			try {
				Recipe<?> recipe = null;

				try {
					recipe = Recipe.CONDITIONAL_CODEC.parse(registryops, entry.getValue()).getOrThrow(JsonParseException::new).get().carrier();
				}
				catch (Exception ex) {
					AoAWikiHelperMod.LOGGER.log(Level.WARN, "Unknown recipe found: " + id + ", using only json format.");
				}

				String recipeType = GsonHelper.getAsString(json.getAsJsonObject(), "type");
				RecipePrintHandler.Factory factory = RECIPE_HANDLERS.get(recipeType);

				if (factory == null) {
					AoAWikiHelperMod.LOGGER.log(Level.INFO, "No recipe handler found for recipe of type '" + recipeType + "' for recipe: " + id);

					continue;
				}

				RecipePrintHandler recipePrintHandler = factory.create(id, json.getAsJsonObject(), recipe);
				Recipe<?> cachedRecipe = recipe;

				COMPUTE_QUEUE.add(new Runnable() {
					private final Recipe<?> recipe = cachedRecipe;
					private final ResourceLocation recipeId = id;
					private final RecipePrintHandler printHandler = recipePrintHandler;

					@Override
					public void run() {
						final Set<ResourceLocation> byIngredient = new ObjectOpenHashSet<>();
						final Set<ResourceLocation> byOutput = new ObjectOpenHashSet<>();

						if (this.recipe != null)
							populateIngredientsByRecipe(byIngredient, byOutput, this.recipeId, this.recipe);

						populateIngredientsByHandler(byIngredient, byOutput, this.recipeId, this.printHandler);

						for (ResourceLocation ingredient : byIngredient) {
							RECIPES_BY_INGREDIENT.put(ingredient, id);
						}

						for (ResourceLocation output : byOutput) {
							RECIPES_BY_OUTPUT.put(output, id);
						}
					}
				});

				RECIPE_PRINTERS.put(id, recipePrintHandler);
			}
			catch (Exception ex) {
				AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Failed recipe skim for: " + id + ", skipping recipe.", ex);
			}
		}
	}

	private void populateIngredientsByRecipe(Set<ResourceLocation> byIngredient, Set<ResourceLocation> byOutput, ResourceLocation id, Recipe<?> recipe) {
		for (Ingredient ingredient : recipe.getIngredients()) {
			for (ItemStack stack : ingredient.getItems()) {
				byIngredient.add(RegistryUtil.getId(stack.getItem()));
			}

			if (recipe instanceof InfusionRecipe infusionRecipe) {
				for (ItemStack stack : infusionRecipe.getInput().getItems()) {
					byIngredient.add(RegistryUtil.getId(stack.getItem()));
				}
			}
		}

		byOutput.add(RegistryUtil.getId(recipe.getResultItem(ServerLifecycleHooks.getCurrentServer().registryAccess()).getItem()));
	}

	private void populateIngredientsByHandler(Set<ResourceLocation> byIngredient, Set<ResourceLocation> byOutput, ResourceLocation id, RecipePrintHandler recipePrintHandler) {
        byIngredient.addAll(recipePrintHandler.getIngredientsForLookup());
        byOutput.addAll(recipePrintHandler.getOutputsForLookup());
	}

	@Override
	public String getName() {
		return "Recipes Skimmer";
	}
}
