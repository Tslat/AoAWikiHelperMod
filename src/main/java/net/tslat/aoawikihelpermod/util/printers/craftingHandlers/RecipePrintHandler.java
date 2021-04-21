package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class RecipePrintHandler {
	public abstract String[] toTableEntry(@Nullable Item targetItem);
	public abstract String getTableGroup();
	public abstract ResourceLocation getRecipeId();

	public boolean isPlainTextPrintout() {
		return false;
	}

	public interface Factory {
		RecipePrintHandler create(JsonObject rawRecipe, IRecipe<?> recipe);
	}

	public static class RecipeIngredientsHandler {
		public static final String[] slotPrefixes = new String[] {"a1", "a2", "a3", "b1", "b2", "b3", "c1", "c2", "c3"};

		private final ArrayList<Tuple<String, Integer>> ingredients;
		private final ArrayList<Integer> ingredientsIndex;

		public RecipeIngredientsHandler(int ingredientsSize) {
			this.ingredients = new ArrayList<Tuple<String, Integer>>(ingredientsSize);
			this.ingredientsIndex = new ArrayList<Integer>(ingredientsSize);
		}

		protected void addIngredient(String ingredientName, int position) {
			int matchedIndex = -1;

			for (int i = 0; i < ingredients.size(); i++) {
				if (ingredients.get(i).getA().equals(ingredientName)) {
					matchedIndex = i;

					break;
				}
			}

			if (matchedIndex == -1) {
				this.ingredientsIndex.set(position, this.ingredients.size());
				this.ingredients.add(new Tuple<String, Integer>(ingredientName, 1));
			}
			else {
				this.ingredientsIndex.set(position, matchedIndex);
				this.ingredients.set(matchedIndex, new Tuple<String, Integer>(ingredientName, this.ingredients.get(matchedIndex).getB() + 1));
			}
		}

		protected void addIngredient(String ingredientName) {
			addIngredient(ingredientName, this.ingredientsIndex.size());
		}
	}
}
