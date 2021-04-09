package net.tslat.aoawikihelpermod.recipes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.tslat.aoawikihelpermod.trades.TradesWriter;

public class PrintItemRecipesCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printitemrecipes")
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

        World world = context.getSource().getWorld();

        if (!world.isRemote) {
            PlayerEntity pl = (PlayerEntity)sender;
            ItemStack targetStack = pl.getHeldItemMainhand();

            if (targetStack.isEmpty()) {
                sender.sendMessage(new StringTextComponent("You're not holding anything!"));

                return;
            }
            else if (!targetStack.getItem().getRegistryName().getNamespace().equals("aoa3")) {
                sender.sendMessage(new StringTextComponent("The item you are holding is not from AoA! You are holding: " + targetStack.getDisplayName().getString() + " (" + targetStack.getItem().getRegistryName().toString() + ")"));

                return;
            }

            boolean copyToClipboard = attemptToCopy;
            if (copyToClipboard && context.getSource().getServer().isDedicatedServer()) {
                sender.sendMessage(new StringTextComponent("Can't copy contents of file to clipboard on dedicated servers, skipping."));
                copyToClipboard = false;
            }

            RecipeWriter.printItemRecipes(targetStack, sender, world.getRecipeManager(), copyToClipboard);
        }
    }
}
