package net.tslat.aoawikihelpermod;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.tslat.aoawikihelpermod.recipes.RecipeInterfaceInfusion;
import net.tslat.aoawikihelpermod.recipes.RecipeInterfaceShaped;
import net.tslat.aoawikihelpermod.recipes.RecipeInterfaceShapeless;
import net.tslat.aoawikihelpermod.recipes.RecipeWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = "aoawikihelpermod", name = "AoA-Wiki Helper Mod", version = "1.0")
public class AoAWikiHelperMod {
	public static Logger logger;
	private static ModContainer aoaModContainer;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		aoaModContainer = Loader.instance().getIndexedModList().get("aoa3");

		if (aoaModContainer == null) {
			logger.log(Level.FATAL, "Unable to find AoA3 mod container. Shutting down");
			Loader.instance().runtimeDisableMod("aoawikihelpermod");

			return;
		}

		if (event.getSide() == Side.CLIENT)
			KeyBindings.init();



		prepRecipeWriter(event.getModConfigurationDirectory());
	}

	private static void prepRecipeWriter(File modConfigDir) {
		RecipeWriter.setConfigDir(new File(modConfigDir, "AoAWikiHelper"));
		RecipeWriter.scrapeForRecipes(aoaModContainer);
		RecipeWriter.registerRecipeInterface("ShapelessRecipes", RecipeInterfaceShapeless.class);
		RecipeWriter.registerRecipeInterface("InfusionTableRecipe", RecipeInterfaceInfusion.class);
		RecipeWriter.registerRecipeInterface("ShapedRecipes", RecipeInterfaceShaped.class);
	}

	/*private static ArrayList<Tuple<Integer, Tuple<String, String>>> buildIngredientsArrayFromList(NonNullList<Ingredient> ingredientsList) {
		ArrayList<Tuple<Integer, Tuple<String, String>>> ingredients = new ArrayList<Tuple<Integer, Tuple<String, String>>>();
		int indexMarker = 0;

		for (Ingredient ing : ingredientsList) {
			ItemStack[] matchingStacks = ing.getMatchingStacks();
			String ingredientName = "";
			String oreDictName = null;

			if (matchingStacks.length > 1)
				oreDictName = getOreDict(matchingStacks);

			ingredientName = matchingStacks[0].getDisplayName();

			if (ingredients.isEmpty()) {
				ingredients.add(new Tuple<Integer, Tuple<String, String>>(1, new Tuple<String, String>(ingredientName, oreDictName)));
			}
			else {
				ListIterator<Tuple<Integer, Tuple<String, String>>> iterator = ingredients.listIterator();

				while (iterator.hasNext()) {
					Tuple<Integer, Tuple<String, String>> entry = iterator.next();

					if (entry.getSecond().getFirst().equals(ingredientName) && entry.getSecond().getSecond().equals(oreDictName)) {
						iterator.set(new Tuple<Integer, Tuple<String, String>>(entry.getFirst() + 1, new Tuple<String, String>(ingredientName, oreDictName)));
						break;
					}
				}
			}
		}

		return ingredients;
	}

	private static String buildIngredientsLineFromArray(ArrayList<Tuple<Integer, Tuple<String, String>>> ingredients) {
		StringBuilder builder = new StringBuilder("|| ");

		for (Tuple<Integer, Tuple<String, String>> ing : ingredients) {

		}
	}*/
}
