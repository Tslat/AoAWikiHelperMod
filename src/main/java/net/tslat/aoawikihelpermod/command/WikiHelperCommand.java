package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.*;

public class WikiHelperCommand {
	public static void registerSubCommands(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> cmd = Commands.literal("wikihelper");

		cmd.then(OverviewCommand.register(dispatcher));

		dispatcher.register(cmd);
	}

	protected static StringTextComponent getCmdPrefix(String subcommand) {
		return new StringTextComponent(TextFormatting.DARK_RED + "[AoAWikiHelper|" + TextFormatting.GOLD + subcommand + TextFormatting.DARK_RED + "]");
	}

	protected static void error(CommandSource source, String subcommand, String message, ITextComponent... args) {
		source.sendFailure(getCmdPrefix(subcommand).append(new TranslationTextComponent(message, args).setStyle(Style.EMPTY.applyFormat(TextFormatting.DARK_RED))));
	}

	protected static void info(CommandSource source, String subcommand, String message, ITextComponent... args) {
		source.sendSuccess(getCmdPrefix(subcommand).append(new TranslationTextComponent(message, args).setStyle(Style.EMPTY.applyFormat(TextFormatting.GRAY))), true);
	}

	protected static void success(CommandSource source, String subcommand, String message, ITextComponent... args) {
		source.sendSuccess(getCmdPrefix(subcommand).append(new TranslationTextComponent(message, args).setStyle(Style.EMPTY.applyFormat(TextFormatting.GREEN))), true);
	}

	protected static void warn(CommandSource source, String subcommand, String message, ITextComponent... args) {
		source.sendSuccess(getCmdPrefix(subcommand).append(new TranslationTextComponent(message, args).setStyle(Style.EMPTY.applyFormat(TextFormatting.RED))), true);
	}
}
