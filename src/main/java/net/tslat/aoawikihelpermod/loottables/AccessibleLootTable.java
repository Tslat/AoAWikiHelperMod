package net.tslat.aoawikihelpermod.loottables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
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
import net.tslat.aoa3.library.loot.conditions.PlayerHasLevel;
import net.tslat.aoa3.library.loot.conditions.PlayerHoldingItem;
import net.tslat.aoa3.library.loot.functions.GrantXp;
import net.tslat.aoa3.library.loot.functions.SetRandomMetadata;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class AccessibleLootTable {
	protected final List<AccessibleLootPool> pools;
	protected final String type;

	public AccessibleLootTable(List<LootPool> pools, String type) {
		this.pools = new ArrayList<AccessibleLootPool>(pools.size());
		this.type = type;

		for (LootPool pool : pools) {
			this.pools.add(new AccessibleLootPool(pool, this));
		}
	}

	public static class AccessibleLootPool {
		protected final List<AccessibleLootEntry> lootEntries;
		protected final RandomValueRange rolls;
		protected final RandomValueRange bonusRolls;
		protected final List<LootCondition> conditions;
		protected final StringBuilder notesBuilder = new StringBuilder();
		protected final AccessibleLootTable parentTable;

		public AccessibleLootPool(LootPool pool, AccessibleLootTable parentTable) {
			this.rolls = pool.getRolls();
			this.bonusRolls = pool.getBonusRolls();
			this.parentTable = parentTable;

			conditions = ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186454_b");
			List<LootEntry> entries = ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186453_a");
			lootEntries = new ArrayList<AccessibleLootEntry>(entries.size());

			for (LootEntry entry : entries) {
				lootEntries.add(new AccessibleLootEntry(entry, parentTable));
			}

			lootEntries.sort(new LootEntryComparator());

			if (conditions != null) {
				boolean firstConditionPrinted = false;

				for (LootCondition condition : conditions) {
					if (condition instanceof RandomChance) {
						float chance = ObfuscationReflectionHelper.getPrivateValue(RandomChance.class, (RandomChance)condition, "field_186630_a");

						if (firstConditionPrinted)
							notesBuilder.append("<br/>");

						notesBuilder.append("This pool has a fixed ");
						notesBuilder.append(((int)(chance * 10000)) / 100d);
						notesBuilder.append("% chance to roll for drops. ");
					}
					else if (condition instanceof RandomChanceWithLooting) {
						float chance = ObfuscationReflectionHelper.getPrivateValue(RandomChanceWithLooting.class, (RandomChanceWithLooting)condition, "field_186627_a");
						float lootingMod = ObfuscationReflectionHelper.getPrivateValue(RandomChanceWithLooting.class, (RandomChanceWithLooting)condition, "field_186628_b");

						if (firstConditionPrinted)
							notesBuilder.append("<br/>");

						notesBuilder.append("This pool has a fixed ");
						notesBuilder.append(((int)(chance * 10000)) / 100d);
						notesBuilder.append("% chance to roll for drops, with an additional ");
						notesBuilder.append(((int)(lootingMod * 10000)) / 100d);
						notesBuilder.append("% chance per level of looting. ");
					}
					else if (condition instanceof KilledByPlayer) {
						if (firstConditionPrinted)
							notesBuilder.append("<br/>");

						notesBuilder.append("This pool will only roll if the entity is killed directly by a player. ");
					}
					else if (condition instanceof PlayerHasLevel) {
						String skill = I18n.translateToLocal("skills." + ObfuscationReflectionHelper.getPrivateValue(PlayerHasLevel.class, (PlayerHasLevel)condition, "skill").toString().toLowerCase() + ".name");
						int level = ObfuscationReflectionHelper.getPrivateValue(PlayerHasLevel.class, (PlayerHasLevel)condition, "level");

						if (firstConditionPrinted)
							notesBuilder.append("<br/>");

						notesBuilder.append("This pool will only roll if the player has at least level ").append(level).append(" ").append(skill).append(". ");
					}
					else if (condition instanceof PlayerHoldingItem) {
						Item item = ObfuscationReflectionHelper.getPrivateValue(PlayerHoldingItem.class, (PlayerHoldingItem)condition, "tool");
						EnumHand hand = ObfuscationReflectionHelper.getPrivateValue(PlayerHoldingItem.class, (PlayerHoldingItem)condition, "hand");

						if (firstConditionPrinted)
							notesBuilder.append("<br/>");

						notesBuilder.append("This pool will only roll if the player is holding ").append(item.getItemStackDisplayName(new ItemStack(item)));

						if (hand != null)
							notesBuilder.append(" in the ").append(hand.toString().toLowerCase().replace("_", ""));

						notesBuilder.append(". ");
					}
					else {
						continue;
					}

					firstConditionPrinted = true;
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
		protected final AccessibleLootTable parentTable;

		public AccessibleLootEntry(LootEntry entry, AccessibleLootTable parentTable) {
			this.isTable = entry instanceof LootEntryTable;
			this.weight = ObfuscationReflectionHelper.getPrivateValue(LootEntry.class, entry, "field_186364_c");
			this.quality = ObfuscationReflectionHelper.getPrivateValue(LootEntry.class, entry, "field_186365_d");
			this.conditions = ObfuscationReflectionHelper.getPrivateValue(LootEntry.class, entry, "field_186366_e");
			this.parentTable = parentTable;

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
				notesBuilder.append("Chance is increased with each level of luck");

				if (parentTable.type.equals(""))
					notesBuilder.append(" and/or looting");

				notesBuilder.append(".");
			}
			else if (quality < 0) {
				notesBuilder.append("Chance is decreased with each level of luck");

				if (parentTable.type.equals(""))
					notesBuilder.append(" and/or looting");

				notesBuilder.append(".");
			}
		}

		public String getEntryName(@Nullable EntityPlayer pl) {
			if (item == Items.AIR) {
				if (table == null)
					return "Nothing";

				String tableName = table.getResourcePath().substring(Math.max(0, table.getResourcePath().indexOf(":")));
				String[] splitName = tableName.split("/");
				tableName = AoAWikiHelperMod.capitaliseAllWords(splitName[splitName.length - 1].replace("_", " ").replace(" table", ""));

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
							else if (function instanceof GrantXp) {
								String skill = I18n.translateToLocal("skills." + ObfuscationReflectionHelper.getPrivateValue(GrantXp.class, (GrantXp)function, "skill").toString().toLowerCase() + ".name");
								float xp = ObfuscationReflectionHelper.getPrivateValue(GrantXp.class, (GrantXp)function, "xp");

								notesBuilder.append("Gives ").append(xp).append("xp").append(" in ").append(skill);
							}
							else if (function instanceof SetRandomMetadata) {
								notesBuilder.append("Chooses a random variant from all possible variants of the item. ");
							}
						} catch (Exception e) {}
					}
				}

				if (conditions != null) {
					for (LootCondition condition : conditions) {
						if (condition instanceof KilledByPlayer) {
							notesBuilder.append("Only drops if the entity is killed directly by a player. ");
						}
						else if (condition instanceof PlayerHasLevel) {
							String skill = I18n.translateToLocal("skills." + ObfuscationReflectionHelper.getPrivateValue(PlayerHasLevel.class, (PlayerHasLevel)condition, "skill").toString().toLowerCase() + ".name");
							int level = ObfuscationReflectionHelper.getPrivateValue(PlayerHasLevel.class, (PlayerHasLevel)condition, "level");

							notesBuilder.append("Excluded from loot roll if the player does not have at least level ").append(level).append(" ").append(skill).append(". ");
						}
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
