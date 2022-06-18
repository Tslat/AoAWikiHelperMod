package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.client.model.FluidModel;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public final class RenderUtil {
	public static void clearRenderBuffer() {
		RenderSystem.clearColor(0, 0, 0, 0);
		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
	}

	public static void translateToIsometricView(PoseStack matrix) {
		matrix.scale(1, 1, -1);
		matrix.translate(0, 0, 1000);
		matrix.mulPose(Vector3f.XP.rotationDegrees(30)); // Dimetric Projection
		matrix.mulPose(Vector3f.YP.rotationDegrees(225));
		matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
	}

	public static void setupFakeGuiLighting() {
		RenderSystem.setupGui3DDiffuseLighting(
				Util.make(
						new Vector3f(0.5f, 1f, 0.5f), Vector3f::normalize),
				Util.make(
						new Vector3f(0.6f, -1.5f, -3.5f), Vector3f::normalize
				));
	}

	public static void renderStandardisedBlock(BlockRenderDispatcher blockRenderer, PoseStack matrix, MultiBufferSource renderBuffer, BlockState block, @Nullable BlockPos pos) {
		setupFakeGuiLighting();

		RenderShape blockRenderType = block.getRenderShape();

		if (blockRenderType == RenderShape.INVISIBLE) {
			if (block.getBlock() instanceof LiquidBlock)
				//StaticFluidRenderer.renderFluid(matrix.last(), pos, FakeWorld.INSTANCE.get(), renderBuffer.getBuffer(RenderType.translucent()), block);

			return;
		}

		if (pos == null)
			pos = new BlockPos(0, 0, 0);

		switch (blockRenderType) {
			case MODEL -> {
				int tint = Minecraft.getInstance().getBlockColors().getColor(block, FakeWorld.INSTANCE.get(), pos, 0);
				float red = (float)(tint >> 16 & 255) / 255f;
				float green = (float)(tint >> 8 & 255) / 255f;
				float blue = (float)(tint & 255) / 255f;
				BakedModel blockModel = blockRenderer.getBlockModel(block);
				/*boolean changeLighting = !blockModel.usesBlockLight();

				if (changeLighting)
					Lighting.setupForFlatItems();*/

				blockRenderer.getModelRenderer().renderModel(matrix.last(), renderBuffer.getBuffer(ItemBlockRenderTypes.getRenderType(block, true)), block, blockModel, red, green, blue, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

				/*if (changeLighting)
					Lighting.setupFor3DItems();*/
			}
			case ENTITYBLOCK_ANIMATED -> {
				ItemStack stack = new ItemStack(block.getBlock());
				RenderProperties.get(stack).getItemStackRenderer().renderByItem(stack, ItemTransforms.TransformType.NONE, matrix, renderBuffer, 15728880, OverlayTexture.NO_OVERLAY);
			}
		}
	}

	private static BakedModel getFluidModel() {
		return FluidModel.WATER.bake(null,
				null,
				ForgeModelBakery.defaultTextureGetter(),
				BlockModelRotation.X0_Y0,
				null,
				new ResourceLocation("block/water.json")
				);
	}
}
