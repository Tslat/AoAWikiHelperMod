package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.LootTableHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LootTablePrintHandler {
	private List<PoolPrintData> cachedPools = null;
	@Nullable
	private String tableNotes = null;
	private String tableType = "generic";

	private final ResourceLocation tableId;

	private final JsonObject rawTable;
	private final LootTable lootTable;

	public LootTablePrintHandler(ResourceLocation tableId, JsonObject rawTable, LootTable lootTable) {
		this.tableId = tableId;
		this.rawTable = rawTable;
		this.lootTable = lootTable;
	}

	public List<PoolPrintData> getPools() {
		if (this.cachedPools != null)
			return this.cachedPools;

		this.tableType = GsonHelper.getAsString(rawTable, "type", "generic");

		if (this.tableType.contains(":"))
			this.tableType = this.tableType.split(":")[1];

		ArrayList<PoolPrintData> pools = new ArrayList<PoolPrintData>();
		int index = 1;

		for (LootPool pool : LootTableHelper.getPools(lootTable)) {
			pools.add(new PoolPrintData(pool, index));
			index++;
		}

		String notes = LootTableHelper.getFunctionsDescription("table", Arrays.asList(lootTable.functions));

		if (notes.length() > 1)
			this.tableNotes = notes;

		this.cachedPools = ImmutableList.copyOf(pools);

		return this.cachedPools;
	}

	@Nullable
	public String getTableNotes() {
		if (this.cachedPools == null)
			getPools();

		return this.tableNotes;
	}

	public String getType() {
		if (this.cachedPools == null)
			getPools();

		return this.tableType;
	}

	public static class PoolPrintData {
		private final ArrayList<String> lootEntries = new ArrayList<String>();
		private final int poolIndex;
		private final String rolls;
		@Nullable
		private final String bonusRolls;
		@Nullable
		private final String notes;

		protected PoolPrintData(LootPool pool, int poolIndex) {
			this.poolIndex = poolIndex;
			String rolls = FormattingHelper.getStringFromRange(pool.getRolls());
			String bonusRolls = FormattingHelper.getStringFromRange(pool.getBonusRolls());

			if (bonusRolls.equals("1") || bonusRolls.equals("0"))
				bonusRolls = null;

			this.rolls = rolls;
			this.bonusRolls = bonusRolls;

			List<LootItemCondition> conditions = LootTableHelper.getConditions(pool);
			List<LootItemFunction> functions = Arrays.asList(pool.functions);
			StringBuilder poolNotesBuilder = new StringBuilder();

			if (!conditions.isEmpty())
				poolNotesBuilder.append(LootTableHelper.getConditionsDescription("pool", conditions));

			if (functions.size() > 0) {
				if (poolNotesBuilder.length() > 0)
					poolNotesBuilder.append("<br/>");

				poolNotesBuilder.append(LootTableHelper.getFunctionsDescription("pool",  functions));
			}

			this.notes = poolNotesBuilder.length() > 0 ? poolNotesBuilder.toString() : null;

			for (LootPoolEntryContainer entry : LootTableHelper.getLootEntries(pool)) {
				String entryLine = LootTableHelper.getLootEntryLine(poolIndex, entry, Arrays.asList(entry.conditions));

				if (entryLine.length() > 0)
					this.lootEntries.add(entryLine);
			}
		}

		public ArrayList<String> getEntries() {
			return this.lootEntries;
		}

		public int getPoolIndex() {
			return this.poolIndex;
		}

		public String getRollsDescription() {
			return this.rolls;
		}

		@Nullable
		public String getBonusRollsDescription() {
			return this.bonusRolls;
		}

		@Nullable
		public String getPoolNotes() {
			return this.notes;
		}
	}
}
