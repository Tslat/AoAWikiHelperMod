package net.tslat.aoawikihelpermod.dataskimmers;

import com.google.common.collect.HashMultimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.content.entity.base.AoATrader;
import net.tslat.aoa3.content.entity.npc.trader.UndeadHeraldEntity;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.ObjectHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.MerchantTradePrintHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MerchantsSkimmer {
	public static final HashMap<VillagerProfession, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>>> TRADE_PRINTERS_BY_PROFESSION = new HashMap<>();
	public static final HashMap<ResourceLocation, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>>> TRADE_PRINTERS_BY_AOA_TRADER = new HashMap<>();

	public static final HashMultimap<ResourceLocation, MerchantTradePrintHandler> TRADES_BY_COST = HashMultimap.create();
	public static final HashMultimap<ResourceLocation, MerchantTradePrintHandler> TRADES_BY_ITEM = HashMultimap.create();

	private static Villager merchantInstance = null;

	public static void init(MinecraftServer server) {
		ServerLevel world = server.getLevel(Level.OVERWORLD);

		if (world == null)
			return;

		TRADES_BY_ITEM.clear();
		TRADES_BY_ITEM.clear();
		TRADE_PRINTERS_BY_PROFESSION.clear();
		TRADE_PRINTERS_BY_AOA_TRADER.clear();

		for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : ForgeRegistries.VILLAGER_PROFESSIONS.getEntries()) {
			ResourceLocation id = entry.getKey().location();
			Int2ObjectMap<VillagerTrades.ItemListing[]> trades = VillagerTrades.TRADES.get(entry.getValue());

			for (int i = 1; i <= 5; i++) {
				if (!trades.containsKey(i))
					continue;

				VillagerTrades.ItemListing[] offers = trades.get(i);

				for (VillagerTrades.ItemListing offer : offers) {
					mapTradeToIngredients(world, entry.getValue(), i, offer);
				}
			}
		}

		for (EntityType<?> entityType : ObjectHelper.scrapeRegistryForEntities(type -> ForgeRegistries.ENTITY_TYPES.getKey(type).getNamespace().equals(AdventOfAscension.MOD_ID))) {
			try {
				Entity entity = entityType.create(world, null, null, null, new BlockPos(0, 100, 0), MobSpawnType.TRIGGERED, false, false);

				if (entity == null)
					continue;

				if (entity instanceof AoATrader) {
					AoATrader trader = (AoATrader)entity;
					Int2ObjectMap<VillagerTrades.ItemListing[]> trades = trader.getTradesMap();

					if (trades == null)
						continue;

					for (int i = 1; i <= 5; i++) {
						if (!trades.containsKey(i))
							continue;

						VillagerTrades.ItemListing[] offers = trades.get(i);

						for (VillagerTrades.ItemListing offer : offers) {
							mapTradeToIngredients(trader, i, offer);
						}
					}

					if (trader instanceof UndeadHeraldEntity) {
						for (ServerLevel tradeWorld : server.getAllLevels()) {
							MerchantOffer offer = ((UndeadHeraldEntity)trader).getAdditionalBannerTrade(tradeWorld);

							if (offer != null)
								mapTradeToIngredients(trader, 1, offer);
						}
					}
				}

				entity.discard();
			}
			catch (Exception ex) {
				AoAWikiHelperMod.LOGGER.log(org.apache.logging.log4j.Level.ERROR, "Unable to instantiate entity, skipping", ex);
			}
		}
	}

	private static void mapTradeToIngredients(AoATrader trader, int professionLevel, MerchantOffer offer) {
		MerchantTradePrintHandler handler = new MerchantTradePrintHandler(trader, professionLevel, offer);
		Int2ObjectMap<ArrayList<MerchantTradePrintHandler>> tradeMap = TRADE_PRINTERS_BY_AOA_TRADER.get(ForgeRegistries.ENTITY_TYPES.getKey(trader.getType()));

		if (tradeMap == null) {
			tradeMap = new Int2ObjectOpenHashMap<>();

			TRADE_PRINTERS_BY_AOA_TRADER.put(ForgeRegistries.ENTITY_TYPES.getKey(trader.getType()), tradeMap);
		}

		ArrayList<MerchantTradePrintHandler> tieredTrades = tradeMap.get(professionLevel);

		if (tieredTrades == null) {
			tieredTrades = new ArrayList<>();

			tradeMap.put(professionLevel, tieredTrades);
		}

		tieredTrades.add(handler);
		TRADES_BY_COST.put(ForgeRegistries.ITEMS.getKey(offer.getBaseCostA().getItem()), handler);

		if (offer.getCostB() != ItemStack.EMPTY && offer.getCostB() != null)
			TRADES_BY_COST.put(ForgeRegistries.ITEMS.getKey(offer.getCostB().getItem()), handler);

		TRADES_BY_ITEM.put(ForgeRegistries.ITEMS.getKey(offer.getResult().getItem()), handler);
	}

	private static void mapTradeToIngredients(AoATrader trader, int professionLevel, VillagerTrades.ItemListing trade) {
		MerchantOffer offer = trade.getOffer(trader, RandomSource.create());

		if (offer != null)
			mapTradeToIngredients(trader, professionLevel, offer);
	}

	private static void mapTradeToIngredients(ServerLevel world, VillagerProfession profession, int professionLevel, VillagerTrades.ItemListing trade) {
		if (merchantInstance == null) {
			merchantInstance = new Villager(EntityType.VILLAGER, world);
			merchantInstance.finalizeSpawn(world, world.getCurrentDifficultyAt(new BlockPos(0, 0, 0)), MobSpawnType.TRIGGERED, null, null);
		}

		merchantInstance.setVillagerData(merchantInstance.getVillagerData().setProfession(profession));

		MerchantOffer offer;

		if (trade instanceof VillagerTrades.TreasureMapForEmeralds) {
			VillagerTrades.TreasureMapForEmeralds emeraldTrade = (VillagerTrades.TreasureMapForEmeralds)trade;

			offer = new MerchantOffer(new ItemStack(Items.EMERALD, emeraldTrade.emeraldCost), new ItemStack(Items.COMPASS), new ItemStack(Items.FILLED_MAP), emeraldTrade.maxUses, emeraldTrade.villagerXp, 0.2f);
		}
		else {
			offer = trade.getOffer(merchantInstance, RandomSource.create());
		}

		if (offer == null)
			return;

		MerchantTradePrintHandler handler = new MerchantTradePrintHandler(profession, professionLevel, offer);
		Int2ObjectMap<ArrayList<MerchantTradePrintHandler>> tradeMap = TRADE_PRINTERS_BY_PROFESSION.get(profession);

		if (tradeMap == null) {
			tradeMap = new Int2ObjectOpenHashMap<>();

			TRADE_PRINTERS_BY_PROFESSION.put(profession, tradeMap);
		}

		ArrayList<MerchantTradePrintHandler> tieredTrades = tradeMap.get(professionLevel);

		if (tieredTrades == null) {
			tieredTrades = new ArrayList<>();

			tradeMap.put(professionLevel, tieredTrades);
		}

		tieredTrades.add(handler);
		TRADES_BY_COST.put(ForgeRegistries.ITEMS.getKey(offer.getBaseCostA().getItem()), handler);

		if (offer.getCostB() != ItemStack.EMPTY && offer.getCostB() != null)
			TRADES_BY_COST.put(ForgeRegistries.ITEMS.getKey(offer.getCostB().getItem()), handler);

		TRADES_BY_ITEM.put(ForgeRegistries.ITEMS.getKey(offer.getResult().getItem()), handler);
	}
}
