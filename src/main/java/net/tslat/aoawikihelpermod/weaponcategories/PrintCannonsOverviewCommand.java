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
import net.tslat.aoa3.item.weapon.cannon.BaseCannon;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.AttributeHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintCannonsOverviewCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printcannonsoverview")
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
            List<BaseCannon> cannons = new ArrayList<BaseCannon>();

            for (Item item: ForgeRegistries.ITEMS.getValues()) {
                if (item instanceof BaseCannon) {
                    cannons.add((BaseCannon)item);
                }
            }

            cannons = cannons.stream().sorted(Comparator.comparing(cannon -> cannon.getDisplayName(new ItemStack(cannon)).getString())).collect(Collectors.toList());

            data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
            data.add("|- style=\"background-color:#eee\"");
            data.add("! Name !! data-sort-type=number | Damage !! Unholster time !! Fire rate !! Recoil !! Durability !! Effects");
            data.add("|-");

            for (BaseCannon cannon : cannons) {
                ItemStack cannonStack = new ItemStack(cannon);
                String name = cannon.getDisplayName(cannonStack).getString();
                String unholsterTime = NumberUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-AttributeHandler.getStackAttributeValue(cannonStack, SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, AttributeHandler.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s";
                String firingSpeed = (2000 / cannon.getFiringDelay()) / (double)100 + "/sec";

                data.add("| [[File:" + name + ".png|64px|link=]] '''[[" + name + "]]''' || {{hp|" + NumberUtil.roundToNthDecimalPlace((float)cannon.getDamage(), 1) + "}} || " + unholsterTime + " || " + firingSpeed + " || " + NumberUtil.roundToNthDecimalPlace(cannon.getRecoil(), 1) + " || " + cannon.getMaxDamage(cannonStack) + " || ");
                data.add("|-");
            }

            data.add("|}");
            CategoryTableWriter.writeData("Cannons", data, player, copyToClipboard);
        }
    }
}
