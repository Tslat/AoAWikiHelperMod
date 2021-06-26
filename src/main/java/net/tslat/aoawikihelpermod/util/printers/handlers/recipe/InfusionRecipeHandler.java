package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InfusionRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final IRecipe<?> recipe;
	private final boolean isImbuing;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public InfusionRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = recipe;
		this.isImbuing = rawRecipe.has("infusion");
	}

	@Override
	public String getTableGroup() {
		return isImbuing ? "Imbuing" : "Infusion";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] getColumnTitles() {
		if (isImbuing)
			return new String[] {"Enchantment", "Used For", "Infusion Req.", "Infusion XP", "Ingredients", "Recipe"};

		return new String[] {"Item", "Ingredients", "Recipe"};
	}

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<ResourceLocation>();

		for (JsonElement element : JSONUtils.getAsJsonArray(rawRecipe, "ingredients")) {
			ResourceLocation id = ObjectHelper.getIngredientItemId(element);

			if (id != null)
				ingredients.add(id);
		}

		return ingredients.isEmpty() ? Collections.emptyList() : ingredients;
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		if (isImbuing)
			return Collections.emptyList();

		return Collections.singletonList(ObjectHelper.getIngredientItemId(this.rawRecipe.get("result")));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		return isImbuing ? makeImbuingRecipe(targetItem) : makeInfusionRecipe(targetItem);
	}

	private String[] makeImbuingRecipe(@Nullable Item targetItem) {
		String[] printData = new String[6];

		int infusionReq = 1;
		int minXp = 0;
		int maxXp = 0;

		if (rawRecipe.has("infusion_level"))
			infusionReq = JSONUtils.getAsInt(rawRecipe, "infusion_level");

		if (rawRecipe.has("infusion_xp")) {
			JsonElement xpJson = rawRecipe.get("infusion_xp");

			if (xpJson.isJsonPrimitive()) {
				minXp = maxXp = xpJson.getAsInt();
			}
			else if (xpJson.isJsonObject()) {
				JsonObject xpJsonObj = xpJson.getAsJsonObject();

				if (xpJsonObj.has("min") && xpJsonObj.has("max")) {
					minXp = Math.max(0, JSONUtils.getAsInt(xpJsonObj, "min"));
					maxXp = Math.max(minXp, JSONUtils.getAsInt(xpJsonObj, "max"));
				}
			}
		}

		JsonObject infusionJson = rawRecipe.getAsJsonObject("infusion");
		String enchantmentName;
		ResourceLocation enchantmentId = new ResourceLocation(JSONUtils.getAsString(infusionJson, "enchantment"));
		Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(enchantmentId);
		int enchantLevel = 1;

		if (infusionJson.has("level"))
			enchantLevel = JSONUtils.getAsInt(infusionJson, "level");

		if (enchant == null) {
			enchantmentName = StringUtil.toTitleCase(enchantmentId.getPath()) + enchantLevel;
		}
		else {
			enchantmentName = ObjectHelper.getEnchantmentName(enchant, enchantLevel);
		}

		JsonArray ingredients = JSONUtils.getAsJsonArray(rawRecipe, "ingredients");
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(ingredients.size() + 1);

		for (JsonElement ele : ingredients) {
			ingredientsHandler.addIngredient(ele);
		}

		printData[0] = enchantmentName;
		printData[1] = getImbuingApplicableTo(enchantmentId);
		printData[2] = String.valueOf(infusionReq);
		printData[3] = (minXp != maxXp) ? minXp + "-" + maxXp : String.valueOf(minXp);
		printData[4] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[5] = WikiTemplateHelper.makeInfusionTemplate(ingredientsHandler.getIngredientsWithSlots(), "", null);

		this.printoutData.put(targetItem, printData);

		return printData;
	}

	private String[] makeInfusionRecipe(@Nullable Item targetItem) {
		String[] printData = new String[3];

		JsonArray ingredients = JSONUtils.getAsJsonArray(rawRecipe, "ingredients");
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(ingredients.size() + 1);
		Pair<String, String> input = ObjectHelper.getIngredientName(rawRecipe.getAsJsonObject("input"));
		String targetItemName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		String inputItemName = FormattingHelper.createLinkableText(input.getSecond(), false, input.getFirst().equals("minecraft"), !input.getSecond().equals(targetItemName));

		for (JsonElement ele : ingredients) {
			ingredientsHandler.addIngredient(ele);
		}

		ingredientsHandler.addOutput(JSONUtils.getAsJsonObject(rawRecipe, "result"));

		String output = FormattingHelper.createLinkableText(ingredientsHandler.getOutput().getRight(), ingredientsHandler.getOutput().getLeft() > 1, ingredientsHandler.getOutput().getMiddle().equals("minecraft"), !ingredientsHandler.getOutput().getRight().equals(targetItemName));

		printData[0] = output;
		printData[1] = 1 + " " + inputItemName + "<br/>" + ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeInfusionTemplate(ingredientsHandler.getIngredientsWithSlots(), input.getSecond(), ingredientsHandler.getOutput());

		this.printoutData.put(targetItem, printData);

		return printData;
	}

	public static String getImbuingApplicableTo(ResourceLocation enchantId) {
		switch (enchantId.toString()) {
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
				return "?";
		}
	}
}
