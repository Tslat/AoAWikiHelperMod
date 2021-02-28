package net.tslat.aoawikihelpermod.trades;

import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.entity.base.AoATrader;
import net.tslat.aoa3.entity.npc.AoATraderRecipe;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TradesWriter {
    public static File configDir = null;
    private static PrintWriter writer = null;

    protected static void printTradeOutputs(ICommandSource sender, ItemStack targetStack, boolean copyToClipboard) {
        if (writer != null) {
            sender.sendMessage(new StringTextComponent("You're already outputting data! Wait a moment and try again"));

            return;
        }

        String fileName = targetStack.getItem().getDisplayName(targetStack).getString() + " Output Trades.txt";
        int count = 0;
        int traderCount = 0;

        enableWriter(fileName);

        World world = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD);
        Method tradesMethod = ObfuscationReflectionHelper.findMethod(AoATrader.class, "getTradesList", Void.class, NonNullList.class);
        HashMap<AoATrader, ArrayList<AoATraderRecipe>> matchedTraderTrades = new HashMap<AoATrader, ArrayList<AoATraderRecipe>>();

        for (EntityType entry: ForgeRegistries.ENTITIES.getValues()) {
            Entity traderCandidate = entry.create(world);

            if (AoATrader.class.isAssignableFrom(traderCandidate.getClass())) {
                AoATrader trader = (AoATrader)traderCandidate;
                NonNullList<AoATraderRecipe> trades = NonNullList.<AoATraderRecipe>create();
                ArrayList<AoATraderRecipe> matchedTrades = new ArrayList<AoATraderRecipe>();


                try {
                    tradesMethod.invoke(trader, trades);
                }
                catch (Exception e) {
                    continue;
                }

                if (trades.isEmpty())
                    continue;

                for (AoATraderRecipe recipe : trades) {
                    if (quickCompareStacks(targetStack, recipe.getSellingStack()))
                        matchedTrades.add(recipe);
                }

                if (!matchedTrades.isEmpty())
                    matchedTraderTrades.put(trader, matchedTrades);
            }
        }

        write("{|class=\"wikitable\"");
        write("|-");
        write("! Item !! Cost !! Trader");

        for (AoATrader trader : matchedTraderTrades.keySet().stream().sorted(new TraderComparator()).collect(Collectors.toList())) {
            traderCount++;

            for (AoATraderRecipe trade : matchedTraderTrades.get(trader)) {
                count++;

                ItemStack buyStack1 = trade.getBuyingStackFirst();
                ItemStack buyStack2 = trade.getBuyingStackSecond();
                ItemStack sellStack = trade.getSellingStack();

                write("|-");
                write("|" + buildPictureLink(sellStack) + " " + sellStack.getCount() + " " + buildItemLink(sellStack, targetStack) + " || " + buildPictureLink(buyStack1) + " " + buyStack1.getCount() + " " + buildItemLink(buyStack1, targetStack) + (buyStack2 == ItemStack.EMPTY ? "" : " + " + buildPictureLink(buyStack2) + " " + buyStack2.getCount() + " " + buildItemLink(buyStack2, targetStack)) + " || [[" + trader.getDisplayName().getString() + "]]");
            }
        }

        write("|}");

        disableWriter();
        sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Printed out " + count + " trades from " + traderCount + " traders paying out ", new File(configDir, fileName), targetStack.getDisplayName().getString(), copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
    }

    protected static void printTradeUsages(ICommandSource sender, ItemStack targetStack, boolean copyToClipboard) {
        if (writer != null) {
            sender.sendMessage(new StringTextComponent("You're already outputting data! Wait a moment and try again"));

            return;
        }

        String fileName = targetStack.getItem().getDisplayName(targetStack).getString() + " Usage Trades.txt";
        int count = 0;
        int traderCount = 0;

        enableWriter(fileName);

        World world = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD);
        Method tradesMethod = ObfuscationReflectionHelper.findMethod(AoATrader.class, "getTradesList", Void.class, NonNullList.class);
        HashMap<AoATrader, ArrayList<AoATraderRecipe>> matchedTraderTrades = new HashMap<AoATrader, ArrayList<AoATraderRecipe>>();

        for (EntityType entry : ForgeRegistries.ENTITIES.getValues()) {
            Entity traderCandidate = entry.create(world);

            if (AoATrader.class.isAssignableFrom(traderCandidate.getClass())) {
                AoATrader trader = (AoATrader)traderCandidate;
                NonNullList<AoATraderRecipe> trades = NonNullList.<AoATraderRecipe>create();
                ArrayList<AoATraderRecipe> matchedTrades = new ArrayList<AoATraderRecipe>();

                try {
                    tradesMethod.invoke(trader, trades);
                }
                catch (Exception e) {
                    continue;
                }

                if (trades.isEmpty())
                    continue;

                for (AoATraderRecipe recipe : trades) {
                    if (quickCompareStacks(targetStack, recipe.getBuyingStackFirst(), recipe.getBuyingStackSecond()))
                        matchedTrades.add(recipe);
                }

                if (!matchedTrades.isEmpty())
                    matchedTraderTrades.put(trader, matchedTrades);
            }
        }

        write("{|class=\"wikitable\"");
        write("|-");
        write("! Item !! Cost !! Trader");

        for (AoATrader trader : matchedTraderTrades.keySet().stream().sorted(new TraderComparator()).collect(Collectors.toList())) {
            traderCount++;

            for (AoATraderRecipe trade : matchedTraderTrades.get(trader)) {
                count++;

                ItemStack buyStack1 = trade.getBuyingStackFirst();
                ItemStack buyStack2 = trade.getBuyingStackSecond();
                ItemStack sellStack = trade.getSellingStack();

                write("|-");
                write("|" + buildPictureLink(sellStack) + " " + sellStack.getCount() + " " + buildItemLink(sellStack, targetStack) + " || " + buildPictureLink(buyStack1) + " " + buyStack1.getCount() + " " + buildItemLink(buyStack1, targetStack) + (buyStack2 == ItemStack.EMPTY ? "" : " + " + buildPictureLink(buyStack2) + " " + buyStack2.getCount() + " " + buildItemLink(buyStack2, targetStack)) + " || [[" + trader.getDisplayName().getString() + "]]");
            }
        }

        write("|}");

        disableWriter();
        sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Printed out " + count + " trades from " + traderCount + " traders using ", new File(configDir, fileName), targetStack.getDisplayName().getString(), copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
    }

    protected static void printTraderTrades(ICommandSource sender, String name, NonNullList<AoATraderRecipe> trades, boolean copyToClipboard) {
        if (writer != null) {
            sender.sendMessage(new StringTextComponent("You're already outputting data! Wait a moment and try again"));

            return;
        }

        String fileName = name + " Trades.txt";
        int count = 0;

        enableWriter(fileName);

        write("{|class=\"wikitable\"");
        write("|-");
        write("! Price !! Item");

        for (AoATraderRecipe recipe : trades) {
            count++;
            write("|-");

            ItemStack firstBuyStack = recipe.getBuyingStackFirst();
            ItemStack secondBuyStack = recipe.getBuyingStackSecond();
            ItemStack sellStack = recipe.getSellingStack();

            write("|" + buildPictureLink(firstBuyStack) + " " + firstBuyStack.getCount() + " " + buildItemLink(firstBuyStack, null) + (secondBuyStack == ItemStack.EMPTY ? "" : " + " + buildPictureLink(secondBuyStack) + " " + secondBuyStack.getCount() + " " + buildItemLink(secondBuyStack, null)));
            write("|" + buildPictureLink(sellStack) + " " + sellStack.getCount() + " " + buildItemLink(sellStack, null));
        }

        write("|}");

        disableWriter();
        sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Printed out " + count + " trades from ", new File(configDir, fileName), name, copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
    }

    private static boolean quickCompareStacks(ItemStack stack1, ItemStack... compareStacks) {
        for (ItemStack stack : compareStacks) {
            if (stack1.getItem() == stack.getItem() && stack1.getItem().getDisplayName(stack1).getString().equals(stack.getItem().getDisplayName(stack).getString()))
                return true;
        }

        return false;
    }

    private static String buildPictureLink(ItemStack stack) {
        return "[[File:" + stack.getItem().getDisplayName(stack).getString() + ".png" + (stack.getItem() instanceof BlockItem ? "|32px" : "") + "]]";
    }

    private static String buildItemLink(ItemStack item, @Nullable ItemStack targetStack) {
        String stackName = item.getItem().getDisplayName(item).getString();
        String pluralName = item.getCount() > 1 && !stackName.endsWith("s") && !stackName.endsWith("y") ? stackName.endsWith("x") || stackName.endsWith("o") ? stackName + "es" : stackName + "s" : stackName;
        boolean shouldLink = targetStack == null || item.getItem() != targetStack.getItem();
        StringBuilder builder = new StringBuilder(shouldLink ? "[[" : "");

        if (item.getItem().getRegistryName().getNamespace().equals("minecraft")) {
            builder.append("mcw:");
            builder.append(stackName);
            builder.append("|");
        }
        else if (shouldLink && !pluralName.equals(stackName)) {
            builder.append(stackName);
            builder.append("|");
        }

        builder.append(pluralName);

        if (shouldLink)
            builder.append("]]");

        return builder.toString();
    }

    private static class TraderComparator implements Comparator<AoATrader> {
        @Override
        public int compare(AoATrader trader1, AoATrader trader2) {
            return trader1.getDisplayName().getString().compareTo(trader2.getDisplayName().getString());
        }
    }

    private static void enableWriter(final String fileName) {
        configDir = AoAWikiHelperMod.prepConfigDir("Trades");

        File streamFile = new File(configDir, fileName);

        try {
            if (streamFile.exists())
                streamFile.delete();

            streamFile.createNewFile();

            writer = new PrintWriter(streamFile);
        }
        catch (Exception e) {}
    }

    private static void write(String line) {
        if (writer != null)
            writer.println(line);
    }

    private static void disableWriter() {
        if (writer != null)
            IOUtils.closeQuietly(writer);

        writer = null;
    }
}
