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
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoawikihelpermod.render.*;
import net.tslat.aoawikihelpermod.util.FormattingHelper;

import javax.annotation.Nullable;
import java.util.Optional;

public class IsometricCommand implements Command<CommandSourceStack> {
	private static final IsometricCommand CMD = new IsometricCommand();
	public static final SuggestionProvider<CommandSourceStack> ENTITY_ID_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("all_entities"), (context, suggestionBuilder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream(), suggestionBuilder, EntityType::getKey, (entityType) -> Component.translatable(Util.makeDescriptionId("entity", EntityType.getKey(entityType)))));

	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext buildContext) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("iso").requires(source -> source.getEntity() instanceof Player).executes(CMD);

		builder.then(Commands.literal("entity")
				.then(Commands.argument("entity_id", ResourceLocationArgument.id())
						.suggests(ENTITY_ID_SUGGESTIONS)
						.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), null, 300, false, 0))
						.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
								.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), CompoundTagArgument.getCompoundTag(context, "nbt"), 300, false, 0))
								.then(Commands.argument("animated", BoolArgumentType.bool())
										.then(Commands.argument("record_length", IntegerArgumentType.integer(10, 1200))
												.executes(context ->  printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), CompoundTagArgument.getCompoundTag(context, "nbt"), 300, BoolArgumentType.getBool(context, "animated"), 0))
												.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
														.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), CompoundTagArgument.getCompoundTag(context, "nbt"), 300, BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust"))))))
								.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
										.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), CompoundTagArgument.getCompoundTag(context, "nbt"), IntegerArgumentType.getInteger(context, "image_size"), false, 0))
										.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
												.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), CompoundTagArgument.getCompoundTag(context, "nbt"), IntegerArgumentType.getInteger(context, "image_size"), false, FloatArgumentType.getFloat(context, "rotation_adjust"))))
										.then(Commands.argument("animated", BoolArgumentType.bool())
												.then(Commands.argument("record_length", IntegerArgumentType.integer(10, 1200))
														.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), CompoundTagArgument.getCompoundTag(context, "nbt"), IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), 0))
														.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
																.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), CompoundTagArgument.getCompoundTag(context, "nbt"), IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust"))))))))
						.then(Commands.argument("animated", BoolArgumentType.bool())
								.then(Commands.argument("record_length", IntegerArgumentType.integer(10, 1200))
										.executes(context ->  printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), null, 300, BoolArgumentType.getBool(context, "animated"), 0))
										.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
												.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), null, 300, BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust"))))))
						.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
								.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), null, IntegerArgumentType.getInteger(context, "image_size"), false, 0))
								.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
										.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), null, IntegerArgumentType.getInteger(context, "image_size"), false, FloatArgumentType.getFloat(context, "rotation_adjust"))))
								.then(Commands.argument("animated", BoolArgumentType.bool())
										.then(Commands.argument("record_length", IntegerArgumentType.integer(10, 1200))
												.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), null, IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), 0))
												.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
														.executes(context -> printEntityIso(context, ResourceLocationArgument.getId(context, "entity_id"), null, IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust")))))))));
		builder.then(Commands.literal("armour")
				.then(Commands.argument("boots", ItemArgument.item(buildContext))
						.then(Commands.argument("leggings", ItemArgument.item(buildContext))
								.then(Commands.argument("chestplate", ItemArgument.item(buildContext))
										.then(Commands.argument("helmet", ItemArgument.item(buildContext))
												.executes(context -> printEntityIso(context, new ResourceLocation("armor_stand"), armourNbtFromArguments(context), 300, false, 0))
												.then(Commands.argument("animated", BoolArgumentType.bool())
														.then(Commands.argument("record_length", IntegerArgumentType.integer(10, 1200))
																.executes(context -> printEntityIso(context, new ResourceLocation("armor_stand"), armourNbtFromArguments(context), 300, BoolArgumentType.getBool(context, "animated"), 0))
																.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
																		.executes(context -> printEntityIso(context, new ResourceLocation("armor_stand"), armourNbtFromArguments(context), 300, BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust"))))))
												.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
														.executes(context -> printEntityIso(context, new ResourceLocation("armor_stand"), armourNbtFromArguments(context), IntegerArgumentType.getInteger(context, "image_size"), false, 0))
														.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
																.executes(context -> printEntityIso(context, new ResourceLocation("armor_stand"), armourNbtFromArguments(context), IntegerArgumentType.getInteger(context, "image_size"), false, FloatArgumentType.getFloat(context, "rotation_adjust"))))
														.then(Commands.argument("animated", BoolArgumentType.bool())
																.then(Commands.argument("record_length", IntegerArgumentType.integer(10, 1200))
																		.executes(context -> printEntityIso(context, new ResourceLocation("armor_stand"), armourNbtFromArguments(context), IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), 0))
																		.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
																				.executes(context -> printEntityIso(context, new ResourceLocation("armor_stand"), armourNbtFromArguments(context), IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), FloatArgumentType.getFloat(context, "rotation_adjust"))))))))))));
		builder.then(Commands.literal("block")
				.then(Commands.argument("block_id", BlockStateArgument.block(buildContext))
						.executes(context -> printBlockIso(context, 300, false, false, 0, 0))
						.then(Commands.literal("states")
								.executes(context -> printBlockIso(context, 300, false, true, 0, 20))
								.then(Commands.argument("frametime", IntegerArgumentType.integer(1, 200))
										.executes(context -> printBlockIso(context, 300, false, true, 0, IntegerArgumentType.getInteger(context, "frametime")))
										.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
												.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, true, 0, IntegerArgumentType.getInteger(context, "frametime")))
												.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
														.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, true, FloatArgumentType.getFloat(context, "rotation_adjust"), IntegerArgumentType.getInteger(context, "frametime"))))
												.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
														.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, true, FloatArgumentType.getFloat(context, "rotation_adjust"), IntegerArgumentType.getInteger(context, "frametime")))))
								.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
										.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, true, 0, 20))
										.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
												.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, true, FloatArgumentType.getFloat(context, "rotation_adjust"), 20)))
										.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
												.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, true, FloatArgumentType.getFloat(context, "rotation_adjust"), 20)))))
						.then(Commands.argument("animated", BoolArgumentType.bool())
								.executes(context -> printBlockIso(context, 300, BoolArgumentType.getBool(context, "animated"), false, 0, 0))
								.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
										.executes(context -> printBlockIso(context, 300, BoolArgumentType.getBool(context, "animated"), false, FloatArgumentType.getFloat(context, "rotation_adjust"), 0))))
						.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
								.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, false, 0, 0))
								.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
										.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), false, false, FloatArgumentType.getFloat(context, "rotation_adjust"), 0)))
								.then(Commands.argument("animated", BoolArgumentType.bool())
										.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), false, 0, 0))
										.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
												.executes(context -> printBlockIso(context, IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "animated"), false, FloatArgumentType.getFloat(context, "rotation_adjust"), 0)))))
						.then(Commands.argument("rotation_adjust", FloatArgumentType.floatArg(0, 360))
								.executes(context -> printBlockIso(context, 300, false, false, FloatArgumentType.getFloat(context, "rotation_adjust"), 0))))));

		builder.then(Commands.literal("structure")
						.then(Commands.argument("template_id", ResourceLocationArgument.id()).suggests(StructuresCommand.SUGGEST_TEMPLATES)
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

		builder.then(Commands.literal("item")
						.then(Commands.argument("hand", EnumArgument.enumArgument(InteractionHand.class))
								.requires(CommandSourceStack::isPlayer)
								.executes(context -> printItemIso(context, context.getSource().getPlayer().getItemInHand(context.getArgument("hand", InteractionHand.class)), -1, false, false))
								.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
										.executes(context -> printItemIso(context, context.getSource().getPlayer().getItemInHand(context.getArgument("hand", InteractionHand.class)), IntegerArgumentType.getInteger(context, "image_size"), false, false))
										.then(Commands.argument("render_ingame_model", BoolArgumentType.bool())
												.executes(context -> printItemIso(context, context.getSource().getPlayer().getItemInHand(context.getArgument("hand", InteractionHand.class)), IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "render_ingame_model"), false))
												.then(Commands.argument("animated", BoolArgumentType.bool())
														.executes(context -> printItemIso(context, context.getSource().getPlayer().getItemInHand(context.getArgument("hand", InteractionHand.class)), IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "render_ingame_model"), BoolArgumentType.getBool(context, "animated"))))))
								.then(Commands.argument("render_ingame_model", BoolArgumentType.bool())
										.executes(context -> printItemIso(context, context.getSource().getPlayer().getItemInHand(context.getArgument("hand", InteractionHand.class)), -1, BoolArgumentType.getBool(context, "render_ingame_model"), false))
										.then(Commands.argument("animated", BoolArgumentType.bool())
												.executes(context -> printItemIso(context, context.getSource().getPlayer().getItemInHand(context.getArgument("hand", InteractionHand.class)), -1, BoolArgumentType.getBool(context, "render_ingame_model"), BoolArgumentType.getBool(context, "animated"))))))
						.then(Commands.argument("stack", ItemArgument.item(buildContext))
								.executes(context -> printItemIso(context, new ItemStack(ItemArgument.getItem(context, "stack").getItem()), -1, false, false))
								.then(Commands.argument("image_size", IntegerArgumentType.integer(0, 1000))
										.executes(context -> printItemIso(context, new ItemStack(ItemArgument.getItem(context, "stack").getItem()), IntegerArgumentType.getInteger(context, "image_size"), false, false))
										.then(Commands.argument("render_ingame_model", BoolArgumentType.bool())
												.executes(context -> printItemIso(context, new ItemStack(ItemArgument.getItem(context, "stack").getItem()), IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "render_ingame_model"), false))
												.then(Commands.argument("animated", BoolArgumentType.bool())
														.executes(context -> printItemIso(context, new ItemStack(ItemArgument.getItem(context, "stack").getItem()), IntegerArgumentType.getInteger(context, "image_size"), BoolArgumentType.getBool(context, "render_ingame_model"), BoolArgumentType.getBool(context, "animated"))))))
								.then(Commands.argument("render_ingame_model", BoolArgumentType.bool())
										.executes(context -> printItemIso(context, new ItemStack(ItemArgument.getItem(context, "stack").getItem()), -1, BoolArgumentType.getBool(context, "render_ingame_model"), false))
										.then(Commands.argument("animated", BoolArgumentType.bool())
												.executes(context -> printItemIso(context, new ItemStack(ItemArgument.getItem(context, "stack").getItem()), -1, BoolArgumentType.getBool(context, "render_ingame_model"), BoolArgumentType.getBool(context, "animated")))))));

		return builder;
	}

	protected String commandName() {
		return "Iso";
	}

	@Override
	public int run(CommandContext<CommandSourceStack> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out isometric images of different objects");

		return 1;
	}

	private static int printItemIso(CommandContext<CommandSourceStack> context, ItemStack stack, int imageSize, boolean renderIngameModel, boolean animated) throws CommandSyntaxException {
		context.getSource().getPlayerOrException();

		IsometricPrinterScreen.queuePrintTask(() -> {
			if (animated) {
				return new AnimatedItemIsoPrinter(
						stack,
						imageSize,
						renderIngameModel,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
			else {
				return new ItemIsoPrinter(
						stack,
						imageSize,
						renderIngameModel,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
		});

		return 1;
	}

	private static int printEntityIso(CommandContext<CommandSourceStack> context, ResourceLocation entityId, @Nullable CompoundTag nbt, int imageSize, boolean animated, float rotation) throws CommandSyntaxException {
		context.getSource().getPlayerOrException();

		IsometricPrinterScreen.queuePrintTask(() -> {
			if (animated) {
				return new AnimatedEntityIsoPrinter(
						entityId,
						nbt,
						imageSize,
						IntegerArgumentType.getInteger(context, "record_length"),
						rotation,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
			else {
				return new EntityIsoPrinter(
						entityId,
						nbt,
						imageSize,
						rotation,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
		});

		return 1;
	}

	private static int printBlockIso(CommandContext<CommandSourceStack> context, int imageSize, boolean animated, boolean stateScroller, float rotation, int frameTime) throws CommandSyntaxException {
		context.getSource().getPlayerOrException();

		final BlockInput blockInput = BlockStateArgument.getBlock(context, "block_id");
		Optional<Property<?>> property = null;

		if (stateScroller) {
			property = blockInput.getDefinedProperties().stream().findFirst();

			if (property.isEmpty()) {
				WikiHelperCommand.error(context.getSource(), "Iso", "No property defined to display for state scroller!");

				return 0;
			}
		}

		final Optional<Property<?>> finalProperty = property;

		IsometricPrinterScreen.queuePrintTask(() -> {
			if (stateScroller) {
				return new AnimatedStateBasedBlockIsoPrinter(
						blockInput.getState(),
						finalProperty.get(),
						frameTime,
						imageSize,
						rotation,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
			else if (animated) {
				return new AnimatedBlockIsoPrinter(
						blockInput.getState(),
						imageSize,
						rotation,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
			else {
				return new BlockIsoPrinter(
						blockInput.getState(),
						imageSize,
						rotation,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null)));
			}
		});

		return 1;
	}

	private static int printStructureIso(CommandContext<CommandSourceStack> context, int imageSize, float rotation, boolean doFullStructure) throws CommandSyntaxException {
		context.getSource().getPlayerOrException();

		ResourceLocation templateId;

		try {
			templateId = StructuresCommand.TemplateIdArgument.getTemplateId(context, "template_id");
		}
		catch (Exception ex) {
			WikiHelperCommand.error(context.getSource(), "Iso", ex.getMessage());

			return 1;
		}

		IsometricPrinterScreen.queuePrintTask(() ->
				new StructureIsoPrinter(
						templateId,
						doFullStructure,
						rotation,
						imageSize,
						context.getSource(),
						CMD.commandName(),
						file -> WikiHelperCommand.success(context.getSource(), "Iso", FormattingHelper.generateResultMessage(file, file.getName(), null))));

		return 1;
	}

	private static CompoundTag armourNbtFromArguments(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		final CompoundTag armourTag = new CompoundTag();
		final ListTag armorItems = new ListTag();
		final ItemStack boots = ItemArgument.getItem(context, "boots").createItemStack(1, false);
		final ItemStack leggings = ItemArgument.getItem(context, "leggings").createItemStack(1, false);
		final ItemStack chestplate = ItemArgument.getItem(context, "chestplate").createItemStack(1, false);
		final ItemStack helmet = ItemArgument.getItem(context, "helmet").createItemStack(1, false);

		for (ItemStack stack : new ItemStack[] {boots, leggings, chestplate, helmet}) {
			final CompoundTag piece = new CompoundTag();

			piece.putString("id", RegistryUtil.getId(stack.getItem()).toString());
			piece.putInt("Count", 1);

			armorItems.add(piece);
		}

		armourTag.put("ArmorItems", armorItems);
		armourTag.put("Invisible", ByteTag.valueOf(true));

		return armourTag;
	}
}
