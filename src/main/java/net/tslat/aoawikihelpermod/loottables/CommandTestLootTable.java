package net.tslat.aoawikihelpermod.loottables;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.utils.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class CommandTestLootTable extends CommandBase {
	@Override
	public String getName() {
		return "testloottable";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/testloottable <Loot Table Path> optionals:[luck:|times:|printtoconsole|pool:]";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();

		if (!world.isRemote) {
			if (args.length < 1) {
				sender.sendMessage(new TextComponentString("What loot table do you want to test?"));

				return;
			}

			try {
				LootTable table = world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(args[0]));

				if (table == LootTable.EMPTY_LOOT_TABLE) {
					sender.sendMessage(new TextComponentString("Unable to find loot table: " + args[0]));

					return;
				}

				int luck = 0;
				EntityPlayer player = sender instanceof EntityPlayer ? (EntityPlayer)sender : null;
				int timesToTest = 1;
				boolean printToConsole = false;
				LootPool specificPool = null;

				for (String arg : args) {
					if (arg.contains("luck:")) {
						luck = Integer.valueOf(arg.split("luck:")[1]);
					}
					else if (arg.contains("times:")) {
						timesToTest = Integer.valueOf(arg.split("times:")[1]);
					}
					else if (arg.contains("printtoconsole")) {
						printToConsole = true;
					}
					else if (arg.contains("pool:")) {
						specificPool = table.getPool(arg.split("pool:")[1]);
					}
				}

				LootContext.Builder builder = new LootContext.Builder((WorldServer)world).withLuck(luck);
				LootContext context;

				if (player != null) {
					builder.withPlayer(player);
				}

				context = builder.build();

				HashMap<String, Integer> lootMap = new HashMap<String, Integer>();

				for (int i = 0; i < timesToTest; i++) {
					List<ItemStack> lootStacks;

					if (specificPool != null) {
						specificPool.generateLoot(lootStacks = new ArrayList<ItemStack>(), world.rand, context);
					}
					else {
						lootStacks = table.generateLootForPools(world.rand, context);
					}

					if (lootStacks.isEmpty())
						lootMap.merge("empty", 1, Integer::sum);

					for (ItemStack stack : lootStacks) {
						lootMap.merge(stack.getUnlocalizedName(), 1, Integer::sum);
					}
				}

				HashMap<String, Integer> sortedLootMap = lootMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, LinkedHashMap::new));

				if (printToConsole || player == null) {
					System.out.print("---~~~---~~~---~~~\n");
					System.out.print("AoA v" + AdventOfAscension.version + " loot table printout: " + args[0] + "\n");

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

					System.out.print("        " + TextFormatting.DARK_GRAY + "Empty: " + TextFormatting.GRAY + emptyDrops + TextFormatting.RESET + " times. (" + TextFormatting.GRAY + StringUtil.roundToNthDecimalPlace((emptyDrops / (float)timesToTest) * 100, 5) + "%" + TextFormatting.RESET + ")\n");

					sortedLootMap.remove("empty");

					for (Map.Entry<String, Integer> entry : sortedLootMap.entrySet()) {
						System.out.print("        " + TextFormatting.DARK_GRAY + "Item: " + TextFormatting.GOLD + StringUtil.getLocaleString(entry.getKey() + ".name") + TextFormatting.RESET + ", dropped " + TextFormatting.GRAY + entry.getValue() + TextFormatting.RESET + " times. (" + TextFormatting.GRAY + StringUtil.roundToNthDecimalPlace((entry.getValue() / (float)count) * 100, 5) + "%" + TextFormatting.RESET + ")\n");
					}

					int dropCount = count - emptyDrops;

					System.out.print("\n");
					System.out.print("Total drops: " + dropCount + ". Drop ratio: " + dropCount + "/" + timesToTest + " (" + StringUtil.roundToNthDecimalPlace(dropCount / (float)timesToTest, 5) + ")\n");
					System.out.print("---~~~---~~~---~~~\n");
				}
				else {
					System.out.print("---~~~---~~~---~~~\n");
					sender.sendMessage(new TextComponentString("AoA v" + AdventOfAscension.version + " loot table printout: " + args[0]));
					sender.sendMessage(new TextComponentString("---~~~---~~~---~~~"));
					sender.sendMessage(new TextComponentString("Total rolls: " + timesToTest));
					sender.sendMessage(new TextComponentString("With luck: " + luck));
					sender.sendMessage(new TextComponentString("Drops:"));

					int count = 0;
					Integer emptyDrops = sortedLootMap.get("empty");

					if (emptyDrops == null)
						emptyDrops = 0;

					for (Integer val : sortedLootMap.values()) {
						count += val;
					}

					sender.sendMessage(new TextComponentString(TextFormatting.DARK_GRAY + "Empty: " + TextFormatting.GRAY + emptyDrops + TextFormatting.RESET + " times. (" + TextFormatting.GRAY + StringUtil.roundToNthDecimalPlace((emptyDrops / (float)timesToTest) * 100, 5) + "%" + TextFormatting.RESET + ")"));

					sortedLootMap.remove("empty");

					for (Map.Entry<String, Integer> entry : sortedLootMap.entrySet()) {
						sender.sendMessage(new TextComponentString(TextFormatting.DARK_GRAY + "Item: " + TextFormatting.GOLD + StringUtil.getLocaleString(entry.getKey() + ".name") + TextFormatting.RESET + ", dropped " + TextFormatting.GRAY + entry.getValue() + TextFormatting.RESET + " times. (" + TextFormatting.GRAY + StringUtil.roundToNthDecimalPlace((entry.getValue() / (float)count) * 100, 5) + "%" + TextFormatting.RESET + ")"));
					}

					int dropCount = count - 0;

					sender.sendMessage(new TextComponentString("Total drops: " + dropCount + ". Drop ratio: " + dropCount + "/" + timesToTest + " (" + StringUtil.roundToNthDecimalPlace(dropCount / (float)timesToTest, 5) + ")"));
				}
			}
			catch (Exception e) {
				sender.sendMessage(new TextComponentString("Unable to test loot table: " + args[0]));
				e.printStackTrace();
			}
		}
	}
}
