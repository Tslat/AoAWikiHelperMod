package net.tslat.aoawikihelpermod.weaponcategories;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.item.tool.shovel.BaseShovel;
import net.tslat.aoa3.util.NumberUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintShovelsOverviewCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printshovelsoverview")
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
            List<BaseShovel> shovels = new ArrayList<BaseShovel>();

            for (Item item: ForgeRegistries.ITEMS.getValues()) {
                if(item instanceof BaseShovel) {
                    shovels.add((BaseShovel)item);
                }
            }

            shovels = shovels.stream().sorted(Comparator.comparing(shovel -> shovel.getDisplayName(new ItemStack(shovel)).getString())).collect(Collectors.toList());

            data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
            data.add("|- style=\"background-color:#eee\"");
            data.add("! Name !! data-sort-type=number | Damage !! Efficiency !! Durability !! Effects");
            data.add("|-");

            for (BaseShovel shovel : shovels) {
                ItemStack shovelStack = new ItemStack(shovel);
                String name = shovel.getDisplayName(shovelStack).getString();
                float efficiency = shovel.getTier().getEfficiency();
                float damage = shovel.getTier().getAttackDamage() + 1.5F + 1;

                data.add("| [[File:" + name + ".png|32px|link=]] '''[[" + name + "]]''' || {{hp|" + NumberUtil.roundToNthDecimalPlace(damage, 1) + "}} || " + NumberUtil.roundToNthDecimalPlace(efficiency, 1) + " || " + shovel.getMaxDamage(shovelStack) + " || ");
                data.add("|-");
            }

            data.add("|}");

            CategoryTableWriter.writeData("Shovels", data, player, copyToClipboard);
        }
    }
}
