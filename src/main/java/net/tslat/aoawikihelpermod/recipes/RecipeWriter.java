package net.tslat.aoawikihelpermod.recipes;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.registries.ForgeRegistries;
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

    public static void registerRecipeInterface(String recipeClassSimpleName, Class<? extends IRecipeInterface> handler) {
        recipeInterfaces.put(recipeClassSimpleName, handler);
    }

    public static boolean haveRecipeInterface(IRecipe recipe) {
        return recipeInterfaces.containsKey(recipe.getClass().getSimpleName());
    }

    public static IRecipeInterface findRecipeInterface(IRecipe recipe) {
        String recipeClassName = recipe.getClass().getSimpleName();

        try {
            if (recipeInterfaces.containsKey(recipeClassName)) {
                return recipeInterfaces.get(recipeClassName).getConstructor(IRecipe.class, JsonObject.class).newInstance(recipe, recipeMap.get(recipe.getId()));
            }
        }
        catch (Exception ex) {
            AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Unable to instantiate new recipe interface for given recipe type: " + recipeClassName);
            ex.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static JsonObject getRecipeJson(IRecipe recipe) {
        return recipeMap.get(recipe.getId());
    }

    public static void printItemRecipeUsages(@Nonnull ItemStack targetStack, ICommandSource sender, RecipeManager manager, boolean copyToClipboard) {
        if (writer != null) {
            sender.sendMessage(new StringTextComponent("You're already outputting data! Wait a moment and try again"));

            return;
        }

        ArrayListMultimap<String, IRecipe> matchedRecipes = ArrayListMultimap.<String, IRecipe>create();
        Item recipeItem = targetStack.getItem();

        recipeSearch:
        for (IRecipe recipe : manager.getRecipes()) {

            if (recipe.getId() == null || !recipe.getId().getNamespace().equals("aoa3"))
                continue;

            if (!haveRecipeInterface(recipe))
                continue;

            IRecipeInterface recipeInterface = findRecipeInterface(recipe);

            if (recipeInterface == null)
                continue;

            String recipeGroup = recipeInterface.recipeGroup();

            for (Object i: recipe.getIngredients()) {
                if(!(i instanceof Ingredient)) {
                    return;
                }

                Ingredient ingredient = (Ingredient)i;

                if (ingredient.test(targetStack)) {
                    matchedRecipes.put(recipeGroup, recipe);

                    continue recipeSearch;
                }
            }

            if (recipeInterface.matchAdditionalIngredients(targetStack))
                matchedRecipes.put(recipeGroup, recipe);
        }

        String fileName = recipeItem.getDisplayName(targetStack).getString() + " Usages.txt";
        String lastKey = "";
        boolean printImageLines = matchedRecipes.values().size() > 20;
        int count = 0;

        enableWriter(fileName);

        for (String key : matchedRecipes.keySet().stream().sorted(Comparator.<String>naturalOrder()).collect(Collectors.toList())) {
            for (IRecipe recipe : matchedRecipes.get(key).stream().sorted(new RecipeOutputComparator()).collect(Collectors.toList())) {
                IRecipeInterface recipeInterface = findRecipeInterface(recipe);

                if (recipeInterface == null)
                    continue;

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
                write("| " + recipeInterface.buildSummaryLine(targetStack, matchedRecipes));

                for (String string : recipeInterface.buildAdditionalTemplateLines(targetStack, printImageLines)) {
                    write(string);
                }

                write("}}");
                count++;
            }
        }

        write("|-");
        write("|}");
        disableWriter();
        sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Printed out " + count + " recipes containing ", new File(configDir, fileName), targetStack.getDisplayName().getString(), copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
    }

    public static void printItemRecipes(ItemStack targetStack, ICommandSource sender, RecipeManager manager, boolean copyToClipboard) {

        if (writer != null) {
            sender.sendMessage(new StringTextComponent("You're already outputting data! Wait a moment and try again"));

            return;
        }

        ArrayListMultimap<String, IRecipe> matchedRecipes = ArrayListMultimap.<String, IRecipe>create();
        Item recipeItem = targetStack.getItem();

        for (IRecipe recipe : manager.getRecipes()) {
            if (recipe.getId() == null || !recipe.getId().getNamespace().equals("aoa3"))
                continue;

            if (!haveRecipeInterface(recipe))
                continue;

            IRecipeInterface recipeInterface = findRecipeInterface(recipe);

            if (recipeInterface == null)
                continue;

            String recipeGroup = recipeInterface.recipeGroup();

            if (recipe.getRecipeOutput().getItem() == recipeItem)
                matchedRecipes.put(recipeGroup, recipe);
        }

        String fileName = recipeItem.getDisplayName(targetStack).getString() + " Recipes.txt";
        String lastKey = "";
        boolean printImageLines = matchedRecipes.values().size() > 20;
        int count = 0;

        enableWriter(fileName);

        for (String key : matchedRecipes.keySet().stream().sorted(Comparator.<String>naturalOrder()).collect(Collectors.toList())) {
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
                write("| '''" + recipe.getRecipeOutput().getDisplayName().getString() + "''' || " + recipeInterface.buildIngredientSummaryLine(targetStack, matchedRecipes) + " || {{" + recipeInterface.getWikiTemplateName());

                for (String string : recipeInterface.buildAdditionalTemplateLines(targetStack, printImageLines)) {
                    write(string);
                }

                write("}}");
                count++;
            }
        }

        write("|-");
        write("|}");

        disableWriter();
        sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Printed out " + count + " recipes for ", new File(configDir, fileName), targetStack.getDisplayName().getString(), copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
    }

    private static void enableWriter(final String fileName) {
        configDir = AoAWikiHelperMod.prepConfigDir("Recipes");

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
            ModInfo info = (ModInfo)aoaModContainer.getModInfo();
            ModFileInfo fileInfo = info.getOwningFile();
            ModFile file = fileInfo.getFile();

            FileSystem fs = FileSystems.newFileSystem(file.getFilePath(), null);
            Path rootPath = fs.getPath("/data/aoa3/recipes");
/*
            if (modSource.isFile()) {
                FileSystem fs = FileSystems.newFileSystem(modSource.toPath(), null);
                rootPath = fs.getPath("/assets/aoa3/recipes");
            }
            else {
                rootPath = modSource.toPath().resolve("assets/aoa3/recipes");
            }*/

            Iterator<Path> recipeFiles = Files.walk(rootPath).iterator();

            while (recipeFiles.hasNext()) {
                Path recipePath = recipeFiles.next();

                if (!"json".equals(FilenameUtils.getExtension(recipePath.toString())) || recipePath.toString().contains("_factories"))
                    continue;

                String relativePathString = rootPath.relativize(recipePath).toString();
                ResourceLocation key = new ResourceLocation("aoa3", FilenameUtils.removeExtension(relativePathString).replaceAll("\\\\", "/"));
                BufferedReader reader = Files.newBufferedReader(recipePath);
                Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
                JsonObject recipeJson = JSONUtils.fromJson(gson, reader, JsonObject.class);

                recipeMap.put(key, recipeJson);
            }

            AoAWikiHelperMod.LOGGER.log(Level.INFO, "Found " + recipeMap.size() + " recipes to log");
        }
        catch (Exception e) {
            AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Unexpected error while parsing recipes from resource folder.");
            e.printStackTrace();
        }
    }

    private static class RecipeOutputComparator implements Comparator<IRecipe> {
        @Override
        public int compare(IRecipe recipe1, IRecipe recipe2) {
            String outputName = recipe1.getRecipeOutput().getDisplayName().getString();
            String outputName2 = recipe2.getRecipeOutput().getDisplayName().getString();
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

            try {
                IRecipeInterface recipeInterface = findRecipeInterface(recipe1);

                return (int)recipeInterface.getClass().getMethod("sortingCompare", IRecipeInterface.class).invoke(recipeInterface, findRecipeInterface(recipe2));
            }
            catch (Exception e) {
                System.out.println("Can't find the recipe interface's class.. somehow. I don't even know, really.");
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