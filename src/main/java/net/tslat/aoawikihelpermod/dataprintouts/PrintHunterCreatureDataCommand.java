package net.tslat.aoawikihelpermod.dataprintouts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
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
import java.util.stream.Collectors;

public class PrintHunterCreatureDataCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printhuntercreaturedata")
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

            data.add("{|class=\"wikitable\"");
            data.add("|-");
            data.add("! Mob !! Mob ID !! Default Level !! Default XP");
            data.add("|-");

            HashMap<EntityType<? extends MobEntity>, Tuple<Integer, Float>> hunterCreatureMap = ObfuscationReflectionHelper.getPrivateValue(HunterUtil.class, null, "hunterCreatureMap");

            for (Map.Entry<EntityType<? extends MobEntity>, Tuple<Integer, Float>> entry : hunterCreatureMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getRegistryName())).collect(Collectors.toList())) {
                Entity entity = entry.getKey().create(world);

                if (entity == null) {
                    System.out.println("Unable to create entity with id: " + entry.getKey().getRegistryName());

                    continue;
                }

                data.add("| [[" + entity.getDisplayName().getString() + "]] || " + entry.getKey().getRegistryName() + " || " + entry.getValue().getA() + " || " + entry.getValue().getB());
                data.add("|-");
            }

            data.add("|}");

            DataPrintoutWriter.writeData("Hunter Mobs", data, sender, copyToClipboard);
        }
    }
}
