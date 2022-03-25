package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.tslat.aoawikihelpermod.render.*;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

public class IsometricCommand implements Command<CommandSource> {
	private static final IsometricCommand CMD = new IsometricCommand();
	public static final SuggestionProvider<CommandSource> ENTITY_ID_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("all_entities"), (context, suggestionBuilder) -> ISuggestionProvider.suggestResource(Registry.ENTITY_TYPE.stream(), suggestionBuilder, EntityType::getKey, (entityType) -> new TranslationTextComponent(Util.makeDescriptionId("entity", EntityType.getKey(entityType)))));

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("iso").requires(source -> source.getEntity() instanceof PlayerEntity).executes(CMD);

		builder.then(Commands.literal("entity")
				.then(Commands.argument("entity_id", ResourceLocationArgument.id())
						.suggests(ENTITY_ID_SUGGESTIONS)
						.executes(context -> printEntityIso(context, 300, false, 0))
						.then(Commands.argument("animated", BoolArgumentType.bool())
								.then(Commands.argument("record_length", IntegerArgumentType.integer(10, 1200))
										.executes(context ->  printEntityIso(context, 300, BoolArgumentType.getBool(context, "animated"), 0))
										.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
												.executes(context -> printEntityIso(context, 300, BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust"))))))
						.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
								.executes(context -> printEntityIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, 0))
								.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
										.executes(context -> printEntityIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, FloatArgumentType.getFloat(context, "rotation_adjust"))))
								.then(Commands.argument("animated", BoolArgumentType.bool())
										.then(Commands.argument("record_length", IntegerArgumentType.integer(10, 1200))
												.executes(context -> printEntityIso(context, IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), 0))
												.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
														.executes(context -> printEntityIso(context, IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust")))))))));
		builder.then(Commands.literal("block")
				.then(Commands.argument("block_id", BlockStateArgument.block())
						.executes(context -> printBlockIso(context, 300, false, 0))
						.then(Commands.argument("animated", BoolArgumentType.bool())
								.executes(context -> printBlockIso(context, 300, BoolArgumentType.getBool(context, "animated"), 0))
								.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
										.executes(context -> printBlockIso(context, 300, BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust")))))
						.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
								.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, 0))
								.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
										.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, FloatArgumentType.getFloat(context, "rotation_adjust"))))
								.then(Commands.argument("animated", BoolArgumentType.bool())
										.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), 0))
										.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
												.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust"))))))
						.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
								.executes(context -> printBlockIso(context, 300, false, FloatArgumentType.getFloat(context, "rotation_adjust"))))));

		builder.then(Commands.literal("structure")
						.then(Commands.argument("template_id", net.tslat.aoawikihelpermod.command.StructuresCommand.TemplateIdArgument.instance())
								.executes(context -> printStructureIso(context, 1000, 0, false))
								.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
										.executes(context -> printStructureIso(context, IntegerArgumentType.getInteger(context, "image_size"), 0, false))
										.then(Commands.argument("rotation", FloatArgumentType.floatArg(0, 360))
												.executes(context -> printStructureIso(context, IntegerArgumentType.getInteger(context, "image_size"), FloatArgumentType.getFloat(context, "rotation"), false))
												.then(Commands.argument("do_full_structure", BoolArgumentType.bool())
														.executes(context -> printStructureIso(context, IntegerArgumentType.getInteger(context, "image_size"), FloatArgumentType.getFloat(context, "rotation"), BoolArgumentType.getBool(context, "do_full_structure")))))
										.then(Commands.argument("do_full_structure", BoolArgumentType.bool())
												.executes(context -> printStructureIso(context, IntegerArgumentType.getInteger(context, "image_size"), 0, BoolArgumentType.getBool(context, "do_full_structure")))))
								.then(Commands.argument("do_full_structure", BoolArgumentType.bool())
										.executes(context -> printStructureIso(context, 1000, 0, BoolArgumentType.getBool(context, "do_full_structure"))))));

		return builder;
	}

	protected String commandName() {
		return "Iso";
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out isometric images of different objects");

		return 1;
	}

	private static int printEntityIso(CommandContext<CommandSource> context, int imageSize, boolean animated, float rotation) throws CommandSyntaxException {
		context.getSource().getPlayerOrException();

		IsometricPrinterScreen.queuePrintTask(() -> {
			if (animated) {
				return new AnimatedEntityIsoPrinter(
						ResourceLocationArgument.getId(context, "entity_id"),
						imageSize,
						IntegerArgumentType.getInteger(context, "record_length"),
						rotation,
						context.getSource(), CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
			else {
				return new EntityIsoPrinter(
						ResourceLocationArgument.getId(context, "entity_id"),
						imageSize,
						rotation,
						context.getSource(), CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
		});

		return 1;
	}

	private static int printBlockIso(CommandContext<CommandSource> context, int imageSize, boolean animated, float rotation) throws CommandSyntaxException {
		context.getSource().getPlayerOrException();

		IsometricPrinterScreen.queuePrintTask(() -> {
			if (animated) {
				return new AnimatedBlockIsoPrinter(
						BlockStateArgument.getBlock(context, "block_id").getState(),
						imageSize,
						rotation,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
			else {
				return new BlockIsoPrinter(
						BlockStateArgument.getBlock(context, "block_id").getState(),
						imageSize,
						rotation,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
		});

		return 1;
	}

	private static int printStructureIso(CommandContext<CommandSource> context, int imageSize, float rotation, boolean doFullStructure) throws CommandSyntaxException {
		context.getSource().getPlayerOrException();

		IsometricPrinterScreen.queuePrintTask(() -> {
			try {
				return new StructureIsoPrinter(
						StructuresCommand.TemplateIdArgument.getTemplateId(context, "template_id"),
						doFullStructure,
						rotation,
						imageSize,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
			catch (CommandSyntaxException ex) {
				ex.printStackTrace();
			}

			return null;
		});

		return 1;
	}
}
