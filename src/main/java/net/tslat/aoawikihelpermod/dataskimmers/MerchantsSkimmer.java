package net.tslat.aoawikihelpermod.dataskimmers;

import com.google.common.collect.HashMultimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.entity.base.AoATrader;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.MerchantTradePrintHandler;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MerchantsSkimmer {
	public static final HashMap<VillagerProfession, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>>> TRADE_PRINTERS_BY_PROFESSION = new HashMap<VillagerProfession, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>>>();
	public static final HashMap<ResourceLocation, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>>> TRADE_PRINTERS_BY_AOA_TRADER = new HashMap<ResourceLocation, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>>>();

	public static final HashMultimap<ResourceLocation, MerchantTradePrintHandler> TRADES_BY_COST = HashMultimap.create();
	public static final HashMultimap<ResourceLocation, MerchantTradePrintHandler> TRADES_BY_ITEM = HashMultimap.create();

	private static VillagerEntity merchantInstance = null;

	public static void init(MinecraftServer server) {
		ServerWorld world = server.getLevel(World.OVERWORLD);

		if (world == null)
			return;

		for (VillagerProfession profession : ForgeRegistries.PROFESSIONS.getValues()) {
			ResourceLocation id = profession.getRegistryName();
			Int2ObjectMap<VillagerTrades.ITrade[]> trades = VillagerTrades.TRADES.get(profession);

			for (int i = 1; i <= 5; i++) {
				if (!trades.containsKey(i))
					continue;

				VillagerTrades.ITrade[] offers = trades.get(i);

				for (VillagerTrades.ITrade offer : offers) {
					mapTradeToIngredients(world, profession, i, offer);
				}
			}
		}

		for (EntityType<?> entityType : ObjectHelper.scrapeRegistryForEntities(type -> type.getRegistryName().getNamespace().equals(AdventOfAscension.MOD_ID))) {
			try {
				Entity entity = entityType.create(world, null, null, null, new BlockPos(0, 0, 0), SpawnReason.TRIGGERED, false, false);

				if (entity == null)
					continue;

				if (entity instanceof AoATrader) {
					Int2ObjectMap<VillagerTrades.ITrade[]> trades = ((AoATrader)entity).getTradesMap();

					if (trades == null)
						continue;

					for (int i = 1; i <= 5; i++) {
						if (!trades.containsKey(i))
							continue;

						VillagerTrades.ITrade[] offers = trades.get(i);

						for (VillagerTrades.ITrade offer : offers) {
							mapTradeToIngredients((AoATrader)entity, i, offer);
						}
					}
				}

				entity.kill();
			}
			catch (Exception ex) {
				AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Unable to instantiate entity, skipping", ex);
			}
		}
	}

	private static void mapTradeToIngredients(AoATrader trader, int professionLevel, VillagerTrades.ITrade trade) {
		MerchantOffer offer = trade.getOffer(trader, new Random());

		if (offer == null)
			return;

		MerchantTradePrintHandler handler = new MerchantTradePrintHandler(trader, professionLevel, offer);
		Int2ObjectMap<ArrayList<MerchantTradePrintHandler>> tradeMap = TRADE_PRINTERS_BY_AOA_TRADER.get(trader.getType().getRegistryName());

		if (tradeMap == null) {
			tradeMap = new Int2ObjectOpenHashMap<ArrayList<MerchantTradePrintHandler>>();

			TRADE_PRINTERS_BY_AOA_TRADER.put(trader.getType().getRegistryName(), tradeMap);
		}

		ArrayList<MerchantTradePrintHandler> tieredTrades = tradeMap.get(professionLevel);

		if (tieredTrades == null) {
			tieredTrades = new ArrayList<MerchantTradePrintHandler>();

			tradeMap.put(professionLevel, tieredTrades);
		}

		tieredTrades.add(handler);
		TRADES_BY_COST.put(offer.getBaseCostA().getItem().getRegistryName(), handler);

		if (offer.getCostB() != ItemStack.EMPTY && offer.getCostB() != null)
			TRADES_BY_COST.put(offer.getCostB().getItem().getRegistryName(), handler);

		TRADES_BY_ITEM.put(offer.getResult().getItem().getRegistryName(), handler);
	}

	private static void mapTradeToIngredients(ServerWorld world, VillagerProfession profession, int professionLevel, VillagerTrades.ITrade trade) {
		if (merchantInstance == null) {
			merchantInstance = new VillagerEntity(EntityType.VILLAGER, world);
			merchantInstance.finalizeSpawn(world, world.getCurrentDifficultyAt(new BlockPos(0, 0, 0)), SpawnReason.TRIGGERED, null, null);
		}

		merchantInstance.setVillagerData(merchantInstance.getVillagerData().setProfession(profession));

		MerchantOffer offer = trade.getOffer(merchantInstance, new Random());

		if (offer == null)
			return;

		MerchantTradePrintHandler handler = new MerchantTradePrintHandler(profession, professionLevel, offer);
		Int2ObjectMap<ArrayList<MerchantTradePrintHandler>> tradeMap = TRADE_PRINTERS_BY_PROFESSION.get(profession);

		if (tradeMap == null) {
			tradeMap = new Int2ObjectOpenHashMap<ArrayList<MerchantTradePrintHandler>>();

			TRADE_PRINTERS_BY_PROFESSION.put(profession, tradeMap);
		}

		ArrayList<MerchantTradePrintHandler> tieredTrades = tradeMap.get(professionLevel);

		if (tieredTrades == null) {
			tieredTrades = new ArrayList<MerchantTradePrintHandler>();

			tradeMap.put(professionLevel, tieredTrades);
		}

		tieredTrades.add(handler);
		TRADES_BY_COST.put(offer.getBaseCostA().getItem().getRegistryName(), handler);

		if (offer.getCostB() != ItemStack.EMPTY && offer.getCostB() != null)
			TRADES_BY_COST.put(offer.getCostB().getItem().getRegistryName(), handler);

		TRADES_BY_ITEM.put(offer.getResult().getItem().getRegistryName(), handler);
	}
}
