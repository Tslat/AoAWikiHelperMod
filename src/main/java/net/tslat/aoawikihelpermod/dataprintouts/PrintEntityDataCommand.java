package net.tslat.aoawikihelpermod.dataprintouts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.entity.base.*;
import net.tslat.aoa3.entity.minion.AoAMinion;
import net.tslat.aoa3.entity.mob.creeponia.AoACreeponiaCreeper;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.skill.HunterUtil;

import java.util.*;

import static net.minecraft.entity.CreatureAttribute.*;

public class PrintEntityDataCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printentitydata")
                        .then(Commands.argument("copyToClipboard", BoolArgumentType.bool())
                                .executes(commandContext -> {
                                    print(commandContext, BoolArgumentType.getBool(commandContext, "copyToClipboard"));
                                    return 0;
                                }))
                        .executes(commandContext -> {
                            print(commandContext, false);
                            return 0;
                        }));
    }

    public static void print(CommandContext<CommandSource> context, boolean attemptToCopy) {
        Entity sender = context.getSource().getEntity();

        if(!(sender instanceof PlayerEntity)) {
            sender.sendMessage(new StringTextComponent("This command can only be done ingame for accuracy."));
            return;
        }

        PlayerEntity player = (PlayerEntity)sender;

        World world = context.getSource().getWorld();

        if(!world.isRemote) {
            boolean copyToClipboard = attemptToCopy;
            if (copyToClipboard && context.getSource().getServer().isDedicatedServer()) {
                sender.sendMessage(new StringTextComponent("Can't copy contents of file to clipboard on dedicated servers, skipping."));
                copyToClipboard = false;
            }

            List<String> data = new ArrayList<String>();

            data.add("AoA v" + AdventOfAscension.VERSION + " entity data printout below: ");
            data.add("");
            data.add("---~~~---~~~---~~~");
            HashMap<String, Integer> mobCounts = new HashMap<String, Integer>();

            for (EntityType entry : ForgeRegistries.ENTITIES) {
                if (!entry.getRegistryName().getNamespace().equals("aoa3"))
                    continue;

                Entity entity = entry.create(world);

                data.add("Entity: " + entity.getName().getString());
                data.add("Id: " + entry.getRegistryName());

                String type = "Other";

                if (!entity.isNonBoss()) {
                    type = "Boss";
                    mobCounts.merge("Boss", 1, Integer::sum);
                }
                else if (entity instanceof AoAMeleeMob) {
                    if (entity instanceof AoARangedAttacker) {
                        type = "Hybrid Melee/Ranged Mob";
                        mobCounts.merge("Hybrid Melee/Ranged Mob", 1, Integer::sum);
                    }
                    else {
                        type = "Melee Mob";
                        mobCounts.merge("Melee Mob", 1, Integer::sum);
                    }
                }
                else if (entity instanceof AoARangedMob) {
                    type = "Ranged Mob";
                    mobCounts.merge("Ranged Mob", 1, Integer::sum);
                }
                else if (entity instanceof AoAFlyingMeleeMob) {
                    type = "Flying Melee Mob";
                    mobCounts.merge("Flying Melee Mob", 1, Integer::sum);
                }
                else if (entity instanceof AoAFlyingRangedMob) {
                    type = "Flying Ranged Mob";
                    mobCounts.merge("Flying Ranged Mob", 1, Integer::sum);
                }
                else if (entity instanceof AoAAmbientNPC) {
                    type = "NPC";
                    mobCounts.merge("NPC", 1, Integer::sum);
                }
                else if (entity instanceof AoAMinion) {
                    type = "Minion";
                    mobCounts.merge("Minion", 1, Integer::sum);
                }
                else if (entity instanceof AnimalEntity) {
                    type = "Animal";
                    mobCounts.merge("Animal", 1, Integer::sum);
                }
                else if (entity instanceof AoATrader) {
                    type = "Trader";
                    mobCounts.merge("Trader", 1, Integer::sum);
                }
                else if (entity instanceof IProjectile) {
                    type = "Projectile";
                    mobCounts.merge("Projectile", 1, Integer::sum);
                }
                else {
                    if (entity instanceof AoACreeponiaCreeper)
                        type = "Creepoid";

                    mobCounts.merge("Other", 1, Integer::sum);
                }

                data.add("Type: " + type);

                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)entity;

                    String creatureType = "None";

                    CreatureAttribute creatureAttribute = livingEntity.getCreatureAttribute();

                    if (creatureAttribute.equals(ARTHROPOD)) {
                        creatureType = "Arthropod";
                    } else if (creatureAttribute.equals(ILLAGER)) {
                        creatureType = "Illager";
                    } else if (creatureAttribute.equals(UNDEAD)) {
                        creatureType = "Arthropod";
                    }

                    data.add("Creature Type: " + creatureType + (!entity.isNonBoss() ? " (Boss)" : ""));
                    data.add("Size: " + entity.getWidth() + "W x " + entity.getHeight() + "H");
                    data.add("Stats:");
                    data.add("    Health: " + livingEntity.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue());
                    data.add("    Armour: " + livingEntity.getAttribute(SharedMonsterAttributes.ARMOR).getBaseValue());
                    data.add("    Knockback Resistance: " + NumberUtil.roundToNthDecimalPlace((float)livingEntity.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getBaseValue(), 2));
                    data.add("    Movement Speed: " + NumberUtil.roundToNthDecimalPlace((float)livingEntity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue(), 3));

                    if (livingEntity.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null)
                        data.add("    Strength: " + livingEntity.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());

                    if (livingEntity instanceof AoACreeponiaCreeper)
                        data.add("    Explosion Strength: " + ((AoACreeponiaCreeper)livingEntity).getExplosionStrength());

                    if (HunterUtil.isHunterCreature(livingEntity)) {
                        data.add("    Hunter Level: " + HunterUtil.getHunterLevel(livingEntity));
                        data.add("    Hunter Xp: " + HunterUtil.getHunterXp(livingEntity));
                    }
                }
                else {
                    data.add("Size: " + entity.getWidth() + "W x " + entity.getHeight() + "H");
                }

                data.add("---~~~---~~~---~~~");
            }

            data.add("Entity printout complete, stats: ");

            int total = 0;

            for (Map.Entry<String, Integer> entry : mobCounts.entrySet()) {
                data.add("    " + entry.getKey() + ": " + entry.getValue());
                total += entry.getValue();
            }

            data.add("    Total: " + total);

            DataPrintoutWriter.writeData("Entity", data, sender, copyToClipboard);
        }
    }
}


