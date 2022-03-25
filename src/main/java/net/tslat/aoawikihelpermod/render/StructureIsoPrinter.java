package net.tslat.aoawikihelpermod.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.JigsawBlock;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.command.CommandSource;
import net.minecraft.tileentity.JigsawTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
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
	protected final ArrayList<Template.BlockInfo> blocks;
	protected Vector3i renderSize;
	protected final float rotation;

	public StructureIsoPrinter(ResourceLocation templateId, boolean expandJigsaw, float rotation, int imageSize, CommandSource commandSource, String commandName, Consumer<File> fileConsumer) {
		super(imageSize, rotation, commandSource, commandName, fileConsumer);

		if (templateId.getPath().startsWith("structures/"))
			templateId = new ResourceLocation(templateId.getNamespace(), templateId.getPath().replaceFirst("structures/", ""));

		this.templateId = templateId;
		this.rotation = rotation;
		this.blocks = new ArrayList<>();

		extractTemplateData(expandJigsaw, templateId);
	}

	private void extractTemplateData(boolean expandJigsaw, ResourceLocation templateId) {
		Template template = StructureTemplateSkimmer.getTemplate(templateId);

		if (template == null) {
			WikiHelperCommand.error(this.commandSource, this.commandName, "Unable to find or load structure: '" + templateId + "'");

			return;
		}

		FakeWorld world = FakeWorld.INSTANCE;
		boolean placeBlocks = true;

		if (expandJigsaw) {
			for (Template.BlockInfo block : template.palettes.get(0).blocks()) {
				if (block.state.getBlock() instanceof JigsawBlock) {
					world.setBlock(block.pos, block.state, 0);

					JigsawTileEntity tile = (JigsawTileEntity)world.getBlockEntity(block.pos);

					if (tile.getPool() != null) {
						generateJigsawPiece(template, tile);
						placeBlocks = false;
					}
				}
			}
		}

		if (placeBlocks) {
			for (Template.BlockInfo block : template.palettes.get(0).blocks()) {
				world.setBlock(block.pos, block.state, 0);
			}
		}

		Pair<Vector3i, List<Template.BlockInfo>> renderInfo = world.getChunkSource().compileBlocksIntoList();
		this.renderSize = renderInfo.getFirst();
		this.blocks.addAll(renderInfo.getSecond());
	}

	@Override
	protected boolean preRenderingCheck() {
		return super.preRenderingCheck() && !this.blocks.isEmpty();
	}

	@Override
	protected void renderObject() {
		MatrixStack matrix = new MatrixStack();

		withAlignedIsometricProjection(matrix, () -> {
			BlockRendererDispatcher blockRenderer = this.minecraft.getBlockRenderer();
			EntityRendererManager renderManager = this.minecraft.getEntityRenderDispatcher();
			IRenderTypeBuffer.Impl renderBuffer = mc.renderBuffers().bufferSource();

			renderManager.setRenderShadow(false);

			try {
				matrix.translate(0, Math.tan(this.renderSize.getX() / (float)this.renderSize.getZ()) * Math.sin(45f) / -Math.cos(35.264f), 0);

				for (Template.BlockInfo block : this.blocks) {
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

	protected void generateJigsawPiece(Template template, JigsawTileEntity tileEntity) {
		Random rand = tileEntity.getLevel().getRandom();
		BlockPos pos = tileEntity.getBlockPos();
		List<AbstractVillagePiece> pieces = Lists.newArrayList();
		TemplateManager templateManager = AoADimensions.OVERWORLD.getWorld().getStructureManager();
		VillageConfig config = new VillageConfig(() -> FakeWorld.INSTANCE.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY).get(tileEntity.getPool()), 15);
		ChunkGenerator chunkGenerator = new FlatChunkGenerator(FlatGenerationSettings.getDefault(FakeWorld.INSTANCE.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)));
		AbstractVillagePiece villagePiece = new AbstractVillagePiece(templateManager, new SingleJigsawPiece(template), new BlockPos(0, 0, 0), 1, Rotation.NONE, new MutableBoundingBox(pos, pos));

		JigsawManager.addPieces(FakeWorld.INSTANCE.registryAccess(), villagePiece, 10, AbstractVillagePiece::new, chunkGenerator, templateManager, pieces, rand);

		pieces.add(villagePiece);

		for (AbstractVillagePiece piece : pieces) {
			piece.place((FakeWorld)tileEntity.getLevel(), FakeWorld.INSTANCE.getStructureManager(), chunkGenerator, rand, MutableBoundingBox.infinite(), pos, false);
		}
	}
}
