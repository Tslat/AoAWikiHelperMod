package net.tslat.aoawikihelpermod.recipes;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;

public class RecipeWriter {
	protected static HashMap<ResourceLocation, JsonObject> recipeMap = new HashMap<ResourceLocation, JsonObject>();
	private static HashMap<String, Class<? extends IRecipeInterface>> recipeInterfaces = new HashMap<String, Class<? extends IRecipeInterface>>(3);
	public static File configDir = null;
	private static PrintWriter writer = null;

	public static void setConfigDir(File file) {
		if (configDir == null) {
			configDir = file;

			if (!configDir.exists())
				configDir.mkdirs();
		}
	}

	public static void registerRecipeInterface(String recipeClassSimpleName, Class<? extends IRecipeInterface> handler) {
		recipeInterfaces.put(recipeClassSimpleName, handler);
	}

	private static IRecipeInterface findRecipeInterface(IRecipe recipe) {
		String recipeClassName = recipe.getClass().getSimpleName();

		try {
			if (recipeInterfaces.containsKey(recipeClassName))
				return recipeInterfaces.get(recipeClassName).getConstructor(IRecipe.class, JsonObject.class).newInstance(recipe, recipeMap.get(recipe.getRegistryName()));
		}
		catch (Exception ex) {
			AoAWikiHelperMod.logger.log(Level.ERROR, "Unable to instantiate new recipe interface for given recipe type: " + recipeClassName);
			ex.printStackTrace();
		}

		return null;
	}

	@Nullable
	public static JsonObject getRecipeJson(IRecipe recipe) {
		return recipeMap.get(recipe.getRegistryName());
	}

	public static void printItemRecipeUsages(ItemStack targetStack) {
		if (targetStack.isEmpty())
			return;

		if (writer != null) {
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString("You're already outputting data! Wait a moment and try again"));

			return;
		}

		ArrayListMultimap<String, IRecipe> matchedRecipes = ArrayListMultimap.<String, IRecipe>create();
		Item recipeItem = targetStack.getItem();

