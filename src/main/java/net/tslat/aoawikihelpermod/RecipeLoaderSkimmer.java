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
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.printers.craftingHandlers.*;

import java.util.HashMap;
import java.util.Map;

public class RecipeLoaderSkimmer extends JsonReloadListener {
	private static final HashMap<String, RecipePrintHandler.Factory> RECIPE_HANDLERS = new HashMap<String, RecipePrintHandler.Factory>();
	public static final HashMap<ResourceLocation, RecipePrintHandler> RECIPE_PRINTERS = new HashMap<ResourceLocation, RecipePrintHandler>();
	public static final HashMultimap<ResourceLocation, ResourceLocation> RECIPES_BY_INGREDIENT = HashMultimap.create();
	public static final HashMultimap<ResourceLocation, ResourceLocation> RECIPES_BY_OUTPUT = HashMultimap.create();

	static {
		RECIPE_HANDLERS.put("crafting", CraftingRecipeHandler::new);
		RECIPE_HANDLERS.put("smelting", SmeltingRecipeHandler::new);
		RECIPE_HANDLERS.put("blasting", BlastingRecipeHandler::new);
		RECIPE_HANDLERS.put("smoking", SmokingRecipeHandler::new);
		RECIPE_HANDLERS.put("campfire", CampfireRecipeHandler::new);
		RECIPE_HANDLERS.put("stonecutting", StonecuttingRecipeHandler::new);
		RECIPE_HANDLERS.put("smithing", SmithingRecipeHandler::new);
		RECIPE_HANDLERS.put("upgrade_kit", UpgradeKitRecipeHandler::new);
		RECIPE_HANDLERS.put("infusion", InfusionRecipeHandler::new);
		RECIPE_HANDLERS.put("trophy", TrophyRecipeHandler::new);
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
				IRecipe<?> recipe = RecipeManager.fromJson(id, json.getAsJsonObject());
				RecipePrintHandler.Factory factory = RECIPE_HANDLERS.get(recipe.getType().toString());

				if (factory == null)
					continue;

				for (Ingredient ingredient : recipe.getIngredients()) {
					for (ItemStack stack : ingredient.getItems()) {
						RECIPES_BY_INGREDIENT.put(stack.getItem().getRegistryName(), id);
					}
				}

				RECIPES_BY_OUTPUT.put(recipe.getResultItem().getItem().getRegistryName(), id);
				RECIPE_PRINTERS.put(id, factory.create(json.getAsJsonObject(), recipe));
			}
			catch (Exception ex) {}
		}
	}

	@Override
	public String getName() {
		return "Recipe Skimmer";
	}
}
