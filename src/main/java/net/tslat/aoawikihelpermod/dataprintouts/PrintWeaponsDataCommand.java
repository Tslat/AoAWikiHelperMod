package net.tslat.aoawikihelpermod.dataprintouts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.entity.base.*;
import net.tslat.aoa3.entity.minion.AoAMinion;
import net.tslat.aoa3.entity.mob.creeponia.AoACreeponiaCreeper;
import net.tslat.aoa3.item.misc.RuneItem;
import net.tslat.aoa3.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.item.weapon.bow.BaseBow;
import net.tslat.aoa3.item.weapon.cannon.BaseCannon;
import net.tslat.aoa3.item.weapon.crossbow.BaseCrossbow;
import net.tslat.aoa3.item.weapon.greatblade.BaseGreatblade;
import net.tslat.aoa3.item.weapon.gun.BaseGun;
import net.tslat.aoa3.item.weapon.maul.BaseMaul;
import net.tslat.aoa3.item.weapon.shotgun.BaseShotgun;
import net.tslat.aoa3.item.weapon.sniper.BaseSniper;
import net.tslat.aoa3.item.weapon.staff.BaseStaff;
import net.tslat.aoa3.item.weapon.sword.BaseSword;
import net.tslat.aoa3.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.item.weapon.vulcane.BaseVulcane;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.skill.HunterUtil;
import net.tslat.aoawikihelpermod.AttributeHandler;

import java.util.*;

import static net.minecraft.entity.CreatureAttribute.*;

