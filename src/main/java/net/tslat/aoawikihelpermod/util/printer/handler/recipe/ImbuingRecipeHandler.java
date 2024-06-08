package net.tslat.aoawikihelpermod.util.printer.handler.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.tslat.aoa3.content.recipe.ImbuingRecipe;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ImbuingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final ImbuingRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<>();

	public ImbuingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (ImbuingRecipe)recipe;
	}

	@Override
	public String getTableGroup() {
		return "Imbuing Chamber";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public String[] getColumnTitles() {
		return new String[] {"Enchantment", "Applicable to", "Ingredients", "Imbuing Level", "Recipe"};
	}

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ArrayList<ResourceLocation> ingredients = new ArrayList<>(2);

		for (JsonElement foci : this.rawRecipe.getAsJsonArray("aspect_foci")) {
			ingredients.add(new ResourceLocation(foci.getAsJsonPrimitive().getAsString() + "_focus"));
		}

		return ingredients.isEmpty() ? Collections.emptyList() : ingredients;
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return List.of();
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		String[] printData = new String[5];

		int imbuingReq = 1;
		int minXp = 0;
		int maxXp = 0;

		if (rawRecipe.has("imbuing_level"))
			imbuingReq = GsonHelper.getAsInt(rawRecipe, "imbuing_level");

		if (rawRecipe.has("imbuing_xp_override")) {
			JsonElement xpJson = rawRecipe.get("imbuing_xp_override");

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

		String enchantmentName;
		ResourceLocation enchantmentId = new ResourceLocation(GsonHelper.getAsString(rawRecipe, "enchantment"));
		Enchantment enchant = BuiltInRegistries.ENCHANTMENT.get(enchantmentId);
		PrintableIngredient powerSource = ObjectHelper.getIngredientName(rawRecipe.getAsJsonObject("power_source"));
		int enchantLevel = 1;

		if (rawRecipe.has("enchantment_level"))
			enchantLevel = GsonHelper.getAsInt(rawRecipe, "enchantment_level");

		if (enchant == null) {
			enchantmentName = StringUtil.toTitleCase(enchantmentId.getPath()) + enchantLevel;
		}
		else {
			enchantmentName = ObjectHelper.getEnchantmentName(enchant, enchantLevel);
		}

		JsonArray foci = GsonHelper.getAsJsonArray(rawRecipe, "aspect_foci");
		RecipeIngredientsHandler ingredientsHandler = new RecipeIngredientsHandler(foci.size() + 1);

		for (JsonElement ele : foci) {
			ResourceLocation id = new ResourceLocation(ele.getAsString());
			ingredientsHandler.addIngredient(ObjectHelper.getFormattedItemDetails(new ResourceLocation(id.getNamespace(), id.getPath() + "_focus"))/*.setCustomImageName(ObjectHelper.getItemName(BuiltInRegistries.ITEM.get(new ResourceLocation(id.getNamespace(), id.getPath() + "_focus"))) + ".png")*/, -1);
		}

		printData[0] = enchantmentName;
		printData[1] = getImbuingApplicableTo(enchantmentId);
		printData[2] = ingredientsHandler.getFormattedIngredientsList(targetItem);
		printData[3] = String.valueOf(imbuingReq);
		//printData[3] = (minXp != maxXp) ? minXp + "-" + maxXp : String.valueOf(minXp);
		printData[4] = WikiTemplateHelper.makeImbuingTemplate(ingredientsHandler, powerSource);

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
