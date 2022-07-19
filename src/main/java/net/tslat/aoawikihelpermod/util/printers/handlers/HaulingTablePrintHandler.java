package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.common.registration.custom.AoASkills;
import net.tslat.aoa3.content.loottable.condition.PlayerHasLevel;
import net.tslat.aoawikihelpermod.util.LootTableHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class HaulingTablePrintHandler {
	private final ArrayList<String> entries = new ArrayList<String>();
	@Nullable
	private String tableNotes = null;

	private final ResourceLocation tableId;

	private final JsonObject rawTable;

	public HaulingTablePrintHandler(ResourceLocation tableId, JsonObject rawTable) {
		this.tableId = tableId;
		this.rawTable = rawTable;
	}

	private void buildEntries() {
		JsonArray entries = rawTable.getAsJsonArray("entities");

		for (JsonElement entry : entries) {
			StringBuilder result = new StringBuilder("group:1; ");
			StringBuilder entryNotes = new StringBuilder("notes:");
			String type = "entity";
			String name = null;
			String weight = "1";
			float quality = 0;

			if (entry.isJsonPrimitive()) {
				type = "item";
				name = ObjectHelper.getItemName(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getAsString()))); // TODO remove
			}
			else if (entry.isJsonObject()) {
				JsonObject obj = entry.getAsJsonObject();

				if (obj.has("weight"))
					weight = String.valueOf(obj.get("weight").getAsInt());

				if (obj.has("weight_mod"))
					quality = obj.get("weight_mod").getAsFloat();

				if (obj.has("level"))
					entryNotes.append("This entry will only roll ").append(LootTableHelper.getConditionDescription(new PlayerHasLevel(AoASkills.HAULING.get(), obj.get("level").getAsInt())));

				if (obj.has("item")) {
					ResourceLocation id = new ResourceLocation(obj.get("item").getAsString());

					type = "item";
					name = ObjectHelper.getItemName(ForgeRegistries.ITEMS.getValue(id));
				}
				else {
					name = ObjectHelper.getEntityName(ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(obj.get("entity").getAsString())));
				}
			}

			result.append(type).append(":").append(name);
			result.append("; weight:").append(weight).append("; ");

			if (quality > 0) {
				if (entryNotes.length() > 6)
					entryNotes.append("<br/>");

				entryNotes.append("Chance is increased with each level of luck or Luck of the Sea");
			}
			else if (quality < 0) {
				if (entryNotes.length() > 6)
					entryNotes.append("<br/>");

				entryNotes.append("Chance is decreased with each level of luck or Luck of the Sea");
			}

			if (entryNotes.length() > 6)
				result.append(entryNotes.toString());

			this.entries.add(result.toString());
		}

		this.tableNotes = buildTableNotes();
	}

	public List<String> getEntries() {
		if (this.entries.isEmpty())
			buildEntries();

		return this.entries;
	}

	@Nullable
	public String getTableNotes() {
		if (this.entries.isEmpty())
			buildEntries();

		return this.tableNotes;
	}

	public ResourceLocation getTableId() {
		return this.tableId;
	}

	@Nullable
	private String buildTableNotes() {
		StringBuilder notesBuilder = new StringBuilder();

		if (tableId.equals(AdventOfAscension.id("fish_default"))) {
			notesBuilder.append("This is the default table for Hauling fish.");
		}
		else if (tableId.equals(AdventOfAscension.id("fish_lava_default"))) {
			notesBuilder.append("This is the default table for lava Hauling fish.");
		}
		else if (tableId.equals(AdventOfAscension.id("traps_default"))) {
			notesBuilder.append("This is the default table for Hauling traps.");
		}
		else if (tableId.equals(AdventOfAscension.id("traps_lava_default"))) {
			notesBuilder.append("This is the default table for lava Hauling traps.");
		}

		if (notesBuilder.length() == 0) {
			if (GsonHelper.getAsBoolean(rawTable, "for_lava", false))
				notesBuilder.append("This table is for lava fishing");

			if (GsonHelper.getAsBoolean(rawTable, "for_traps", false))
				notesBuilder.append("This table is for trap entities");

			if (rawTable.has("biomes")) {
				JsonArray biomeArray = rawTable.getAsJsonArray("biomes");

				if (biomeArray.size() == 1) {
					notesBuilder.append("This table applies to the ").append(ObjectHelper.getBiomeName(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(biomeArray.get(0).getAsString())))).append(" biome");
				}
				else {
					notesBuilder.append("This table to applies to the following biomes:\n");

					for (int i = 0; i < biomeArray.size(); i++) {
						if (i > 0)
							notesBuilder.append("\n");

						notesBuilder.append("* ").append(ObjectHelper.getBiomeName(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(biomeArray.get(i).getAsString()))));
					}
				}
			}

			if (rawTable.has("tags")) {
				JsonArray biomeCategories = rawTable.getAsJsonArray("tags");

				if (biomeCategories.size() == 1) {
					notesBuilder.append("If not overridden by another table with a relevant biome, this table applies all biomes tagged as ").append(biomeCategories.get(0).getAsString());
				}
				else {
					notesBuilder.append("This table to applies to any biome tagged as any of the following categories:\n");

					for (int i = 0; i < biomeCategories.size(); i++) {
						if (i > 0)
							notesBuilder.append("\n");

						notesBuilder.append("* ").append(biomeCategories.get(i).getAsString());
					}
				}
			}
		}

		return notesBuilder.length() == 0 ? null : notesBuilder.toString();
	}
}
