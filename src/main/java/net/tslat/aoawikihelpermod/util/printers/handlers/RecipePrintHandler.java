package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class RecipePrintHandler {
	public abstract String[] toTableEntry(@Nullable Item targetItem);
	public abstract String getTableGroup();
	public abstract ResourceLocation getRecipeId();
	public abstract String[] getColumnTitles();
	public abstract List<ResourceLocation> getIngredientsForLookup();
	public abstract List<ResourceLocation> getOutputsForLookup();

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
		private Triple<Integer, String, String> output = null;

		public RecipeIngredientsHandler(int ingredientsSize) {
			this.ingredients = NonNullList.create();
			this.ingredientsIndex = NonNullList.withSize(ingredientsSize, -1);
		}

		public ArrayList<String> getIngredientsWithSlots() {
			ArrayList<String> lines = new ArrayList<String>(slotPrefixes.length);

			for (int index = 0; index < slotPrefixes.length; index++) {
				if (index >= ingredientsIndex.size())
					break;

				int ingredientIndex = ingredientsIndex.get(index);

				if (ingredientIndex < 0)
					continue;

				String ingName = ingredients.get(ingredientIndex).getRight();

				if (!ingName.isEmpty())
					lines.add(slotPrefixes[index] + "=" + ingName);
			}

			return lines;
		}

		public String getIngredient(int slot) {
			if (slot >= ingredients.size() || ingredientsIndex.get(slot) < 0)
				return "";

			return this.ingredients.get(ingredientsIndex.get(slot)).getRight();
		}

		public ImmutableList<Triple<String, Integer, String>> getIngredients() {
			return ImmutableList.copyOf(ingredients);
		}

		public String getFormattedIngredientsList(@Nullable Item targetItem) {
			StringBuilder builder = new StringBuilder();

			for (Triple<String, Integer, String> ing : getIngredients()) {
				if (ing.getRight().isEmpty())
					continue;

				if (builder.length() > 0)
					builder.append(" +<br/>");

				builder.append(ing.getMiddle());
				builder.append(" ");

				if (ing.getRight().contains(":")) {
					builder.append(FormattingHelper.createLinkableTag(ing.getRight()));
				}
				else {
					builder.append(FormattingHelper.createLinkableText(ing.getRight(), ing.getMiddle() > 1, ing.getLeft().equals("minecraft"), targetItem == null || !ObjectHelper.getItemName(targetItem).equals(ing.getRight())));
				}
			}

			return builder.toString();
		}

		public Triple<Integer, String, String> getOutput() {
			return this.output;
		}

		public String getFormattedOutput(@Nullable Item targetItem) {
			return FormattingHelper.createLinkableText(output.getRight(), false, output.getMiddle().equals("minecraft"), (targetItem == null || !output.getRight().equals(ObjectHelper.getItemName(targetItem))));
		}

		public void addOutput(JsonObject json) {
			this.output = ObjectHelper.getStackDetailsFromJson(json);
		}

		public void addOutput(ItemStack stack) {
			Pair<String, String> names = ObjectHelper.getFormattedItemDetails(stack.getItem().getRegistryName());
			this.output = Triple.of(stack.getCount(), names.getFirst(), names.getSecond());
		}

		public String addIngredient(JsonElement element) {
			if (element.isJsonObject())
				return addIngredient(element.getAsJsonObject());

			if (element.isJsonArray()) {
				JsonArray array = element.getAsJsonArray();
				Pair<String, String> backup = null;
				ITag<Item> potentialTag = null;

				for (JsonElement ele : array) {
					if (ele.isJsonObject()) {
						return addIngredient(ele.getAsJsonObject());
					}
					else if (ele.isJsonPrimitive() && backup == null) {
						Pair<String, String> id = ObjectHelper.getFormattedItemDetails(new ResourceLocation(ele.getAsString()));

						addIngredient(id.getSecond(), id.getFirst(), -1);

						return id.getSecond();
					}
				}
			}

			if (element.isJsonPrimitive()) {
				Pair<String, String> id = ObjectHelper.getFormattedItemDetails(new ResourceLocation(element.getAsString()));

				addIngredient(id.getSecond(), id.getFirst(), -1);

				return id.getSecond();
			}

			throw new JsonParseException("Unrecognised json type for recipe, skipping.");
		}

		public String addIngredient(JsonObject json) {
			return addIngredient(json, -1);
		}

		public String addIngredient(JsonObject json, int position) {
			Pair<String, String> id = ObjectHelper.getIngredientName(json);

			addIngredient(id.getSecond(), id.getFirst(), position);

			return id.getSecond();
		}

		public void addIngredient(String ingredientName, String ownerId) {
			addIngredient(ingredientName, ownerId, -1);
		}

		public void addIngredient(String ingredientName, String ownerId, int position) {
			int matchedIndex = -1;

			if (position == -1) {
				for (int i = 0; i < ingredientsIndex.size(); i++) {
					if (ingredientsIndex.get(i) == -1) {
						position = i;

						break;
					}
				}
			}

			for (int i = 0; i < ingredients.size(); i++) {
				if (ingredients.get(i).getRight().equals(ingredientName)) {
					matchedIndex = i;

					break;
				}
			}

			if (matchedIndex == -1) {
				this.ingredientsIndex.set(position, this.ingredients.size());
				this.ingredients.add(Triple.of(ownerId, 1, ingredientName));
			}
			else {
				this.ingredientsIndex.set(position, matchedIndex);
				this.ingredients.set(matchedIndex, Triple.of(ownerId, this.ingredients.get(matchedIndex).getMiddle() + 1, ingredientName));
			}
		}
	}
}
