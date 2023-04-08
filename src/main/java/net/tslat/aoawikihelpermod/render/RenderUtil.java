package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Map;

public final class RenderUtil {
	private static final Map<SpriteContents.AnimatedTexture, SpriteContents.Ticker> ANIMATED_TEXTURE_TICKERS = new Object2ObjectOpenHashMap<>();

	public static void clearRenderBuffer() {
		RenderSystem.clearColor(0, 0, 0, 0);
		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
	}

	public static void translateToIsometricView(PoseStack matrix) {
		matrix.scale(1, 1, -1);
		matrix.translate(0, 0, 1000);
		matrix.mulPose(Axis.XP.rotationDegrees(30)); // Dimetric Projection
		matrix.mulPose(Axis.YP.rotationDegrees(225));
		matrix.mulPose(Axis.ZP.rotationDegrees(180));
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
				RenderType renderType = ItemBlockRenderTypes.getRenderType(block, true);
				/*boolean changeLighting = !blockModel.usesBlockLight();

				if (changeLighting)
					Lighting.setupForFlatItems();*/

				blockRenderer.getModelRenderer().renderModel(matrix.last(), renderBuffer.getBuffer(renderType), block, blockModel, red, green, blue, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);

				/*if (changeLighting)
					Lighting.setupFor3DItems();*/
			}
			case ENTITYBLOCK_ANIMATED -> {
				ItemStack stack = new ItemStack(block.getBlock());
				IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemDisplayContext.NONE, matrix, renderBuffer, 15728880, OverlayTexture.NO_OVERLAY);
			}
		}
	}

	public static void addSpriteTicker(SpriteContents.Ticker ticker) {
		ANIMATED_TEXTURE_TICKERS.put(ticker.animationInfo, ticker);
	}

	public static SpriteContents.Ticker getTickerForTexture(SpriteContents.AnimatedTexture texture) {
		return ANIMATED_TEXTURE_TICKERS.get(texture);
	}
}
