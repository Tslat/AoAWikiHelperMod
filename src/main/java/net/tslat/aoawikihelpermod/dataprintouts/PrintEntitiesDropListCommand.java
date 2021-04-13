package net.tslat.aoawikihelpermod.dataprintouts;

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
import net.tslat.aoa3.item.weapon.cannon.BaseCannon;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.AttributeHandler;
import net.tslat.aoawikihelpermod.weaponcategories.CategoryTableWriter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintEntitiesDropListCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printentitiesdroplist")
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

            ItemStack targetStack = player.getHeldItemMainhand();

            if (targetStack.isEmpty()) {
                sender.sendMessage(new StringTextComponent("You're not holding anything!"));

                return;
            }
            else if (!targetStack.getItem().getRegistryName().getNamespace().equals("aoa3")) {
                sender.sendMessage(new StringTextComponent("The item you are holding is not from AoA! You are holding: " + targetStack.getDisplayName() + " (" + targetStack.getItem().getRegistryName().toString() + ")"));

                return;
            }

            DataPrintoutWriter.writeItemEntityDropsList(sender, targetStack, copyToClipboard);
        }
    }
}
