package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.render.typeadapter.IsoRenderAdapter;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printer.PrintHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

public class BlockIsoPrinter extends IsometricPrinterScreen {
	protected final ArrayList<IsoRenderAdapter<BlockState>> adapters = new ArrayList<>(1);
	protected final BlockState block;

	public BlockIsoPrinter(BlockState block, int imageSize, float rotationAdjust, CommandSourceStack commandSource, String commandName, Consumer<File> fileConsumer) {
		super(imageSize, rotationAdjust, commandSource, commandName, fileConsumer);

		this.block = block;

		FakeWorld.INSTANCE.get().setBlock(new BlockPos(0, 0, 0), block, 0);
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
		return PrintHelper.configDir.toPath().resolve("Block Renders").resolve(ForgeRegistries.BLOCKS.getKey(block.getBlock()).getNamespace()).resolve(block.getBlock().getName().getString() + " - " + targetSize + "px.png").toFile();
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
	protected void makePreRenderAdjustments(PoseStack matrix) {
		for (IsoRenderAdapter<BlockState> adapter : this.adapters) {
			adapter.makePreRenderAdjustments(this.block, matrix);
		}
	}

	protected boolean customRenderBlock(BlockRenderDispatcher blockRenderer, PoseStack matrix, MultiBufferSource renderBuffer) {
		for (IsoRenderAdapter<BlockState> adapter : this.adapters) {
			if (adapter.handleCustomRender(this.block, matrix, renderBuffer))
				return true;
		}

		return false;
	}

	@Override
	public void onClose() {
		super.onClose();

		FakeWorld.INSTANCE.get().reset();
	}
}
