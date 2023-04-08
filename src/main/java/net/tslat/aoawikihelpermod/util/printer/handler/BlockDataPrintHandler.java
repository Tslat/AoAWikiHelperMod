package net.tslat.aoawikihelpermod.util.printer.handler;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.tslat.aoa3.content.block.functional.light.LampBlock;
import net.tslat.aoa3.util.TagUtil;
import net.tslat.aoawikihelpermod.util.FormattingHelper;
import net.tslat.aoawikihelpermod.util.ObjectHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockDataPrintHandler {
	private static final Map<Property<?>, String> PROPERTY_DESCRIPTIONS = compilePropertyDescriptions();

	private final Block block;

	private String[][] statePrintout = null;
	private String[] tagsPrintout = null;
	private Pair<String, String> logStripPrintout = null;

	private Level level = null;

	public BlockDataPrintHandler(Block block) {
		this.block = block;
	}

	public BlockDataPrintHandler withLevel(Level level) {
		this.level = level;

		return this;
	}

	public Level getLevel() {
		if (this.level == null)
			throw new IllegalStateException("No level provided for BlockDataPrintHandler!");

		return this.level;
	}

	public String[][] getStatePrintout() {
		if (statePrintout != null)
			return statePrintout;

		Collection<Property<?>> properties = block.getStateDefinition().getProperties();
		this.statePrintout = new String[properties.size()][3];
		int i = 0;

		for (Property<?> property : properties) {
			this.statePrintout[i][0] = property.getName();
			this.statePrintout[i][1] = FormattingHelper.listToString(property.getPossibleValues().stream().map(Object::toString).collect(Collectors.toList()), false);
			this.statePrintout[i][2] = PROPERTY_DESCRIPTIONS.getOrDefault(property, "");

			i++;
		}

		return this.statePrintout;
	}

	public String[] getTagsPrintout() {
		if (tagsPrintout != null)
			return tagsPrintout;

		List<ResourceLocation> blockTags = getBlockTags(getLevel());
		List<ResourceLocation> itemTags = getItemTags(getLevel());
		List<ResourceLocation> fluidTags = getFluidTags(getLevel());

		if (blockTags.isEmpty() && itemTags.isEmpty() && fluidTags.isEmpty()) {
			this.tagsPrintout = new String[] {ObjectHelper.getBlockName(block) + " has no tags."};
		}
		else {
			StringBuilder builder = new StringBuilder();

			if (!blockTags.isEmpty()) {
				builder.append("=== Block tags: ====<br/>");
				builder.append(FormattingHelper.listToString(blockTags.stream().map((id) -> "* " + id.toString()).collect(Collectors.toList()), false));
				builder.append("<br/><br/>");
			}

			if (!itemTags.isEmpty()) {
				builder.append("=== Item tags: ===<br/>");
				builder.append(FormattingHelper.listToString(itemTags.stream().map((id) -> "* " + id.toString()).collect(Collectors.toList()), false));
				builder.append("<br/><br/>");
			}

			if (!fluidTags.isEmpty()) {
				builder.append("=== Fluid tags: ===<br/>");
				builder.append(FormattingHelper.listToString(fluidTags.stream().map((id) -> "* " + id.toString()).collect(Collectors.toList()), false));
			}

			String[] lines = builder.toString().split("<br/>");

			this.tagsPrintout = new String[lines.length + 1];
			this.tagsPrintout[0] = ObjectHelper.getBlockName(block) + " has the following tags:<br/>";

			System.arraycopy(lines, 0, this.tagsPrintout, 1, lines.length);
		}

		return this.tagsPrintout;
	}

	@Nullable
	public String getStrippableBlockDescription() {
		if (logStripPrintout != null)
			return logStripPrintout.getFirst();

		prepLogStripDescription();

		return logStripPrintout.getFirst();
	}

	@Nullable
	public String getStrippedBlockDescription() {
		if (logStripPrintout != null)
			return logStripPrintout.getSecond();

		prepLogStripDescription();

		return logStripPrintout.getSecond();
	}

	public void prepLogStripDescription() {
		ArrayList<Block> strippableBlocks = new ArrayList<Block>(1);
		Block stripsTo = AxeItem.STRIPPABLES.get(this.block);

		for (Map.Entry<Block, Block> entry : AxeItem.STRIPPABLES.entrySet()) {
			if (entry.getValue() == this.block)
				strippableBlocks.add(entry.getKey());
		}

		String strippableDescription = null;
		String strippedDescription = null;

		if (!strippableBlocks.isEmpty()) {
			StringBuilder builder = new StringBuilder();

			if (strippableBlocks.size() == 1) {
				builder.append(block);
				builder.append(" can be made by stripping a ");
				builder.append(FormattingHelper.createLinkableText(strippableBlocks.get(0).getName().getString(), false, true));
				builder.append(" by using an axe on it");
			}
			else {
				builder.append(block);
				builder.append(" can be made by stripping any of the following blocks by using an axe on them:\n");

				for (Block strippableBlock : strippableBlocks) {
					builder.append("* ");
					builder.append(FormattingHelper.createLinkableText(strippableBlock.getName().getString(), false, true));
				}
			}

			strippableDescription = builder.toString();
		}

		if (stripsTo != null) {
			StringBuilder builder = new StringBuilder();

			builder.append(FormattingHelper.lazyPluralise(block.getName().getString()));
			builder.append(" can be stripped into a ");
			builder.append(FormattingHelper.createLinkableText(stripsTo.getName().getString(), false, true));
			builder.append(" by using an axe on it.");

			strippedDescription = builder.toString();
		}

		this.logStripPrintout = Pair.of(strippableDescription, strippedDescription);
	}

	public boolean hasHasStateProperties() {
		return !block.defaultBlockState().getProperties().isEmpty();
	}

	private List<ResourceLocation> getBlockTags(Level level) {
		return TagUtil.getAllTagsFor(Registries.BLOCK, this.block, level).map(TagKey::location).toList();
	}

	private List<ResourceLocation> getItemTags(Level level) {
		if (this.block.asItem() == Items.AIR)
			return List.of();

		return TagUtil.getAllTagsFor(Registries.ITEM, this.block.asItem(), level).map(TagKey::location).toList();
	}

	private List<ResourceLocation> getFluidTags(Level level) {
		FluidState fluidState = block.getFluidState(block.defaultBlockState());

		if (fluidState.isEmpty())
			return List.of();

		return TagUtil.getAllTagsFor(Registries.FLUID, fluidState.getType(), level).map(TagKey::location).toList();
	}

	private static Map<Property<?>, String> compilePropertyDescriptions() {
		Map<Property<?>, String> map = new Object2ObjectArrayMap<>();

		map.put(LampBlock.LIT, "Determines whether the block is lit or not.");
		map.put(LampBlock.TOGGLEABLE, "Determines whether the lamp block is able to be lit or turned off by block updates or redstone.");
		map.put(BlockStateProperties.STAIRS_SHAPE, "The visible shape of the stairs.");
		map.put(BlockStateProperties.HALF, "Which vertical half of the block it currently is.");
		map.put(BlockStateProperties.HORIZONTAL_FACING, "Which direction the block is facing in all cardinal directions.");
		map.put(BlockStateProperties.FACING, "Which direction the block is facing in all directions.");
		map.put(BlockStateProperties.WATERLOGGED, "Whether the block is currently waterlogged or not.");
		map.put(BlockStateProperties.STAGE, "Determines the stage of the block from 0-1");
		map.put(BlockStateProperties.LEVEL, "Determines the level of a fluid from 0-15");
		map.put(BlockStateProperties.SNOWY, "Whether the block is a snowy variant of itself");
		map.put(BlockStateProperties.AXIS, "Which axis the block is currently rotated on.");
		map.put(BlockStateProperties.SLAB_TYPE, "Whether the slab is a half-slab or double-slab variant.");
		map.put(BlockStateProperties.POWER, "Determines the amount of redstone power the block is giving or receiving.");
		map.put(BlockStateProperties.POWERED, "Whether the block is powered by redstone or not.");
		map.put(BlockStateProperties.NORTH, "Whether the block is connected to the next block in this direction");
		map.put(BlockStateProperties.SOUTH, map.get(BlockStateProperties.NORTH));
		map.put(BlockStateProperties.EAST, map.get(BlockStateProperties.NORTH));
		map.put(BlockStateProperties.WEST, map.get(BlockStateProperties.NORTH));

		return map;
	}
}
