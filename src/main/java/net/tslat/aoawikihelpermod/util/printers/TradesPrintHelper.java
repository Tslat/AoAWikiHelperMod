package net.tslat.aoawikihelpermod.util.printers;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.tslat.aoawikihelpermod.util.WikiTemplateHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.MerchantTradePrintHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TradesPrintHelper extends TablePrintHelper {
	protected TradesPrintHelper(String fileName, boolean isTraderPrintout) throws IOException {
		super(fileName, isTraderPrintout ?
		                new String[] {"Level", WikiTemplateHelper.tooltip("Number of trades", "The number of trades that will be randomly offered from this list."), "Price", "Price multiplier", "Item", "Stock", "Profession XP"} :
		                new String[] {"Obtained from", "Level", "Price", "Price multiplier", "Item", "Stock", "Profession XP"});
	}

	@Nullable
	public static TradesPrintHelper open(String fileName, boolean isTraderPrintout) {
		try {
			return new TradesPrintHelper(fileName, isTraderPrintout);
		}
		catch (IOException ex) {
			return null;
		}
	}

	public static String getProfessionLevelString(int professionLevel) {
		String profession;

		switch (professionLevel) {
			case 5:
				profession = "Master";
				break;
			case 4:
				profession = "Expert";
				break;
			case 3:
				profession = "Journeyman";
				break;
			case 2:
				profession = "Apprentice";
				break;
			case 1:
			default:
				profession = "Novice";
			break;
		};

		return professionLevel + " (" + profession + ")";

		//return WikiTemplateHelper.makeWikiTemplateObject("LevelCell", true, professionLevel + " (" + profession + ")");
	}

	public void handleTradeMap(Int2ObjectMap<ArrayList<MerchantTradePrintHandler>> trades) {
		for (int i = 1; i <= 5; i++) {
			if (!trades.containsKey(i))
				continue;

			ArrayList<MerchantTradePrintHandler> tieredTrades = trades.get(i);

			if (tieredTrades.size() > 1) {
				for (int j = 0; j < tieredTrades.size(); j++) {
					MerchantTradePrintHandler handler = tieredTrades.get(j);
					String[] handlerLines = handler.getPrintoutData(null);

					if (j == 0) {
						String rowspan = "rowspan=" + tieredTrades.size() + " | ";
						String[] lines = new String[7];
						lines[0] = rowspan + getProfessionLevelString(i);
						lines[1] = rowspan + handler.getTradesToUnlockAtLevel();
						lines[2] = handlerLines[0];
						lines[3] = handlerLines[1];
						lines[4] = handlerLines[2];
						lines[5] = handlerLines[3];
						lines[6] = handlerLines[4];

						entry(lines);
					}
					else {
						forceEntry(handlerLines);
					}
				}
			}
			else {
				for (MerchantTradePrintHandler handler : tieredTrades) {
					String[] lines = new String[7];
					String[] handlerLines = handler.getPrintoutData(null);
					lines[0] = getProfessionLevelString(i);
					lines[1] = String.valueOf(handler.getTradesToUnlockAtLevel());
					lines[2] = handlerLines[0];
					lines[3] = handlerLines[1];
					lines[4] = handlerLines[2];
					lines[5] = handlerLines[3];
					lines[6] = handlerLines[4];

					forceEntry(lines);
				}
			}
		}
	}

	public void handleTradeList(Set<MerchantTradePrintHandler> trades) {
		HashMap<String, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>>> tradeMap = new HashMap<String, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>>>();

		for (MerchantTradePrintHandler trade : trades) {
			String traderName = trade.getTradeDetails().getFirst();
			int tradeLevel = trade.getTradeDetails().getSecond();

			if (!tradeMap.containsKey(traderName))
				tradeMap.put(traderName, new Int2ObjectOpenHashMap<ArrayList<MerchantTradePrintHandler>>());

			Int2ObjectMap<ArrayList<MerchantTradePrintHandler>> traderOfferList = tradeMap.get(traderName);

			if (!traderOfferList.containsKey(tradeLevel))
				traderOfferList.put(tradeLevel, new ArrayList<MerchantTradePrintHandler>());

			ArrayList<MerchantTradePrintHandler> tradesForLevel = traderOfferList.get(tradeLevel);

			tradesForLevel.add(trade);
		}

		for (Map.Entry<String, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>>> entry : tradeMap.entrySet()) {
			int totalTrades = 0;

			for (Map.Entry<Integer, ArrayList<MerchantTradePrintHandler>> tradeEntry : entry.getValue().int2ObjectEntrySet()) {
				totalTrades += tradeEntry.getValue().size();
			}

			if (totalTrades > 1) {
				for (int i = 0; i < totalTrades; i++) {
					if (i == 0) {
						boolean firstEntry = true;

						for (int tier = 1; tier <= 5; tier++) {
							if (!entry.getValue().containsKey(tier))
								continue;

							ArrayList<MerchantTradePrintHandler> tieredTrades = entry.getValue().get(tier);

							if (tieredTrades.size() > 1) {
								for (int j = 0; j < tieredTrades.size(); j++) {
									i++;
									String[] handlerLines = tieredTrades.get(j).getPrintoutData(null);

									if (j == 0) {
										if (firstEntry) {
											String[] lines = new String[7];
											String rowSpan = "rowspan=" + totalTrades + " | ";
											String tradeRowspan = "rowspan=" + tieredTrades.size() + " | ";
											lines[0] = rowSpan + entry.getKey();
											lines[1] = tradeRowspan + getProfessionLevelString(tier);
											lines[2] = handlerLines[0];
											lines[3] = handlerLines[1];
											lines[4] = handlerLines[2];
											lines[5] = handlerLines[3];
											lines[6] = handlerLines[4];

											entry(lines);
										}
										else {
											String[] lines = new String[6];
											String tradeRowspan = "rowspan=" + tieredTrades.size() + " | ";
											lines[0] = tradeRowspan + getProfessionLevelString(tier);
											lines[1] = handlerLines[0];
											lines[2] = handlerLines[1];
											lines[3] = handlerLines[2];
											lines[4] = handlerLines[3];
											lines[5] = handlerLines[4];

											forceEntry(lines);
										}

									}
									else {
										forceEntry(handlerLines);
									}

									firstEntry = false;
								}
							}
							else {
								for (MerchantTradePrintHandler handler : tieredTrades) {
									i++;

									if (firstEntry) {
										String rowSpan = "rowspan=" + totalTrades + " | ";
										String[] lines = new String[7];
										String[] handlerLines = handler.getPrintoutData(null);
										lines[0] = rowSpan + entry.getKey();
										lines[1] = getProfessionLevelString(tier);
										lines[2] = handlerLines[0];
										lines[3] = handlerLines[1];
										lines[4] = handlerLines[2];
										lines[5] = handlerLines[3];
										lines[6] = handlerLines[4];

										entry(lines);
									}
									else {
										String[] lines = new String[6];
										String[] handlerLines = handler.getPrintoutData(null);
										lines[0] = getProfessionLevelString(tier);
										lines[1] = handlerLines[0];
										lines[2] = handlerLines[1];
										lines[3] = handlerLines[2];
										lines[4] = handlerLines[3];
										lines[5] = handlerLines[4];

										forceEntry(lines);
									}

									firstEntry = false;
								}
							}
						}
					}
					else {
						String[] lines = new String[6];

						for (int tier = 1; tier <= 5; tier++) {
							if (!entry.getValue().containsKey(tier))
								continue;

							ArrayList<MerchantTradePrintHandler> tieredTrades = entry.getValue().get(tier);

							if (tieredTrades.size() > 1) {
								for (int j = 0; j < tieredTrades.size(); j++) {
									i++;
									String[] handlerLines = tieredTrades.get(j).getPrintoutData(null);

									if (j == 0) {
										String tradeRowspan = "rowspan=" + tieredTrades.size() + " | ";
										lines[0] = tradeRowspan + getProfessionLevelString(tier);
										lines[1] = handlerLines[0];
										lines[2] = handlerLines[1];
										lines[3] = handlerLines[2];
										lines[4] = handlerLines[3];
										lines[5] = handlerLines[4];

										forceEntry(lines);
									}
									else {
										forceEntry(handlerLines);
									}
								}
							}
							else {
								for (MerchantTradePrintHandler handler : tieredTrades) {
									i++;
									String[] handlerLines = handler.getPrintoutData(null);
									lines[0] = getProfessionLevelString(tier);
									lines[1] = handlerLines[0];
									lines[2] = handlerLines[1];
									lines[3] = handlerLines[2];
									lines[4] = handlerLines[3];
									lines[5] = handlerLines[4];

									forceEntry(lines);
								}
							}
						}
					}
				}
			}
			else {
				String[] lines = new String[7];
				lines[0] = entry.getKey();

				for (int tier = 1; tier <= 5; tier++) {
					if (!entry.getValue().containsKey(tier))
						continue;

					ArrayList<MerchantTradePrintHandler> tieredTrades = entry.getValue().get(tier);

					for (int j = 0; j < tieredTrades.size(); j++) {
						String[] handlerLines = tieredTrades.get(j).getPrintoutData(null);

						if (j == 0) {
							lines[1] = getProfessionLevelString(tier);
							lines[2] = handlerLines[0];
							lines[3] = handlerLines[1];
							lines[4] = handlerLines[2];
							lines[5] = handlerLines[3];
							lines[6] = handlerLines[4];

							entry(lines);
						}
						else {
							forceEntry(handlerLines);
						}
					}
				}
			}
		}
	}
}
