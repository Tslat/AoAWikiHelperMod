package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;

public class StonecuttingRecipeHandler extends RecipePrintHandler {
	private final ResourceLocation recipeId;

	private final JsonObject rawRecipe;
	@Nullable
	private final StonecuttingRecipe recipe;

	private String[] printout = null;

	public StonecuttingRecipeHandler(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe) {
		this.recipeId = recipeId;
		this.rawRecipe = rawRecipe;
		this.recipe = (StonecuttingRecipe)recipe;
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
	public String[] toTableEntry(@Nullable Item targetItem) {
		if (printout != null)
			return printout;

		String targetName = targetItem == null ? "" : ObjectHelper.getItemName(targetItem);
		Pair<String, String> input = ObjectHelper.getIngredientName(this.rawRecipe.getAsJsonObject("ingredient"));
		Triple<Integer, String, String> output = ObjectHelper.getStackDetailsFromJson(this.rawRecipe.get("result"));

		this.printout = new String[] {
				FormattingHelper.createLinkableItem(input.getSecond(), true, input.getFirst().equals("minecraft"), input.getSecond().equals(targetName)) +
						" can be processed on a " +
						FormattingHelper.createLinkableItem(Blocks.STONECUTTER, false, true) +
						" to produce " +
						output.getLeft() +
						" " +
						FormattingHelper.createLinkableItem(output.getRight(), output.getLeft() > 1, output.getMiddle().equals("minecraft"), output.getRight().equals(targetName)) +
						"."
		};

		return printout;
	}
}
