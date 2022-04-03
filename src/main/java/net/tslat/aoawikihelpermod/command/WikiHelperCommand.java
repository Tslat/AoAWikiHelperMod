package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;

public class WikiHelperCommand {
	public static void registerSubCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("wikihelper");

		if (!AoAWikiHelperMod.isOutdatedAoA) {
			cmd.then(OverviewCommand.register())
					.then(UsagesCommand.register())
					.then(ObtainingCommand.register())
					.then(RecipeCommand.register())
					.then(LootTableCommand.register())
					.then(HaulingTableCommand.register())
					.then(TradesCommand.register())
					.then(StructuresCommand.register())
					.then(BlocksCommand.register())
					.then(ItemsCommand.register());

			if (FMLEnvironment.dist != Dist.DEDICATED_SERVER)
				cmd.then(IsometricCommand.register());
		}
		else {
			cmd.executes(WikiHelperCommand::outdatedCommand);
			cmd.then(Commands.literal("update_aoa").executes(WikiHelperCommand::outdatedCommand));
		}

		dispatcher.register(cmd);
	}

	private static int outdatedCommand(CommandContext<CommandSourceStack> cmd) {
		cmd.getSource().sendFailure(new TextComponent("AoA is outdated! Update AoA to the latest version to use the Wikihelper mod!"));

		return 1;
	}

	public static TextComponent getCmdPrefix(String subcommand) {
		return new TextComponent(ChatFormatting.DARK_RED + "[AoAWikiHelper|" + ChatFormatting.GOLD + subcommand + ChatFormatting.DARK_RED + "] ");
	}

	public static void error(CommandSourceStack source, String subcommand, String message, Component... args) {
		error(source, subcommand, new TranslatableComponent(message, args));
	}

	public static void error(CommandSourceStack source, String subcommand, MutableComponent message, Component... args) {
		source.sendFailure(getCmdPrefix(subcommand).append(message.withStyle(ChatFormatting.DARK_RED)));
	}

	public static void info(CommandSourceStack source, String subcommand, String message, Component... args) {
		info(source, subcommand, new TranslatableComponent(message, args));
	}

	public static void info(CommandSourceStack source, String subcommand, MutableComponent message, Component... args) {
		source.sendSuccess(getCmdPrefix(subcommand).append(message.withStyle(ChatFormatting.GRAY)), true);
	}

	public static void success(CommandSourceStack source, String subcommand, String message, Component... args) {
		success(source, subcommand, new TranslatableComponent(message, args));
	}

	public static void success(CommandSourceStack source, String subcommand, MutableComponent message) {
		source.sendSuccess(getCmdPrefix(subcommand).append(message.withStyle(ChatFormatting.GREEN)), true);
	}

	public static void warn(CommandSourceStack source, String subcommand, String message, Component... args) {
		warn(source, subcommand, new TranslatableComponent(message, args));
	}

	public static void warn(CommandSourceStack source, String subcommand, MutableComponent message) {
		source.sendSuccess(getCmdPrefix(subcommand).append(message.withStyle(ChatFormatting.RED)), true);
	}
}
