package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.client.ForgeHooksClient;

public class StaticFluidRenderer {
	public static void renderFluid(PoseStack.Pose matrixEntry, BlockPos pos, BlockAndTintGetter world, VertexConsumer vertexBuilder, BlockState blockState) {
		try {
			renderFluidBlock(matrixEntry, pos, world, vertexBuilder, blockState);
		}
		catch (Exception ex) {
			CrashReport crashReport = CrashReport.forThrowable(ex, "Statically rendering fluid block");

			CrashReportCategory.populateBlockDetails(crashReport.addCategory("Rendering Fluid"), world, pos, blockState);

			throw new ReportedException(crashReport);
		}
	}

	private static void renderFluidBlock(PoseStack.Pose matrixEntry, BlockPos pos, BlockAndTintGetter world, VertexConsumer vertexBuilder, BlockState blockState) {
		FluidState fluidState = world.getFluidState(pos);
		boolean isLava = fluidState.is(FluidTags.LAVA);
		TextureAtlasSprite[] fluidSprites = ForgeHooksClient.getFluidSprites(world, pos, fluidState);
		int fluidColour = 0;//fluidState.getType().getAttributes().getColor(world, pos);
		float fluidAlpha = (float)(fluidColour >> 24 & 255) / 255f;
		float fluidTintRed = (float)(fluidColour >> 16 & 255) / 255f;
		float fluidTintGreen = (float)(fluidColour >> 8 & 255) / 255f;
		float fluidTintBlue = (float)(fluidColour & 255) / 255f;
		boolean renderTop = !doesAdjacentFluidMatch(world, pos, Direction.UP, fluidState);
		boolean renderBottom = shouldRenderFace(world, pos, fluidState, blockState, Direction.DOWN) && !isFaceOccludedByNeighbour(world, pos, Direction.DOWN, 0.8888889f);
		boolean renderNorth = shouldRenderFace(world, pos, fluidState, blockState, Direction.NORTH);
		boolean renderSouth = shouldRenderFace(world, pos, fluidState, blockState, Direction.SOUTH);
		boolean renderWest = shouldRenderFace(world, pos, fluidState, blockState, Direction.WEST);
		boolean renderEast = shouldRenderFace(world, pos, fluidState, blockState, Direction.EAST);

		if (renderTop || renderBottom || renderEast || renderWest || renderNorth || renderSouth) {
			float bottomShading = world.getShade(Direction.DOWN, true);
			float topShading = world.getShade(Direction.UP, true);
			float northShading = world.getShade(Direction.NORTH, true);
			float westShading = world.getShade(Direction.WEST, true);
			float fluidHeight = getFluidHeight(world, pos, fluidState);
			float southFluidHeight = getFluidHeight(world, pos.south(), fluidState);
			float southEastFluidHeight = getFluidHeight(world, pos.east().south(), fluidState);
			float eastFluidHeight = getFluidHeight(world, pos.east(), fluidState);
			double posX = pos.getX();
			double posY = pos.getY();
			double posZ = pos.getZ();
			float bottomAdjust = renderBottom ? 0.001f : 0;

			if (renderTop && !isFaceOccludedByNeighbour(world, pos, Direction.UP, Math.min(Math.min(fluidHeight, southFluidHeight), Math.min(southEastFluidHeight, eastFluidHeight)))) {
				fluidHeight -= 0.001f;
				southFluidHeight -= 0.001f;
				southEastFluidHeight -= 0.001f;
				eastFluidHeight -= 0.001f;
				Vec3 flow = fluidState.getFlow(world, pos);
				float renderUMin;
				float flowAdjustedUMin;
				float renderUMax;
				float flowAdjustedUMax;
				float renderVMin;
				float renderVMax;
				float flowAdjustedVMax;
				float flowAdjustmentVMin;

				if (flow.x == 0 && flow.z == 0) {
					TextureAtlasSprite stillTexture = fluidSprites[0];
					renderUMin = stillTexture.getU(0);
					renderVMin = stillTexture.getV(0);
					flowAdjustedUMin = renderUMin;
					renderUMax = stillTexture.getU(16);
					renderVMax = stillTexture.getV(16);
					flowAdjustedVMax = renderVMax;
					flowAdjustedUMax = renderUMax;
					flowAdjustmentVMin = renderVMin;
				}
				else {
					TextureAtlasSprite flowingTexture = fluidSprites[1];
					float flowAngle = (float)Math.atan2(flow.z, flow.x) - (float)Math.PI / 2f;
					float flowAngleX = Mth.sin(flowAngle) * 0.25f;
					float flowAngleZ = Mth.cos(flowAngle) * 0.25f;
					renderUMin = flowingTexture.getU(8 + (-flowAngleZ - flowAngleX) * 16);
					renderVMin = flowingTexture.getV(8 + (-flowAngleZ + flowAngleX) * 16);
					flowAdjustedUMin = flowingTexture.getU(8 + (-flowAngleZ + flowAngleX) * 16);
					renderVMax = flowingTexture.getV(8 + (flowAngleZ + flowAngleX) * 16);
					renderUMax = flowingTexture.getU(8 + (flowAngleZ + flowAngleX) * 16);
					flowAdjustedVMax = flowingTexture.getV(8 + (flowAngleZ - flowAngleX) * 16);
					flowAdjustedUMax = flowingTexture.getU(8 + (flowAngleZ - flowAngleX) * 16);
					flowAdjustmentVMin = flowingTexture.getV(8 + (-flowAngleZ - flowAngleX) * 16);
				}

				float averageU = (renderUMin + flowAdjustedUMin + renderUMax + flowAdjustedUMax) / 4f;
				float averageV = (renderVMin + renderVMax + flowAdjustedVMax + flowAdjustmentVMin) / 4f;
				float renderWidthStretch = (float)fluidSprites[0].contents().width() / (fluidSprites[0].getU1() - fluidSprites[0].getU0());
				float renderHeightStretch = (float)fluidSprites[0].contents().height() / (fluidSprites[0].getV1() - fluidSprites[0].getV0());
				float maxStretch = 4f / Math.max(renderHeightStretch, renderWidthStretch);
				renderUMin = Mth.lerp(maxStretch, renderUMin, averageU);
				flowAdjustedUMin = Mth.lerp(maxStretch, flowAdjustedUMin, averageU);
				renderUMax = Mth.lerp(maxStretch, renderUMax, averageU);
				flowAdjustedUMax = Mth.lerp(maxStretch, flowAdjustedUMax, averageU);
				renderVMin = Mth.lerp(maxStretch, renderVMin, averageV);
				renderVMax = Mth.lerp(maxStretch, renderVMax, averageV);
				flowAdjustedVMax = Mth.lerp(maxStretch, flowAdjustedVMax, averageV);
				flowAdjustmentVMin = Mth.lerp(maxStretch, flowAdjustmentVMin, averageV);
				int packedLight = getLightColour(world, pos);
				float red = topShading * fluidTintRed;
				float green = topShading * fluidTintGreen;
				float blue = topShading * fluidTintBlue;

				vert(vertexBuilder, posX + 0, posY + fluidHeight, posZ, red, green, blue, fluidAlpha, renderUMin, renderVMin, packedLight);
				vert(vertexBuilder, posX + 0, posY + southFluidHeight, posZ + 1, red, green, blue, fluidAlpha, flowAdjustedUMin, renderVMax, packedLight);
				vert(vertexBuilder, posX + 1, posY + southEastFluidHeight, posZ + 1, red, green, blue, fluidAlpha, renderUMax, flowAdjustedVMax, packedLight);
				vert(vertexBuilder, posX + 1, posY + eastFluidHeight, posZ, red, green, blue, fluidAlpha, flowAdjustedUMax, flowAdjustmentVMin, packedLight);

				if (fluidState.shouldRenderBackwardUpFace(world, pos.above())) {
					vert(vertexBuilder, posX + 0, posY + fluidHeight, posZ, red, green, blue, fluidAlpha, renderUMin, renderVMin, packedLight);
					vert(vertexBuilder, posX + 1, posY + eastFluidHeight, posZ, red, green, blue, fluidAlpha, flowAdjustedUMax, flowAdjustmentVMin, packedLight);
					vert(vertexBuilder, posX + 1, posY + southEastFluidHeight, posZ + 1, red, green, blue, fluidAlpha, renderUMax, flowAdjustedVMax, packedLight);
					vert(vertexBuilder, posX + 0, posY + southFluidHeight, posZ + 1, red, green, blue, fluidAlpha, flowAdjustedUMin, renderVMax, packedLight);
				}
			}

			if (renderBottom) {
				float u = fluidSprites[0].getU0();
				float u2 = fluidSprites[0].getU1();
				float v = fluidSprites[0].getV0();
				float v2 = fluidSprites[0].getV1();
				int packedLight = getLightColour(world, pos.below());
				float red = bottomShading * fluidTintRed;
				float green = bottomShading * fluidTintGreen;
				float blue = bottomShading * fluidTintBlue;

				vert(vertexBuilder, posX, posY + bottomAdjust, posZ + 1, red, green, blue, fluidAlpha, u, v2, packedLight);
				vert(vertexBuilder, posX, posY + bottomAdjust, posZ, red, green, blue, fluidAlpha, u, v, packedLight);
				vert(vertexBuilder, posX + 1, posY + bottomAdjust, posZ, red, green, blue, fluidAlpha, u2, v, packedLight);
				vert(vertexBuilder, posX + 1, posY + bottomAdjust, posZ + 1, red, green, blue, fluidAlpha, u2, v2, packedLight);
			}

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				float relativeFluidHeight;
				float relativeAdjacentFluidHeight;
				double vertX;
				double vertZ;
				double vertX2;
				double vertZ2;

				switch (direction) {
					case NORTH:
						if (!renderNorth)
							return;

						relativeFluidHeight = fluidHeight;
						relativeAdjacentFluidHeight = eastFluidHeight;
						vertX = posX;
						vertX2 = posX + 1;
						vertZ = posZ + (double)0.001f;
						vertZ2 = posZ + (double)0.001f;
						break;
					case SOUTH:
						if (!renderSouth)
							return;

						relativeFluidHeight = southEastFluidHeight;
						relativeAdjacentFluidHeight = southFluidHeight;
						vertX = posX + 1;
						vertX2 = posX;
						vertZ = posZ + 1 - (double)0.001f;
						vertZ2 = posZ + 1 - (double)0.001f;
						break;
					case WEST:
						if (!renderWest)
							return;

						relativeFluidHeight = southFluidHeight;
						relativeAdjacentFluidHeight = fluidHeight;
						vertX = posX + (double)0.001f;
						vertX2 = posX + (double)0.001f;
						vertZ = posZ + 1;
						vertZ2 = posZ;
						break;
					case EAST:
					default:
						if (!renderEast)
							return;

						relativeFluidHeight = eastFluidHeight;
						relativeAdjacentFluidHeight = southEastFluidHeight;
						vertX = posX + 1 - (double)0.001f;
						vertX2 = posX + 1 - (double)0.001f;
						vertZ = posZ;
						vertZ2 = posZ + 1;
						break;
				}

				if (!isFaceOccludedByNeighbour(world, pos, direction, Math.max(relativeFluidHeight, relativeAdjacentFluidHeight))) {
					BlockPos adjacentPos = pos.relative(direction);
					TextureAtlasSprite texture = fluidSprites[1];

					if (fluidSprites[2] != null && world.getBlockState(adjacentPos).shouldDisplayFluidOverlay(world, adjacentPos, fluidState))
						texture = fluidSprites[2];

					float uMin = texture.getU(0);
					float uMax = texture.getU(8);
					float cornerV = texture.getV((1 - relativeFluidHeight) * 16 * 0.5f);
					float cornerV2 = texture.getV((1 - relativeAdjacentFluidHeight) * 16 * 0.5f);
					float vMax = texture.getV(8);
					int lightColour = getLightColour(world, adjacentPos);
					float shading = direction == Direction.NORTH || direction == Direction.SOUTH ? northShading : westShading;
					float renderRed = topShading * shading * fluidTintRed;
					float renderGreen = topShading * shading * fluidTintGreen;
					float renderBlue = topShading * shading * fluidTintBlue;

					vert(vertexBuilder, vertX, posY + (double)relativeFluidHeight, vertZ, renderRed, renderGreen, renderBlue, fluidAlpha, uMin, cornerV, lightColour);
					vert(vertexBuilder, vertX2, posY + (double)relativeAdjacentFluidHeight, vertZ2, renderRed, renderGreen, renderBlue, fluidAlpha, uMax, cornerV2, lightColour);
					vert(vertexBuilder, vertX2, posY + (double)bottomAdjust, vertZ2, renderRed, renderGreen, renderBlue, fluidAlpha, uMax, vMax, lightColour);
					vert(vertexBuilder, vertX, posY + (double)bottomAdjust, vertZ, renderRed, renderGreen, renderBlue, fluidAlpha, uMin, vMax, lightColour);

					if (texture != fluidSprites[2]) {
						vert(vertexBuilder, vertX, posY + (double)bottomAdjust, vertZ, renderRed, renderGreen, renderBlue, fluidAlpha, uMin, vMax, lightColour);
						vert(vertexBuilder, vertX2, posY + (double)bottomAdjust, vertZ2, renderRed, renderGreen, renderBlue, fluidAlpha, uMax, vMax, lightColour);
						vert(vertexBuilder, vertX2, posY + (double)relativeAdjacentFluidHeight, vertZ2, renderRed, renderGreen, renderBlue, fluidAlpha, uMax, cornerV2, lightColour);
						vert(vertexBuilder, vertX, posY + (double)relativeFluidHeight, vertZ, renderRed, renderGreen, renderBlue, fluidAlpha, uMin, cornerV, lightColour);
					}
				}
			}
		}
	}

	public static int getLightColour(BlockAndTintGetter world, BlockPos pos) {
		int colour = LevelRenderer.getLightColor(world, pos);
		int aboveColour = LevelRenderer.getLightColor(world, pos.above());

		return Math.max(colour & 255, aboveColour & 255) | Math.max(colour >> 16 & 255, aboveColour >> 16 & 255) << 16;
	}

	public static float getFluidHeight(BlockGetter world, BlockPos pos, FluidState fluid) {
		int heightAverager = 0;
		float contributingHeights = 0;

		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockPos sidePos = pos.relative(side);

			if (doesFluidMatch(world, sidePos.above(), fluid))
				return 1;

			FluidState sideFluid = world.getFluidState(sidePos);

			if (sideFluid.getType().isSame(fluid.getType())) {
				float sideFluidHeight = sideFluid.getHeight(world, sidePos);

				if (sideFluidHeight > 0.8f) {
					contributingHeights += sideFluidHeight * 10;
					heightAverager += 10;
				}
				else {
					contributingHeights += sideFluidHeight;
					heightAverager++;
				}
			}
			else if (!world.getBlockState(sidePos).getMaterial().isSolid()) {
				heightAverager++;
			}
		}

		return contributingHeights / (float)heightAverager;
	}

	/*public static TextureAtlasSprite[] getBlockSprites(BlockState blockState) {
		TextureAtlasSprite[] sprites = new TextureAtlasSprite[2];

		sprites[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(blockState).getParticleIcon();
		sprites[1] = blockState.getMaterial() == Material.LAVA ? ModelBakery.LAVA_FLOW.sprite() : ModelBakery.WATER_OVERLAY.sprite();

		return sprites;
	}*/

	public static boolean doesAdjacentFluidMatch(BlockGetter world, BlockPos pos, Direction side, FluidState fluid) {
		return doesFluidMatch(world, pos.relative(side), fluid);
	}

	public static boolean doesFluidMatch(BlockGetter world, BlockPos pos, FluidState fluid) {
		return fluid.getType().isSame(world.getFluidState(pos).getType());
	}

	public static boolean isFaceOccluded(BlockGetter world, Direction side, float height, BlockPos pos, BlockState state) {
		if (!state.canOcclude())
			return false;

		return Shapes.blockOccudes(Shapes.box(0, 0, 0, 1, height, 1), state.getOcclusionShape(world, pos), side);
	}

	public static boolean isFaceOccludedByNeighbour(BlockGetter world, BlockPos pos, Direction side, float height) {
		BlockPos neighbourPos = pos.relative(side);

		return isFaceOccluded(world, side, height, neighbourPos, world.getBlockState(neighbourPos));
	}

	public static boolean isFaceOccludedBySelf(BlockGetter world, BlockPos pos, BlockState state, Direction side) {
		return isFaceOccluded(world, side.getOpposite(), 1, pos, state);
	}

	public static boolean shouldRenderFace(BlockAndTintGetter world, BlockPos pos, FluidState fluidState, BlockState state, Direction side) {
		return !isFaceOccludedBySelf(world, pos, state, side) && !doesAdjacentFluidMatch(world, pos, side, fluidState);
	}

	public static void vert(VertexConsumer vertexBuilder, double x, double y, double z, float red, float green, float blue, float alpha, float u, float v, int packedLight) {
		vertexBuilder.vertex(x, y, z).color(red, green, blue, alpha).uv(u, v).uv2(packedLight).normal(0, 1, 0).endVertex();
	}
}
