package net.tslat.aoawikihelpermod.recipes;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.ArrayList;

public class RecipeInterfaceInfusion extends IRecipeInterface {
	private final int infusionReq;
	private final int infusionXpMin;
	private final int infusionXpMax;
	private final IRecipeInterfaceIngredient inputItem;

	public RecipeInterfaceInfusion(IRecipe recipe, JsonObject json) {
		super(recipe, json);

		infusionReq = json.has("infusion_level") ? JsonUtils.getInt(json, "infusion_level") : 1;

		if (json.has("infusion_xp")) {
			JsonObject xpJson = JsonUtils.getJsonObject(json, "infusion_xp");

			if (xpJson.isJsonPrimitive()) {
				infusionXpMin = infusionXpMax = xpJson.getAsInt();
			}
			else {
				infusionXpMin = Math.max(0, JsonUtils.getInt(xpJson, "min"));
				infusionXpMax = Math.max(infusionXpMin, JsonUtils.getInt(xpJson, "max"));
			}
		}
		else {
			infusionXpMin = 0;
			infusionXpMax = 0;
		}

		JsonContext context = new JsonContext("aoa3");
		JsonObject input = JsonUtils.getJsonObject(json, "input");
		String entryType = context.appendModId(JsonUtils.getString(input, "type", "minecraft:item"));
		ItemStack matchingStack = CraftingHelper.getIngredient(input, context).getMatchingStacks()[0];
		String inputName = matchingStack.getDisplayName();
		String oreDictName = entryType.equals("forge:ore_dict") ? JsonUtils.getString(input.getAsJsonObject(), "ore") : null;

		if (matchingStack.getItem().getRegistryName().getResourceDomain().equals("minecraft"))
			inputName = "mcw:" + inputName;

		inputItem = new IRecipeInterfaceIngredient(inputName, oreDictName);
	}

	@Override
	protected void populateIngredientArrays() {
		JsonContext context = new JsonContext("aoa3");

		for (JsonElement entry : JsonUtils.getJsonArray(json, "ingredients")) {
			String entryType = context.appendModId(JsonUtils.getString(entry.getAsJsonObject(), "type", "minecraft:item"));
			ItemStack matchingStack = CraftingHelper.getIngredient(entry, context).getMatchingStacks()[0];
			String entryName = matchingStack.getDisplayName();
			String oreDictName = entryType.equals("forge:ore_dict") ? JsonUtils.getString(entry.getAsJsonObject(), "ore") : null;

			if (matchingStack.getItem().getRegistryName().getResourceDomain().equals("minecraft"))
				entryName = "mcw:" + entryName;

			addIngredient(entryName, oreDictName);
		}
	}

	@Override
	protected String buildIngredientSummaryLine(ItemStack targetStack) {
		StringBuilder builder = new StringBuilder();
		ArrayList<IRecipeInterfaceIngredient> extendedIngArray = (ArrayList<IRecipeInterfaceIngredient>)ingredientArray.clone();

		extendedIngArray.add(inputItem);

		for (IRecipeInterfaceIngredient ing : extendedIngArray) {
			String ingredientName = ing.getIngredientName();
			String mcwSafeName = ingredientName.replace("mcw:", "");

			builder.append(" +<br/> ");
			builder.append(ing.getCount());
			builder.append(" ");

			if (ing.getOreDictName() != null) {
				builder.append("{{tooltip|");
				builder.append(mcwSafeName);
				builder.append("|Ore Dictionary: '");
				builder.append(ing.getOreDictName());
				builder.append("'}}");
			}
			else if (!targetStack.getDisplayName().equals(mcwSafeName)) {
				builder.append("[[");

				if (ingredientName.contains("mcw:")) {
					builder.append(ingredientName);
					builder.append("|");
				}

				builder.append(mcwSafeName);

				if (ing.getCount() > 1 && (!ingredientName.endsWith("s") && !ingredientName.endsWith("y"))) {
					String pluralSuffix = ingredientName.endsWith("x") ? "es" : "s";

					if (ingredientName.contains("mcw:")) {
						builder.append("]]");
						builder.append(pluralSuffix);
					}
					else {
						builder.append("|");
						builder.append(mcwSafeName);
						builder.append(pluralSuffix);
						builder.append("]]");
					}
				}
				else {
					builder.append("]]");
				}
			}
			else {
				builder.append(mcwSafeName);

				if (ing.getCount() > 1 && (!ingredientName.endsWith("s") && !ingredientName.endsWith("y")))
					builder.append(ingredientName.endsWith("x") ? "es" : "s");
			}
		}

		if (infusionReq > 1) {
			builder.append(" || ");
			builder.append(infusionReq);
		}

		if (infusionXpMin > 0) {
			builder.append(" || ");
			builder.append(infusionXpMin);

			if (infusionXpMin < infusionXpMax) {
				builder.append("-");
				builder.append(infusionXpMax);
			}
		}

		return builder.toString().substring(8);
	}

	@Override
	protected String buildWikiTableHeadingsLine(ArrayListMultimap<String, IRecipe> matchedRecipes) {
		boolean hasLevels = false;
		boolean hasXp = false;

		for (IRecipe recipe : matchedRecipes.get("InfusionTableRecipe")) {
			JsonObject recipeJson = RecipeWriter.getRecipeJson(recipe);

			if (recipeJson != null) {
				if (recipeJson.has("infusion_level"))
					hasLevels = true;

				if (recipeJson.has("infusion_xp"))
					hasXp = true;
			}

			if (hasLevels && hasXp)
				break;
		}

		return "! Item !! Ingredients !! Recipe" + (hasLevels ? "!! Infusion Level" : "") + (hasXp ? "!! Infusion Xp" : "");
	}

	@Override
	protected String getWikiTemplateName() {
		return "Infusion";
	}

	@Override
	protected ArrayList<String> buildAdditionalTemplateLines(ItemStack targetStack) {
		ArrayList<String> lines = new ArrayList<String>();
		ItemStack output = recipe.getRecipeOutput();

		for (int i = 0; i < ingredientNameBySlot.size(); i++) {
			lines.add("|" + slotPrefixes[i] + "=" + ingredientNameBySlot.get(i));
		}

		lines.add("|input=" + inputItem.getIngredientName().replaceAll("mcw:", ""));
		lines.add("|output=" + output.getDisplayName());

		if (output.getCount() > 1)
			lines.add("|amount=" + output.getCount());

		lines.add("|shapeless=1");

		return lines;
	}
}
