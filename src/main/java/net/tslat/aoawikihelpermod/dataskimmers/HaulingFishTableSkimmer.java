package net.tslat.aoawikihelpermod.dataskimmers;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.HaulingTablePrintHandler;

import java.util.HashMap;
import java.util.Map;

public class HaulingFishTableSkimmer extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = Deserializers.createLootTableSerializer().create();
	public static final HashMap<ResourceLocation, HaulingTablePrintHandler> TABLE_PRINTERS = new HashMap<ResourceLocation, HaulingTablePrintHandler>();
	public static final HashMultimap<ResourceLocation, ResourceLocation> TABLES_BY_LOOT = HashMultimap.create();

	public HaulingFishTableSkimmer() {
		super(AoAWikiHelperMod.GSON, "player/misc/hauling_fish");
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

			TABLE_PRINTERS.put(id, new HaulingTablePrintHandler(id, (JsonObject)json));
			populateLootByTable(id, (JsonObject)json);
		}
	}

	private void populateLootByTable(ResourceLocation tableId, JsonObject rawTable) {
		JsonArray entityList = rawTable.getAsJsonArray("entities");

		for (JsonElement element : entityList) {
			if (element.isJsonPrimitive()) {
				TABLES_BY_LOOT.put(new ResourceLocation(element.getAsString()), tableId); // TODO remove
			}
			else if (element.isJsonObject()) {
				JsonObject obj = element.getAsJsonObject();

				if (obj.has("item")) {
					TABLES_BY_LOOT.put(new ResourceLocation(obj.get("item").getAsString()), tableId);
				}
				else {
					ResourceLocation id = new ResourceLocation(obj.get("entity").getAsString());

					if (ObjectHelper.isItem(id))
						TABLES_BY_LOOT.put(id, tableId);
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Hauling Tables Skimmer";
	}
}
