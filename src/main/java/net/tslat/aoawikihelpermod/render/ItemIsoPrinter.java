package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.render.typeadapter.IsoRenderAdapter;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ItemIsoPrinter extends IsometricPrinterScreen {
	protected final ArrayList<IsoRenderAdapter<ItemStack>> adapters = new ArrayList<>(1);
	protected final ItemStack stack;
	protected final int spriteSize;
	protected final boolean renderIngameModel;

	public ItemIsoPrinter(ItemStack stack, int imageSize, boolean renderIngameModel, CommandSourceStack commandSource, String commandName, Consumer<File> fileConsumer) {
		super(determineImageSize(stack, imageSize), 0, commandSource, commandName, fileConsumer);

		this.spriteSize = determineImageSize(stack, -1);
		this.stack = stack;
		this.renderIngameModel = renderIngameModel;
		this.defaultRefScale = 10f;
		this.currentStatus = "Waiting for frame cycle to reset...";

		if (!this.renderIngameModel)
			this.scale = 1;
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
		return PrintHelper.configDir.toPath().resolve("Item Renders").resolve(ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace()).resolve(stack.getItem().getName(stack).getString() + " - " + targetSize + "px.png").toFile();
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (isOnFirstFrame()) {
			if (!this.renderIngameModel || determineScaleAndPosition()) {
				RenderUtil.clearRenderBuffer();
				renderObject();

				NativeImage image = extractSubRange(captureImage(), renderIngameModel ? null : new Vector4f(0, 0, this.targetSize - 1, this.targetSize - 1));

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

		if (this.renderIngameModel) {
			matrix.scale(this.scale, this.scale, 1);
			matrix.translate(this.minecraft.getWindow().getGuiScaledWidth() / 2f / this.scale, this.mc.getWindow().getGuiScaledHeight() / 2f / this.scale, 1050);
			matrix.scale(1, -1, 1);
			matrix.translate(0, 0, -(100 + itemRenderer.blitOffset));
			makePreRenderAdjustments(matrix);
			matrix.translate(this.xAdjust, this.yAdjust, 0);

			RenderUtil.setupFakeGuiLighting();
		}

		ItemRenderer itemRenderer = this.minecraft.getItemRenderer();
		MultiBufferSource.BufferSource renderBuffer = this.mc.renderBuffers().bufferSource();

		try {
			if (!this.renderIngameModel) {
				PoseStack modelViewPoseStack = RenderSystem.getModelViewStack();
				int guiScale = mc.options.guiScale().get();

				modelViewPoseStack.pushPose();

				if (guiScale != 0)
					modelViewPoseStack.scale(4f / guiScale, 4f / guiScale, 4f / guiScale);

				if (this.stack.getItem() instanceof BlockItem || this.spriteSize <= 32) // Literally no clue why this fixes things but I'm tired and it fixes stuff so deal with it
					modelViewPoseStack.scale(0.5f, 0.5f, 0.5f);

				if (this.targetSize != this.spriteSize) {
					float relativeScale = this.targetSize / (float)this.spriteSize;

					modelViewPoseStack.scale(relativeScale, relativeScale, relativeScale);
				}

				itemRenderer.renderAndDecorateItem(this.mc.player, this.stack, 0, 0, 100);
				modelViewPoseStack.popPose();
			}
			else if (!customRenderStack(itemRenderer, matrix, renderBuffer)) {
				renderModelledItemStack(itemRenderer, matrix, renderBuffer, this.stack);
			}
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
		RandomSource rand = RandomSource.create();

		for (Direction face : Direction.values()) {
			rand.setSeed(42L);

			for (BakedQuad quad : model.getQuads(null, face, rand, ModelData.EMPTY, null)) {
				largestFrameSize = Math.max(largestFrameSize, Math.max(quad.getSprite().getHeight(), quad.getSprite().getWidth()));
			}
		}

		rand.setSeed(42L);

		for (BakedQuad quad : model.getQuads(null, null, rand, ModelData.EMPTY, null)) {
			largestFrameSize = Math.max(largestFrameSize, Math.max(quad.getSprite().getHeight(), quad.getSprite().getWidth()));
		}

		return (largestFrameSize > 0 ? largestFrameSize : 16) * 2;
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

	protected void renderModelledItemStack(ItemRenderer itemRenderer, PoseStack matrix, MultiBufferSource.BufferSource renderBuffer, ItemStack stack) {
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
		matrix.mulPose(Vector3f.XP.rotationDegrees(0.0000001f));

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
		RandomSource rand = RandomSource.create();
		int highestFrameTime = 0;
		BakedModel model = itemRenderer.getModel(this.stack, null, null, 0);

		for (Direction face : Direction.values()) {
			rand.setSeed(42L);

			for (BakedQuad quad : model.getQuads(null, face, rand, ModelData.EMPTY, null)) {

				if (quad.getSprite().animatedTexture != null && (quad.getSprite().animatedTexture.frame != 0 || quad.getSprite().animatedTexture.subFrame != 0))
					return false;
			}
		}

		rand.setSeed(42L);

		for (BakedQuad quad : model.getQuads(null, null, rand, ModelData.EMPTY, null)) {
			if (quad.getSprite().animatedTexture != null && (quad.getSprite().animatedTexture.frame != 0 || quad.getSprite().animatedTexture.subFrame != 0))
				return false;
		}

		return true;
	}
}
