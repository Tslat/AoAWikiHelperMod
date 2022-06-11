package net.tslat.aoawikihelpermod.dataskimmers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.ItemDataPrintHandler;

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
	public static ItemDataPrintHandler get(Item item) {
		return DATA_BY_ITEM.get(ForgeRegistries.ITEMS.getKey(item));
	}
}
