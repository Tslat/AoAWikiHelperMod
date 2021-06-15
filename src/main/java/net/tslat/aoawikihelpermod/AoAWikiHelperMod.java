package net.tslat.aoawikihelpermod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.dataskimmers.ItemMiscUsageSkimmer;
import net.tslat.aoawikihelpermod.dataskimmers.LootTablesSkimmer;
import net.tslat.aoawikihelpermod.dataskimmers.MerchantsSkimmer;
import net.tslat.aoawikihelpermod.dataskimmers.RecipesSkimmer;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.tslat.aoawikihelpermod.AoAWikiHelperMod.MOD_ID;

@Mod(MOD_ID)
public class AoAWikiHelperMod {
	public static final String VERSION = "2.2";
	public static final String MOD_ID = "aoawikihelpermod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public static boolean isOutdatedAoA = false;

	public AoAWikiHelperMod() {
		File outputDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID).toFile();

		FileUtils.getOrCreateDirectory(outputDir.toPath(), MOD_ID);
		PrintHelper.init(outputDir);
		patchClickEvent();
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::registerRecipeSkimmer);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		MinecraftForge.EVENT_BUS.addListener(this::loadFinished);
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent ev) {
		WikiHelperCommand.registerSubCommands(ev.getDispatcher());
	}

	@SubscribeEvent
	public void registerRecipeSkimmer(AddReloadListenerEvent ev) {
		ev.addListener(new RecipesSkimmer());
		ev.addListener(new LootTablesSkimmer());
	}

	@SubscribeEvent
	public void serverStarted(FMLServerStartedEvent ev) {
		MerchantsSkimmer.init(ev.getServer());
		ItemMiscUsageSkimmer.init();
	}

	@SubscribeEvent
	public void loadFinished(final FMLLoadCompleteEvent ev) {
		if (VersionChecker.getResult(ModList.get().getModFileById(AdventOfAscension.MOD_ID).getMods().get(0)).status == VersionChecker.Status.OUTDATED)
			isOutdatedAoA = true;
	}

	private void patchClickEvent() {
		if (LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT) != null) {
			try {
				Field targetField = null;

				for (Field field : ClickEvent.Action.class.getDeclaredFields()) {
					String fieldName = field.getName();

					if (fieldName.equals("field_150676_f") || fieldName.equals("allowFromServer") || fieldName.equals("allowedInChat"))
						targetField = field;
				}

				if (targetField == null) {
					LOGGER.warn("Failed to patch in support for opening files from chat. Skipping");

					return;
				}

				targetField.setAccessible(true);

				Field modifiers = Field.class.getDeclaredField("modifiers");

				modifiers.setAccessible(true);
				modifiers.setInt(targetField, targetField.getModifiers() & ~Modifier.FINAL);
				targetField.set(ClickEvent.Action.OPEN_FILE, true);
			}
			catch (Exception ex) {
				LOGGER.error("Failed to patch in support for opening files from chat. Skipping", ex);
			}
		}
	}
}
