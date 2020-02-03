package net.tslat.aoawikihelpermod.loottables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.Smelt;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.codehaus.plexus.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class AccessibleLootTable {
	protected final List<AccessibleLootPool> pools;

	public AccessibleLootTable(List<LootPool> pools) {
		this.pools = new ArrayList<AccessibleLootPool>(pools.size());

		for (LootPool pool : pools) {
			this.pools.add(new AccessibleLootPool(pool));
		}
	}

	public static class AccessibleLootPool {
		protected final List<AccessibleLootEntry> lootEntries;
		protected final RandomValueRange rolls;
		protected final RandomValueRange bonusRolls;
		protected final List<LootCondition> conditions;
		protected final StringBuilder notesBuilder = new StringBuilder();

		public AccessibleLootPool(LootPool pool) {
			this.rolls = pool.getRolls();
			this.bonusRolls = pool.getBonusRolls();

			conditions = ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186454_b");
			List<LootEntry> entries = ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186453_a");
			lootEntries = new ArrayList<AccessibleLootEntry>(entries.size());

			for (LootEntry entry : entries) {
				lootEntries.add(new AccessibleLootEntry(entry));
			}

			lootEntries.sort(new LootEntryComparator());

			if (conditions != null) {
				for (LootCondition condition : conditions) {
					if (condition instanceof RandomChance) {
						float chance = ObfuscationReflectionHelper.getPrivateValue(RandomChance.class, (RandomChance)condition, "field_186630_a");

						notesBuilder.append("The above pool has a fixed ");
						notesBuilder.append(((int)(chance * 10000)) / 100d);
						notesBuilder.append("% chance to roll for drops. ");
					}
					else if (condition instanceof RandomChanceWithLooting) {
						float chance = ObfuscationReflectionHelper.getPrivateValue(RandomChanceWithLooting.class, (RandomChanceWithLooting)condition, "field_186627_a");
						float lootingMod = ObfuscationReflectionHelper.getPrivateValue(RandomChanceWithLooting.class, (RandomChanceWithLooting)condition, "field_186628_b");

						notesBuilder.append("The above pool has a fixed ");
						notesBuilder.append(((int)(chance * 10000)) / 100d);
						notesBuilder.append("% chance to roll for drops, with an additional ");
						notesBuilder.append(((int)(lootingMod * 10000)) / 100d);
						notesBuilder.append("% chance per level of looting. ");
					}
					else if (condition instanceof KilledByPlayer) {
						notesBuilder.append("Will only roll if the entity is killed directly by a player. ");
					}
				}
			}
		}

		@Nonnull
		public String getNotes() {
			return notesBuilder.toString();
		}

		private static class LootEntryComparator implements Comparator<AccessibleLootEntry> {
			@Override
			public int compare(AccessibleLootEntry entry1, AccessibleLootEntry entry2) {
				return Integer.compare(entry2.weight, entry1.weight);
			}
		}
	}

	public static class AccessibleLootEntry {
		protected final int weight;
		protected final int quality;
		protected final Item item;
		protected final LootFunction[] functions;
		protected final LootCondition[] conditions;

		protected ItemStack generatedStack = null;
		protected RandomValueRange amountRange = null;
		protected RandomValueRange bonusRange = null;
		protected StringBuilder notesBuilder = new StringBuilder();

		protected final boolean isTable;
		protected final ResourceLocation table;

		public AccessibleLootEntry(LootEntry entry) {
			this.isTable = entry instanceof LootEntryTable;
			this.weight = ObfuscationReflectionHelper.getPrivateValue(LootEntry.class, entry, "field_186364_c");
			this.quality = ObfuscationReflectionHelper.getPrivateValue(LootEntry.class, entry, "field_186365_d");
			this.conditions = ObfuscationReflectionHelper.getPrivateValue(LootEntry.class, entry, "field_186366_e");

			if (entry instanceof LootEntryItem) {
				this.item = ObfuscationReflectionHelper.getPrivateValue(LootEntryItem.class, (LootEntryItem)entry, "field_186368_a");
				this.functions = ObfuscationReflectionHelper.getPrivateValue(LootEntryItem.class, (LootEntryItem)entry, "field_186369_b");
				this.table = null;
			}
			else if (isTable) {
				this.table = ObfuscationReflectionHelper.getPrivateValue(LootEntryTable.class, (LootEntryTable)entry, "field_186371_a");
				this.functions = null;
				this.item = Items.AIR;
			}
			else {
				this.item = Items.AIR;
				this.functions = null;
				this.table = null;
			}

			if (quality > 0) {
				notesBuilder.append("Chance is increased with each level of luck and/or looting. ");
			}
			else if (quality < 0) {
				notesBuilder.append("Chance is decreased with each level of luck and/or looting. ");
			}

		}

		public String getEntryName(@Nullable EntityPlayer pl) {
			if (item == Items.AIR) {
				if (table == null)
					return "Nothing";

				String tableName = table.getResourcePath().substring(Math.max(0, table.getResourcePath().indexOf(":")));
				String[] splitName = tableName.split("/");
				tableName = StringUtils.capitaliseAllWords(splitName[splitName.length - 1].replace("_", " ").replace(" table", ""));

				if (!tableName.contains("table"))
					tableName = tableName + " Table";

				return tableName;
			}
			else {
				ItemStack stack = new ItemStack(item);
				Random rand = new Random();
				LootContext context = new LootContext.Builder(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0)).withPlayer(pl).build();

				if (functions != null) {
					for (int i = 0; i < functions.length; i++) {
						LootFunction function = functions[i];

						try {
							function.apply(stack, rand, context);

							if (function instanceof SetCount) {
								amountRange = ObfuscationReflectionHelper.getPrivateValue(SetCount.class, (SetCount)function, "field_186568_a");
							}
							else if (function instanceof LootingEnchantBonus) {
								bonusRange = ObfuscationReflectionHelper.getPrivateValue(LootingEnchantBonus.class, (LootingEnchantBonus)function, "field_186563_a");
							}
							else if (function instanceof Smelt) {
								ItemStack smeltedStack = FurnaceRecipes.instance().getSmeltingResult(stack);

								if (smeltedStack != ItemStack.EMPTY) {
									notesBuilder.append("Converts to [[");

									if (smeltedStack.getItem().getRegistryName().getResourceDomain().equals("minecraft")) {
										notesBuilder.append("mcw:");
										notesBuilder.append(smeltedStack.getDisplayName());
										notesBuilder.append("|");
									}

									notesBuilder.append(smeltedStack.getDisplayName());
									notesBuilder.append("]] if killed while the entity is on fire. ");
								}
							}
						} catch (Exception e) {}
					}
				}

				if (conditions != null) {
					for (LootCondition condition : conditions) {
						if (condition instanceof KilledByPlayer)
							notesBuilder.append("Only drops if the entity is killed directly by a player. ");
					}
				}

				if (amountRange == null)
					amountRange = new RandomValueRange(1);

				stack.setCount(1);

				generatedStack = stack;

				return stack.getDisplayName();
			}
		}

		@Nonnull
		public String getNotes() {
			return notesBuilder.toString();
		}
	}
}
