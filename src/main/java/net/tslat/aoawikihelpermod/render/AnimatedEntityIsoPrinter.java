package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public final class AnimatedEntityIsoPrinter extends EntityIsoPrinter {
	private final int renderTicks;
	private int currentFrame = 0;
	private long lastTick;

	private boolean finalisedDetection = false;
	private boolean definedSize = false;
	private float definedMinScale = Float.MAX_VALUE;
	private float definedXOffset = 0;
	private float definedYOffset = 0;
	private Vector4f definedBounds = null;

	private NativeImageGifWriter gifWriter;
	private final int cachedFPS;

	public AnimatedEntityIsoPrinter(ResourceLocation entityId, int imageSize, int recordLength, float rotationAdjust, CommandSource commandSource, String commandName, Consumer<File> fileConsumer) {
		super(entityId, imageSize, rotationAdjust, commandSource, commandName, fileConsumer);

		this.renderTicks = recordLength;
		this.lastTick = this.mc.level.getGameTime();
		this.cachedFPS = this.mc.options.framerateLimit;
		this.mc.options.framerateLimit = 50;
		this.currentStatus = "Determining best scale for render...";
	}

	@Override
	protected boolean preRenderingCheck() {
		if (super.preRenderingCheck()) {
			NativeImageGifWriter gifWriter = null;

			try {
				gifWriter = new NativeImageGifWriter(getOutputFile(), 2);
			}
			catch (IOException ex) {
				WikiHelperCommand.error(commandSource, commandName, "Unable to instantiate gif writer for file. Check log for more details");
				ex.printStackTrace();
			}
			finally {
				this.gifWriter = gifWriter;
			}

			return true;
		}

		return false;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (gifWriter == null)
			return;

		if (definedSize) {
			this.scale = this.definedMinScale;
			this.xAdjust = this.definedXOffset;
			this.yAdjust = this.definedYOffset;

			RenderUtil.clearRenderBuffer();
			renderObject();

			if (!finalisedDetection) {
				Vector4f bounds = getBoundsForRenderedImage(captureImage());
				this.definedBounds = new Vector4f(Math.min(bounds.x(), this.definedBounds.x()), Math.min(bounds.y(), this.definedBounds.y()), Math.max(bounds.z(), this.definedBounds.z()), Math.max(bounds.w(), this.definedBounds.w()));
			}
			else {
				NativeImage image = extractSubRange(captureImage(), this.definedBounds);

				if (image != null) {
					try {
						this.gifWriter.writeFrame(image);
					}
					catch (IOException e) {
						WikiHelperCommand.warn(commandSource, commandName, "Error while caching gif frame. Resulting gif might be malformed or incorrect. See log for more details");
						e.printStackTrace();
					}
				}
			}

			if (this.mc.level.getGameTime() != this.lastTick) {
				this.currentFrame++;
				this.lastTick = this.mc.level.getGameTime();
				this.cachedEntity.tickCount++;
			}
		}
		else {
			if (determineScaleAndPosition()) {
				if (this.mc.level.getGameTime() != this.lastTick) {
					this.currentFrame++;
					this.lastTick = mc.level.getGameTime();
					this.cachedEntity.tickCount++;
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
				this.cachedEntity.tickCount = 0;
				this.currentStatus = "Determining image alignment...";
			}
			else if (!finalisedDetection) {
				this.finalisedDetection = true;
				this.currentFrame = 0;
				this.cachedEntity.tickCount = 0;
				this.currentStatus = "Capturing finalised render...";
			}
			else {
				this.gifWriter.close();
				this.fileConsumer.accept(getOutputFile());
				this.mc.setScreen(null);
			}
		}
	}

	@Override
	public void onClose() {
		this.mc.options.framerateLimit = this.cachedFPS;
		this.gifWriter.exit();

		super.onClose();
	}

	@Override
	protected File getOutputFile() {
		return PrintHelper.configDir.toPath().resolve("Entity Renders").resolve(this.entityId.getNamespace()).resolve(this.cachedEntity.getDisplayName().getString() + " - " + targetSize + "px.gif").toFile();
	}
}
