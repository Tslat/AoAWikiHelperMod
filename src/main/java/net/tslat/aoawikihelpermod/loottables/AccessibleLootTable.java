package net.tslat.aoawikihelpermod.loottables;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.conditions.KilledByPlayer;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.Smelt;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.entity.mob.overworld.ChargerEntity;
import net.tslat.aoa3.library.loot.conditions.HoldingItem;
import net.tslat.aoa3.library.loot.conditions.PlayerHasLevel;
import net.tslat.aoa3.library.loot.functions.GrantSkillXp;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AccessibleLootTable {
    public final List<AccessibleLootPool> pools;
    public final String type;

    public AccessibleLootTable(List<LootPool> pools, String type) {
        this.pools = new ArrayList<AccessibleLootPool>(pools.size());
        this.type = type;

        for (LootPool pool : pools) {
            this.pools.add(new AccessibleLootPool(pool, this));
        }
    }

    public static class AccessibleLootPool {
        public final List<AccessibleLootEntry> lootEntries;
        public final IRandomRange rolls;
        public final RandomValueRange bonusRolls;
        public final List<ILootCondition> conditions;
        public final StringBuilder notesBuilder = new StringBuilder();
        public final AccessibleLootTable parentTable;

        public AccessibleLootPool(LootPool pool, AccessibleLootTable parentTable) {
            this.rolls = pool.getRolls();
            this.bonusRolls = (RandomValueRange)pool.getBonusRolls();
            this.parentTable = parentTable;

            conditions = ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186454_b");
            List<LootEntry> entries = ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186453_a");
            lootEntries = new ArrayList<AccessibleLootEntry>(entries.size());

            for (LootEntry entry : entries) {
                if (entry instanceof StandaloneLootEntry) {
                    lootEntries.add(new AccessibleLootEntry((StandaloneLootEntry)entry, parentTable));
                } else if (entry instanceof ParentedLootEntry) {
                    AoAWikiHelperMod.LOGGER.error("Ran into ParentedLootEntry, have no idea how to deal with this");
                } else {
                    AoAWikiHelperMod.LOGGER.error("Ran into entry that wasn't either standalone or parented somehow");
                }
            }

            lootEntries.sort(new LootEntryComparator());

            if (conditions != null) {
                boolean firstConditionPrinted = false;

                for (ILootCondition condition : conditions) {
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
                        String skill = I18n.format("skills." + ObfuscationReflectionHelper.getPrivateValue(PlayerHasLevel.class, (PlayerHasLevel)condition, "skill").toString().toLowerCase() + ".name");
                        int level = ObfuscationReflectionHelper.getPrivateValue(PlayerHasLevel.class, (PlayerHasLevel)condition, "level");

                        if (firstConditionPrinted)
                            notesBuilder.append("<br/>");

                        notesBuilder.append("This pool will only roll if the player has at least level ").append(level).append(" ").append(skill).append(". ");
                    }
                    else if (condition instanceof HoldingItem) {
                        Item item = ObfuscationReflectionHelper.getPrivateValue(HoldingItem.class, (HoldingItem)condition, "tool");
                        Hand hand = ObfuscationReflectionHelper.getPrivateValue(HoldingItem.class, (HoldingItem)condition, "hand");

                        if (firstConditionPrinted)
                            notesBuilder.append("<br/>");

                        notesBuilder.append("This pool will only roll if the player is holding ").append(item.getDisplayName(new ItemStack(item)).toString());

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
        public final int weight;
        public final int quality;
        public final Item item;
        public final ILootFunction[] functions;
        public final ILootCondition[] conditions;

        public ItemStack generatedStack = null;
        public RandomValueRange amountRange = null;
        public RandomValueRange bonusRange = null;
        public StringBuilder notesBuilder = new StringBuilder();

        public final boolean isTable;
        public final ResourceLocation table;
        public final AccessibleLootTable parentTable;

        public AccessibleLootEntry(StandaloneLootEntry entry, AccessibleLootTable parentTable) {
            this.isTable = entry instanceof TableLootEntry;
            this.weight = ObfuscationReflectionHelper.getPrivateValue(StandaloneLootEntry.class, entry, "field_216158_e");
            this.quality = ObfuscationReflectionHelper.getPrivateValue(StandaloneLootEntry.class, entry, "field_216159_f");
            this.conditions = ObfuscationReflectionHelper.getPrivateValue(LootEntry.class, entry, "field_216144_d");
            this.parentTable = parentTable;

            if (entry instanceof ItemLootEntry) {
                this.item = ObfuscationReflectionHelper.getPrivateValue(ItemLootEntry.class, (ItemLootEntry)entry, "field_186368_a");
                this.functions = ObfuscationReflectionHelper.getPrivateValue(StandaloneLootEntry.class, entry, "field_216160_g");
                this.table = null;
            }
            else if (isTable) {
                this.table = ObfuscationReflectionHelper.getPrivateValue(TableLootEntry.class, (TableLootEntry)entry, "field_186371_a");
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

        public String getEntryName(@Nullable PlayerEntity pl) {
            if (item == Items.AIR) {
                if (table == null)
                    return "Nothing";

                String tableName = table.getPath().substring(Math.max(0, table.getPath().indexOf(":")));
                String[] splitName = tableName.split("/");
                tableName = AoAWikiHelperMod.capitaliseAllWords(splitName[splitName.length - 1].replace("_", " ").replace(" table", ""));

                if (!tableName.contains("table"))
                    tableName = tableName + " Table";

                return tableName;
            }
            else {
                ItemStack stack = new ItemStack(item);
                Random rand = new Random();
                LootContext context;

                try {
                    //These are dummy parameters just to get into the loot table
                    context = new LootContext.Builder(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD))
                            .withParameter(LootParameters.POSITION, new BlockPos(0, 0, 0))
                            .withParameter(LootParameters.THIS_ENTITY, ForgeRegistries.ENTITIES.getValue(new ResourceLocation("aoa3", "charger")).create(pl.getEntityWorld()))
                            .withParameter(LootParameters.DAMAGE_SOURCE, DamageSource.causePlayerDamage(pl))
                            .withParameter(LootParameters.LAST_DAMAGE_PLAYER, pl)
                            .build(LootParameterSets.ENTITY);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    context = null;
                }

                if (functions != null) {
                    for (int i = 0; i < functions.length; i++) {
                        ILootFunction function = functions[i];

                        try {
                            function.apply(stack, context);

                            if (function instanceof SetCount) {
                                amountRange = ObfuscationReflectionHelper.getPrivateValue(SetCount.class, (SetCount)function, "field_186568_a");
                            }
                            else if (function instanceof LootingEnchantBonus) {
                                bonusRange = ObfuscationReflectionHelper.getPrivateValue(LootingEnchantBonus.class, (LootingEnchantBonus)function, "field_186563_a");
                            }
                            else if (function instanceof Smelt) {
                                Optional<FurnaceRecipe> smeltRecipe = (pl.getEntityWorld()).getRecipeManager().getRecipe(IRecipeType.SMELTING, new Inventory(new ItemStack[]{stack}), pl.getEntityWorld());
                                if (smeltRecipe.isPresent()) {
                                    ItemStack smeltedStack = ((FurnaceRecipe)smeltRecipe.get()).getRecipeOutput();

                                    if (smeltedStack != ItemStack.EMPTY) {
                                        notesBuilder.append("Converts to [[");

                                        if (smeltedStack.getItem().getRegistryName().getNamespace().equals("minecraft")) {
                                            notesBuilder.append("mcw:");
                                            notesBuilder.append(smeltedStack.getDisplayName().getString());
                                            notesBuilder.append("|");
                                        }

                                        notesBuilder.append(smeltedStack.getDisplayName().getString());
                                        notesBuilder.append("]] if killed while the entity is on fire. ");
                                    }
                                } else {
                                    AoAWikiHelperMod.LOGGER.error("Found smelting recipe for an unsmeltable item!");
                                }
                            }
                            else if (function instanceof GrantSkillXp) {
                                String skill = I18n.format("skills." + ObfuscationReflectionHelper.getPrivateValue(GrantSkillXp.class, (GrantSkillXp)function, "skill").toString().toLowerCase() + ".name");
                                float xp = ObfuscationReflectionHelper.getPrivateValue(GrantSkillXp.class, (GrantSkillXp)function, "xp");

                                notesBuilder.append("Gives ").append(xp).append("xp").append(" in ").append(skill);
                            }/*
                            else if (function instanceof SetRandomMetadata) {
                                notesBuilder.append("Chooses a random variant from all possible variants of the item. ");
                            }*/
                        } catch (Exception e) {}
                    }
                }

                if (conditions != null) {
                    for (ILootCondition condition : conditions) {
                        if (condition instanceof KilledByPlayer) {
                            notesBuilder.append("Only drops if the entity is killed directly by a player. ");
                        }
                        else if (condition instanceof PlayerHasLevel) {
                            String skill = I18n.format("skills." + ObfuscationReflectionHelper.getPrivateValue(PlayerHasLevel.class, (PlayerHasLevel)condition, "skill").toString().toLowerCase() + ".name");
                            int level = ObfuscationReflectionHelper.getPrivateValue(PlayerHasLevel.class, (PlayerHasLevel)condition, "level");

                            notesBuilder.append("Excluded from loot roll if the player does not have at least level ").append(level).append(" ").append(skill).append(". ");
                        }
                    }
                }

                if (amountRange == null)
                    amountRange = new RandomValueRange(1);

                stack.setCount(1);

                generatedStack = stack;

                return stack.getDisplayName().getString();
            }

        }

        @Nonnull
        public String getNotes() {
            return notesBuilder.toString();
        }
    }
}