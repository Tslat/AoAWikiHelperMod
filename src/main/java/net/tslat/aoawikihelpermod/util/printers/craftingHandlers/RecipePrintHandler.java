package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import org.apache.commons.lang3.tuple.Triple;

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
		RecipePrintHandler create(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable IRecipe<?> recipe);
	}

	public static class RecipeIngredientsHandler {
		public static final String[] slotPrefixes = new String[] {"a1", "a2", "a3", "b1", "b2", "b3", "c1", "c2", "c3"};

		private final NonNullList<Triple<String, Integer, String>> ingredients;
		private final NonNullList<Integer> ingredientsIndex;

		@Nullable
		private ItemStack output = null;

		public RecipeIngredientsHandler(int ingredientsSize) {
			this.ingredients = NonNullList.withSize(ingredientsSize, Triple.of("", 0, ""));
			this.ingredientsIndex = NonNullList.withSize(ingredientsSize, 0);
		}

		public ArrayList<String> getIngredientsWithSlots() {
			ArrayList<String> lines = new ArrayList<String>(slotPrefixes.length);

			for (int index = 0; index < slotPrefixes.length; index++) {
				String ingName = ingredients.get(ingredientsIndex.get(index)).getLeft();

				if (!ingName.isEmpty())
					lines.add(slotPrefixes[index] + "=" + ingName);
			}

			return lines;
		}

		public String getIngredient(int slot) {
			return this.ingredients.get(ingredientsIndex.get(slot)).getLeft();
		}

		public ImmutableList<Triple<String, Integer, String>> getIngredients() {
			return ImmutableList.copyOf(ingredients);
		}

		public String getFormattedIngredientsList(@Nullable Item targetItem) {
			StringBuilder builder = new StringBuilder();

			for (Triple<String, Integer, String> ing : getIngredients()) {
				if (builder.length() > 0)
					builder.append(" +<br/>");

				builder.append(ing.getMiddle());
				builder.append(" ");

				if (ing.getLeft().contains(":")) {
					builder.append(FormattingHelper.createLinkableTag(ing.getLeft()));
				}
				else {
					builder.append(FormattingHelper.createLinkableItem(ing.getLeft(), ing.getMiddle() > 1, ing.getRight().equals("minecraft"), targetItem == null || !ObjectHelper.getItemName(targetItem).equals(ing.getLeft())));
				}
			}

			return builder.toString();
		}

		@Nullable
		public ItemStack getOutput() {
			return this.output;
		}

		public String getOutputFormatted(@Nullable Item targetItem) {
			return this.output.getCount() + " " + FormattingHelper.createLinkableItem(this.output, this.output.getItem() != targetItem);
		}

		public void addOutput(JsonObject json) {
			this.output = CraftingHelper.getItemStack(json, true);
		}

		public void addOutput(ItemStack stack) {
			this.output = stack;
		}

		public String addIngredient(JsonObject json) {
			return addIngredient(json, this.ingredientsIndex.size());
		}

		public String addIngredient(JsonObject json, int position) {
			Pair<String, String> id = ObjectHelper.getIngredientName(json);

			addIngredient(id.getFirst(), id.getSecond(), position);

			return id.getSecond();
		}

		public void addIngredient(String ingredientName, String ownerId) {
			addIngredient(ingredientName, ownerId, this.ingredientsIndex.size());
		}

		public void addIngredient(String ingredientName, String ownerId, int position) {
			int matchedIndex = -1;

			for (int i = 0; i < ingredients.size(); i++) {
				if (ingredients.get(i).getLeft().equals(ingredientName)) {
					matchedIndex = i;

					break;
				}
			}

			if (matchedIndex == -1) {
				this.ingredientsIndex.set(position, this.ingredients.size());
				this.ingredients.add(Triple.of(ingredientName, 1, ownerId));
			}
			else {
				this.ingredientsIndex.set(position, matchedIndex);
				this.ingredients.set(matchedIndex, Triple.of(ingredientName, this.ingredients.get(matchedIndex).getMiddle() + 1, ownerId));
			}
		}
	}
}
