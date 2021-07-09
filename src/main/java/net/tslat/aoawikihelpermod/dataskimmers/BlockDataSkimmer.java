package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.recipe.BlockDataPrintHandler;

import javax.annotation.Nullable;
import java.util.HashMap;

public class BlockDataSkimmer {
	private static final HashMap<ResourceLocation, BlockDataPrintHandler> DATA_BY_BLOCK = new HashMap<ResourceLocation, BlockDataPrintHandler>();

	public static void init() {
		for (Block block : ObjectHelper.scrapeRegistryForBlocks((bl) -> true)) {
			DATA_BY_BLOCK.put(block.getRegistryName(), new BlockDataPrintHandler(block));
		}
	}

	@Nullable
	public static BlockDataPrintHandler get(Block block) {
		return DATA_BY_BLOCK.get(block.getRegistryName());
	}
}
