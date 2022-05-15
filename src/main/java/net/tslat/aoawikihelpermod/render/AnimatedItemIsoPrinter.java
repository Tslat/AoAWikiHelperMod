package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;

public final class AnimatedItemIsoPrinter extends ItemIsoPrinter {
	private final int renderTicks;
	private int currentFrame = 0;
	private long lastTick;

	private boolean finalisedDetection = false;
	private boolean definedSize = false;
	private float definedMinScale = Float.MAX_VALUE;
	private float definedXOffset = 0;
	private float definedYOffset = 0;
	private Vector4f definedBounds = null;

	private final NativeImageGifWriter gifWriter;

	public AnimatedItemIsoPrinter(ItemStack item, int imageSize, CommandSourceStack commandSource, String commandName, Consumer<File> fileConsumer) {
		super(item, imageSize, commandSource, commandName, fileConsumer);

		this.renderTicks = calculateRenderTickTime();
		this.lastTick = Minecraft.getInstance().level.getGameTime();
		NativeImageGifWriter gifWriter = null;

		try {
			gifWriter = new NativeImageGifWriter(getOutputFile());
		}
		catch (IOException ex) {
			WikiHelperCommand.error(commandSource, commandName, "Unable to instantiate gif writer for file. Check log for more details");
			ex.printStackTrace();
		}
		finally {
			this.gifWriter = gifWriter;
		}

		this.currentStatus = "Determining best scale for render...";
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (gifWriter == null)
			return;

		if (definedSize) {
			if (currentFrame == 0 && !isOnFirstFrame()) {
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
					NativeImage image = extractSubRange(captureImage(), this.definedBounds);

					if (image != null) {
						try {
							this.gifWriter.writeFrame(image);
						} catch (IOException e) {
							WikiHelperCommand.warn(commandSource, commandName, "Error while writing frame to gif file. Resulting gif might be malformed or incorrect. See log for more details");
							e.printStackTrace();
						}
					}

					this.currentFrame++;
					this.lastTick = Minecraft.getInstance().level.getGameTime();
				}
			}
		}
		else {
			if (determineScaleAndPosition()) {
				if (Minecraft.getInstance().level.getGameTime() != lastTick) {
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

		drawCurrentStatus(matrixStack);

		if (this.currentFrame >= this.renderTicks) {
			if (!definedSize) {
				this.definedSize = true;
				this.currentFrame = 0;
			}
			else if (!finalisedDetection) {
				this.finalisedDetection = true;
				this.currentFrame = 0;
			}
			else {
				this.gifWriter.close();
				this.fileConsumer.accept(getOutputFile());
				Minecraft.getInstance().setScreen(null);
			}
		}
	}

	@Override
	protected File getOutputFile() {
		return PrintHelper.configDir.toPath().resolve("Item Renders").resolve(stack.getItem().getRegistryName().getNamespace()).resolve(stack.getItem().getName(stack).getString() + " - " + targetSize + "px.gif").toFile();
	}

	private boolean isOnFirstFrame() {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		Random rand = new Random();
		int highestFrameTime = 0;
		BakedModel model = itemRenderer.getModel(this.stack, null, null, 0);

		for (Direction face : Direction.values()) {
			rand.setSeed(42L);

			for (BakedQuad quad : model.getQuads(null, face, rand, EmptyModelData.INSTANCE)) {
				if (quad.getSprite().animatedTexture.frame != 0 || quad.getSprite().animatedTexture.subFrame != 0)
					return false;
			}
		}

		rand.setSeed(42L);

		for (BakedQuad quad : model.getQuads(null, null, rand, EmptyModelData.INSTANCE)) {
			if (quad.getSprite().animatedTexture.frame != 0 || quad.getSprite().animatedTexture.subFrame != 0)
				return false;
		}

		return true;
	}

	private int calculateRenderTickTime() {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		Random rand = new Random();
		int highestFrameTime = 0;
		BakedModel model = itemRenderer.getModel(this.stack, null, null, 0);

		for (Direction face : Direction.values()) {
			rand.setSeed(42L);

			for (BakedQuad quad : model.getQuads(null, face, rand, EmptyModelData.INSTANCE)) {
				TextureAtlasSprite sprite = quad.getSprite();
				int totalLength = 0;

				for (TextureAtlasSprite.FrameInfo frameInfo : sprite.animatedTexture.frames) {
					totalLength += frameInfo.time;
				}

				highestFrameTime = Math.max(highestFrameTime, totalLength);
			}
		}

		rand.setSeed(42L);

		for (BakedQuad quad : model.getQuads(null, null, rand, EmptyModelData.INSTANCE)) {
			TextureAtlasSprite sprite = quad.getSprite();
			int totalLength = 0;

			for (TextureAtlasSprite.FrameInfo frameInfo : sprite.animatedTexture.frames) {
				totalLength += frameInfo.time;
			}

			highestFrameTime = Math.max(highestFrameTime, totalLength);
		}

		return highestFrameTime;
	}

	@Override
	public void onClose() {
		this.gifWriter.exit();

		super.onClose();
	}
}