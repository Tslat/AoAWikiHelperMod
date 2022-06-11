package net.tslat.aoawikihelpermod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.tslat.aoa3.common.registration.AoARegistries;
import net.tslat.aoawikihelpermod.command.BlocksCommand;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.dataskimmers.*;
import net.tslat.aoawikihelpermod.render.typeadapter.IsoRenderAdapters;
import net.tslat.aoawikihelpermod.util.LootTableHelper;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static net.tslat.aoawikihelpermod.AoAWikiHelperMod.MOD_ID;

@Mod(MOD_ID)
public class AoAWikiHelperMod {
	public static final String VERSION = "2.9.5";
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
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadFinished);

		SingletonArgumentInfo argumentInfo = SingletonArgumentInfo.contextAware(BlocksCommand.BlockArgument::block);

		ArgumentTypeInfos.registerByClass(BlocksCommand.BlockArgument.class, argumentInfo);
		AoARegistries.ARGUMENT_TYPES.register("wikihelper_block", () -> argumentInfo);
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent ev) {
		WikiHelperCommand.registerSubCommands(ev.getDispatcher());
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
		TagDataSkimmer.init();
	}

	@SubscribeEvent
	public void loadFinished(final FMLLoadCompleteEvent ev) {
		//if (VersionChecker.getResult(ModList.get().getModFileById(AdventOfAscension.MOD_ID).getMods().get(0)).status == VersionChecker.Status.OUTDATED)
		//	isOutdatedAoA = true;

		IsoRenderAdapters.init();
	}

	private void patchClickEvent() {
		try {
			ObfuscationReflectionHelper.setPrivateValue(ClickEvent.Action.class, ClickEvent.Action.OPEN_FILE, true, "f_130635_");
		}
		catch (Exception ex) {
			LOGGER.error("Failed to patch in support for opening files from chat. Skipping", ex);
		}
	}
}
