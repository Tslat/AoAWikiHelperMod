package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.tslat.aoa3.util.RegistryUtil;
import net.tslat.aoa3.util.StringUtil;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printer.PrintHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class AnimatedStateBasedBlockIsoPrinter extends BlockIsoPrinter {
	private final int renderTicks;
	private int currentFrame = 0;
	private int currentState = 0;
	private long lastTick;

	private boolean finalisedDetection = false;
	private boolean definedSize = false;
	private float definedMinScale = Float.MAX_VALUE;
	private float definedXOffset = 0;
	private float definedYOffset = 0;
	private Vector4f definedBounds = null;
	private final Property property;
	private final int frameTime;

	private final NativeImageGifWriter gifWriter;
	private final List<BlockState> states;

	public AnimatedStateBasedBlockIsoPrinter(BlockState block, Property<?> property, int frameTime, int imageSize, float rotationAdjust, CommandSourceStack commandSource, String commandName, Consumer<File> fileConsumer) {
		super(block, imageSize, rotationAdjust, commandSource, commandName, fileConsumer);

		this.property = property;
		this.frameTime = frameTime;
		this.states = compileStateArray();
		this.renderTicks = calculateRenderTickTime();
		this.lastTick = Minecraft.getInstance().level.getGameTime();
		this.gifWriter = makeGifWriter(commandSource, commandName);

		this.currentStatus = "Determining best scale for render...";
	}

	@Nullable
	protected NativeImageGifWriter makeGifWriter(CommandSourceStack commandSource, String commandName) {
		try {
			return new NativeImageGifWriter(getOutputFile(), 5 * this.frameTime);
		}
		catch (IOException ex) {
			WikiHelperCommand.error(commandSource, commandName, "Unable to instantiate gif writer for file. Check log for more details");
			ex.printStackTrace();
		}

		return null;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (gifWriter == null)
			return;

		final boolean onFirstFrame = isOnFirstFrame();

		if (definedSize) {
			if (currentFrame == 0 && !onFirstFrame) {
				RenderUtil.clearRenderBuffer();

				this.currentStatus = "Waiting for frame cycle to reset...";
			}
			else if (!finalisedDetection) {
				this.scale = this.definedMinScale;
				this.xAdjust = this.definedXOffset;
				this.yAdjust = this.definedYOffset;

				RenderUtil.clearRenderBuffer();
				renderObject();

				Vector4f bounds = getBoundsForRenderedImage(captureImage());
				this.definedBounds = new Vector4f(Math.min(bounds.x(), this.definedBounds.x()), Math.min(bounds.y(), this.definedBounds.y()), Math.max(bounds.z(), this.definedBounds.z()), Math.max(bounds.w(), this.definedBounds.w()));
				this.currentStatus = "Determining image alignment...";

				if (Minecraft.getInstance().level.getGameTime() != lastTick) {
					this.currentFrame++;
					this.lastTick = Minecraft.getInstance().level.getGameTime();
				}
			}
			else {
				this.currentStatus = "Capturing finalised render...";
				this.scale = this.definedMinScale;
				this.xAdjust = this.definedXOffset;
				this.yAdjust = this.definedYOffset;

				RenderUtil.clearRenderBuffer();
				renderObject();

				if (Minecraft.getInstance().level.getGameTime() != lastTick) {
					if (this.currentFrame == 0) {
						NativeImage image = extractSubRange(captureImage(), this.definedBounds);

						if (image != null) {
							try {
								this.gifWriter.writeFrame(image);
							} catch (IOException e) {
								WikiHelperCommand.warn(commandSource, commandName, "Error while writing frame to gif file. Resulting gif might be malformed or incorrect. See log for more details");
								e.printStackTrace();
							}
						}
					}

					this.currentFrame++;
					this.lastTick = Minecraft.getInstance().level.getGameTime();
				}
			}
		}
		else {
			if (!onFirstFrame) {
				if (Minecraft.getInstance().level.getGameTime() != this.lastTick) {
					this.currentFrame++;
					this.lastTick = Minecraft.getInstance().level.getGameTime();
				}
			}
			else if (determineScaleAndPosition()) {
				if (Minecraft.getInstance().level.getGameTime() != this.lastTick) {
					this.currentFrame++;
					this.lastTick = Minecraft.getInstance().level.getGameTime();
				}

				if (this.scale < this.definedMinScale) {
					this.definedMinScale = this.scale;
					this.definedXOffset = this.xAdjust;
					this.definedYOffset = this.yAdjust;
					this.definedBounds = getBoundsForRenderedImage(captureImage());
				}
			}
		}

		drawCurrentStatus(guiGraphics);

		if (this.currentFrame >= this.renderTicks) {
			if (this.currentState < this.states.size() - 1) {
				this.currentState++;
				this.currentFrame = 0;
			}
			else if (!definedSize) {
				this.definedSize = true;
				this.currentFrame = 0;
				this.currentState = 0;
			}
			else if (!finalisedDetection) {
				this.finalisedDetection = true;
				this.currentFrame = 0;
				this.currentState = 0;
			}
			else {
				this.gifWriter.close();
				this.fileConsumer.accept(getOutputFile());
				Minecraft.getInstance().setScreen(null);
			}
		}
	}

	@Override
	protected void renderObject() {
		PoseStack matrix = new PoseStack();

		withAlignedIsometricProjection(matrix, () -> {
			matrix.scale(5, 5, 5);
			BlockRenderDispatcher blockRenderer = this.minecraft.getBlockRenderer();
			EntityRenderDispatcher renderManager = this.minecraft.getEntityRenderDispatcher();
			MultiBufferSource.BufferSource renderBuffer = mc.renderBuffers().bufferSource();

			renderManager.setRenderShadow(false);

			try {
				if (!customRenderBlock(blockRenderer, matrix, renderBuffer))
					RenderUtil.renderStandardisedBlock(blockRenderer, matrix, renderBuffer, this.states.get(this.currentState), null);
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
	protected File getOutputFile() {
		return PrintHelper.configDir.toPath().resolve("Block Renders").resolve(RegistryUtil.getId(block.getBlock()).getNamespace()).resolve(block.getBlock().getName().getString() + " - " + StringUtil.toTitleCase(property.getName()) + " - " + targetSize + "px.gif").toFile();
	}

	private boolean isOnFirstFrame() {
		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		RandomSource rand = RandomSource.create();
		int highestFrameTime = 0;

		for (BlockState state : this.states) {
			BakedModel model = blockRenderer.getBlockModel(state);
			TextureAtlasSprite[] fluidSprites = !state.getFluidState().isEmpty() ? FluidSpriteCache.getFluidSprites(FakeWorld.INSTANCE.get(), BlockPos.ZERO, state.getFluidState()) : null;

			if (fluidSprites != null) {
				TextureAtlasSprite sprite = fluidSprites[1];

				if (sprite.contents().animatedTexture == null)
					return true;

				SpriteContents.Ticker ticker = RenderUtil.getTickerForTexture(sprite.contents().animatedTexture);

				if (ticker == null)
					return true;

				return ticker.frame == 0 && ticker.subFrame == 0;
			}

			for (Direction face : Direction.values()) {
				rand.setSeed(42L);

				for (BakedQuad quad : model.getQuads(state, face, rand, ModelData.EMPTY, null)) {
					TextureAtlasSprite sprite = quad.getSprite();

					if (sprite.contents().animatedTexture == null)
						continue;

					SpriteContents.Ticker ticker = RenderUtil.getTickerForTexture(sprite.contents().animatedTexture);

					if (ticker == null)
						continue;

					if (ticker.frame != 0 || ticker.subFrame != 0)
						return false;
				}
			}

			rand.setSeed(42L);

			for (BakedQuad quad : model.getQuads(state, null, rand, ModelData.EMPTY, null)) {
				TextureAtlasSprite sprite = quad.getSprite();

				if (sprite.contents().animatedTexture == null)
					continue;

				SpriteContents.Ticker ticker = RenderUtil.getTickerForTexture(sprite.contents().animatedTexture);

				if (ticker == null)
					continue;

				if (ticker.frame != 0 || ticker.subFrame != 0)
					return false;
			}
		}

		return true;
	}

	private List<BlockState> compileStateArray() { // Don't change this even if you think you know what you're doing, it'll break I guarantee it
		try {
			return this.property
					.getPossibleValues()
					.stream()
					.sorted(Comparator.naturalOrder())
					.map(value -> this.block.getBlock().defaultBlockState().setValue(this.property, (Comparable)value))
					.toList();
		}
		catch (Exception ex) {
			WikiHelperCommand.error(commandSource, commandName, "Unable to construct BlockState from given property '" + this.property.getName() + "'");
			ex.printStackTrace();
		}

		return List.of();
	}

	private int calculateRenderTickTime() {
		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		RandomSource rand = RandomSource.create();
		int highestFrameTime = 1;

		for (BlockState state : this.states) {
			BakedModel model = blockRenderer.getBlockModel(state);
			TextureAtlasSprite[] fluidSprites = !state.getFluidState().isEmpty() ? FluidSpriteCache.getFluidSprites(FakeWorld.INSTANCE.get(), BlockPos.ZERO, state.getFluidState()) : null;

			if (fluidSprites != null) {
				TextureAtlasSprite sprite = fluidSprites[1];
				SpriteContents.AnimatedTexture animatedTexture = sprite.contents().animatedTexture;

				if (animatedTexture != null && animatedTexture.frames != null) {
					for (SpriteContents.FrameInfo frameInfo : animatedTexture.frames) {
						highestFrameTime += frameInfo.time;
					}
				}

				return highestFrameTime;
			}

			for (Direction face : Direction.values()) {
				rand.setSeed(42L);

				for (BakedQuad quad : model.getQuads(state, face, rand, ModelData.EMPTY, null)) {
					TextureAtlasSprite sprite = quad.getSprite();
					int totalLength = 0;

					if (sprite.contents().animatedTexture == null)
						continue;

					for (SpriteContents.FrameInfo frameInfo : sprite.contents().animatedTexture.frames) {
						totalLength += frameInfo.time;
					}

					highestFrameTime = Math.max(highestFrameTime, totalLength);
				}
			}

			rand.setSeed(42L);

			for (BakedQuad quad : model.getQuads(state, null, rand, ModelData.EMPTY, null)) {
				TextureAtlasSprite sprite = quad.getSprite();
				int totalLength = 0;

				if (sprite.contents().animatedTexture == null)
					continue;

				for (SpriteContents.FrameInfo frameInfo : sprite.contents().animatedTexture.frames) {
					totalLength += frameInfo.time;
				}

				highestFrameTime = Math.max(highestFrameTime, totalLength);
			}
		}

		return highestFrameTime;
	}

	@Override
	public void onClose() {
		if (this.gifWriter != null)
			this.gifWriter.exit();

		super.onClose();
	}
}
