package net.tslat.aoawikihelpermod.render.typeadapter.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.aoa3.content.block.functional.plant.MultiBlockCrop;
import net.tslat.aoawikihelpermod.render.RenderUtil;
import net.tslat.aoawikihelpermod.render.typeadapter.SimpleRotationRenderAdapter;

public class MultiBlockCropRenderAdapter extends SimpleRotationRenderAdapter<BlockState> {
	public MultiBlockCropRenderAdapter() {
		super(block -> block.getBlock() instanceof MultiBlockCrop, Vector3f.YP, 0f);
	}

	@Override
	public boolean handleCustomRender(BlockState renderingObject, PoseStack matrix, MultiBufferSource buffer) {
		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		MultiBlockCrop crop = (MultiBlockCrop)renderingObject.getBlock();


		for (int i = 0; i < crop.getGrowthHeight(); i++) {
			RenderUtil.renderStandardisedBlock(blockRenderer, matrix, buffer, renderingObject.setValue(crop.getHeightProperty(), i), null);
			matrix.translate(0, 1, 0);
		}

		return true;
	}
}
