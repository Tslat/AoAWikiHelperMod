package net.tslat.aoawikihelpermod.recipes;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public abstract class IRecipeInterface {
	protected IRecipe recipe;
	protected JsonObject json;
	protected ArrayList<IRecipeInterfaceIngredient> ingredientArray = new ArrayList<IRecipeInterfaceIngredient>();
	protected ArrayList<String> ingredientNameBySlot = new ArrayList<String>();
	protected final String[] slotPrefixes = new String[] {"a1", "a2", "a3", "b1", "b2", "b3", "c1", "c2", "c3"};

	public IRecipeInterface(IRecipe recipe, JsonObject json) {
		this.recipe = recipe;
		this.json = json;

		populateIngredientArrays();
	}

	@Nullable
	protected static String getOreDict(ItemStack[] commonStacks) {
		HashSet<String> matchingIds = null;

		for (ItemStack stack : commonStacks) {
			int[] oreIds = OreDictionary.getOreIDs(stack);

			if (oreIds.length == 0)
				return null;

			if (matchingIds == null) {
				matchingIds = new HashSet<String>(oreIds.length);

				for (int i : oreIds) {
					matchingIds.add(OreDictionary.getOreName(i));
				}
			}
			else {
				HashSet<String> commonIds = new HashSet<String>(oreIds.length);

				for (int i : oreIds) {
					String ore = OreDictionary.getOreName(i);

					if (matchingIds.contains(ore))
						commonIds.add(ore);
				}

				if (commonIds.isEmpty())
					return null;

				matchingIds = commonIds;
			}
		}

		if (matchingIds == null || matchingIds.size() != 1)
			return null;

		return matchingIds.iterator().next();
	}

	protected abstract void populateIngredientArrays();
	protected abstract String buildWikiTableHeadingsLine(ArrayListMultimap<String, IRecipe> matchedRecipes);
	protected abstract String buildIngredientSummaryLine(ItemStack targetStack);
	protected abstract String getWikiTemplateName();
	protected abstract ArrayList<String> buildAdditionalTemplateLines(ItemStack targetStack);

	protected void addIngredient(String ingredientName, @Nullable String oreDictName) {
		ingredientNameBySlot.add(ingredientName.replace("mcw:", ""));

		IRecipeInterfaceIngredient ing = new IRecipeInterfaceIngredient(ingredientName, oreDictName);

		for (IRecipeInterfaceIngredient ing2 : ingredientArray) {
			if (ing2.matchingIngredient(ing)) {
				ing2.increment();

				return;
			}
		}

		ingredientArray.add(ing);
	}

	protected static class IRecipeInterfaceIngredient {
		private int ingredientCount = 1;
		private final String ingredientName;
		private final String oreDictName;

		protected IRecipeInterfaceIngredient(String ingredientName, @Nullable String oreDictName) {
			this.ingredientName = ingredientName;
			this.oreDictName = oreDictName;
		}

		protected void increment() {
			this.ingredientCount += 1;
		}

		protected int getCount() {
			return this.ingredientCount;
		}

		protected String getIngredientName() {
			return this.ingredientName;
		}

		protected String getOreDictName() {
			return this.oreDictName;
		}

		protected boolean matchingIngredient(IRecipeInterfaceIngredient ingredient) {
			return this.ingredientName.equals(ingredient.ingredientName) && Objects.equals(this.oreDictName, ingredient.oreDictName);
		}
	}
}
