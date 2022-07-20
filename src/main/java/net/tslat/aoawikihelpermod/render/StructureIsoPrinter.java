package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.dataskimmers.StructureTemplateSkimmer;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

		this.defaultRefScale = 0.25f;
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

		FakeWorld world = FakeWorld.INSTANCE.get();
		boolean placeBlocks = true;

		if (!expandJigsaw) {
			for (StructureTemplate.StructureBlockInfo block : template.palettes.get(0).blocks()) {
				world.setBlock(block.pos, block.state, 0);
			}
		}
		else {

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
		return PrintHelper.configDir.toPath().resolve("Structure Renders").resolve(this.templateId.getNamespace()).resolve(templateId.getPath().replaceAll("/", "-") + " - " + targetSize + "px.png").toFile();
	}

	@Override
	public void onClose() {
		super.onClose();

		FakeWorld.INSTANCE.get().reset();
	}
}
