package net.tslat.aoawikihelpermod.util.printers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;

public class ShapedRecipePrintHelper extends TablePrintHelper {
	@Nullable
	private final Item targetItem;

	private ShapedRecipePrintHelper(String fileName, @Nullable Item targetItem) throws IOException {
		super(fileName, "Name", "Ingredients", "Recipe");

		withProperty("class", "wikitable");

		this.targetItem = targetItem;
	}

	@Nullable
	public static ShapedRecipePrintHelper open(String fileName, @Nullable Item targetItem) {
		try {
			return new ShapedRecipePrintHelper(fileName, targetItem);
		}
		catch (IOException ex) {
			return null;
		}
	}

	public void withRecipe(ShapedRecipe recipe) {
		entry(recipeToTableEntry(recipe));
	}

	public String[] recipeToTableEntry(ShapedRecipe recipe) {
		String[] entryLines = new String[3];
		ItemStack resultStack = recipe.getResultItem();
		String output = ObjectHelper.getItemName(resultStack.getItem());

		if (targetItem != resultStack.getItem()) {
			output = FormattingHelper.createLinkableItem(resultStack, true);
		}
		else {
			output = FormattingHelper.bold(output);
		}

		ArrayList<String> ingredients = new ArrayList<String>();

		for (Ingredient )



		entryLines[0] = output;

		return entryLines;
	}
}
