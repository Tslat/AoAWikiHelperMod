package net.tslat.aoawikihelpermod.loottables;

import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.IRandomRange;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

public class LootTableWriter {
    public static File configDir = null;
    private static PrintWriter writer = null;

    public static void writeTable(String name, List<LootPool> pools, ICommandSource sender, boolean copyToClipboard, String type) {
        AccessibleLootTable table = new AccessibleLootTable(pools, type);
        PlayerEntity pl = sender instanceof PlayerEntity ? (PlayerEntity)sender : null;
        String fileName = name + " Loot Table.txt";

        enableWriter(fileName);
        write("{{LootTable");

        if (!type.equals(""))
            write("|type=" + type);

        for (int i = 0; i < table.pools.size(); i++) {
            AccessibleLootTable.AccessibleLootPool pool = table.pools.get(i);
            int poolNum = i + 1;

            write("|pool" + poolNum + "=");

            for (AccessibleLootTable.AccessibleLootEntry entry : pool.lootEntries) {
                String entryName = entry.getEntryName(pl);
                String amount = entry.amountRange == null ? null : (entry.amountRange.getMin() == entry.amountRange.getMax() ? String.valueOf((int)entry.amountRange.getMin()) : (int)entry.amountRange.getMin() + "-" + (int)entry.amountRange.getMax());
                StringBuilder builder = new StringBuilder("item:");
                String notes = entry.getNotes();

                builder.append(entryName);

                if (entry.isTable || entryName.equals("Nothing"))
                    builder.append("; image:none");

                builder.append("; weight:");
                builder.append(entry.weight);

                if (amount != null) {
                    builder.append("; quantity:");
                    builder.append(amount);
                }

                if (entry.bonusRange != null && (entry.bonusRange.getMin() != 0 || entry.bonusRange.getMax() != 0)) {
                    builder.append("; looting: ");
                    builder.append((int)entry.bonusRange.getMin());

                    if (entry.bonusRange.getMax() != entry.bonusRange.getMin()) {
                        builder.append("-");
                        builder.append((int)entry.bonusRange.getMax());
                    }
                }

                builder.append("; group:");
                builder.append(poolNum);

                if (notes.length() > 0) {
                    builder.append("; notes:");
                    builder.append(notes);
                }

                write(builder.toString());
            }

            IRandomRange poolRolls = pool.rolls;

            if (poolRolls instanceof ConstantRange) {
                write("|rolls" + poolNum + "=" + ((ConstantRange)poolRolls).generateInt(new Random()));
            } else if (poolRolls instanceof RandomValueRange) {
                write("|rolls" + poolNum + "=" + (int)((RandomValueRange)poolRolls).getMin() + "-" + (int)((RandomValueRange)poolRolls).getMax());
            } else {
                AoAWikiHelperMod.LOGGER.error("Found some range type that's not constant or random value, skipping");
            }

            if (pool.bonusRolls != null && (pool.bonusRolls.getMin() != 0 || pool.bonusRolls.getMax() != 0))
                write("|bonusrolls" + poolNum + "=" + (int)pool.bonusRolls.getMin() + (pool.bonusRolls.getMin() == pool.bonusRolls.getMax() ? "" : "-" + (int)pool.bonusRolls.getMax()));

            String poolNotes = pool.getNotes();

            if (!poolNotes.equals("")) {
                write("|notes" + poolNum + "=" + poolNotes);
            }
        }

        write("}}");
        disableWriter();
        sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Generated loot table: ", new File(configDir, fileName), name, copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
    }

    private static void enableWriter(final String fileName) {
        configDir = AoAWikiHelperMod.prepConfigDir("Loot Tables");

        File streamFile = new File(configDir, fileName);

        try {
            if (streamFile.exists())
                streamFile.delete();

            streamFile.createNewFile();

            writer = new PrintWriter(streamFile);
        }
        catch (Exception e) {}
    }

    private static void disableWriter() {
        if (writer != null)
            IOUtils.closeQuietly(writer);

        writer = null;
    }

    private static void write(String line) {
        if (writer != null)
            writer.println(line);
    }
}