public class PrintWeaponsDataCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printweaponsdata")
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

            data.add("AoA v" + AdventOfAscension.VERSION + " weapons data printout below: ");
            data.add("");
            data.add("---~~~---~~~---~~~");

            HashSet<BaseSword> swords = new HashSet<BaseSword>();
            HashSet<BaseGreatblade> greatblades = new HashSet<BaseGreatblade>();
            HashSet<BaseMaul> mauls = new HashSet<BaseMaul>();
            HashSet<BaseShotgun> shotguns = new HashSet<BaseShotgun>();
            HashSet<BaseSniper> snipers = new HashSet<BaseSniper>();
            HashSet<BaseCannon> cannons = new HashSet<BaseCannon>();
            HashSet<BaseBlaster> blasters = new HashSet<BaseBlaster>();
            HashSet<BaseCrossbow> crossbows = new HashSet<BaseCrossbow>();
            HashSet<BaseThrownWeapon> thrownWeapons = new HashSet<BaseThrownWeapon>();
            HashSet<BaseGun> guns = new HashSet<BaseGun>();
            HashSet<BaseVulcane> vulcanes = new HashSet<BaseVulcane>();
            HashSet<BaseBow> bows = new HashSet<BaseBow>();
            HashSet<BaseStaff> staves = new HashSet<BaseStaff>();

            for (Item item : ForgeRegistries.ITEMS) {
                if (item.getRegistryName().getNamespace().equals("aoa3")) {
                    if (item instanceof BaseSword) {
                        swords.add((BaseSword)item);
                    }
                    else if (item instanceof BaseGreatblade) {
                        greatblades.add((BaseGreatblade)item);
                    }
                    else if (item instanceof BaseMaul) {
                        mauls.add((BaseMaul)item);
                    }
                    else if (item instanceof BaseShotgun) {
                        shotguns.add((BaseShotgun)item);
                    }
                    else if (item instanceof BaseSniper) {
                        snipers.add((BaseSniper)item);
                    }
                    else if (item instanceof BaseCannon) {
                        cannons.add((BaseCannon)item);
                    }
                    else if (item instanceof BaseBlaster) {
                        blasters.add((BaseBlaster)item);
                    }
                    else if (item instanceof BaseCrossbow) {
                        crossbows.add((BaseCrossbow)item);
                    }
                    else if (item instanceof BaseThrownWeapon) {
                        thrownWeapons.add((BaseThrownWeapon)item);
                    }
                    else if (item instanceof BaseGun) {
                        guns.add((BaseGun)item);
                    }
                    else if (item instanceof BaseVulcane) {
                        vulcanes.add((BaseVulcane)item);
                    }
                    else if (item instanceof BaseBow) {
                        bows.add((BaseBow)item);
                    }
                    else if (item instanceof BaseStaff) {
                        staves.add((BaseStaff)item);
                    }
                }
            }

            data.add("Swords: ---~~~---~~~---~~~");

            for (BaseSword sword : swords) {
                data.add(new ItemStack(sword).getDisplayName().getString());
                data.add("ID: " + sword.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage: " + sword.getAttackDamage());
                data.add("    Durability: " + sword.getMaxDamage());
                data.add("    Attack Speed: " + NumberUtil.roundToNthDecimalPlace((float)AttributeHandler.getStackAttributeValue(new ItemStack(sword), SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, Item.ATTACK_SPEED_MODIFIER), 2));
                data.add("---~~~---~~~---~~~");
            }

            data.add("Greatblades: ---~~~---~~~---~~~");

            for (BaseGreatblade greatblade : greatblades) {
                data.add(new ItemStack(greatblade).getDisplayName().getString());
                data.add("ID: " + greatblade.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage: " + greatblade.getAttackDamage());
                data.add("    Durability: " + greatblade.getMaxDamage());
                data.add("    Attack Speed: " + NumberUtil.roundToNthDecimalPlace((float)AttributeHandler.getStackAttributeValue(new ItemStack(greatblade), SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, Item.ATTACK_SPEED_MODIFIER), 2));
                data.add("    Reach: " + ((int)greatblade.getReach()) + " blocks");
                data.add("---~~~---~~~---~~~");
            }

            data.add("Mauls: ---~~~---~~~---~~~");

            for (BaseMaul maul : mauls) {
                data.add(new ItemStack(maul).getDisplayName().getString());
                data.add("ID: " + maul.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage: " + maul.getAttackDamage());
                data.add("    Durability: " + maul.getMaxDamage());
                data.add("    Attack Speed: " + NumberUtil.roundToNthDecimalPlace((float)AttributeHandler.getStackAttributeValue(new ItemStack(maul), SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, Item.ATTACK_SPEED_MODIFIER), 2));
                data.add("    Knockback: " + (maul.getBaseKnockback()));
                data.add("---~~~---~~~---~~~");
            }

            data.add("Shotguns: ---~~~---~~~---~~~");

            for (BaseShotgun shotgun : shotguns) {
                data.add(new ItemStack(shotgun).getDisplayName().getString());
                data.add("ID: " + shotgun.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage per pellet: " + shotgun.getDamage());
                data.add("    Pellets: " + shotgun.getPelletCount());
                data.add("    Durability: " + shotgun.getMaxDamage());
                data.add("    Firing Speed: " + (2000 / shotgun.getFiringDelay()) / (double)100 + "/sec");
                data.add("    Recoil: " + shotgun.getRecoil());

                data.add("    Unholster Time: " + NumberUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-AttributeHandler.getStackAttributeValue(new ItemStack(shotgun), SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, AttributeHandler.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s");
                data.add("---~~~---~~~---~~~");
            }

            data.add("Snipers: ---~~~---~~~---~~~");

            for (BaseSniper sniper : snipers) {
                data.add(new ItemStack(sniper).getDisplayName().getString());
                data.add("ID: " + sniper.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage per shot: " + sniper.getDamage());
                data.add("    Durability: " + sniper.getMaxDamage());
                data.add("    Firing Speed: " + (2000 / sniper.getFiringDelay()) / (double)100 + "/sec");
                data.add("    Recoil: " + sniper.getRecoil());
                data.add("    Unholster Time: " + NumberUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-AttributeHandler.getStackAttributeValue(new ItemStack(sniper), SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, AttributeHandler.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s");
                data.add("---~~~---~~~---~~~");
            }

            data.add("Guns: ---~~~---~~~---~~~");

            for (BaseGun gun : guns) {
                data.add(new ItemStack(gun).getDisplayName().getString());
                data.add("ID: " + gun.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage per shot: " + gun.getDamage());
                data.add("    Durability: " + gun.getMaxDamage());
                data.add("    Firing Speed: " + (2000 / gun.getFiringDelay()) / (double)100 + "/sec");
                data.add("    Recoil: " + gun.getRecoil());
                data.add("    Unholster Time: " + NumberUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-AttributeHandler.getStackAttributeValue(new ItemStack(gun), SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, AttributeHandler.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s");
                data.add("---~~~---~~~---~~~");
            }

            data.add("Cannons: ---~~~---~~~---~~~");

            for (BaseCannon cannon : cannons) {
                data.add(new ItemStack(cannon).getDisplayName().getString());
                data.add("ID: " + cannon.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage per shot: " + cannon.getDamage());
                data.add("    Durability: " + cannon.getMaxDamage());
                data.add("    Firing Speed: " + (2000 / cannon.getFiringDelay()) / (double)100 + "/sec");
                data.add("    Recoil: " + cannon.getRecoil());
                data.add("    Unholster Time: " + NumberUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-AttributeHandler.getStackAttributeValue(new ItemStack(cannon), SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, AttributeHandler.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s");
                data.add("---~~~---~~~---~~~");
            }

            data.add("Blasters: ---~~~---~~~---~~~");

            for (BaseBlaster blaster : blasters) {
                data.add(new ItemStack(blaster).getDisplayName().getString());
                data.add("ID: " + blaster.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage: " + blaster.getDamage());
                data.add("    Durability: " + blaster.getMaxDamage());
                data.add("    Firing Speed: " + (2000 / blaster.getFiringDelay()) / (double)100 + "/sec");
                data.add("    Energy Cost: " + blaster.getEnergyCost());
                data.add("    Unholster Time: " + NumberUtil.roundToNthDecimalPlace(1 / (float)(AttributeHandler.getStackAttributeValue(new ItemStack(blaster), SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, Item.ATTACK_SPEED_MODIFIER)), 2) + "s");
                data.add("---~~~---~~~---~~~");
            }

            data.add("Crossbows: ---~~~---~~~---~~~");

            for (BaseCrossbow crossbow : crossbows) {
                data.add(new ItemStack(crossbow).getDisplayName().getString());
                data.add("ID: " + crossbow.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage per shot: " + crossbow.getDamage());
                data.add("    Durability: " + crossbow.getMaxDamage());
                data.add("---~~~---~~~---~~~");
            }

            data.add("Bows: ---~~~---~~~---~~~");

            for (BaseBow bow : bows) {
                data.add(new ItemStack(bow).getDisplayName().getString());
                data.add("ID: " + bow.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage per arrow: " + bow.getDamage());
                data.add("    Durability: " + bow.getMaxDamage());
                data.add("    Draw Time: " + (((int)(72000 / bow.getDrawSpeedMultiplier()) / 720) / (double)100) + "s");
                data.add("---~~~---~~~---~~~");
            }

            data.add("Thrown Weapons: ---~~~---~~~---~~~");

            for (BaseThrownWeapon throwable : thrownWeapons) {
                data.add(new ItemStack(throwable).getDisplayName().getString());
                data.add("ID: " + throwable.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage: " + throwable.getDamage());
                data.add("    Throw Speed: " + (2000 / throwable.getFiringDelay()) / (double)100 + "/sec");
                data.add("---~~~---~~~---~~~");
            }

            data.add("Vulcanes: ---~~~---~~~---~~~");

            for (BaseVulcane vulcane : vulcanes) {
                data.add(new ItemStack(vulcane).getDisplayName().getString());
                data.add("ID: " + vulcane.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Damage: " + vulcane.getDamage());
                data.add("    Durability: " + vulcane.getMaxDamage());
                data.add("---~~~---~~~---~~~");
            }

            data.add("Staves: ---~~~---~~~---~~~");

            for (BaseStaff staff : staves) {
                data.add(new ItemStack(staff).getDisplayName().getString());
                data.add("ID: " + staff.getRegistryName().toString());
                data.add("Stats: ");
                data.add("    Durability: " + staff.getMaxDamage());
                data.add("    Runes: ");

                Set<Map.Entry<RuneItem, Integer>> runes = staff.getRunes().entrySet();

                for (Map.Entry<RuneItem, Integer> runeEntry : runes) {
                    data.add("        " + runeEntry.getValue() + "x " + new ItemStack(runeEntry.getKey()).getDisplayName().getString());
                }

                data.add("---~~~---~~~---~~~");
            }


            data.add("Weapons printout complete, stats: ");
            data.add("    Swords: " + swords.size());
            data.add("    Greatblades: " + greatblades.size());
            data.add("    Mauls: " + mauls.size());
            data.add("    Shotguns: " + shotguns.size());
            data.add("    Snipers: " + snipers.size());
            data.add("    Cannons: " + cannons.size());
            data.add("    Blasters: " + blasters.size());
            data.add("    Crossbows: " + crossbows.size());
            data.add("    Thrown Weapons: " + thrownWeapons.size());
            data.add("    Guns: " + guns.size());
            data.add("    Vulcanes: " + vulcanes.size());
            data.add("    Bows: " + bows.size());
            data.add("    Staves: " + staves.size());
            data.add("");
            data.add("Total: " + (swords.size() + greatblades.size() + mauls.size() + shotguns.size() + snipers.size() + cannons.size() + blasters.size() + crossbows.size() + thrownWeapons.size() + guns.size() + vulcanes.size() + bows.size() + staves.size()));
            data.add("---~~~---~~~---~~~");

            DataPrintoutWriter.writeData("Weapons", data, sender, copyToClipboard);
        }
    }
}
