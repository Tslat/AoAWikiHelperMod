package net.tslat.aoawikihelpermod.util.printers.craftingHandlers;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class RecipePrintHandler {
	protected static final String[] slotPrefixes = new String[] {"a1", "a2", "a3", "b1", "b2", "b3", "c1", "c2", "c3"};

	protected final ArrayList<RecipeHandlerItem> recipeItems = new ArrayList<RecipeHandlerItem>();
	protected final ArrayList<Integer> itemIndex = new ArrayList<Integer>();

	public abstract String[] toTableEntry(@Nullable Item targetItem);
	public abstract String getTableGroup();
	public abstract ResourceLocation getRecipeId();

	public interface Factory {
		RecipePrintHandler create(JsonObject rawRecipe, IRecipe<?> recipe);
	}

	protected static class RecipeHandlerItem {
		private int count = 1;
		private final String name;
	}
}
