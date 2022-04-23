package net.tslat.aoawikihelpermod.util.printers.handlers.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.Blocks;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.RecipePrintHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class StonecuttingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final StonecutterRecipe recipe;

	private final HashMap<Item, String[]> printoutData = new HashMap<>();

	public StonecuttingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (StonecutterRecipe)recipe;
	}

	@Override
	public String getTableGroup() {
		return "Stonecutting";
	}

	@Override
	public ResourceLocation getRecipeId() {
		return this.recipeId;
	}

	@Override
	public boolean isPlainTextPrintout() {
		return true;
	}

	@Override
	public String[] getColumnTitles() {
		return null;
	}

	@Override
	public List<ResourceLocation> getIngredientsForLookup() {
		ResourceLocation id = ObjectHelper.getIngredientItemId(this.rawRecipe.get("ingredient"));

		return id == null ? Collections.emptyList() : Collections.singletonList(id);
	}

	@Override
	public List<ResourceLocation> getOutputsForLookup() {
		return Collections.singletonList(ObjectHelper.getIngredientItemId(this.rawRecipe.get("result")));
	}

	@Override
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		PrintableIngredient input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("ingredient"));
		PrintableIngredient output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));

		String[] printData = new String[3];
		printData[0] = FormattingHelper.createImageBlock(Blocks.STONECUTTER) + " " + FormattingHelper.createLinkableItem(Blocks.STONECUTTER, false, true);
		printData[1] = FormattingHelper.createImageBlock(input.formattedName) + " " + FormattingHelper.createLinkableText(input.formattedName, false, input.isVanilla(), !input.matches(targetName));
		printData[2] = FormattingHelper.createImageBlock(output.formattedName) + " " + FormattingHelper.createLinkableText(output.formattedName, false, output.isVanilla(), !output.matches(targetName));

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
