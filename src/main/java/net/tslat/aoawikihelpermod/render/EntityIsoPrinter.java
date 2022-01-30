package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class EntityIsoPrinter extends Screen {
	private final Consumer<File> fileConsumer;
	private final Entity entityToRender;
	private final int targetSize;
	private final int windowWidth;
	private final int windowHeight;

	private float scale = 10f;
	private float xAdjust = 0;
	private float yAdjust = 0;

	private EntityIsoPrinter(Entity entity, int imageSize, Consumer<File> fileConsumer) {
		super(new StringTextComponent("Isometric Rendering"));

		this.fileConsumer = fileConsumer;
		this.entityToRender = entity;
		this.entityToRender.tickCount = 0;
		this.entityToRender.xRot = 0;
		this.entityToRender.xRotO = 0;
		this.entityToRender.yRot = 0;
		this.entityToRender.yRotO = 0;

		if (entityToRender instanceof LivingEntity) {
			LivingEntity living = (LivingEntity)entityToRender;

			living.yBodyRot = 0;
			living.yBodyRotO = 0;
			living.yHeadRot = 0;
			living.yHeadRotO = 0;
		}

		MainWindow window = Minecraft.getInstance().getWindow();
		this.windowWidth = window.getScreenWidth();
		this.windowHeight = window.getScreenHeight();

		if (imageSize == 0)
			imageSize = Math.min(this.windowWidth, this.windowHeight);

		imageSize = Math.min(Math.min(this.windowWidth, this.windowHeight), imageSize);

		this.targetSize = imageSize;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public static void queuePrintEntity(Runnable task) {
		if (RenderSystem.isOnRenderThread()) {
			task.run();
		}
		else {
			RenderSystem.recordRenderCall(task::run);
		}
	}

	public static void printEntity(ResourceLocation entityId, int imageSize, Consumer<File> fileConsumer) {
		if (!ForgeRegistries.ENTITIES.containsKey(entityId))
			return;

		if (Minecraft.getInstance().level == null)
			return;

		Entity instance = ForgeRegistries.ENTITIES.getValue(entityId).create(Minecraft.getInstance().level);

		if (instance == null) {
			AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Unable to instantiate entity of type: '" + entityId + "', skipping.");

			fileConsumer.accept(null);
			return;
		}

		Minecraft.getInstance().setScreen(new EntityIsoPrinter(instance, imageSize, fileConsumer));
	}

	@Override
	public void render(MatrixStack matrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
		determineScaleAndPosition();

		clearRenderBuffer();
		renderEntity();

		NativeImage image = extractSubRange(captureImage());

		fileConsumer.accept(saveImage(image));
		Minecraft.getInstance().setScreen(null);
	}

	private NativeImage captureImage() {
		Framebuffer buffer = Minecraft.getInstance().getMainRenderTarget();
		NativeImage image = new NativeImage(buffer.width, buffer.height, true);

		RenderSystem.bindTexture(buffer.getColorTextureId());
		image.downloadTexture(0, false);
		image.flipY();

		return image;
	}

	@Nullable
	private NativeImage extractSubRange(NativeImage source) {
		NativeImage output = new NativeImage(targetSize, targetSize, true);

		try {
			Vector4f bounds = getBoundsForRenderedImage(source);

			if (bounds == null) {
				AoAWikiHelperMod.LOGGER.log(Level.WARN, "Unable to detect rendered entity, safely exiting rendering operation");

				return null;
			}

			int minX = (int)bounds.x();
			int minY = (int)bounds.y();
			int xOffset = (int)((targetSize - (bounds.z() - minX)) / 2f);
			int yOffset = (int)((targetSize - (bounds.w() - minY)) / 2f);

			for (int x = minX; x < bounds.z(); x++) {
				for (int y = minY; y < bounds.w(); y++) {
					output.setPixelRGBA(x - minX + xOffset, y - minY + yOffset, source.getPixelRGBA(x, y));
				}
			}
		}
		catch (Exception ex) {
			AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Encountered an error while trying to process an entity Isometric: '" + entityToRender.getType().getRegistryName() + "' (" + targetSize + "px)");
			ex.printStackTrace();
			source.close();

			return null;
		}

		source.close();

		return output;
	}

	@Nullable
	private File saveImage(NativeImage image) {
		if (image == null)
			return null;

		File outputFile = PrintHelper.configDir.toPath().resolve("Entity Renders").resolve(entityToRender.getType().getRegistryName().getNamespace()).resolve(entityToRender.getDisplayName().getString() + " - " + targetSize + "px.png").toFile();
		AtomicBoolean succeeded = new AtomicBoolean(true);

		Util.ioPool().execute(() -> {
			try {
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
			finally {
				image.close();
			}
		});

		return succeeded.get() ? outputFile : null;
	}

	private void determineScaleAndPosition() {
		float firstScale = 10f;
		float secondScale = 20f;
		NativeImage capture;
		Vector4f firstBounds;
		Vector4f secondBounds;

		while (true) {
			scale = firstScale;

			clearRenderBuffer();
			renderEntity();

			capture = captureImage();
			firstBounds = getBoundsForRenderedImage(capture);

			if (firstBounds == null) {
				AoAWikiHelperMod.LOGGER.log(Level.WARN, "Unable to detect rendered entity, safely exiting rendering operation");
				capture.close();

				return;
			}

			capture.close();

			if (firstBounds.x() > 0 && firstBounds.y() > 0)
				break;

			firstScale -= 0.5f;
		}

		while (true) {
			scale = secondScale;
			clearRenderBuffer();
			renderEntity();

			capture = captureImage();
			secondBounds = getBoundsForRenderedImage(capture);

			if (secondBounds == null) {
				AoAWikiHelperMod.LOGGER.log(Level.WARN, "Unable to detect rendered entity, safely exiting rendering operation");
				capture.close();

				return;
			}
			capture.close();

			if (secondBounds.x() > 0 && secondBounds.y() > 0)
				break;

			secondScale -= 0.5f;
		}

		float perScaleWidth = (((firstBounds.z() - firstBounds.x()) / firstScale) + ((secondBounds.z() - secondBounds.x()) / secondScale)) / 2f;
		float perScaleHeight = (((firstBounds.w() - firstBounds.y()) / firstScale) + ((secondBounds.w() - secondBounds.y()) / secondScale)) / 2f;

		if (perScaleHeight > perScaleWidth) {
			scale = (float)Math.floor((targetSize - 1) / perScaleHeight);
		}
		else {
			scale = (float)Math.floor((targetSize - 1) / perScaleWidth);
		}

		float translatedScale = minecraft.getWindow().getGuiScaledWidth() / (float)this.windowWidth / 2f;
		xAdjust = translatedScale;

		while (true) {
			yAdjust = translatedScale - perScaleHeight * translatedScale;

			clearRenderBuffer();
			renderEntity();

			capture = captureImage();
			Vector4f thirdBounds = getBoundsForRenderedImage(capture);

			if (thirdBounds == null) {
				AoAWikiHelperMod.LOGGER.log(Level.WARN, "Unable to detect rendered entity, safely exiting rendering operation");
				capture.close();

				return;
			}

			capture.close();

			if (thirdBounds.x() > 0 && thirdBounds.y() > 0 && thirdBounds.z() < thirdBounds.x() + targetSize && thirdBounds.w() < thirdBounds.y() + targetSize)
				break;

			scale -= 0.5f;
		}
	}

	@Nullable
	private Vector4f getBoundsForRenderedImage(NativeImage image) {
		int minX = windowWidth;
		int minY = windowHeight;
		int maxX = 0;
		int maxY = 0;

		for (int x = 0; x < windowWidth; x++) {
			for (int y = 0; y < windowHeight; y++) {
				if (image.getPixelRGBA(x, y) != 0) {
					minX = Math.min(x, minX);
					minY = Math.min(y, minY);
					maxX = Math.max(x, maxX);
					maxY = Math.max(y, maxY);
				}
			}
		}

		if (minX == windowWidth && minY == windowHeight && maxX == 0 && maxY == 0)
			return null;

		return new Vector4f(minX, minY, maxX, maxY);
	}

	private void clearRenderBuffer() {
		RenderSystem.clearColor(0, 0, 0, 0);
		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
	}

	private void renderEntity() {
		MatrixStack matrix = new MatrixStack();

		RenderSystem.pushMatrix();
		matrix.pushPose();

		matrix.translate(minecraft.getWindow().getGuiScaledWidth() / 2f, minecraft.getWindow().getGuiScaledHeight() / 2f, 1050);
		matrix.scale(1, 1, -1);
		matrix.translate(0, 0, 1000);
		matrix.mulPose(Vector3f.XP.rotationDegrees(215f));
		matrix.mulPose(Vector3f.YP.rotationDegrees(-45));
		matrix.scale(scale, scale, scale);
		matrix.translate(xAdjust, yAdjust, 0);

		Minecraft mc = Minecraft.getInstance();
		EntityRendererManager renderManager = mc.getEntityRenderDispatcher();
		IRenderTypeBuffer.Impl renderBuffer = mc.renderBuffers().bufferSource();

		renderManager.setRenderShadow(false);

		try {
			renderManager.render(entityToRender, 0, 0, 0, 0, 1, matrix, renderBuffer, 15728880);
		}
		catch (Exception ex) {
			AoAWikiHelperMod.LOGGER.log(Level.ERROR, "Error while rendering entity. Likely a non-standard entity of some sort.");
			ex.printStackTrace();
		}

		renderBuffer.endBatch();
		renderManager.setRenderShadow(true);

		matrix.popPose();
		RenderSystem.popMatrix();
	}
}
