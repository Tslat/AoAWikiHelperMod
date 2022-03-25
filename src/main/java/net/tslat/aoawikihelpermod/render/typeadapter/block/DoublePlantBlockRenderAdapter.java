package net.tslat.aoawikihelpermod.render.typeadapter.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.vector.Vector3f;
import net.tslat.aoawikihelpermod.render.RenderUtil;
import net.tslat.aoawikihelpermod.render.typeadapter.SimpleRotationRenderAdapter;

public class DoublePlantBlockRenderAdapter extends SimpleRotationRenderAdapter<BlockState> {
	public DoublePlantBlockRenderAdapter() {
		super(block -> block.getBlock() instanceof DoublePlantBlock, Vector3f.YP, -45f);
	}

	@Override
	public boolean handleCustomRender(BlockState renderingObject, MatrixStack matrix, IRenderTypeBuffer buffer) {
		BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

		RenderUtil.renderStandardisedBlock(blockRenderer, matrix, buffer, renderingObject, null);
		matrix.translate(0, 1, 0);
		RenderUtil.renderStandardisedBlock(blockRenderer, matrix, buffer, renderingObject.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), null);

		return true;
	}
}
