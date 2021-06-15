package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoa3.util.misc.MutableSupplier;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.printers.TablePrintHelper;

import java.io.File;
import java.util.HashSet;
import java.util.stream.Stream;

public class StructuresCommand implements Command<CommandSource> {
	private static final StructuresCommand CMD = new StructuresCommand();
	private static final SuggestionProvider<CommandSource> SUGGESTION_PROVIDER = SuggestionProviders.register(new ResourceLocation(AoAWikiHelperMod.MOD_ID, "structure_pieces"), (context, builder) -> ISuggestionProvider.suggestResource(Stream.of(
			new ResourceLocation(AdventOfAscension.MOD_ID, "structures/abyss/abyssal_lotto_hut/abyssal_lotto_hut"),
			new ResourceLocation(AdventOfAscension.MOD_ID, "structures/shyrelands/decorations/ruined_arch"),
			new ResourceLocation("minecraft", "structures/ruined_portal/giant_portal_1"),
			new ResourceLocation("minecraft", "structures/igloo/bottom"),
			new ResourceLocation("minecraft", "structures/bastion/bridge/bridge_pieces/bridge")), builder));

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("structure").executes(CMD);

		builder.then(Commands.argument("structure_piece_id", net.tslat.aoa3.command.StructuresCommand.StructureIdArgument.instance()).suggests(SUGGESTION_PROVIDER).executes(StructuresCommand::printStructurePiece));

		return builder;
	}

	protected String commandName() {
		return "Structure";
	}

	@Override
	public int run(CommandContext<CommandSource> context) {
		WikiHelperCommand.info(context.getSource(), commandName(), "Print out a structure details for specific pieces or template pools.");

		return 1;
	}

	private static int printStructurePiece(CommandContext<CommandSource> cmd) {
		try {
			ResourceLocation id = net.tslat.aoa3.command.StructuresCommand.StructureIdArgument.getStructureId(cmd, "structure_piece_id");
			Template template = ServerLifecycleHooks.getCurrentServer().getStructureManager().get(id);

			if (template == null) {
				WikiHelperCommand.error(cmd.getSource(), "Structures", "Invalid structure piece ID: '" + id + "'");

				return 1;
			}

			String[] lines = new String[4];
			HashSet<Block> blocks = new HashSet<Block>();
			HashSet<ResourceLocation> entities = new HashSet<ResourceLocation>();
			StringBuilder contentsBuilder = new StringBuilder();
			BlockPos size = template.getSize();

			for (Template.Palette palette : template.palettes) {
				for (Template.BlockInfo blockInfo : palette.blocks()) {
					Block block = blockInfo.state.getBlock();

					if (blocks.contains(block) || block == Blocks.AIR || block == Blocks.JIGSAW || block == Blocks.STRUCTURE_BLOCK)
						continue;

					if (contentsBuilder.length() > 0)
						contentsBuilder.append("<br/>");

					String blockName = block.getName().getString();

					contentsBuilder
							.append(FormattingHelper.createImageBlock(blockName))
							.append(" ")
							.append(FormattingHelper.createLinkableText(blockName, false, block.getRegistryName().getNamespace().equals("minecraft"), true));
					blocks.add(block);
				}
			}

			for (Template.EntityInfo entity : template.entityInfoList) {
				if (entity.nbt.contains("id")) {
					ResourceLocation entityId = new ResourceLocation(entity.nbt.getString("id"));

					if (entities.contains(entityId))
						continue;

					if (contentsBuilder.length() > 0)
						contentsBuilder.append("<br/>");

					String entityName = StringUtil.toTitleCase(entityId.getPath());

					contentsBuilder
							.append(FormattingHelper.createImageBlock(entityName))
							.append(" ")
							.append(FormattingHelper.createLinkableText(entityName, false, entityId.getNamespace().equals("minecraft"), true));
					entities.add(entityId);
				}
			}

			lines[0] = "<code>" + id + "</code>";
			lines[1] = "Size: " + size.getX() + "x" + size.getY() + "x" + size.getZ();
			lines[2] = contentsBuilder.toString();
			lines[3] = "";

			File outputFile;
			MutableSupplier<String> clipboardContent = new MutableSupplier<String>(null);
			String fileName = "Structure Piece - ";
			String pathName = id.getPath();

			if (pathName.contains("\\")) {
				pathName = StringUtil.toTitleCase(pathName.replaceAll("\\\\", " - "));
			}
			else if (pathName.contains("/")) {
				pathName = StringUtil.toTitleCase(pathName.replaceAll("/", " - "));
			}

			fileName = fileName + pathName;

			try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "Structure piece", "Description", "Contents", "Image")) {
				printHelper.withProperty("class", "wikitable");
				printHelper.withClipboardOutput(clipboardContent);
				printHelper.entry(lines);

				outputFile = printHelper.getOutputFile();
			}

			WikiHelperCommand.success(cmd.getSource(), "Structures", FormattingHelper.generateResultMessage(outputFile, fileName, clipboardContent.get()));
		}
		catch (Exception ex) {
			WikiHelperCommand.error(cmd.getSource(), "Structures", "Error encountered while printing structure piece details, see log for details");

			ex.printStackTrace();
		}

		return 1;
	}
}
