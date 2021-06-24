package net.tslat.aoawikihelpermod.dataskimmers;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.loot.*;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.LootTableHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.LootTablePrintHandler;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class LootTablesSkimmer extends JsonReloadListener {
	private static final Gson GSON = LootSerializers.createLootTableSerializer().create();
	public static final HashMap<ResourceLocation, LootTablePrintHandler> TABLE_PRINTERS = new HashMap<ResourceLocation, LootTablePrintHandler>();
	public static final HashMultimap<ResourceLocation, ResourceLocation> TABLES_BY_LOOT = HashMultimap.create();

	public LootTablesSkimmer() {
		super(AoAWikiHelperMod.GSON, "loot_tables");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsonMap, IResourceManager resourceManager, IProfiler profiler) {
		TABLE_PRINTERS.clear();
		TABLES_BY_LOOT.clear();

		for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
			ResourceLocation id = entry.getKey();
			JsonElement json = entry.getValue();

			if (id.getPath().startsWith("_") || !json.isJsonObject())
				continue;

			try {
				LootTable table = null;

				try (IResource resource = resourceManager.getResource(getPreparedPath(id))) {
					table = ForgeHooks.loadLootTable(GSON, id, json, true, null);
				}
				catch (Exception ex) {
					AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Invalid loot table json found, skipping");
					ex.printStackTrace();
				}

				if (table != null)
					populateLootByTable(id, table, (JsonObject)json);

				TABLE_PRINTERS.put(id, new LootTablePrintHandler(id, (JsonObject)json, table));
			}
			catch (Exception ex) {
				AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Failed loot table skim for: " + id + ", skipping table.");
			}
		}
	}

	private void populateLootByTable(ResourceLocation tableId, LootTable table, JsonObject rawTable) {
		for (LootPool pool : LootTableHelper.getPools(table)) {
			for (LootEntry entry : LootTableHelper.getLootEntries(pool)) {
				if (entry instanceof ItemLootEntry) {
					TABLES_BY_LOOT.put(((ItemLootEntry)entry).item.getRegistryName(), tableId);
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Loot Tables Skimmer";
	}
}
