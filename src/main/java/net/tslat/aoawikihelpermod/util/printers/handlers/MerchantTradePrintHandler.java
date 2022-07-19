package net.tslat.aoawikihelpermod.util.printers.handlers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.content.entity.base.AoATrader;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class MerchantTradePrintHandler {
	private static final Method getMaxTradesMethod;

	static {
		Method tempMethod;

		try {
			tempMethod = AoATrader.class.getDeclaredMethod("getMaxTradesToUnlock", int.class);
			tempMethod.setAccessible(true);
		}
		catch (NoSuchMethodException ex) {
			tempMethod = null;

			AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Unable to find 'getMaxTradesToUnlock' method from AoA.", ex);
		}

		getMaxTradesMethod = tempMethod;
	}

	private final MerchantOffer trade;
	private final Pair<String, Integer> tradeDetails;
	private final int tradesToUnlockAtLevel;

	private final HashMap<Item, String[]> printoutData = new HashMap<>();

	public MerchantTradePrintHandler(AoATrader trader, int professionLevel, MerchantOffer trade) {
		this.trade = trade;
		this.tradeDetails = Pair.of(FormattingHelper.createLinkableText(StringUtil.toTitleCase(ForgeRegistries.ENTITY_TYPES.getKey(trader.getType()).getPath()), false, true), professionLevel);
		int tradesToUnlock;

		try {
			tradesToUnlock = (int)getMaxTradesMethod.invoke(trader, professionLevel);
		}
		catch (IllegalAccessException | InvocationTargetException ex) {
			tradesToUnlock = 2;

			AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Failed to retrieve result from getMaxTradesToUnlock", ex);
		}

		this.tradesToUnlockAtLevel = tradesToUnlock;
	}

	public MerchantTradePrintHandler(VillagerProfession profession, int professionLevel, MerchantOffer trade) {
		this.trade = trade;
		this.tradeDetails = Pair.of(FormattingHelper.createLinkableText(StringUtil.toTitleCase(ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession).getPath()), true, true), professionLevel);
		this.tradesToUnlockAtLevel = 2;
	}

	public Pair<String, Integer> getTradeDetails() {
		return this.tradeDetails;
	}

	public int getTradesToUnlockAtLevel() {
		return this.tradesToUnlockAtLevel;
	}

	public String[] getPrintoutData(@Nullable Item targetItem) {
		if (this.printoutData.containsKey(targetItem))
			return this.printoutData.get(targetItem);

		String cost1 = ObjectHelper.getItemName(this.trade.getBaseCostA().getItem());
		String cost2 = this.trade.getCostB() != ItemStack.EMPTY ? ObjectHelper.getItemName(this.trade.getCostB().getItem()) : null;
		String result = ObjectHelper.getItemName(this.trade.getResult().getItem());
		String priceMultiplier = NumberUtil.roundToNthDecimalPlace(this.trade.getPriceMultiplier(), 2);

		String[] printData = new String[5];

		printData[0] = FormattingHelper.createImageBlock(cost1) + " " + this.trade.getBaseCostA().getCount() + " " + FormattingHelper.createLinkableItem(this.trade.getBaseCostA(), this.trade.getBaseCostA().getItem() != targetItem);

		if (cost2 != null)
			printData[0] += " + " + FormattingHelper.createImageBlock(cost2) + " " + this.trade.getCostB().getCount() + " " + FormattingHelper.createLinkableItem(this.trade.getCostB(), this.trade.getCostB().getItem() != targetItem);

		printData[1] = priceMultiplier;
		printData[2] = FormattingHelper.createImageBlock(result) + " " + this.trade.getResult().getCount() + " " + FormattingHelper.createLinkableItem(this.trade.getResult(), this.trade.getResult().getItem() != targetItem);
		printData[3] = String.valueOf(this.trade.getMaxUses());
		printData[4] = this.trade.shouldRewardExp() ? String.valueOf(this.trade.getXp()) : "0";

		this.printoutData.put(targetItem, printData);

		return printData;
	}
}
