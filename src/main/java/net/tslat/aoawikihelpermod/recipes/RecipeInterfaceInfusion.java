package net.tslat.aoawikihelpermod.recipes;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;

public class RecipeInterfaceInfusion extends IRecipeInterface {
	private final int infusionReq;
	private final int infusionXpMin;
	private final int infusionXpMax;

	private final IRecipeInterfaceIngredient inputItem;

	protected final Enchantment enchant;
	private final int enchantLevel;

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

		if (json.has("input")) {
			JsonObject input = JsonUtils.getJsonObject(json, "input");
			String entryType = context.appendModId(JsonUtils.getString(input, "type", "minecraft:item"));
			ItemStack matchingStack = CraftingHelper.getIngredient(input, context).getMatchingStacks()[0];
			String inputName = matchingStack.getDisplayName();
			String oreDictName = entryType.equals("forge:ore_dict") ? JsonUtils.getString(input.getAsJsonObject(), "ore") : null;

			if (matchingStack.getItem().getRegistryName().getResourceDomain().equals("minecraft"))
				inputName = "mcw:" + inputName;

			inputItem = new IRecipeInterfaceIngredient(inputName, oreDictName);
			enchant = null;
			enchantLevel = -1;
		}
		else if (json.has("infusion")) {
			JsonObject enchantObj = JsonUtils.getJsonObject(json, "infusion");
			enchant = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(JsonUtils.getString(enchantObj, "enchantment")));
			enchantLevel = JsonUtils.getInt(enchantObj, "level");
			inputItem = null;
		}
		else {
			throw new JsonSyntaxException("Infusion recipe missing both input and imbuing section, not possible to deserialize");
		}
	}

	@Override
	protected String recipeGroup() {
		return inputItem != null ? "Infusion" : "Imbuing";
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

		if (inputItem != null)
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

		if (inputItem != null)
			return "! Item !! Ingredients" + (hasLevels ? " !! Infusion Level" : "") + (hasXp ? " !! Infusion Xp" : "") + " !! Recipe";

		return "! Enchantment !! Applicable To !! Ingredients" + (hasLevels ? " !! Infusion Level" : "") + (hasXp ? " !! Infusion Xp" : "") + " !! Recipe";
	}

	@Override
	protected String getWikiTemplateName() {
		return enchant == null ? "Infusion" : "Imbuing";
	}

	@Override
	protected boolean matchAdditionalIngredients(ItemStack targetStack) {
		if (inputItem == null)
			return false;

		return targetStack.getDisplayName().equals(inputItem.getIngredientName());
	}

	@Override
	protected ArrayList<String> buildAdditionalTemplateLines(ItemStack targetStack, boolean printImageLines) {
		ArrayList<String> lines = new ArrayList<String>();
		ItemStack output = recipe.getRecipeOutput();

		for (int i = 0; i < ingredientNameBySlot.size(); i++) {
			String slotName = ingredientNameBySlot.get(i);

			lines.add("|" + slotPrefixes[i] + "=" + slotName);

			if (printImageLines && !slotName.equals(""))
				lines.add("|" + slotPrefixes[i] + "image=" + slotName + ".png");
		}

		if (inputItem != null) {
			lines.add("|input=" + inputItem.getIngredientName().replaceAll("mcw:", ""));

			if (printImageLines)
				lines.add("|inputimage=" + inputItem.getIngredientName().replaceAll("mcw:", "") + ".png");
		}

		lines.add("|output=" + output.getDisplayName());

		if (printImageLines)
			lines.add("|outputimage=" + output.getDisplayName() + ".png");

		if (output.getCount() > 1)
			lines.add("|amount=" + output.getCount());

		lines.add("|shapeless=1");

		return lines;
	}

	@Override
	protected String buildSummaryLine(ItemStack targetStack) {
		if (enchant == null)
			return super.buildSummaryLine(targetStack);

		return "[[" + (enchant.getRegistryName().getResourceDomain().equals("minecraft") ? "mcw:" : "") + I18n.translateToLocal(enchant.getName()) + "|" + enchant.getTranslatedName(enchantLevel) + "]] || " + getImbuingApplicableTo() + " || " + buildIngredientSummaryLine(targetStack) + " || {{Infusion";
	}

	@Override
	public int sortingCompare(IRecipeInterface comparingInterface) {
		if (enchant == null)
			return super.sortingCompare(comparingInterface);

		RecipeInterfaceInfusion otherInterface = (RecipeInterfaceInfusion)comparingInterface;

		return (I18n.translateToLocal(enchant.getName()) + enchantLevel).compareTo(I18n.translateToLocal(otherInterface.enchant.getName()) + otherInterface.enchantLevel);
	}

	private String getImbuingApplicableTo() {
		switch (enchant.getRegistryName().toString()) {
			case "aoa3:archmage":
				return "Staves";
			case "aoa3:brace":
				return "Guns,<br/>Archerguns,<br/>Shotguns";
			case "aoa3:control":
				return "Guns,<br/>Archerguns,<br/>Shotguns,<br/>Snipers,<br/>Cannons";
			case "aoa3:crush":
				return "Mauls";
			case "aoa3:form":
				return "Shotguns";
			case "aoa3:greed":
				return "Guns,<br/>Archerguns,<br/>Shotguns,<br/>Snipers,<br/>Cannons,<br/>Blasters,<br/>Staves";
			case "aoa3:intervention":
				return "Any unstackable item";
			case "aoa3:recharge":
				return "Blasters";
			case "aoa3:sever":
				return "Greatblades";
			case "aoa3:shell":
				return "Guns,<br/>Shotguns,<br/>Snipers";
			case "minecraft:protection":
			case "minecraft:fire_protection":
			case "minecraft:blast_protection":
			case "minecraft:projectile_protection":
				return "Any armour";
			case "minecraft:respiration":
			case "minecraft:aqua_affinity":
				return "Any helmet";
			case "minecraft:thorns":
				return "Any chestplate";
			case "minecraft:feather_falling":
			case "minecraft:depth_strider":
			case "minecraft:frost_walker":
				return "Any boots";
			case "minecraft:binding_curse":
				return "Any wearable item";
			case "minecraft:sharpness":
			case "minecraft:smite":
			case "minecraft:bane_of_arthropods":
			case "minecraft:knockback":
			case "minecraft:fire_aspect":
			case "minecraft:looting":
			case "minecraft:sweeping":
				return "Swords,<br/>Axes";
			case "minecraft:efficiency":
			case "minecraft:silk_touch":
			case "minecraft:fortune":
				return "Any tool";
			case "minecraft:power":
			case "minecraft:punch":
			case "minecraft:flame":
			case "minecraft:infinity":
				return "Bows";
			case "minecraft:luck_of_the_sea":
			case "minecraft:lure":
				return "Fishing Rods";
			case "minecraft:unbreaking":
			case "minecraft:mending":
				return "Any damageable item";
			case "minecraft:vanishing_curse":
				return "Any item";
			default:
				return "";
		}
	}
}
