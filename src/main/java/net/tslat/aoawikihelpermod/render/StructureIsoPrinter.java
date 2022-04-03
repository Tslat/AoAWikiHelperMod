package net.tslat.aoawikihelpermod.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.tslat.aoa3.common.registration.AoADimensions;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.dataskimmers.StructureTemplateSkimmer;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class StructureIsoPrinter extends IsometricPrinterScreen {
	protected final ResourceLocation templateId;
	protected final ArrayList<StructureTemplate.StructureBlockInfo> blocks;
	protected Vec3i renderSize;
	protected final float rotation;

	public StructureIsoPrinter(ResourceLocation templateId, boolean expandJigsaw, float rotation, int imageSize, CommandSourceStack commandSource, String commandName, Consumer<File> fileConsumer) {
		super(imageSize, rotation, commandSource, commandName, fileConsumer);

		if (templateId.getPath().startsWith("structures/"))
			templateId = new ResourceLocation(templateId.getNamespace(), templateId.getPath().replaceFirst("structures/", ""));

		this.templateId = templateId;
		this.rotation = rotation;
		this.blocks = new ArrayList<>();

		extractTemplateData(expandJigsaw, templateId);
	}

	private void extractTemplateData(boolean expandJigsaw, ResourceLocation templateId) {
		StructureTemplate template = StructureTemplateSkimmer.getTemplate(templateId);

		if (template == null) {
			WikiHelperCommand.error(this.commandSource, this.commandName, "Unable to find or load structure: '" + templateId + "'");

			return;
		}

		FakeWorld world = FakeWorld.INSTANCE;
		boolean placeBlocks = true;

		if (expandJigsaw) {
			for (StructureTemplate.StructureBlockInfo block : template.palettes.get(0).blocks()) {
				if (block.state.getBlock() instanceof JigsawBlock) {
					world.setBlock(block.pos, block.state, 0);

					JigsawBlockEntity tile = (JigsawBlockEntity)world.getBlockEntity(block.pos);

					if (tile.getPool() != null) {
						generateJigsawPiece(template, tile);
						placeBlocks = false;
					}
				}
			}
		}

		if (placeBlocks) {
			for (StructureTemplate.StructureBlockInfo block : template.palettes.get(0).blocks()) {
				world.setBlock(block.pos, block.state, 0);
			}
		}

		Pair<Vec3i, List<StructureTemplate.StructureBlockInfo>> renderInfo = world.getChunkSource().compileBlocksIntoList();
		this.renderSize = renderInfo.getFirst();
		this.blocks.addAll(renderInfo.getSecond());
	}

	@Override
	protected boolean preRenderingCheck() {
		return super.preRenderingCheck() && !this.blocks.isEmpty();
	}

	@Override
	protected void renderObject() {
		PoseStack matrix = new PoseStack();

		withAlignedIsometricProjection(matrix, () -> {
			BlockRenderDispatcher blockRenderer = this.minecraft.getBlockRenderer();
			EntityRenderDispatcher renderManager = this.minecraft.getEntityRenderDispatcher();
			MultiBufferSource.BufferSource renderBuffer = mc.renderBuffers().bufferSource();

			renderManager.setRenderShadow(false);

			try {
				matrix.translate(0, Math.tan(this.renderSize.getX() / (float)this.renderSize.getZ()) * Math.sin(45f) / -Math.cos(35.264f), 0);

				for (StructureTemplate.StructureBlockInfo block : this.blocks) {
					matrix.pushPose();
					matrix.translate(block.pos.getX() - this.renderSize.getX() / 2f, block.pos.getY() - this.renderSize.getY() / 2f, block.pos.getZ() - this.renderSize.getZ() / 2f);
					RenderUtil.renderStandardisedBlock(blockRenderer, matrix, renderBuffer, block.state, block.pos);
					matrix.popPose();
				}
			}
			catch (Exception ex) {
				WikiHelperCommand.error(this.commandSource, this.commandName, "Encountered an error while rendering the block. Likely a non-standard block of some sort. Check the log for more details.");
				ex.printStackTrace();
			}

			renderBuffer.endBatch();
			renderManager.setRenderShadow(true);
		});
	}

	@Override
	protected File getOutputFile() {
		return PrintHelper.configDir.toPath().resolve("Structure Renders").resolve(this.templateId.getNamespace()).resolve("test" + " - " + targetSize + "px.png").toFile();
	}

	@Override
	public void onClose() {
		super.onClose();

		FakeWorld.INSTANCE.reset();
	}

	protected void generateJigsawPiece(StructureTemplate template, JigsawBlockEntity tileEntity) {
		Random rand = tileEntity.getLevel().getRandom();
		BlockPos pos = tileEntity.getBlockPos();
		List<PoolElementStructurePiece> pieces = Lists.newArrayList();
		StructureManager templateManager = AoADimensions.OVERWORLD.getWorld().getStructureManager();
		Registry<StructureSet> structureSetRegistry = FakeWorld.INSTANCE.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
		ChunkGenerator chunkGenerator = new FlatLevelSource(structureSetRegistry, FlatLevelGeneratorSettings.getDefault(FakeWorld.INSTANCE.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), structureSetRegistry));
		PoolElementStructurePiece villagePiece = new PoolElementStructurePiece(templateManager, new SinglePoolElement(template), new BlockPos(0, 0, 0), 1, Rotation.NONE, new BoundingBox(pos));

		JigsawPlacement.addPieces(FakeWorld.INSTANCE.registryAccess(), villagePiece, 10, PoolElementStructurePiece::new, chunkGenerator, templateManager, pieces, rand, tileEntity.getLevel());

		pieces.add(villagePiece);

		for (PoolElementStructurePiece piece : pieces) {
			piece.place((FakeWorld)tileEntity.getLevel(), FakeWorld.INSTANCE.getStructureManager(), chunkGenerator, rand, BoundingBox.infinite(), pos, false);
		}
	}
}
