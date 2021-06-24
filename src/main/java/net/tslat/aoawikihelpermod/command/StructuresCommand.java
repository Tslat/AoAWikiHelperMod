package net.tslat.aoawikihelpermod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoa3.util.misc.MutableSupplier;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.printers.TablePrintHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

public class StructuresCommand implements Command<CommandSource> {
	private static final StructuresCommand CMD = new StructuresCommand();

	public static ArgumentBuilder<CommandSource, ?> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("structure").executes(CMD);

		builder.then(Commands.argument("structure_piece_id", net.tslat.aoa3.command.StructuresCommand.StructureIdArgument.instance()).executes(StructuresCommand::printStructurePiece));

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
			ResourceLocation id;
			Template template;

			try {
				id = net.tslat.aoa3.command.StructuresCommand.StructureIdArgument.getStructureId(cmd, "structure_piece_id");
				template = ServerLifecycleHooks.getCurrentServer().getStructureManager().get(id);
			}
			catch (Exception ex) {
				template = null;
				id = null;
			}

			if (template == null) {
				WikiHelperCommand.error(cmd.getSource(), "Structures", "Invalid or unable to locate structure piece ID: '" + id + "'");

				return 1;
			}

			String[] lines = new String[4];
			HashSet<Block> blocks = new HashSet<Block>();
			HashSet<ResourceLocation> entities = new HashSet<ResourceLocation>();
			ArrayList<String> entityEntries = new ArrayList<String>();
			StringBuilder contentsBuilder = new StringBuilder();
			BlockPos size = template.getSize();

			for (Template.Palette palette : template.palettes) {
				for (Template.BlockInfo blockInfo : palette.blocks()) {
					Block block = blockInfo.state.getBlock();

					if (block == Blocks.AIR || block == Blocks.JIGSAW || block == Blocks.STRUCTURE_BLOCK)
						continue;

					blocks.add(block);
				}
			}

			for (Block block : blocks.stream().sorted(Comparator.comparing(bl -> bl.getName().getString())).collect(Collectors.toList())) {
				if (contentsBuilder.length() > 0)
					contentsBuilder.append("<br/>");

				String blockName = block.getName().getString();

				contentsBuilder
						.append(FormattingHelper.createImageBlock(blockName))
						.append(" ")
						.append(FormattingHelper.createLinkableText(blockName, false, block.getRegistryName().getNamespace().equals("minecraft"), true));
			}

			for (Template.EntityInfo entity : template.entityInfoList) {
				if (entity.nbt.contains("id")) {
					ResourceLocation entityId = new ResourceLocation(entity.nbt.getString("id"));

					if (entities.add(entityId)) {
						String entityName = StringUtil.toTitleCase(entityId.getPath());

						entityEntries.add(FormattingHelper.createImageBlock(entityName) + " " + FormattingHelper.createLinkableText(entityName, false, entityId.getNamespace().equals("minecraft"), true));
					}
				}
			}

			lines[0] = "<code>" + id + "</code>";
			lines[1] = "Size: " + size.getX() + "x" + size.getY() + "x" + size.getZ();
			lines[2] = contentsBuilder.toString();
			lines[3] = FormattingHelper.createImageBlock("Image", 300);

			if (!entityEntries.isEmpty())
				lines[1] += "<br/><br/>The following entities are generated with this structure:<br/>" + FormattingHelper.listToString(entityEntries.stream().sorted().collect(Collectors.toList()), false);

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

			try (TablePrintHelper printHelper = TablePrintHelper.open(fileName, "scope=\"col\" style=\"width:15%\" | Structure piece", "scope=\"col\" style=\"width:25%\" | Description", "scope=\"col\" style=\"width:30%\" | Contents", "scope=\"col\" style=\"width:30%\" | Image")) {
				printHelper.withProperty("class", "wikitable mw-collapsible mw-collapsed");
				printHelper.withProperty("data-expandtext", "Show");
				printHelper.withProperty("data-collapsetest", "Hide");
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
