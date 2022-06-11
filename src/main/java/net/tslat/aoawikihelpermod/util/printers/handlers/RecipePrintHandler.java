package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

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
		RecipePrintHandler create(ResourceLocation recipeId, JsonObject rawRecipe, @Nullable Recipe<?> recipe);
	}

	public static class RecipeIngredientsHandler {
		public static final String[] slotPrefixes = new String[] {"a1", "a2", "a3", "b1", "b2", "b3", "c1", "c2", "c3"};

		private final NonNullList<PrintableIngredient> ingredients;
		private final NonNullList<Integer> ingredientsIndex;
		private PrintableIngredient output = null;

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

				PrintableIngredient ing = ingredients.get(ingredientIndex);

				if (!ing.formattedName.isEmpty()) {
					String slotPrefix = slotPrefixes[index];

					if (ing.imageName != null) {
						lines.add(slotPrefix + "image=" + ing.imageName);
						lines.add(slotPrefix + "=" + ing.imageName.substring(0, ing.imageName.lastIndexOf(".")));
					}
					else {
						lines.add(slotPrefix + "=" + ing.formattedName);
					}
				}
			}

			return lines;
		}

		public String getIngredient(int slot) {
			if (slot >= ingredients.size() || ingredientsIndex.get(slot) < 0)
				return "";

			return this.ingredients.get(ingredientsIndex.get(slot)).formattedName;
		}

		public ImmutableList<PrintableIngredient> getIngredients() {
			return ImmutableList.copyOf(ingredients);
		}

		public String getFormattedIngredientsList(@Nullable Item targetItem) {
			StringBuilder builder = new StringBuilder();

			for (PrintableIngredient ing : getIngredients()) {
				if (ing.formattedName.isEmpty())
					continue;

				if (builder.length() > 0)
					builder.append(" +<br/>");

				builder.append(ing.count);
				builder.append(" ");

				if (ing.isTag()) {
					builder.append(FormattingHelper.createLinkableTag(ing.formattedName, Items.STONE));
				}
				else {
					builder.append(FormattingHelper.createLinkableText(ing.formattedName, ing.count > 1, targetItem == null || !ing.matches(ObjectHelper.getItemName(targetItem)), ing.formattedName.contains(" (item)") ? ing.formattedName.replace(" (item)", "") : null));
				}
			}

			return builder.toString();
		}

		public PrintableIngredient getOutput() {
			return this.output;
		}

		public String getFormattedOutput(@Nullable Item targetItem) {
			return FormattingHelper.createLinkableText(output.formattedName, false, (targetItem == null || !output.matches(ObjectHelper.getItemName(targetItem))));
		}

		public void addOutput(JsonObject json) {
			this.output = ObjectHelper.getStackDetailsFromJson(json);
		}

		public void addOutput(ItemStack stack) {
			this.output = ObjectHelper.getFormattedItemDetails(ForgeRegistries.ITEMS.getKey(stack.getItem()));
			this.output.count = stack.getCount();
		}

		public String addIngredient(JsonElement element) {
			if (element.isJsonObject())
				return addIngredient(element.getAsJsonObject());

			if (element.isJsonArray()) {
				JsonArray array = element.getAsJsonArray();
				ITag<Item> potentialTag = null;

				for (JsonElement ele : array) {
					if (ele.isJsonObject()) {
						return addIngredient(ele.getAsJsonObject());
					}
					else if (ele.isJsonPrimitive()) {
						PrintableIngredient ingredient = ObjectHelper.getFormattedItemDetails(new ResourceLocation(ele.getAsString()));

						addIngredient(ingredient, -1);

						return ingredient.formattedName;
					}
				}
			}

			if (element.isJsonPrimitive()) {
				PrintableIngredient ingredient = ObjectHelper.getFormattedItemDetails(new ResourceLocation(element.getAsString()));

				addIngredient(ingredient, -1);

				return ingredient.formattedName;
			}

			throw new JsonParseException("Unrecognised json type for recipe, skipping.");
		}

		public String addIngredient(JsonObject json) {
			return addIngredient(json, -1);
		}

		public String addIngredient(JsonObject json, int position) {
			PrintableIngredient ingredient = ObjectHelper.getIngredientName(json);

			addIngredient(ingredient, position);

			return ingredient.formattedName;
		}

		public void addIngredient(String ingredientName, String ownerId) {
			addIngredient(ingredientName, ownerId, -1);
		}

		public void addIngredient(String ingredientName, String ownerId, int position) {
			addIngredient(new PrintableIngredient(ownerId, ingredientName), position);
		}

		public void addIngredient(PrintableIngredient ingredient, int position) {
			if (position == -1) {
				for (int i = 0; i < ingredientsIndex.size(); i++) {
					if (ingredientsIndex.get(i) == -1) {
						position = i;

						break;
					}
				}
			}

			for (int i = 0; i < ingredients.size(); i++) {
				if (ingredients.get(i).matches(ingredient.formattedName)) {
					this.ingredientsIndex.set(position, i);
					this.ingredients.get(i).increment();

					return;
				}
			}


			this.ingredientsIndex.set(position, this.ingredients.size());
			this.ingredients.add(ingredient);
		}
	}

	public static class PrintableIngredient {
		public final String ownerId;
		public final String formattedName;
		@Nullable
		public String imageName = null;
		public int count;

		public PrintableIngredient(String ownerId, String formattedName, int count) {
			this.ownerId = ownerId;
			this.formattedName = formattedName;
			this.count = count;
		}

		public PrintableIngredient(String ownerId, String formattedName) {
			this(ownerId, formattedName, 1);
		}

		public void increment() {
			this.count += 1;
		}

		public boolean isVanilla() {
			return this.ownerId.equals("minecraft");
		}

		public boolean isTag() {
			return this.formattedName.contains(":");
		}

		public boolean matches(String other) {
			return this.formattedName.equals(other);
		}

		public void setCustomImageName(String imageName) {
			this.imageName = imageName;
		}
	}
}
