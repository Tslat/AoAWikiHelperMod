package net.tslat.aoawikihelpermod.trades;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tslat.aoa3.entity.base.AoATrader;
import net.tslat.aoa3.entity.base.AoATraderRecipe;

import java.lang.reflect.Method;

public class CommandPrintTraderTrades extends CommandBase {
	@Override
	public String getName() {
		return "printtradertrades";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printtradertrades <trader_entity_id> [clipboard] - Prints out all the trades for a given AoA trader. Optionally copy contents to clipboard.";
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
				sender.sendMessage(new TextComponentString("No trader entity id provided. Example Usage: /printtradertrades aoa3:explosives_expert"));

				return;
			}

			EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(args[0]));
			NonNullList<AoATraderRecipe> trades = null;
			AoATrader trader = null;

			if (entry == null) {
				sender.sendMessage(new TextComponentString("Can't find entity by id: \"" + args[0] + "\"."));

				return;
			}

			Class<? extends Entity> entityClass = entry.getEntityClass();

			if (!AoATrader.class.isAssignableFrom(entityClass)) {
				sender.sendMessage(new TextComponentString("Entity \"" + entityClass.getSimpleName() + "\" is not an AoATrader subclass."));

				return;
			}

			try {
				trader = (AoATrader)entry.newInstance(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
				Method tradesMethod = ObfuscationReflectionHelper.findMethod(AoATrader.class, "getTradesList", Void.class, NonNullList.class);
				trades = NonNullList.<AoATraderRecipe>create();

				tradesMethod.invoke(trader, trades);

			}
			catch (Exception e) {
				sender.sendMessage(new TextComponentString("Unable to expand provided entity ID into AoA trader."));
				e.printStackTrace();

				return;
			}

			boolean copyToClipboard = args.length > 1 && args[1].equalsIgnoreCase("clipboard");

			if (copyToClipboard && server.isDedicatedServer()) {
				sender.sendMessage(new TextComponentString("Can't copy contents of file to clipboard on dedicated servers, skipping."));
				copyToClipboard = false;
			}

			TradesWriter.printTraderTrades(sender, trader.getDisplayName().getUnformattedText(), trades, copyToClipboard);
		}
	}
}
