package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.render.typeadapter.IsoRenderAdapter;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

public class BlockIsoPrinter extends IsometricPrinterScreen {
	protected final ArrayList<IsoRenderAdapter<BlockState>> adapters = new ArrayList<>(1);
	protected final BlockState block;

	public BlockIsoPrinter(BlockState block, int imageSize, float rotationAdjust, CommandSource commandSource, String commandName, Consumer<File> fileConsumer) {
		super(imageSize, rotationAdjust, commandSource, commandName, fileConsumer);

		this.block = block;

		FakeWorld.INSTANCE.setBlock(new BlockPos(0, 0, 0), block, 0);
	}

	@Override
	protected boolean preRenderingCheck() {
		if (super.preRenderingCheck()) {
			adapters.addAll(getApplicableAdapters(BlockState.class, this.block));

			return true;
		}

		return false;
	}

	@Override
	protected File getOutputFile() {
		return PrintHelper.configDir.toPath().resolve("Block Renders").resolve(block.getBlock().getRegistryName().getNamespace()).resolve(block.getBlock().getName().getString() + " - " + targetSize + "px.png").toFile();
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
				if (!customRenderBlock(blockRenderer, matrix, renderBuffer))
					RenderUtil.renderStandardisedBlock(blockRenderer, matrix, renderBuffer, this.block, null);
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
	protected void makePreRenderAdjustments(MatrixStack matrix) {
		for (IsoRenderAdapter<BlockState> adapter : this.adapters) {
			adapter.makePreRenderAdjustments(this.block, matrix);
		}
	}

	protected boolean customRenderBlock(BlockRendererDispatcher blockRenderer, MatrixStack matrix, IRenderTypeBuffer renderBuffer) {
		for (IsoRenderAdapter<BlockState> adapter : this.adapters) {
			if (adapter.handleCustomRender(this.block, matrix, renderBuffer))
				return true;
		}

		return false;
	}

	@Override
	public void onClose() {
		super.onClose();

		FakeWorld.INSTANCE.reset();
	}
}
