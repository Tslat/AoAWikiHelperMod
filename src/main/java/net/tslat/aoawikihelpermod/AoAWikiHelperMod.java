package net.tslat.aoawikihelpermod;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.tslat.aoawikihelpermod.dataprintouts.PrintEntityDataCommand;
import net.tslat.aoawikihelpermod.dataprintouts.PrintHunterCreatureDataCommand;
import net.tslat.aoawikihelpermod.dataprintouts.PrintWeaponsDataCommand;
import net.tslat.aoawikihelpermod.recipes.*;
import net.tslat.aoawikihelpermod.trades.PrintTradeOutputsCommand;
import net.tslat.aoawikihelpermod.trades.PrintTradeUsagesCommand;
import net.tslat.aoawikihelpermod.trades.PrintTraderTradesCommand;
import net.tslat.aoawikihelpermod.weaponcategories.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Mod("aoawikihelpermod")
public class AoAWikiHelperMod
{
    private static ModContainer aoaModContainer;
    private static File configDir;

    public static final Logger LOGGER = LogManager.getLogger();

    public AoAWikiHelperMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void setup(FMLCommonSetupEvent event) {
        aoaModContainer = ModList.get().getModContainerById("aoa3").orElse(null);
        configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), "AoAWikiHelper");
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent evt)
    {
        RecipeWriter.scrapeForRecipes(aoaModContainer);
        RecipeWriter.registerRecipeInterface("InfusionRecipe", RecipeInterfaceInfusion.class);
        RecipeWriter.registerRecipeInterface("ShapedRecipe", RecipeInterfaceShaped.class);
        RecipeWriter.registerRecipeInterface("ShapelessRecipe", RecipeInterfaceShapeless.class);

        PrintEntityDataCommand.register(evt.getCommandDispatcher());
        PrintHunterCreatureDataCommand.register(evt.getCommandDispatcher());
        PrintWeaponsDataCommand.register(evt.getCommandDispatcher());

        PrintInfusionEnchantsCommand.register(evt.getCommandDispatcher());
        PrintItemRecipesCommand.register(evt.getCommandDispatcher());
        PrintItemUsageRecipesCommand.register(evt.getCommandDispatcher());

        PrintTraderTradesCommand.register(evt.getCommandDispatcher());
        PrintTradeUsagesCommand.register(evt.getCommandDispatcher());
        PrintTradeOutputsCommand.register(evt.getCommandDispatcher());

        PrintAxesOverviewCommand.register(evt.getCommandDispatcher());
        PrintBowsOverviewCommand.register(evt.getCommandDispatcher());
        PrintBlastersOverviewCommand.register(evt.getCommandDispatcher());
        PrintCannonsOverviewCommand.register(evt.getCommandDispatcher());
        PrintCrossbowsOverviewCommand.register(evt.getCommandDispatcher());
        PrintGreatbladesOverviewCommand.register(evt.getCommandDispatcher());
        PrintGunsOverviewCommand.register(evt.getCommandDispatcher());
        PrintMaulsOverviewCommand.register(evt.getCommandDispatcher());
        PrintPickaxesOverviewCommand.register(evt.getCommandDispatcher());
        PrintShotgunsOverviewCommand.register(evt.getCommandDispatcher());
        PrintShovelsOverviewCommand.register(evt.getCommandDispatcher());
        PrintSnipersOverviewCommand.register(evt.getCommandDispatcher());
        PrintStavesOverviewCommand.register(evt.getCommandDispatcher());
        PrintSwordsOverviewCommand.register(evt.getCommandDispatcher());
        PrintThrownWeaponsOverviewCommand.register(evt.getCommandDispatcher());
    }

    public static File prepConfigDir(String subdirectory) {
        if (!configDir.exists())
            configDir.mkdirs();

        File subDir = new File(configDir, subdirectory);

        if (!subDir.exists())
            subDir.mkdirs();

        return subDir;
    }

    public static ITextComponent generateInteractiveMessagePrintout(String prefix, File file, String linkName, String suffix) {
        String fileUrl = file.getAbsolutePath().replace("\\", "/");

        return ITextComponent.Serializer.fromJson("{\"translate\":\"" + prefix + "%s" + "\",\"with\":[{\"text\":\"" + linkName + "\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_file\",\"value\":\"" + fileUrl + "\"}}]}").appendText(suffix);
    }

    public static boolean copyFileToClipboard(File streamFile) {
        final StringBuilder content = new StringBuilder();

        try (InputStreamReader streamReader = new InputStreamReader(new FileInputStream(streamFile), StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(streamReader)) {
            reader.lines().forEach(line -> {
                content.append(line);
                content.append("\n");
            });

            Toolkit toolkit = Toolkit.getDefaultToolkit();

            if (toolkit == null || GraphicsEnvironment.isHeadless())
                return false;

            toolkit.getSystemClipboard().setContents(new StringSelection(content.toString()), null);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Unable to copy contents of file to clipboard, skipping");
        }

        return false;
    }

    public static String capitaliseAllWords(@Nonnull String input) {
        if (input.isEmpty())
            return input;

        StringBuilder buffer = new StringBuilder(input.length()).append(Character.toTitleCase(input.charAt(0)));

        for (int i = 1; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (Character.isWhitespace(ch)) {
                buffer.append(ch);
                buffer.append(Character.toTitleCase(input.charAt(i + 1)));
                i++;
            }
            else {
                buffer.append(ch);
            }
        }

        return buffer.toString();
    }

}
