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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.tslat.aoawikihelpermod.AoAWikiHelperMod.MOD_ID;

@Mod(MOD_ID)
public class AoAWikiHelperMod {
	public static final String VERSION = "2.0-Beta-1";
	public static final String MOD_ID = "aoawikihelpermod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public AoAWikiHelperMod() {
		File outputDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID).toFile();

		FileUtils.getOrCreateDirectory(outputDir.toPath(), MOD_ID);
		patchClickEvent();
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::registerRecipeSkimmer);
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent ev) {
		WikiHelperCommand.registerSubCommands(ev.getDispatcher());
	}

	@SubscribeEvent
	public void registerRecipeSkimmer(AddReloadListenerEvent ev) {
		ev.addListener(new RecipeLoaderSkimmer());
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
