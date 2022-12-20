package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;

public class WikiHelperCommand {
	public static void registerSubCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("wikihelper");

		if (!AoAWikiHelperMod.isOutdatedAoA) {
			cmd.then(OverviewCommand.register())
					.then(UsagesCommand.register(context))
					.then(ObtainingCommand.register(context))
					.then(RecipeCommand.register())
					.then(LootTableCommand.register())
					.then(HaulingTableCommand.register())
					.then(TradesCommand.register())
					.then(StructuresCommand.register())
					.then(BlocksCommand.register())
					.then(ItemsCommand.register(context))
					.then(TagsCommand.register())
					.then(InfoboxCommand.register());

			if (FMLEnvironment.dist != Dist.DEDICATED_SERVER)
				cmd.then(IsometricCommand.register(context));
		}
		else {
			cmd.executes(WikiHelperCommand::outdatedCommand);
			cmd.then(Commands.literal("update_aoa").executes(WikiHelperCommand::outdatedCommand));
		}

		dispatcher.register(cmd);
	}

	private static int outdatedCommand(CommandContext<CommandSourceStack> cmd) {
		cmd.getSource().sendFailure(Component.literal("AoA is outdated! Update AoA to the latest version to use the Wikihelper mod!"));

		return 1;
	}

	public static MutableComponent getCmdPrefix(String subcommand) {
		return Component.literal(ChatFormatting.DARK_RED + "[AoAWikiHelper|" + ChatFormatting.GOLD + subcommand + ChatFormatting.DARK_RED + "] ");
	}

	public static void error(CommandSourceStack source, String subcommand, String message) {
		error(source, subcommand, Component.literal(message));
	}

	public static void error(CommandSourceStack source, String subcommand, MutableComponent message) {
		source.sendFailure(getCmdPrefix(subcommand).append(message.withStyle(ChatFormatting.DARK_RED)));
	}

	public static void info(CommandSourceStack source, String subcommand, String message) {
		info(source, subcommand, Component.literal(message));
	}

	public static void info(CommandSourceStack source, String subcommand, MutableComponent message) {
		source.sendSuccess(getCmdPrefix(subcommand).append(message.withStyle(ChatFormatting.GRAY)), true);
	}

	public static void success(CommandSourceStack source, String subcommand, String message) {
		success(source, subcommand, Component.literal(message));
	}

	public static void success(CommandSourceStack source, String subcommand, MutableComponent message) {
		source.sendSuccess(getCmdPrefix(subcommand).append(message.withStyle(ChatFormatting.GREEN)), true);
	}

	public static void warn(CommandSourceStack source, String subcommand, String message) {
		warn(source, subcommand, Component.literal(message));
	}

	public static void warn(CommandSourceStack source, String subcommand, MutableComponent message) {
		source.sendSuccess(getCmdPrefix(subcommand).append(message.withStyle(ChatFormatting.RED)), true);
	}
}
