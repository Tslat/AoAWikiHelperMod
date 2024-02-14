package net.tslat.aoawikihelpermod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.advent.Logging;
import net.tslat.aoa3.util.ObjectUtil;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.dataskimmers.*;
import net.tslat.aoawikihelpermod.render.typeadapter.IsoRenderAdapters;
import net.tslat.aoawikihelpermod.util.LootTableHelper;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printer.PrintHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static net.tslat.aoawikihelpermod.AoAWikiHelperMod.MOD_ID;

@Mod(MOD_ID)
public class AoAWikiHelperMod {
	public static final String VERSION = "2.16.2";
	public static final String MOD_ID = "aoawikihelpermod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public static boolean isOutdatedAoA = false;

	public AoAWikiHelperMod(IEventBus modBus) {
		File outputDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID).toFile();

		try {
			ObjectUtil.getOrCreateDirectory(outputDir.toPath(), MOD_ID);
		}
		catch (IOException ex) {
			Logging.error("Failed to create output directory.. this is not good", ex);
		}

		PrintHelper.init(outputDir);
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		NeoForge.EVENT_BUS.addListener(this::registerRecipeSkimmer);
		NeoForge.EVENT_BUS.addListener(this::serverStarted);
		modBus.addListener(this::loadFinished);
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent ev) {
		WikiHelperCommand.registerSubCommands(ev.getDispatcher(), ev.getBuildContext());
	}

	@SubscribeEvent
	public void registerRecipeSkimmer(AddReloadListenerEvent ev) {
		ev.addListener(new RecipesSkimmer());
		ev.addListener(new LootTablesSkimmer());
		ev.addListener(new HaulingFishTableSkimmer());
		ev.addListener(new StructureTemplateSkimmer());
	}

	@SubscribeEvent
	public void serverStarted(ServerStartedEvent ev) {
		FakeWorld.init();
		BlockDataSkimmer.init();
		ItemDataSkimmer.init();
		MerchantsSkimmer.init(ev.getServer());
		LootTableHelper.init();
		ItemMiscUsageSkimmer.init();
		TagDataSkimmer.init(ev.getServer());
	}

	@SubscribeEvent
	public void loadFinished(final FMLLoadCompleteEvent ev) {
		IsoRenderAdapters.init();
	}

	public static String getAoAVersion() {
		return AdventOfAscension.VERSION;
	}
}
