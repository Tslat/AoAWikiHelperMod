package net.tslat.aoawikihelpermod.dataskimmers;

import com.google.common.collect.HashMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.printer.handler.LootTablePrintHandler;
import org.apache.logging.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class LootTablesSkimmer extends SimpleJsonResourceReloadListener {
	public static final HashMap<ResourceLocation, LootTablePrintHandler> TABLE_PRINTERS = new HashMap<ResourceLocation, LootTablePrintHandler>();
	public static final HashMultimap<ResourceLocation, ResourceLocation> TABLES_BY_LOOT = HashMultimap.create();

	public LootTablesSkimmer() {
		super(AoAWikiHelperMod.GSON, "loot_tables");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
		TABLE_PRINTERS.clear();
		TABLES_BY_LOOT.clear();

		for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
			ResourceLocation id = entry.getKey();
			JsonElement json = entry.getValue();

			if (id.getPath().startsWith("_") || !json.isJsonObject())
				continue;

			try {
				LootTable table;

				if (resourceManager.getResource(getPreparedPath(id)).isPresent()) {
					table = LootTable.CODEC.decode(JsonOps.INSTANCE, json)
							.resultOrPartial(err -> AoAWikiHelperMod.LOGGER.log(Level.ERROR, err))
							.map(Pair::getFirst)
							.orElse(null);
				}
				else {
					AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Invalid loot table json found, skipping");
					continue;
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
		for (LootPool pool : table.pools) {
			for (LootPoolEntryContainer entry : pool.entries) {
				if (entry instanceof LootItem) {
					TABLES_BY_LOOT.put(RegistryUtil.getId(((LootItem)entry).item.value()), tableId);
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Loot Tables Skimmer";
	}
}
