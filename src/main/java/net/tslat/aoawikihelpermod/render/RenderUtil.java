package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.FluidModel;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public final class RenderUtil {
	public static void clearRenderBuffer() {
		RenderSystem.clearColor(0, 0, 0, 0);
		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
	}

	public static IRenderTypeBuffer makeShadeFreeBuffer(IRenderTypeBuffer baseBuffer) {
		return renderType -> baseBuffer.getBuffer(new RenderType(AoAWikiHelperMod.MOD_ID + ":iso", renderType.format(), renderType.mode(), renderType.bufferSize(), renderType.affectsCrumbling(), false, () -> {
			renderType.setupRenderState();
			RenderSystem.disableLighting();
		}, () -> {
			RenderSystem.enableLighting();
			renderType.clearRenderState();
		}){});
	}

	public static void translateToIsometricView(MatrixStack matrix) {
		matrix.scale(1, 1, -1);
		matrix.translate(0, 0, 1000);
		matrix.mulPose(Vector3f.XP.rotationDegrees(215.264f));
		matrix.mulPose(Vector3f.YP.rotationDegrees(-45));
	}

	public static void renderStandardisedBlock(BlockRendererDispatcher blockRenderer, MatrixStack matrix, IRenderTypeBuffer renderBuffer, BlockState block, @Nullable BlockPos pos) {
		BlockRenderType blockRenderType = block.getRenderShape();

		if (blockRenderType == BlockRenderType.INVISIBLE) {
			if (block.getBlock() instanceof FlowingFluidBlock)
				StaticFluidRenderer.renderFluid(matrix.last(), pos, FakeWorld.INSTANCE, renderBuffer.getBuffer(RenderType.translucent()), block);

			return;
		}

		if (pos == null)
			pos = new BlockPos(0, 0, 0);

		switch(blockRenderType) {
			case MODEL:
				int tint = Minecraft.getInstance().getBlockColors().getColor(block, FakeWorld.INSTANCE, pos, 0);
				float red = (float)(tint >> 16 & 255) / 255f;
				float green = (float)(tint >> 8 & 255) / 255f;
				float blue = (float)(tint & 255) / 255f;
				IBakedModel blockModel = blockRenderer.getBlockModel(block);

				blockRenderer.getModelRenderer().renderModel(matrix.last(), (blockModel.useAmbientOcclusion() ? renderBuffer : RenderUtil.makeShadeFreeBuffer(renderBuffer)).getBuffer(RenderTypeLookup.getRenderType(block, true)), block, blockModel, red, green, blue, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
				break;
			case ENTITYBLOCK_ANIMATED:
				ItemStack stack = new ItemStack(block.getBlock());

				stack.getItem().getItemStackTileEntityRenderer().renderByItem(stack, ItemCameraTransforms.TransformType.NONE, matrix, renderBuffer, 15728880, OverlayTexture.NO_OVERLAY);
				break;
		}
	}

	private static IBakedModel getFluidModel() {
		return FluidModel.WATER.bake(null,
				null,
				RenderMaterial::sprite,
				SimpleModelTransform.IDENTITY,
				null,
				new ResourceLocation("block/water.json")
				);
	}
}
