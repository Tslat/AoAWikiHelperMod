package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.render.typeadapter.IsoRenderAdapter;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

public class ItemIsoPrinter extends IsometricPrinterScreen {
	protected final ArrayList<IsoRenderAdapter<ItemStack>> adapters = new ArrayList<>(1);
	protected final ItemStack stack;
	protected final int spriteSize;

	public ItemIsoPrinter(ItemStack stack, int imageSize, CommandSourceStack commandSource, String commandName, Consumer<File> fileConsumer) {
		super(determineImageSize(stack, imageSize), 0, commandSource, commandName, fileConsumer);

		this.spriteSize = determineImageSize(stack, -1);
		this.stack = stack;
		this.defaultRefScale = 10f;
		this.currentStatus = "Waiting for frame cycle to reset...";
	}

	@Override
	protected boolean preRenderingCheck() {
		if (super.preRenderingCheck()) {
			adapters.addAll(getApplicableAdapters(ItemStack.class, this.stack));

			return true;
		}

		return false;
	}

	@Override
	protected File getOutputFile() {
		return PrintHelper.configDir.toPath().resolve("Item Renders").resolve(stack.getItem().getRegistryName().getNamespace()).resolve(stack.getItem().getName(stack).getString() + " - " + targetSize + "px.png").toFile();
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (isOnFirstFrame()) {
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
	}

	@Override
	protected void renderObject() {
		PoseStack matrix = new PoseStack();

		matrix.pushPose();
		matrix.scale(this.scale, this.scale, 1);
		matrix.translate(this.minecraft.getWindow().getGuiScaledWidth() / 2f / this.scale, this.minecraft.getWindow().getGuiScaledHeight() / 2f / this.scale, 1050);
		matrix.scale(1, -1, 1);
		matrix.translate(0, 0, -(100 + itemRenderer.blitOffset));
		makePreRenderAdjustments(matrix);
		matrix.translate(this.xAdjust, this.yAdjust, 0);

		RenderUtil.setupFakeGuiLighting();
		ItemRenderer itemRenderer = this.minecraft.getItemRenderer();
		MultiBufferSource.BufferSource renderBuffer = mc.renderBuffers().bufferSource();

		try {
			if (!customRenderStack(itemRenderer, matrix, renderBuffer))
				renderItemStack(itemRenderer, matrix, renderBuffer, this.stack);
		}
		catch (Exception ex) {
			WikiHelperCommand.error(this.commandSource, this.commandName, "Encountered an error while rendering the item. Likely a non-standard item. Likely of some sort. Check the log for more details.");
			ex.printStackTrace();
		}

		renderBuffer.endBatch();

		matrix.popPose();
	}

	public static int determineImageSize(ItemStack stack, int defaultImageSize) {
		if (defaultImageSize != -1)
			return defaultImageSize;

		if (stack.getItem() instanceof BlockItem)
			return 32;

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		BakedModel model = itemRenderer.getModel(stack, null, null, 0);

		int largestFrameSize = 0;
		Random rand = new Random();

		for (Direction face : Direction.values()) {
			rand.setSeed(42L);

			for (BakedQuad quad : model.getQuads(null, face, rand, EmptyModelData.INSTANCE)) {
				largestFrameSize = Math.max(largestFrameSize, Math.max(quad.getSprite().getHeight(), quad.getSprite().getWidth()));
			}
		}

		rand.setSeed(42L);

		for (BakedQuad quad : model.getQuads(null, null, rand, EmptyModelData.INSTANCE)) {
			largestFrameSize = Math.max(largestFrameSize, Math.max(quad.getSprite().getHeight(), quad.getSprite().getWidth()));
		}

		return largestFrameSize > 0 ? largestFrameSize : 32;
	}

	@Override
	protected void makePreRenderAdjustments(PoseStack matrix) {
		for (IsoRenderAdapter<ItemStack> adapter : this.adapters) {
			adapter.makePreRenderAdjustments(this.stack, matrix);
		}
	}

	protected boolean customRenderStack(ItemRenderer itemRenderer, PoseStack matrix, MultiBufferSource renderBuffer) {
		for (IsoRenderAdapter<ItemStack> adapter : this.adapters) {
			if (adapter.handleCustomRender(this.stack, matrix, renderBuffer))
				return true;
		}

		return false;
	}

	protected void renderItemStack(ItemRenderer itemRenderer, PoseStack matrix, MultiBufferSource.BufferSource renderBuffer, ItemStack stack) {
		Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1, 1, 1, 1);

		PoseStack modelViewPose = RenderSystem.getModelViewStack();
		modelViewPose.pushPose();
		modelViewPose.translate(0, 0, (100f + itemRenderer.blitOffset));
		modelViewPose.translate(8, 8, 0);
		modelViewPose.scale(1, -1, 1);
		modelViewPose.scale(16, 16, 16);

		RenderSystem.applyModelViewMatrix();

		BakedModel model = itemRenderer.getModel(stack, null, null, 0);
		boolean changeLighting = !model.usesBlockLight();

		if (changeLighting)
			Lighting.setupForFlatItems();

		itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, matrix, renderBuffer, 15728880, OverlayTexture.NO_OVERLAY, model);
		RenderSystem.enableDepthTest();

		if (changeLighting)
			Lighting.setupFor3DItems();

		modelViewPose.popPose();
		RenderSystem.applyModelViewMatrix();
	}

	private boolean isOnFirstFrame() {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		Random rand = new Random();
		int highestFrameTime = 0;
		BakedModel model = itemRenderer.getModel(this.stack, null, null, 0);

		for (Direction face : Direction.values()) {
			rand.setSeed(42L);

			for (BakedQuad quad : model.getQuads(null, face, rand, EmptyModelData.INSTANCE)) {

				if (quad.getSprite().animatedTexture != null && (quad.getSprite().animatedTexture.frame != 0 || quad.getSprite().animatedTexture.subFrame != 0))
					return false;
			}
		}

		rand.setSeed(42L);

		for (BakedQuad quad : model.getQuads(null, null, rand, EmptyModelData.INSTANCE)) {
			if (quad.getSprite().animatedTexture != null && (quad.getSprite().animatedTexture.frame != 0 || quad.getSprite().animatedTexture.subFrame != 0))
				return false;
		}

		return true;
	}
}
