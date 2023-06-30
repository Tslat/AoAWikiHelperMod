package net.tslat.aoawikihelpermod.render;

import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.aoa3.util.ColourUtil;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.render.typeadapter.IsoRenderAdapter;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class IsometricPrinterScreen extends Screen {
	private static final HashMultimap<Class<?>, IsoRenderAdapter<?>> adapters = HashMultimap.create();

	protected final Consumer<File> fileConsumer;
	protected final CommandSourceStack commandSource;
	protected final String commandName;

	protected final float rotationAdjust;
	protected final int targetSize;
	protected final int windowWidth;
	protected final int windowHeight;

	protected float scale = 10f;
	protected float xAdjust = 0;
	protected float yAdjust = 0;

	protected final Minecraft mc = Minecraft.getInstance();
	@Nonnull
	protected String currentStatus = "Rendering...";

	protected float defaultRefScale = 1;

	protected IsometricPrinterScreen(int maxSize, float rotationAdjust, CommandSourceStack commandSource, String commandName, Consumer<File> fileConsumer) {
		super(Component.literal("Isometric Rendering"));

		this.fileConsumer = fileConsumer;
		this.commandSource = commandSource;
		this.commandName = commandName;
		this.rotationAdjust = rotationAdjust;

		Window window = mc.getWindow();
		this.windowWidth = window.getScreenWidth();
		this.windowHeight = window.getScreenHeight();

		if (maxSize == 0)
			maxSize = Math.min(this.windowWidth, this.windowHeight);

		maxSize = Math.min(Math.min(this.windowWidth, this.windowHeight), maxSize);

		this.targetSize = maxSize;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected boolean preRenderingCheck() {
		return mc.level != null;
	}

	protected void makePreRenderAdjustments(PoseStack matrix) {
		matrix.scale(10, 10, 10);
	}

	protected abstract void renderObject();

	protected abstract File getOutputFile();

	@Override
	public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
		if (determineScaleAndPosition()) {
			RenderUtil.clearRenderBuffer();
			renderObject();

			NativeImage image = extractSubRange(captureImage(), null);

			if (image != null)
				fileConsumer.accept(saveImage(image));
		}

		this.onClose();
		this.mc.setScreen(null);
	}

	protected void drawCurrentStatus(GuiGraphics guiGraphics) {
		int stringWidth = this.mc.font.width(this.currentStatus);

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, 100);
		guiGraphics.drawString(this.mc.font, this.currentStatus, (mc.getWindow().getGuiScaledWidth() - stringWidth) / 2f, 10, ColourUtil.WHITE, false);
		guiGraphics.pose().popPose();
	}

	protected NativeImage captureImage() {
		RenderTarget buffer = this.mc.getMainRenderTarget();
		NativeImage image = new NativeImage(buffer.width, buffer.height, true);

		RenderSystem.bindTexture(buffer.getColorTextureId());
		image.downloadTexture(0, false);
		image.flipY();

		return image;
	}

	@Nullable
	protected File saveImage(NativeImage image) {
		if (image == null)
			return null;

		File outputFile = getOutputFile();
		AtomicBoolean succeeded = new AtomicBoolean(true);

		Util.ioPool().execute(() -> {
			try (image) {
				if (outputFile.exists()) {
					outputFile.delete();
				}
				else {
					outputFile.getParentFile().mkdirs();
				}

				image.writeToFile(outputFile);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				succeeded.set(false);
			}
		});

		return succeeded.get() ? outputFile : null;
	}

	@Nullable
	protected final NativeImage extractSubRange(NativeImage source, @Nullable Vector4f bounds) {
		NativeImage output = new NativeImage(this.targetSize, this.targetSize, true);

		try (source) {
			if (bounds == null)
				bounds = getBoundsForRenderedImage(source);

			if (bounds == null) {
				WikiHelperCommand.error(this.commandSource, this.commandName, "Unable to detect isometric while attempting to trim down image size. Could be a bug, or you tried to render the image too small");

				return null;
			}


			int minX = (int)bounds.x();
			int minY = (int)bounds.y();
			int xOffset = (int)((this.targetSize - (bounds.z() - minX)) / 2f);
			int yOffset = (int)((this.targetSize - (bounds.w() - minY)) / 2f);

			if (xOffset < 0 || yOffset < 0)
				return null;

			for (int x = minX; x <= bounds.z(); x++) {
				for (int y = minY; y <= bounds.w(); y++) {
					output.setPixelRGBA(x - minX + xOffset, y - minY + yOffset, source.getPixelRGBA(x, y));
				}
			}
		}
		catch (Exception ex) {
			WikiHelperCommand.error(this.commandSource, this.commandName, "Encountered an error while trying to post-process an isometric rendering. Check the log for more details");
			ex.printStackTrace();

			return null;
		}

		return output;
	}

	protected final boolean determineScaleAndPosition() {
		float refScale = defaultRefScale;
		float avgHeight = 0;
		float avgWidth = 0;
		NativeImage capture;
		float count = 0;

		for (int i = 1; i <= 5; i++) {
			Vector4f bounds;
			boolean stopScaling = false;

			while (true) {
				this.scale = refScale;

				RenderUtil.clearRenderBuffer();
				renderObject();

				capture = captureImage();
				bounds = getBoundsForRenderedImage(capture);

				if (bounds == null) {
					if (refScale == this.defaultRefScale) {
						refScale = 50;

						continue;
					}

					WikiHelperCommand.error(this.commandSource, this.commandName, "Unable to detect rendered object after attempting to scale up. Report this as a bug.");
					capture.close();

					return false;
				}

				capture.close();

				if (bounds.x() > 0 && bounds.y() > 0 && bounds.z() < Math.min(this.windowWidth - 1, bounds.x() + this.targetSize) && bounds.w() < Math.min(this.windowHeight - 1, bounds.y() + this.targetSize))
					break;

				refScale -= 0.25f;

				if (refScale <= Math.pow(2, i - 1)) {
					stopScaling = true;
					break;
				}

				if (refScale <= 0) {
					WikiHelperCommand.error(this.commandSource, this.commandName, "Failed to properly capture an isometric rendering for this object. Report this as a bug.");

					return false;
				}
			}

			if (stopScaling)
				break;

			avgWidth += (bounds.z() - bounds.x()) / refScale;
			avgHeight += (bounds.w() - bounds.y()) / refScale;
			count++;
			refScale = (float)Math.pow(2, i);
		}

		this.scale = (this.targetSize - 1) / (Math.max(avgWidth, avgHeight) / count);

		while (true) {
			RenderUtil.clearRenderBuffer();
			renderObject();

			capture = captureImage();
			Vector4f renderBoundary = getBoundsForRenderedImage(capture);

			if (renderBoundary == null) {
				WikiHelperCommand.error(this.commandSource, this.commandName, "Unable to detect rendered object after attempting to scale up. Report this as a bug.");
				capture.close();

				return false;
			}

			capture.close();

			if (renderBoundary.x() > 0 && renderBoundary.y() > 0 && renderBoundary.z() < Math.min(this.windowWidth - 1, renderBoundary.x() + this.targetSize) && renderBoundary.w() < Math.min(this.windowHeight - 1, renderBoundary.y() + this.targetSize))
				break;

			this.scale *= 0.99f;

			if (this.scale <= 0.005f) {
				WikiHelperCommand.error(this.commandSource, this.commandName, "Failed to properly capture an isometric rendering for this object. Report this as a bug.");

				return false;
			}
		}

		return true;
	}

	@Nullable
	protected final Vector4f getBoundsForRenderedImage(NativeImage image) {
		int minX = this.windowWidth;
		int minY = this.windowHeight;
		int maxX = 0;
		int maxY = 0;

		try {
			for (int x = 0; x < this.windowWidth; x++) {
				for (int y = 0; y < this.windowHeight; y++) {
					if (image.getPixelRGBA(x, y) != 0) {
						minX = Math.min(x, minX);
						minY = Math.min(y, minY);
						maxX = Math.max(x, maxX);
						maxY = Math.max(y, maxY);
					}
				}
			}
		}
		catch (IllegalArgumentException ex) {
			minX = this.windowWidth;
			minY = this.windowHeight;
			maxX = 0;
			maxY = 0;
		}

		if (minX >= this.windowWidth - 1 && minY >= this.windowHeight - 1 && maxX == 0 && maxY == 0)
			return null;

		return new Vector4f(minX, minY, maxX, maxY);
	}

	protected final void withAlignedIsometricProjection(PoseStack matrix, Runnable renderOps) {
		matrix.pushPose();

		matrix.scale(this.scale, this.scale, 1);
		matrix.translate(this.minecraft.getWindow().getGuiScaledWidth() / 2f / this.scale, this.minecraft.getWindow().getGuiScaledHeight() / 2f / this.scale, 1050);
		RenderUtil.translateToIsometricView(matrix);
		makePreRenderAdjustments(matrix);
		matrix.mulPose(Axis.YP.rotationDegrees(this.rotationAdjust));
		matrix.translate(this.xAdjust, this.yAdjust, 0);

		renderOps.run();

		matrix.popPose();
	}

	public static void queuePrintTask(Supplier<? extends IsometricPrinterScreen> screenProvider) {
		Runnable task = () -> {
			IsometricPrinterScreen screen = screenProvider.get();

			if (!screen.preRenderingCheck())
				return;

			Minecraft.getInstance().setScreen(screen);
		};

		if (RenderSystem.isOnRenderThread()) {
			task.run();
		}
		else {
			RenderSystem.recordRenderCall(task::run);
		}
	}

	public static <T> void registerAdapter(Class<T> adapterClass, IsoRenderAdapter<? extends T> adapter) {
		adapters.put(adapterClass, adapter);
	}

	public static void registerEntityAdapter(IsoRenderAdapter<? extends Entity> adapter) {
		registerAdapter(Entity.class, adapter);
	}

	public static void registerBlockAdapter(IsoRenderAdapter<? extends BlockState> adapter) {
		registerAdapter(BlockState.class, adapter);
	}

	protected final <T> List<IsoRenderAdapter<T>> getApplicableAdapters(Class<T> renderingClass, T renderingObject) {
		return adapters.get(renderingClass).stream().map(adapter -> (IsoRenderAdapter<T>)adapter).filter(adapter -> adapter.willHandle(renderingObject)).collect(Collectors.toList());
	}
}
