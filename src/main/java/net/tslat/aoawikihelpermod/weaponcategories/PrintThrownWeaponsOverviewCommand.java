package net.tslat.aoawikihelpermod.weaponcategories;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.util.NumberUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintThrownWeaponsOverviewCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printthrownweaponsoverview")
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
            List<BaseThrownWeapon> thrownWeapons = new ArrayList<BaseThrownWeapon>();

            for (Item item: ForgeRegistries.ITEMS.getValues()) {
                if (item instanceof BaseThrownWeapon) {
                    thrownWeapons.add((BaseThrownWeapon)item);
                }
            }

            thrownWeapons = thrownWeapons.stream().sorted(Comparator.comparing(thrownWeapon -> thrownWeapon.getDisplayName(new ItemStack(thrownWeapon)).getString())).collect(Collectors.toList());

            data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
            data.add("|- style=\"background-color:#eee\"");
            data.add("! Name !! data-sort-type=number | Damage !! Throw rate !! Effects");
            data.add("|-");

            for (BaseThrownWeapon thrownWeapon : thrownWeapons) {
                ItemStack thrownWeaponStack = new ItemStack(thrownWeapon);
                String name = thrownWeapon.getDisplayName(thrownWeaponStack).getString();
                String firingRate = NumberUtil.roundToNthDecimalPlace((2000 / (int) ObfuscationReflectionHelper.getPrivateValue(BaseThrownWeapon.class, thrownWeapon, "firingDelay")) / (float)100, 2) + "/sec";
                double damage = ObfuscationReflectionHelper.getPrivateValue(BaseThrownWeapon.class, thrownWeapon, "dmg");

                data.add("| [[File:" + name + ".png|32px|link=]] '''[[" + name + "]]''' || {{hp|" + NumberUtil.roundToNthDecimalPlace((float)damage, 1) + "}} || " + firingRate + " || ");
                data.add("|-");
            }

            data.add("|}");
            CategoryTableWriter.writeData("Thrown Weapons", data, player, copyToClipboard);
        }
    }
}
