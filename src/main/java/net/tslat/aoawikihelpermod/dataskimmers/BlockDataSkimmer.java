package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.BlockDataPrintHandler;

import javax.annotation.Nullable;
import java.util.HashMap;

public class BlockDataSkimmer {
	private static final HashMap<ResourceLocation, BlockDataPrintHandler> DATA_BY_BLOCK = new HashMap<ResourceLocation, BlockDataPrintHandler>();

	public static void init() {
		for (Block block : ObjectHelper.scrapeRegistryForBlocks(bl -> true)) {
			DATA_BY_BLOCK.put(RegistryUtil.getId(block), new BlockDataPrintHandler(block));
		}
	}

	@Nullable
	public static BlockDataPrintHandler get(Block block, ServerLevel level) {
		BlockDataPrintHandler handler = DATA_BY_BLOCK.getOrDefault(RegistryUtil.getId(block), null);

		return handler == null ? null : handler.withLevel(level);
	}
}
