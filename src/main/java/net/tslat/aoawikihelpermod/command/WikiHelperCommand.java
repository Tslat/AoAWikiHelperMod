package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.*;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;

public class WikiHelperCommand {
	public static void registerSubCommands(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> cmd = Commands.literal("wikihelper");

		if (!AoAWikiHelperMod.isOutdatedAoA) {
			cmd.then(OverviewCommand.register())
					.then(UsagesCommand.register())
					.then(ObtainingCommand.register())
					.then(RecipeCommand.register())
					.then(LootTableCommand.register())
					.then(TradesCommand.register())
					.then(StructuresCommand.register())
					.then(BlocksCommand.register());
		}
		else {
			cmd.executes(WikiHelperCommand::outdatedCommand);
			cmd.then(Commands.literal("update_aoa").executes(WikiHelperCommand::outdatedCommand));
		}

		dispatcher.register(cmd);
	}

	private static int outdatedCommand(CommandContext<CommandSource> cmd) {
		cmd.getSource().sendFailure(new StringTextComponent("AoA is outdated! Update AoA to the latest version to use the Wikihelper mod!"));

		return 1;
	}

	protected static StringTextComponent getCmdPrefix(String subcommand) {
		return new StringTextComponent(TextFormatting.DARK_RED + "[AoAWikiHelper|" + TextFormatting.GOLD + subcommand + TextFormatting.DARK_RED + "] ");
	}

	protected static void error(CommandSource source, String subcommand, String message, ITextComponent... args) {
		error(source, subcommand, new TranslationTextComponent(message, args));
	}

	protected static void error(CommandSource source, String subcommand, IFormattableTextComponent message, ITextComponent... args) {
		source.sendFailure(getCmdPrefix(subcommand).append(message.setStyle(Style.EMPTY.applyFormat(TextFormatting.DARK_RED))));
	}

	protected static void info(CommandSource source, String subcommand, String message, ITextComponent... args) {
		info(source, subcommand, new TranslationTextComponent(message, args));
	}

	protected static void info(CommandSource source, String subcommand, IFormattableTextComponent message, ITextComponent... args) {
		source.sendSuccess(getCmdPrefix(subcommand).append(message.setStyle(Style.EMPTY.applyFormat(TextFormatting.GRAY))), true);
	}

	protected static void success(CommandSource source, String subcommand, String message, ITextComponent... args) {
		success(source, subcommand, new TranslationTextComponent(message, args));
	}

	protected static void success(CommandSource source, String subcommand, IFormattableTextComponent message) {
		source.sendSuccess(getCmdPrefix(subcommand).append(message.setStyle(Style.EMPTY.applyFormat(TextFormatting.GREEN))), true);
	}

	protected static void warn(CommandSource source, String subcommand, String message, ITextComponent... args) {
		warn(source, subcommand, new TranslationTextComponent(message, args));
	}

	protected static void warn(CommandSource source, String subcommand, IFormattableTextComponent message) {
		source.sendSuccess(getCmdPrefix(subcommand).append(message.setStyle(Style.EMPTY.applyFormat(TextFormatting.RED))), true);
	}
}
