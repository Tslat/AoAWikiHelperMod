package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.render.typeadapter.IsoRenderAdapter;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printers.PrintHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

public class EntityIsoPrinter extends IsometricPrinterScreen {
	protected final ArrayList<IsoRenderAdapter<Entity>> adapters = new ArrayList<>(1);
	protected ResourceLocation entityId;
	protected Entity cachedEntity;

	public EntityIsoPrinter(ResourceLocation entityId, int imageSize, float rotationAdjust, CommandSource commandSource, String commandName, Consumer<File> fileConsumer) {
		super(imageSize, rotationAdjust, commandSource, commandName, fileConsumer);

		this.entityId = entityId;
	}

	@Override
	protected boolean preRenderingCheck() {
		if (!super.preRenderingCheck())
			return false;

		if (!ForgeRegistries.ENTITIES.containsKey(this.entityId)) {
			WikiHelperCommand.error(this.commandSource, this.commandName, "Entity by ID: '" + this.entityId + "' does not appear to exist.");

			return false;
		}

		this.cachedEntity = ForgeRegistries.ENTITIES.getValue(this.entityId).create(FakeWorld.INSTANCE);

		if (this.cachedEntity == null) {
			WikiHelperCommand.error(this.commandSource, this.commandName, "Unable to instantiate entity of type: '" + this.entityId + "'. Could be an invalid entity or a bug.");

			return false;
		}

		this.cachedEntity.tickCount = 0;
		this.cachedEntity.xRot = 0;
		this.cachedEntity.xRotO = 0;
		this.cachedEntity.yRot = 0;
		this.cachedEntity.yRotO = 0;

		if (cachedEntity instanceof LivingEntity) {
			LivingEntity living = (LivingEntity)cachedEntity;

			living.yBodyRot = 0;
			living.yBodyRotO = 0;
			living.yHeadRot = 0;
			living.yHeadRotO = 0;
		}

		adapters.addAll(getApplicableAdapters(Entity.class, this.cachedEntity));

		return true;
	}

	@Override
	protected void renderObject() {
		MatrixStack matrix = new MatrixStack();

		withAlignedIsometricProjection(matrix, () -> {
			EntityRendererManager renderManager = this.minecraft.getEntityRenderDispatcher();
			IRenderTypeBuffer.Impl renderBuffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());

			renderManager.setRenderShadow(false);

			try {
				if (!customRenderEntity(matrix, renderBuffer))
					renderManager.render(this.cachedEntity, 0, 0, 0, 0, 1, matrix, renderBuffer, 15728880);
			}
			catch (Exception ex) {
				WikiHelperCommand.error(this.commandSource, this.commandName, "Encountered an error while rendering the entity. Likely a non-standard entity of some sort. Check the log for more details.");
				ex.printStackTrace();
			}

			renderBuffer.endBatch();
			renderManager.setRenderShadow(true);
		});
	}

	@Override
	protected void makePreRenderAdjustments(MatrixStack matrix) {
		matrix.translate(this.cachedEntity.getBbWidth() / 3.5f, -this.cachedEntity.getBbHeight() / 2f, 0);

		for (IsoRenderAdapter<Entity> adapter : this.adapters) {
			adapter.makePreRenderAdjustments(this.cachedEntity, matrix);
		}
	}

	@Override
	protected File getOutputFile() {
		return PrintHelper.configDir.toPath().resolve("Entity Renders").resolve(cachedEntity.getType().getRegistryName().getNamespace()).resolve(cachedEntity.getDisplayName().getString() + " - " + targetSize + "px.png").toFile();
	}

	protected boolean customRenderEntity(MatrixStack matrix, IRenderTypeBuffer renderBuffer) {
		for (IsoRenderAdapter<Entity> adapter : this.adapters) {
			if (adapter.handleCustomRender(this.cachedEntity, matrix, renderBuffer))
				return true;
		}

		return false;
	}
}
