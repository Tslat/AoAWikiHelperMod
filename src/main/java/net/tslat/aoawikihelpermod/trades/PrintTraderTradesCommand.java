package net.tslat.aoawikihelpermod.trades;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.entity.base.AoATrader;
import net.tslat.aoa3.entity.npc.AoATraderRecipe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class PrintTraderTradesCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printtradertrades")
                        .then(Commands.argument("traderEntityId", EntitySummonArgument.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                            .then(Commands.argument("copyToClipboard", BoolArgumentType.bool())
                                .executes(commandContext -> {
                                    print(commandContext, EntitySummonArgument.getEntityId(commandContext, "traderEntityId"), BoolArgumentType.getBool(commandContext, "copyToClipboard"));
                                    return 0;
                                }))
                            .executes(commandContext -> {
                                print(commandContext, EntitySummonArgument.getEntityId(commandContext, "traderEntityId"), false);
                                return 0;
                            })));
    }

    public static void print(CommandContext<CommandSource> context, ResourceLocation traderId, boolean attemptToCopy) {
        Entity sender = context.getSource().getEntity();

        if(!(sender instanceof PlayerEntity)) {
            sender.sendMessage(new StringTextComponent("This command can only be done ingame for accuracy."));
            return;
        }

        World world = context.getSource().getWorld();

        if (!world.isRemote) {
            if(!traderId.getNamespace().equals("aoa3")) {
                sender.sendMessage(new StringTextComponent("The provided entity ID (" + traderId + ") is not from AoA."));
                return;
            }

            EntityType entry = ForgeRegistries.ENTITIES.getValue(traderId);
            NonNullList<AoATraderRecipe> trades = null;
            AoATrader trader = null;

            if (entry == null) {
                sender.sendMessage(new StringTextComponent("Can't find entity with id: \"" + traderId + "\"."));

                return;
            }

            try {
                trader = (AoATrader)entry.create(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD));
                Method tradesMethod = ObfuscationReflectionHelper.findMethod(AoATrader.class, "getTradesList", NonNullList.class);
                trades = NonNullList.<AoATraderRecipe>create();

                tradesMethod.invoke(trader, trades);
            }
            catch (Exception e) {
                sender.sendMessage(new StringTextComponent("Unable to expand provided entity ID into the AoA trader class."));
                e.printStackTrace();

                return;
            }

            boolean copyToClipboard = attemptToCopy;

            if (copyToClipboard && context.getSource().getServer().isDedicatedServer()) {
                sender.sendMessage(new StringTextComponent("Can't copy contents of file to clipboard on dedicated servers, skipping."));
                copyToClipboard = false;
            }

            TradesWriter.printTraderTrades(sender, trader.getDisplayName().getString(), trades, copyToClipboard);
        }
    }
}
