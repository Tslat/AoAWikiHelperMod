package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
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

		if (pos == null)
			pos = BlockPos.ZERO;

		switch (block.getRenderShape()) {
			case MODEL -> {
				int tint = Minecraft.getInstance().getBlockColors().getColor(block, FakeWorld.INSTANCE.get(), pos, 0);
				float red = (float)(tint >> 16 & 255) / 255f;
				float green = (float)(tint >> 8 & 255) / 255f;
				float blue = (float)(tint & 255) / 255f;
				BakedModel blockModel = blockRenderer.getBlockModel(block);
				RenderSystem.enableDepthTest();
				RenderSystem.enableBlend();
				/*boolean changeLighting = !blockModel.usesBlockLight();

				if (changeLighting)
					Lighting.setupForFlatItems();*/

				for (RenderType renderType : blockModel.getRenderTypes(block, FakeWorld.INSTANCE.get().getRandom(), ModelData.EMPTY)) {
					if (renderType == RenderType.translucent())
						renderType = Sheets.translucentCullBlockSheet();

					blockRenderer.getModelRenderer().renderModel(matrix.last(), renderBuffer.getBuffer(renderType), block, blockModel, red, green, blue, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
				}

				if (block.getBlock() instanceof EntityBlock entityBlock) {
					BlockEntity blockEntity = entityBlock.newBlockEntity(pos, block);

					if (blockEntity != null)
						Minecraft.getInstance().getBlockEntityRenderDispatcher().render(blockEntity, Minecraft.getInstance().getPartialTick(), matrix, renderBuffer);
				}

				/*if (changeLighting)
					Lighting.setupFor3DItems();*/
			}
			case ENTITYBLOCK_ANIMATED -> {
				ItemStack stack = new ItemStack(block.getBlock());
				IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemDisplayContext.NONE, matrix, renderBuffer, 15728880, OverlayTexture.NO_OVERLAY);
			}
			case INVISIBLE -> {
				if (block.getBlock() instanceof LiquidBlock liquidBlock) {
					Tesselator tesselator = Tesselator.getInstance();
					BufferBuilder builder = tesselator.getBuilder();

					try {
						FluidState fluidState = block.getFluidState();
						RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
						PoseStack worldStack = RenderSystem.getModelViewStack();

						if (renderType == RenderType.translucent())
							renderType = RenderType.solid(); // TODO figure out why tf translucent isn't working

						renderType.setupRenderState();
						worldStack.pushPose();
						worldStack.mulPoseMatrix(matrix.last().pose());
						RenderSystem.applyModelViewMatrix();

						builder.begin(renderType.mode(), renderType.format());
						blockRenderer.renderLiquid(pos, FakeWorld.INSTANCE.get(), builder, block, fluidState);

						if (builder.building())
							tesselator.end();

						renderType.clearRenderState();
						worldStack.popPose();
						RenderSystem.applyModelViewMatrix();
					}
					catch (Exception ex) {
						ex.printStackTrace();

						if (builder.building())
							tesselator.end();
					}
				}
				else if (block.getBlock() instanceof EntityBlock entityBlock) {
					BlockEntity blockEntity = entityBlock.newBlockEntity(pos, block);

					if (blockEntity != null)
						Minecraft.getInstance().getBlockEntityRenderDispatcher().render(blockEntity, Minecraft.getInstance().getPartialTick(), matrix, renderBuffer);
				}
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
