package net.tslat.aoawikihelpermod.weaponcategories;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.item.misc.RuneItem;
import net.tslat.aoa3.item.weapon.staff.BaseStaff;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.AttributeHandler;

import java.util.*;
import java.util.stream.Collectors;

public class PrintStavesOverviewCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printstavesoverview")
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
            List<BaseStaff> staves = new ArrayList<BaseStaff>();

            for (Item item: ForgeRegistries.ITEMS.getValues()) {
                if(item instanceof BaseStaff) {
                    staves.add((BaseStaff)item);
                }
            }

            staves = staves.stream().sorted(Comparator.comparing(staff -> staff.getDisplayName(new ItemStack(staff)).getString())).collect(Collectors.toList());

            data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
            data.add("|- style=\"background-color:#eee\" | data-sort-type=number |");
            data.add("! Name !! Runes !! Durability !! Effects");
            data.add("|-");

            for (BaseStaff staff : staves) {
                ItemStack staffStack = new ItemStack(staff);
                String name = staff.getDisplayName(staffStack).getString();
                StringBuilder runesLineBuilder = new StringBuilder();
                Set<Map.Entry<RuneItem, Integer>> runes = staff.getRunes().entrySet();

                for (Map.Entry<RuneItem, Integer> runeEntry : runes) {
                    String runeName = runeEntry.getKey().getDisplayName(new ItemStack(runeEntry.getKey())).getString();

                    runesLineBuilder.append("<br/>");
                    runesLineBuilder.append(runeEntry.getValue()).append("x ").append("[[File:").append(runeName).append(".png|link=]] [[").append(runeName);

                    if (runeEntry.getValue() > 1)
                        runesLineBuilder.append("|").append(runeName).append("s");

                    runesLineBuilder.append("]]");
                }

                data.add("| [[File:" + name + ".png|64px|link=]] '''[[" + name + "]]''' || " + runesLineBuilder.toString().substring(5) + " || " + staff.getMaxDamage(staffStack) + " || ");
                data.add("|-");
            }

            data.add("|}");
            CategoryTableWriter.writeData("Staves", data, player, copyToClipboard);
        }
    }
}
