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
import net.tslat.aoa3.item.weapon.shotgun.BaseShotgun;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.AttributeHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintShotgunsOverviewCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printshotgunsoverview")
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
            List<BaseShotgun> shotguns = new ArrayList<BaseShotgun>();

            for (Item item: ForgeRegistries.ITEMS.getValues()) {
                if (item instanceof BaseShotgun) {
                    shotguns.add((BaseShotgun)item);
                }
            }

            data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
            data.add("|- style=\"background-color:#eee\"");
            data.add("! Name !! data-sort-type=number | Damage !! Unholster time !! Fire rate !! Pellets !! Recoil !! Durability !! Effects");
            data.add("|-");

            for (BaseShotgun shotgun : shotguns) {
                ItemStack shotgunStack = new ItemStack(shotgun);
                String name = shotgun.getDisplayName(shotgunStack).getString();
                String unholsterTime = NumberUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-AttributeHandler.getStackAttributeValue(shotgunStack, SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, AttributeHandler.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s";
                String firingSpeed = (2000 / shotgun.getFiringDelay()) / (double)100 + "/sec";

                data.add("| [[File:" + name + ".png|64px|link=]] '''[[" + name + "]]''' || {{hp|" + NumberUtil.roundToNthDecimalPlace((float)shotgun.getDamage(), 1) + "}} || " + unholsterTime + " || " + firingSpeed + " || " + shotgun.getPelletCount() + " || " + NumberUtil.roundToNthDecimalPlace(shotgun.getRecoil(), 1) + " || " + shotgun.getMaxDamage(shotgunStack) + " || ");
                data.add("|-");
            }

            data.add("|}");
            CategoryTableWriter.writeData("Shotguns", data, player, copyToClipboard);
        }
    }
}
