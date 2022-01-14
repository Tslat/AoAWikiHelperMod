package net.tslat.aoawikihelpermod.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.tslat.aoa3.util.ColourUtil;
import net.tslat.aoa3.util.RenderUtil;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

public class IsoRenderScreen extends Screen {
	private final Entity cachedEntity;
	private float scale = 50f;
	private float xAdjust = 0;
	private float yAdjust = 0;

	public IsoRenderScreen() {
		super(new StringTextComponent("Isometric Rendering"));

		this.cachedEntity = EntityType.SHEEP.create(Minecraft.getInstance().level);
		this.cachedEntity.tickCount = 0;
		this.cachedEntity.yRot = 0;
		this.cachedEntity.xRot = 0;
		this.cachedEntity.xRotO = 0;
		this.cachedEntity.yRotO = 0;
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrix);

		String name = renderEntity(matrix);

		if (checkIsoBoundsAndPosition()) {
			capturePicture(name);
			Minecraft.getInstance().setScreen(null);
		}
		else {
			adjustScaleAndPosition();
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void capturePicture(String fileName) {
		try {
			NativeImage image = ScreenShotHelper.takeScreenshot(1920, 1080, Minecraft.getInstance().getMainRenderTarget());

			Util.ioPool().execute(() -> {
				try {
					image.writeToFile(PrintHelper.configDir.toPath().resolve(fileName + ".png").toFile());
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean checkIsoBoundsAndPosition() {
		return false;
	}

	private void adjustScaleAndPosition() {
		Framebuffer buffer = Minecraft.getInstance().getMainRenderTarget();
		NativeImage image = new NativeImage(buffer.width, buffer.height, false);
		int minX = 0;
		int maxX = 0;
		int minY = 0;
		int maxY = 0;

		RenderSystem.bindTexture(buffer.getColorTextureId());
		image.downloadTexture(0, true);
		image.flipY();

		for (int x = 0; x < buffer.width; x++) {
			for (int y = 0; y < buffer.height; y++) {
				if (x == 2 && minX != 1) {
					xAdjust -= 1;

					return;
				}



				int colour = image.getPixelRGBA(x, y);

				if (colour != 0) {
					if (x == 0) {
						xAdjust += 1;

						return;
					}

					if (y == 0) {
						yAdjust += 1;

						return;
					}

					if (minX == 0 || x < minX)
						minX = x;

					if (minY == 0 || y < minY)
						minY = y;

					if (x - minX > 300 || y - minY > 300) {
						scale -= 0.25f;

						return;
					}
				}
			}
		}
	}

	private String renderEntity(MatrixStack matrix) {
		matrix.pushPose();
		matrix.translate(width / 2f, height / 2f, 1050.0F);
		matrix.scale(1.0F, 1.0F, -1.0F);

		Minecraft mc = Minecraft.getInstance();

		matrix.translate(0, 0, 1000.0D);
		matrix.scale(scale, scale, scale);
		matrix.translate(xAdjust, yAdjust, 0);

		matrix.mulPose(Vector3f.XP.rotationDegrees(215f));
		matrix.mulPose(Vector3f.YP.rotationDegrees(-45));

		EntityRendererManager renderManager = mc.getEntityRenderDispatcher();
		IRenderTypeBuffer.Impl renderBuffer = mc.renderBuffers().bufferSource();

		renderManager.setRenderShadow(false);
		renderManager.render(cachedEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrix, renderBuffer, 15728880);
		renderBuffer.endBatch();
		renderManager.setRenderShadow(true);
		matrix.popPose();

		return cachedEntity.getDisplayName().getString();
	}

	@Override
	public void renderBackground(MatrixStack matrixStack, int vOffset) {
		RenderUtil.drawColouredBox(matrixStack, 0, 0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), ColourUtil.RGBA(0, 0, 0, 255));

		MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this, matrixStack));
	}
}
