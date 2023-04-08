package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printer.handler.ItemDataPrintHandler;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ItemDataSkimmer {
	private static final HashMap<ResourceLocation, ItemDataPrintHandler> DATA_BY_ITEM = new HashMap<ResourceLocation, ItemDataPrintHandler>();

	public static void init() {
		for (Item item : ObjectHelper.scrapeRegistryForItems(bl -> true)) {
			DATA_BY_ITEM.put(ForgeRegistries.ITEMS.getKey(item), new ItemDataPrintHandler(item));
		}
	}

	@Nullable
	public static ItemDataPrintHandler get(Item item, ServerLevel level) {
		ItemDataPrintHandler handler = DATA_BY_ITEM.getOrDefault(ForgeRegistries.ITEMS.getKey(item), null);

		return handler == null ? null : handler.withLevel(level);
	}
}
