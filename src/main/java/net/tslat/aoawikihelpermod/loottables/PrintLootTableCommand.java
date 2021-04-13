package net.tslat.aoawikihelpermod.loottables;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntitySummonArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.item.tool.axe.BaseAxe;
import net.tslat.aoa3.item.weapon.sword.BaseSword;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintLootTableCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printloottable")
                        .then(Commands.argument("lootTableId", ResourceLocationArgument.resourceLocation())
                                .then(Commands.argument("copyToClipboard", BoolArgumentType.bool())
                                        .executes(commandContext -> {
                                            print(commandContext, ResourceLocationArgument.getResourceLocation(commandContext, "lootTableId"), BoolArgumentType.getBool(commandContext, "copyToClipboard"));
                                            return 0;
                                        }))
                                .executes(commandContext -> {
                                    print(commandContext, ResourceLocationArgument.getResourceLocation(commandContext, "lootTableId"), false);
                                    return 0;
                                })));
    }
    public static void print(CommandContext<CommandSource> context, ResourceLocation lootTableId, boolean attemptToCopy) {
        Entity sender = context.getSource().getEntity();

        if(!(sender instanceof PlayerEntity)) {
            sender.sendMessage(new StringTextComponent("This command can only be done ingame for accuracy."));
            return;
        }

        PlayerEntity player = (PlayerEntity)sender;

        World world = context.getSource().getWorld();

        if(!world.isRemote) {

            LootTable table = ServerLifecycleHooks.getCurrentServer().getLootTableManager().getLootTableFromLocation(lootTableId);

            if (table == LootTable.EMPTY_LOOT_TABLE) {
                sender.sendMessage(new StringTextComponent(TextFormatting.RED + "Unable to find loot table: " + lootTableId));

                return;
            }

            String[] pathSplit = lootTableId.toString().substring(Math.max(0, lootTableId.toString().indexOf(":"))).split("/");
            List<LootPool> pools = ObfuscationReflectionHelper.getPrivateValue(LootTable.class, table, "field_186466_c");
            String tableName = AoAWikiHelperMod.capitaliseAllWords(pathSplit[pathSplit.length - 1].replace("_", " "));
            boolean copyToClipboard = false;
            String type = "";

            /*
            for (int i = 1; i < args.length; i++) {
                switch (args[i].toLowerCase()) {
                    case "clipboard":
                        copyToClipboard = true;
                        break;
                    case "chest":
                        type = "chest";
                        break;
                    case "generic":
                        type = "generic";
                    default:
                        break;
                }
            }
            */

            if (pools.isEmpty()) {
                sender.sendMessage(new StringTextComponent("Loot table " + lootTableId + " is empty. Nothing to print."));

                return;
            }

            if (copyToClipboard && context.getSource().getServer().isDedicatedServer()) {
                sender.sendMessage(new StringTextComponent("Can't copy contents of file to clipboard on dedicated servers, skipping."));
                copyToClipboard = false;
            }

            LootTableWriter.writeTable(tableName, pools, sender, copyToClipboard, type);
        }
    }
}

