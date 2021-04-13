package net.tslat.aoawikihelpermod.loottables;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;

import java.util.*;
import java.util.stream.Collectors;

public class TestLootTableCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("testloottable")
                        .then(Commands.argument("lootTableId", ResourceLocationArgument.resourceLocation())
                                .then(Commands.argument("luck", IntegerArgumentType.integer())
                                    .then(Commands.argument("timesToTest", IntegerArgumentType.integer())
                                            .then(Commands.argument("printToConsole", BoolArgumentType.bool())
                                                    .executes(commandContext -> {
                                                        print(commandContext,
                                                                ResourceLocationArgument.getResourceLocation(commandContext, "lootTableId"),
                                                                IntegerArgumentType.getInteger(commandContext, "luck"),
                                                                IntegerArgumentType.getInteger(commandContext, "timesToTest"),
                                                                BoolArgumentType.getBool(commandContext, "printToConsole")
                                                        );
                                                        return 0;
                                                    }))
                                            .executes(commandContext -> {
                                                print(commandContext,
                                                        ResourceLocationArgument.getResourceLocation(commandContext, "lootTableId"),
                                                        IntegerArgumentType.getInteger(commandContext, "luck"),
                                                        IntegerArgumentType.getInteger(commandContext, "timesToTest"),
                                                        false
                                                );
                                                return 0;
                                            })))));

    }

    public static void print(CommandContext<CommandSource> context, ResourceLocation lootTableId, int luck, int timesToTest, boolean printToConsole) {
        Entity sender = context.getSource().getEntity();

        if(!(sender instanceof PlayerEntity)) {
            sender.sendMessage(new StringTextComponent("This command can only be done ingame for accuracy."));
            return;
        }

        PlayerEntity player = (PlayerEntity)sender;

        World world = context.getSource().getWorld();

        if(!world.isRemote) {

            try {
                LootTable table = ServerLifecycleHooks.getCurrentServer().getLootTableManager().getLootTableFromLocation(lootTableId);

                if (table == LootTable.EMPTY_LOOT_TABLE) {
                    sender.sendMessage(new StringTextComponent("Unable to find loot table: " + lootTableId));

                    return;
                }

                LootPool specificPool = null;

             /*
                if (arg.contains("pool:")) {
                    specificPool = table.getPool(arg.split("pool:")[1]);
                }*/


                LootContext lootContext = new LootContext.Builder((ServerWorld)world)
                        .withParameter(LootParameters.POSITION, new BlockPos(0, 0, 0))
                        .withParameter(LootParameters.THIS_ENTITY, ForgeRegistries.ENTITIES.getValue(new ResourceLocation("aoa3", "charger")).create(player.getEntityWorld()))
                        .withParameter(LootParameters.DAMAGE_SOURCE, DamageSource.causePlayerDamage(player))
                        .withParameter(LootParameters.LAST_DAMAGE_PLAYER, player)
                        .withLuck(luck)
                        .build(LootParameterSets.ENTITY);

                HashMap<String, Integer> lootMap = new HashMap<String, Integer>();

                for (int i = 0; i < timesToTest; i++) {
                    List<ItemStack> lootStacks;
                    /*
                    if (specificPool != null) {
                        specificPool.generateLoot(lootStacks = new ArrayList<ItemStack>(), world.rand, context);
                    }
                    else {*/
                        lootStacks = table.generate(lootContext);
                    /*}*/

                    if (lootStacks.isEmpty())
                        lootMap.merge("empty", 1, Integer::sum);

                    for (ItemStack stack : lootStacks) {
                        lootMap.merge(stack.getTranslationKey(), 1, Integer::sum);
                    }
                }

                HashMap<String, Integer> sortedLootMap = lootMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, LinkedHashMap::new));

                if (printToConsole || player == null) {
                    System.out.print("---~~~---~~~---~~~\n");
                    System.out.print("AoA v" + AdventOfAscension.VERSION + " loot table printout: " + lootTableId + "\n");

                    if (specificPool != null)
                        System.out.print("Pool: " + specificPool.getName());

                    System.out.print("\n");
                    System.out.print("---~~~---~~~---~~~\n");
                    System.out.print("    Total rolls: " + timesToTest + "\n");
                    System.out.print("    With luck: " + luck + "\n");

                    if (player != null)
                        System.out.print("    Tested as a player\n");

                    System.out.print("    Drops:\n");

                    int count = 0;
                    Integer emptyDrops = sortedLootMap.get("empty");

                    if (emptyDrops == null)
                        emptyDrops = 0;

                    for (Integer val : sortedLootMap.values()) {
                        count += val;
                    }

                    System.out.print("        " + TextFormatting.DARK_GRAY + "Empty: " + TextFormatting.GRAY + emptyDrops + TextFormatting.RESET + " times. (" + TextFormatting.GRAY + NumberUtil.roundToNthDecimalPlace((emptyDrops / (float)timesToTest) * 100, 5) + "%" + TextFormatting.RESET + ")\n");

                    sortedLootMap.remove("empty");

                    for (Map.Entry<String, Integer> entry : sortedLootMap.entrySet()) {
                        System.out.print("        " + TextFormatting.DARK_GRAY + "Item: " + TextFormatting.GOLD + I18n.format(entry.getKey()) + TextFormatting.RESET + ", dropped " + TextFormatting.GRAY + entry.getValue() + TextFormatting.RESET + " times. (" + TextFormatting.GRAY + NumberUtil.roundToNthDecimalPlace((entry.getValue() / (float)count) * 100, 5) + "%" + TextFormatting.RESET + ")\n");
                    }

                    int dropCount = count - emptyDrops;

                    System.out.print("\n");
                    System.out.print("Total drops: " + dropCount + ". Drop ratio: " + dropCount + "/" + timesToTest + " (" + NumberUtil.roundToNthDecimalPlace(dropCount / (float)timesToTest, 5) + ")\n");
                    System.out.print("---~~~---~~~---~~~\n");
                }
                else {
                    System.out.print("---~~~---~~~---~~~\n");
                    sender.sendMessage(new StringTextComponent("AoA v" + AdventOfAscension.VERSION + " loot table printout: " + lootTableId));
                    sender.sendMessage(new StringTextComponent("---~~~---~~~---~~~"));
                    sender.sendMessage(new StringTextComponent("Total rolls: " + timesToTest));
                    sender.sendMessage(new StringTextComponent("With luck: " + luck));
                    sender.sendMessage(new StringTextComponent("Drops:"));

                    int count = 0;
                    Integer emptyDrops = sortedLootMap.get("empty");

                    if (emptyDrops == null)
                        emptyDrops = 0;

                    for (Integer val : sortedLootMap.values()) {
                        count += val;
                    }

                    sender.sendMessage(new StringTextComponent(TextFormatting.DARK_GRAY + "Empty: " + TextFormatting.GRAY + emptyDrops + TextFormatting.RESET + " times. (" + TextFormatting.GRAY + NumberUtil.roundToNthDecimalPlace((emptyDrops / (float)timesToTest) * 100, 5) + "%" + TextFormatting.RESET + ")"));

                    sortedLootMap.remove("empty");

                    for (Map.Entry<String, Integer> entry : sortedLootMap.entrySet()) {
                        sender.sendMessage(new StringTextComponent(TextFormatting.DARK_GRAY + "Item: " + TextFormatting.GOLD + I18n.format(entry.getKey()) + TextFormatting.RESET + ", dropped " + TextFormatting.GRAY + entry.getValue() + TextFormatting.RESET + " times. (" + TextFormatting.GRAY + NumberUtil.roundToNthDecimalPlace((entry.getValue() / (float)count) * 100, 5) + "%" + TextFormatting.RESET + ")"));
                    }

                    int dropCount = count - 0;

                    sender.sendMessage(new StringTextComponent("Total drops: " + dropCount + ". Drop ratio: " + dropCount + "/" + timesToTest + " (" + NumberUtil.roundToNthDecimalPlace(dropCount / (float)timesToTest, 5) + ")"));
                }
            } catch (Exception ex) {
                sender.sendMessage(new StringTextComponent("Unable to test loot table: " + lootTableId));
                ex.printStackTrace();
            }
        }
    }
}
