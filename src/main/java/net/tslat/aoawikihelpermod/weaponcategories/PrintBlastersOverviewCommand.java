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
import net.tslat.aoa3.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.constant.AttackSpeed;
import net.tslat.aoawikihelpermod.AttributeHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintBlastersOverviewCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printblastersoverview")
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
            List<BaseBlaster> blasters = new ArrayList<BaseBlaster>();

            for (Item item: ForgeRegistries.ITEMS.getValues()) {
                if(item instanceof BaseBlaster) {
                    blasters.add((BaseBlaster)item);
                }
            }

            blasters = blasters.stream().sorted(Comparator.comparing(blaster -> blaster.getDisplayName(new ItemStack(blaster)).getString())).collect(Collectors.toList());

            data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
            data.add("|- style=\"background-color:#eee\"");
            data.add("! Name !! data-sort-type=number | Damage !! Unholster time !! Fire rate !! Energy cost !! Durability !! Effects");
            data.add("|-");

            for (BaseBlaster blaster : blasters) {
                ItemStack blasterStack = new ItemStack(blaster);
                String name = blaster.getDisplayName(blasterStack).getString();
                String unholsterTime = NumberUtil.roundToNthDecimalPlace(1 / (float)(AttributeHandler.getStackAttributeValue(blasterStack, SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, Item.ATTACK_SPEED_MODIFIER)), 2) + "s";
                String firingSpeed = (2000 / blaster.getFiringDelay()) / (double)100 + "/sec";

                data.add("| [[File:" + name + ".png|64px|link=]] '''[[" + name + "]]''' || {{hp|" + NumberUtil.roundToNthDecimalPlace((float)blaster.getDamage(), 1) + "}} || " + unholsterTime + " || " + firingSpeed + " || " + NumberUtil.roundToNthDecimalPlace(blaster.getEnergyCost(), 2) + " || " + blaster.getMaxDamage(blasterStack) + " || ");
                data.add("|-");
            }
            
            data.add("|}");
            CategoryTableWriter.writeData("Blasters", data, player, copyToClipboard);
        }
    }
}