		recipeSearch:
		for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
			for (Ingredient ingredient : recipe.getIngredients()) {
				if (ingredient.apply(targetStack)) {
					matchedRecipes.put(recipe.getClass().getSimpleName(), recipe);

					continue recipeSearch;
				}
			}
		}

		enableWriter(recipeItem.getItemStackDisplayName(targetStack) + " Usages.txt");

		String lastKey = "";
		int count = 0;

		for (String key : matchedRecipes.keySet().stream().sorted(Comparator.<String>naturalOrder().reversed()).collect(Collectors.toList())) {
			for (IRecipe recipe : matchedRecipes.get(key).stream().sorted(new RecipeOutputComparator()).collect(Collectors.toList())) {
				IRecipeInterface recipeInterface = findRecipeInterface(recipe);

				if (recipeInterface == null)
					return;

				if (!recipeInterface.getWikiTemplateName().equals(lastKey)) {
					if (!lastKey.equals("")) {
						write("|-");
						write("|}");
					}

					write("");
					write("=== " + recipeInterface.getWikiTemplateName() + " ===");
					write("{|class=\"wikitable\"");
					write("|-");
					write(recipeInterface.buildWikiTableHeadingsLine(matchedRecipes));
					lastKey = recipeInterface.getWikiTemplateName();
				}

				write("|-");
				write("| '''[[" + recipe.getRecipeOutput().getDisplayName() + "]]''' || " + recipeInterface.buildIngredientSummaryLine(targetStack) + " || {{" + recipeInterface.getWikiTemplateName());

				for (String string : recipeInterface.buildAdditionalTemplateLines(targetStack)) {
					write(string);
				}

				write("}}");
				count++;
			}
		}

		write("|-");
		write("|}");

		disableWriter();
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Printed out " + count + " recipes containing " + TextFormatting.DARK_BLUE + targetStack.getDisplayName()));
	}

	public static void printItemRecipes(ItemStack targetStack) {
		if (targetStack.isEmpty())
			return;

		if (writer != null) {
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString("You're already outputting data! Wait a moment and try again"));

			return;
		}

		ArrayListMultimap<String, IRecipe> matchedRecipes = ArrayListMultimap.<String, IRecipe>create();
		Item recipeItem = targetStack.getItem();

		for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
			if (recipe.getRecipeOutput().getItem() == recipeItem)
				matchedRecipes.put(recipe.getClass().getSimpleName(), recipe);
		}

		enableWriter(recipeItem.getItemStackDisplayName(targetStack) + " Recipes.txt");

		String lastKey = "";
		int count = 0;

		for (String key : matchedRecipes.keySet().stream().sorted(Comparator.<String>naturalOrder().reversed()).collect(Collectors.toList())) {
			for (IRecipe recipe : matchedRecipes.get(key).stream().sorted(new RecipeOutputComparator()).collect(Collectors.toList())) {
				IRecipeInterface recipeInterface = findRecipeInterface(recipe);

				if (recipeInterface == null)
					return;

				if (!recipeInterface.getWikiTemplateName().equals(lastKey)) {
					if (!lastKey.equals("")) {
						write("|-");
						write("|}");
					}

					write("");
					write("=== " + recipeInterface.getWikiTemplateName() + " ===");
					write("{|class=\"wikitable\"");
					write("|-");
					write(recipeInterface.buildWikiTableHeadingsLine(matchedRecipes));
					lastKey = recipeInterface.getWikiTemplateName();
				}

				write("|-");
				write("| '''" + recipe.getRecipeOutput().getDisplayName() + "''' || " + recipeInterface.buildIngredientSummaryLine(targetStack) + " || {{" + recipeInterface.getWikiTemplateName());

				for (String string : recipeInterface.buildAdditionalTemplateLines(targetStack)) {
					write(string);
				}

				write("}}");
				count++;
			}
		}

		write("|-");
		write("|}");

		disableWriter();
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Printed out " + count + " recipes for " + TextFormatting.DARK_BLUE + targetStack.getDisplayName()));
	}

	private static void enableWriter(final String fileName) {
		if (!configDir.exists())
			configDir.mkdirs();

		File streamFile = new File(configDir, fileName);

		try {
			if (streamFile.exists())
				streamFile.delete();

			streamFile.createNewFile();

			writer = new PrintWriter(streamFile);
		}
		catch (Exception e) {}
	}

	private static void write(String line) {
		if (writer != null)
			writer.println(line);
	}

	private static void disableWriter() {
		if (writer != null)
			IOUtils.closeQuietly(writer);

		writer = null;
	}

	public static void scrapeForRecipes(@Nonnull ModContainer aoaModContainer) {
		try {
			File modSource = aoaModContainer.getSource();
			Path rootPath;

			if (modSource.isFile()) {
				FileSystem fs = FileSystems.newFileSystem(modSource.toPath(), null);
				rootPath = fs.getPath("/assets/aoa3/recipes");
			}
			else {
				rootPath = modSource.toPath().resolve("assets/aoa3/recipes");
			}

			Iterator<Path> recipeFiles = Files.walk(rootPath).iterator();

			while (recipeFiles.hasNext()) {
				Path recipePath = recipeFiles.next();

				if (!"json".equals(FilenameUtils.getExtension(recipePath.toString())) || recipePath.toString().contains("_factories"))
					continue;

				String relativePathString = rootPath.relativize(recipePath).toString();
				ResourceLocation key = new ResourceLocation("aoa3", FilenameUtils.removeExtension(relativePathString).replaceAll("\\\\", "/"));
				BufferedReader reader = Files.newBufferedReader(recipePath);
				Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
				JsonObject recipeJson = JsonUtils.fromJson(gson, reader, JsonObject.class);

				recipeMap.put(key, recipeJson);
			}

			AoAWikiHelperMod.logger.log(Level.INFO, "Found " + recipeMap.size() + " recipes to log");
		}
		catch (Exception e) {
			AoAWikiHelperMod.logger.log(Level.ERROR, "Unexpected error while parsing recipes from resource folder.");
			e.printStackTrace();
		}
	}

	private static class RecipeOutputComparator implements Comparator<IRecipe> {
		@Override
		public int compare(IRecipe recipe1, IRecipe recipe2) {
			String outputName = recipe1.getRecipeOutput().getDisplayName();
			String outputName2 = recipe2.getRecipeOutput().getDisplayName();
			int output1Weight = getArmourOrderWeight(outputName);

			if (output1Weight >= 0) {
				int output2Weight = getArmourOrderWeight(outputName2);

				if (output2Weight >= 0) {
					String setName = outputName.split(" ")[0];
					String setName2 = outputName2.split(" ")[0];

					if (setName.equals(setName2))
						return Integer.compare(output1Weight, output2Weight);
				}
			}

			return outputName.compareTo(outputName2);
		}

		private int getArmourOrderWeight(String name) {
			if (name.contains("Helmet"))
				return 0;

			if (name.contains("Chestplate"))
				return 1;

			if (name.contains("Leggings"))
				return 2;

			if (name.contains("Boots"))
				return 3;

			return -1;
		}
	}
}
