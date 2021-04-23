package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.registry.Registry;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

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

		private final NonNullList<Tuple<String, Integer>> ingredients;
		private final NonNullList<Integer> ingredientsIndex;

		public RecipeIngredientsHandler(int ingredientsSize) {
			this.ingredients = NonNullList.withSize(ingredientsSize, new Tuple<String, Integer>("", 0));
			this.ingredientsIndex = NonNullList.withSize(ingredientsSize, 0);
		}

		public ArrayList<String> getIngredientsWithSlots() {
			ArrayList<String> lines = new ArrayList<String>(slotPrefixes.length);

			for (int index = 0; index < slotPrefixes.length; index++) {
				lines.add(slotPrefixes[index] + "=" + ingredients.get(ingredientsIndex.get(index)).getA());
			}

			return lines;
		}

		public String getIngredient(int slot) {
			return this.ingredients.get(ingredientsIndex.get(slot)).getA();
		}

		public ImmutableList<Tuple<String, Integer>> getIngredients() {
			return ImmutableList.copyOf(ingredients);
		}

		public void addIngredient(JsonObject json) {
			addIngredient(json, this.ingredientsIndex.size());
		}

		public void addIngredient(JsonObject json, int position) {
			if ((json.has("item") && json.has("tag")) || (!json.has("item") && !json.has("tag")))
				throw new JsonParseException("Invalidly formatted ingredient, unable to proceed with recipe.");

			String ingredientName = null;

			if (json.has("item")) {
				ResourceLocation id = new ResourceLocation(JSONUtils.getAsString(json, "item"));
				Item item = Registry.ITEM.getOptional(id).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + id + "'"));
				ingredientName = ObjectHelper.getItemName(item);
			}
			else if (json.has("tag")) {
				ingredientName = JSONUtils.getAsString(json, "tag");
			}

			addIngredient(ingredientName, position);
		}

		public void addIngredient(String ingredientName) {
			addIngredient(ingredientName, this.ingredientsIndex.size());
		}

		public void addIngredient(String ingredientName, int position) {
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
	}
}
