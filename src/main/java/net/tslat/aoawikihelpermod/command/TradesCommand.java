package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.tslat.aoa3.library.object.MutableSupplier;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.dataskimmers.MerchantsSkimmer;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.printers.TradesPrintHelper;
import net.tslat.aoawikihelpermod.util.printers.handlers.MerchantTradePrintHandler;

import java.io.File;
import java.util.ArrayList;

public class TradesCommand implements Command<CommandSourceStack> {
	private static final TradesCommand CMD = new TradesCommand();
	private static final SuggestionProvider<CommandSourceStack> PROFESSIONS_SUGGESTIONS_PROVIDER = SuggestionProviders.register(new ResourceLocation(AoAWikiHelperMod.MOD_ID, "merchant_trades"), (context, builder) -> SharedSuggestionProvider.suggestResource(MerchantsSkimmer.TRADE_PRINTERS_BY_PROFESSION.keySet().stream().map(ForgeRegistryEntry::getRegistryName), builder));
	private static final SuggestionProvider<CommandSourceStack> AOA_TRADERS_SUGGESTIONS_PROVIDER = SuggestionProviders.register(new ResourceLocation(AoAWikiHelperMod.MOD_ID, "aoa_traders"), (context, builder) -> SharedSuggestionProvider.suggestResource(MerchantsSkimmer.TRADE_PRINTERS_BY_AOA_TRADER.keySet().stream(), builder));

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("trades").executes(CMD);

		builder.then(Commands.literal("profession")
				.then(Commands.argument("profession_id", ResourceLocationArgument.id()).suggests(PROFESSIONS_SUGGESTIONS_PROVIDER).executes(TradesCommand::printByProfession)));
		builder.then(Commands.literal("trader")
				.then(Commands.argument("trader_id", ResourceLocationArgument.id()).suggests(AOA_TRADERS_SUGGESTIONS_PROVIDER).executes(TradesCommand::printByTrader)));

		return builder;
	}

	protected String commandName() {
		return "Trades";
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out trade details by either a specific trader or profession.");

		return 1;
	}

	private static void printTrades(CommandSourceStack source, String fileName, ResourceLocation id, Int2ObjectMap<ArrayList<MerchantTradePrintHandler>> trades) {
		File outputFile;
		MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);

		try (TradesPrintHelper printHelper = TradesPrintHelper.open(fileName, true)) {
			printHelper.withClipboardOutput(clipboardContent);
			printHelper.withProperty("class", "wikitable");
			printHelper.handleTradeMap(trades);

			outputFile = printHelper.getOutputFile();
		}

		WikiHelperCommand.success(source, "Trades", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
	}

	private static int printByProfession(CommandContext<CommandSourceStack> cmd) {
		try {
			ResourceLocation id = ResourceLocationArgument.getId(cmd, "profession_id");
			VillagerProfession profession = ForgeRegistries.PROFESSIONS.getValue(id);

			if (profession == null) {
				WikiHelperCommand.error(cmd.getSource(), "Trades", "Invalid profession ID: '" + id + "'");

				return 1;
			}

			Int2ObjectMap<ArrayList<MerchantTradePrintHandler>> printHandlerMap = MerchantsSkimmer.TRADE_PRINTERS_BY_PROFESSION.get(profession);

			if (printHandlerMap == null) {
				WikiHelperCommand.info(cmd.getSource(), "Trades", "Profession '" + id + "' has no associated trades");

				return 1;
			}

			printTrades(cmd.getSource(), "Trades - Profession - " + StringUtil.toTitleCase(id.getPath()), id, printHandlerMap);
		}
		catch (Exception ex) {
			WikiHelperCommand.error(cmd.getSource(), "Trades", "Error encountered while printing trades, see log for details");

			ex.printStackTrace();
		}

		return 1;
	}

	private static int printByTrader(CommandContext<CommandSourceStack> cmd) {
		try {
			ResourceLocation id = ResourceLocationArgument.getId(cmd, "trader_id");

			if (ForgeRegistries.ENTITIES.getValue(id) == null) {
				WikiHelperCommand.error(cmd.getSource(), "Trades", "Invalid trader ID: '" + id + "'");

				return 1;
			}

			Int2ObjectMap<ArrayList<MerchantTradePrintHandler>> trades = MerchantsSkimmer.TRADE_PRINTERS_BY_AOA_TRADER.get(id);

			if (trades == null) {
				WikiHelperCommand.info(cmd.getSource(), "Trades", "Trader '" + id + "' has no associated trades");

				return 1;
			}

			printTrades(cmd.getSource(), "Trades - " + StringUtil.toTitleCase(id.getPath()), id, trades);
		}
		catch (Exception ex) {
			WikiHelperCommand.error(cmd.getSource(), "Trades", "Error encountered while printing trades, see log for details");

			ex.printStackTrace();
		}

		return 1;
	}
}
