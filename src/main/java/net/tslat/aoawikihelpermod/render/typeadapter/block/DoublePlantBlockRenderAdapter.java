package net.tslat.aoawikihelpermod.render.typeadapter.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.tslat.aoawikihelpermod.render.RenderUtil;
import net.tslat.aoawikihelpermod.render.typeadapter.SimpleRotationRenderAdapter;

public class DoublePlantBlockRenderAdapter extends SimpleRotationRenderAdapter<BlockState> {
	public DoublePlantBlockRenderAdapter() {
		super(block -> block.getBlock() instanceof DoublePlantBlock, Axis.YP, -45f);
	}

	@Override
	public boolean handleCustomRender(BlockState renderingObject, PoseStack matrix, MultiBufferSource buffer) {
		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

		RenderUtil.renderStandardisedBlock(blockRenderer, matrix, buffer, renderingObject, null);
		matrix.translate(0, 1, 0);
		RenderUtil.renderStandardisedBlock(blockRenderer, matrix, buffer, renderingObject.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), null);

		return true;
	}
}
