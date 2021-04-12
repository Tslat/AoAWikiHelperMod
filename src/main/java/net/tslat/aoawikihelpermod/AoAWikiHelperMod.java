package net.tslat.aoawikihelpermod;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static net.tslat.aoawikihelpermod.AoAWikiHelperMod.MOD_ID;

@Mod(MOD_ID)
public class AoAWikiHelperMod {
	public static final String VERSION = "2.0";
	public static final String MOD_ID = "aoawikihelpermod";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public AoAWikiHelperMod() {
		File outputDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID).toFile();

		FileUtils.getOrCreateDirectory(outputDir.toPath(), MOD_ID);
	}

	public void registerCommands(RegisterCommandsEvent ev) {
		WikiHelperCommand.registerSubCommands(ev.getDispatcher());
	}
}
