package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.render.EntityIsoPrinter;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

public class IsometricCommand implements Command<CommandSource> {
	private static final IsometricCommand CMD = new IsometricCommand();
	public static final SuggestionProvider<CommandSource> ENTITY_ID_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("all_entities"), (context, suggestionBuilder) -> ISuggestionProvider.suggestResource(Registry.ENTITY_TYPE.stream(), suggestionBuilder, EntityType::getKey, (entityType) -> new TranslationTextComponent(Util.makeDescriptionId("entity", EntityType.getKey(entityType)))));

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("iso").executes(CMD);

		builder.then(Commands.argument("entity_id", ResourceLocationArgument.id()).suggests(ENTITY_ID_SUGGESTIONS)
						.then(Commands.argument("image_size", IntegerArgumentType.integer())
								.executes(IsometricCommand::printEntityIso)));

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

	private static int printEntityIso(CommandContext<CommandSource> context) {
		EntityIsoPrinter.queuePrintEntity(() -> {
			ResourceLocation entityId = ResourceLocationArgument.getId(context, "entity_id");
			int imageSize = IntegerArgumentType.getInteger(context, "image_size");

			if (!ForgeRegistries.ENTITIES.containsKey(entityId)) {
				WikiHelperCommand.error(context.getSource(), CMD.commandName(), "Invalid entity ID '" + entityId + "'");

				return;
			}

			EntityIsoPrinter.printEntity(entityId, imageSize, file -> {
				if (file == null) {
					WikiHelperCommand.error(context.getSource(), CMD.commandName(), "Error while printing out entity, check the log. '" + entityId + "'");

					return;
				}

				WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null));
			});
		});

		return 1;
	}
}
