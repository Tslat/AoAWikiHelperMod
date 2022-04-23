package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
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
	private final Recipe<?> recipe;
	private final boolean isImbuing;

	private final HashMap<Item, String[]> printoutData = new HashMap<Item, String[]>();

	public InfusionRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
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

		for (JsonElement element : GsonHelper.getAsJsonArray(rawRecipe, "ingredients")) {
			ResourceLocation id = ObjectHelper.getIngredientItemId(element);

			if (id != null)
				ingredients.add(id);
		}

		if (!isImbuing)
			ingredients.add(ObjectHelper.getIngredientItemId(rawRecipe.get("input")));

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
			infusionReq = GsonHelper.getAsInt(rawRecipe, "infusion_level");

		if (rawRecipe.has("infusion_xp")) {
			JsonElement xpJson = rawRecipe.get("infusion_xp");

			if (xpJson.isJsonPrimitive()) {
				minXp = maxXp = xpJson.getAsInt();
			}
			else if (xpJson.isJsonObject()) {
				JsonObject xpJsonObj = xpJson.getAsJsonObject();

				if (xpJsonObj.has("min") && xpJsonObj.has("max")) {
					minXp = Math.max(0, GsonHelper.getAsInt(xpJsonObj, "min"));
					maxXp = Math.max(minXp, GsonHelper.getAsInt(xpJsonObj, "max"));
				}
			}
		}

		JsonObject infusionJson = rawRecipe.getAsJsonObject("infusion");
		String enchantmentName;
		ResourceLocation enchantmentId = new ResourceLocation(GsonHelper.getAsString(infusionJson, "enchantment"));
		Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(enchantmentId);
		int enchantLevel = 1;

		if (infusionJson.has("level"))
			enchantLevel = GsonHelper.getAsInt(infusionJson, "level");

		if (enchant == null) {
			enchantmentName = StringUtil.toTitleCase(enchantmentId.getPath()) + enchantLevel;
		}
		else {
			enchantmentName = ObjectHelper.getEnchantmentName(enchant, enchantLevel);
		}

		JsonArray ingredients = GsonHelper.getAsJsonArray(rawRecipe, "ingredients");
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

		JsonArray ingredients = GsonHelper.getAsJsonArray(rawRecipe, "ingredients");
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(ingredients.size() + 1);
		PrintableIngredient input = ObjectHelper.getIngredientName(rawRecipe.getAsJsonObject("input"));
		String targetItemName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		String inputItemName = FormattingHelper.createLinkableText(input.formattedName, false, !input.matches(targetItemName));

		for (JsonElement ele : ingredients) {
			ingredientsHandler.addIngredient(ele);
		}

		ingredientsHandler.addOutput(GsonHelper.getAsJsonObject(rawRecipe, "result"));

		PrintableIngredient result = ingredientsHandler.getOutput();
		String output = FormattingHelper.createLinkableText(result.formattedName, result.count > 1, !result.matches(targetItemName));

		printData[0] = output;
		printData[1] = 1 + " " + inputItemName + "+<br/>" + ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[2] = WikiTemplateHelper.makeInfusionTemplate(ingredientsHandler.getIngredientsWithSlots(), input.formattedName, result);

		this.printoutData.put(targetItem, printData);

		return printData;
	}

	public static String getImbuingApplicableTo(ResourceLocation enchantId) {
		return switch (enchantId.toString()) {
			case "aoa3:archmage" -> "Staves";
			case "aoa3:brace" -> "Guns,<br/>Archerguns,<br/>Shotguns";
			case "aoa3:control" -> "Guns,<br/>Archerguns,<br/>Shotguns,<br/>Snipers,<br/>Cannons";
			case "aoa3:form" -> "Shotguns";
			case "aoa3:greed" -> "Guns,<br/>Archerguns,<br/>Shotguns,<br/>Snipers,<br/>Cannons,<br/>Blasters,<br/>Staves";
			case "aoa3:intervention" -> "Any unstackable item";
			case "aoa3:recharge" -> "Blasters";
			case "aoa3:sever" -> "Greatblades";
			case "aoa3:shell" -> "Guns,<br/>Shotguns,<br/>Snipers";
			case "minecraft:protection", "minecraft:fire_protection", "minecraft:blast_protection", "minecraft:projectile_protection" -> "Any armour";
			case "minecraft:respiration", "minecraft:aqua_affinity" -> "Any helmet";
			case "minecraft:thorns" -> "Any chestplate";
			case "minecraft:feather_falling", "minecraft:depth_strider", "minecraft:frost_walker" -> "Any boots";
			case "minecraft:binding_curse" -> "Any wearable item";
			case "minecraft:sharpness", "minecraft:smite", "minecraft:bane_of_arthropods", "minecraft:knockback", "minecraft:fire_aspect", "minecraft:looting", "minecraft:sweeping" -> "Swords,<br/>Axes,<br/>Greatblades";
			case "minecraft:efficiency", "minecraft:silk_touch", "minecraft:fortune" -> "Any tool";
			case "minecraft:power", "minecraft:punch", "minecraft:flame", "minecraft:infinity" -> "Bows";
			case "minecraft:luck_of_the_sea", "minecraft:lure" -> "Fishing Rods";
			case "minecraft:unbreaking", "minecraft:mending" -> "Any damageable item";
			case "minecraft:vanishing_curse" -> "Any item";
			default -> "?";
		};
	}
}
