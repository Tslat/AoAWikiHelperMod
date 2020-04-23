package net.tslat.aoawikihelpermod;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.tslat.aoawikihelpermod.dataprintouts.CommandPrintEntitiesDropList;
import net.tslat.aoawikihelpermod.dataprintouts.CommandPrintEntityData;
import net.tslat.aoawikihelpermod.dataprintouts.CommandPrintWeaponsData;
import net.tslat.aoawikihelpermod.loottables.CommandPrintLootTable;
import net.tslat.aoawikihelpermod.loottables.CommandTestLootTable;
import net.tslat.aoawikihelpermod.recipes.*;
import net.tslat.aoawikihelpermod.trades.CommandPrintTradeOutputs;
import net.tslat.aoawikihelpermod.trades.CommandPrintTradeUsages;
import net.tslat.aoawikihelpermod.trades.CommandPrintTraderTrades;
import net.tslat.aoawikihelpermod.weaponcategories.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Mod(modid = "aoawikihelpermod", name = "AoA-Wiki Helper Mod", version = "1.7")
public class AoAWikiHelperMod {
	public static Logger logger;
	private static ModContainer aoaModContainer;
	private static File configDir;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		aoaModContainer = Loader.instance().getIndexedModList().get("aoa3");
		configDir = new File(event.getModConfigurationDirectory(), "AoAWikiHelper");

		if (aoaModContainer == null) {
			logger.log(Level.FATAL, "Unable to find AoA3 mod container. Shutting down");
			Loader.instance().runtimeDisableMod("aoawikihelpermod");

			return;
		}

		prepRecipeWriter(event.getModConfigurationDirectory());

		if (event.getSide() == Side.CLIENT) {
			try {
				EnumHelper.setFailsafeFieldValue(ClickEvent.Action.class.getDeclaredField("allowedInChat"), ClickEvent.Action.OPEN_FILE, true);
			}
			catch (Exception e) {
				logger.log(Level.WARN, "Oops, got caught doing some naughty stuff. Let's pretend that didn't happen.");
			}
		}
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandPrintArchergunsOverview());
		event.registerServerCommand(new CommandPrintAxesOverview());
		event.registerServerCommand(new CommandPrintBlastersOverview());
		event.registerServerCommand(new CommandPrintBowsOverview());
		event.registerServerCommand(new CommandPrintCannonsOverview());
		event.registerServerCommand(new CommandPrintEntitiesDropList());
		event.registerServerCommand(new CommandPrintEntityData());
		event.registerServerCommand(new CommandPrintGreatbladesOverview());
		event.registerServerCommand(new CommandPrintGunsOverview());
		event.registerServerCommand(new CommandPrintInfusionEnchants());
		event.registerServerCommand(new CommandPrintItemRecipes());
		event.registerServerCommand(new CommandPrintItemUsageRecipes());
		event.registerServerCommand(new CommandPrintLootTable());
		event.registerServerCommand(new CommandPrintMaulsOverview());
		event.registerServerCommand(new CommandPrintPickaxesOverview());
		event.registerServerCommand(new CommandPrintShotgunsOverview());
		event.registerServerCommand(new CommandPrintShovelsOverview());
		event.registerServerCommand(new CommandPrintSnipersOverview());
		event.registerServerCommand(new CommandPrintStavesOverview());
		event.registerServerCommand(new CommandPrintSwordsOverview());
		event.registerServerCommand(new CommandPrintThrownWeaponsOverview());
		event.registerServerCommand(new CommandPrintTradeOutputs());
		event.registerServerCommand(new CommandPrintTradeUsages());
		event.registerServerCommand(new CommandPrintTraderTrades());
		event.registerServerCommand(new CommandPrintWeaponsData());
		event.registerServerCommand(new CommandTestLootTable());
	}

	public static File prepConfigDir(String subdirectory) {
		if (!configDir.exists())
			configDir.mkdirs();

		File subDir = new File(configDir, subdirectory);

		if (!subDir.exists())
			subDir.mkdirs();

		return subDir;
	}

	private static void prepRecipeWriter(File modConfigDir) {
		RecipeWriter.scrapeForRecipes(aoaModContainer);
		RecipeWriter.registerRecipeInterface("ShapelessRecipes", RecipeInterfaceShapeless.class);
		RecipeWriter.registerRecipeInterface("InfusionTableRecipe", RecipeInterfaceInfusion.class);
		RecipeWriter.registerRecipeInterface("ShapedRecipes", RecipeInterfaceShaped.class);
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

	public static ITextComponent generateInteractiveMessagePrintout(String prefix, File file, String linkName, String suffix) {
		String fileUrl = file.getAbsolutePath().replace("\\", "/");

		return ITextComponent.Serializer.jsonToComponent("{\"translate\":\"" + prefix + "%s" + "\",\"with\":[{\"text\":\"" + linkName + "\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_file\",\"value\":\"" + fileUrl + "\"}}]}").appendText(suffix);
	}

	public static boolean copyFileToClipboard(File streamFile) {
		final StringBuilder content = new StringBuilder();

		try (InputStreamReader streamReader = new InputStreamReader(new FileInputStream(streamFile), StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(streamReader)) {
			reader.lines().forEach(line -> {
				content.append(line);
				content.append("\n");
			});

			Toolkit toolkit = Toolkit.getDefaultToolkit();

			if (toolkit == null)
				return false;

			toolkit.getSystemClipboard().setContents(new StringSelection(content.toString()), null);
			return true;
		}
		catch (Exception e) {
			AoAWikiHelperMod.logger.log(Level.ERROR, "Unable to copy contents of file to clipboard, skipping");
		}

		return false;
	}
}
