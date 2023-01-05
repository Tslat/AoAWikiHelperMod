package net.tslat.aoawikihelpermod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public final class ClientHelper {
	public static ObjectHelper.VariableResponse isRenderTransparent(Block block) {
		ObjectHelper.VariableResponse transparent = null;
		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		RandomSource rand = RandomSource.createNewThreadLocalInstance();

		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			BakedModel model = blockRenderer.getBlockModel(state);

			for (RenderType renderType : model.getRenderTypes(state, rand, ModelData.EMPTY)) {
				transparent = ObjectHelper.VariableResponse.merge(transparent,
						renderType == RenderType.translucent() || renderType == RenderType.translucentNoCrumbling() || renderType == RenderType.translucentMovingBlock());
			}
		}

		return transparent;
	}
}
