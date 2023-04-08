package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoawikihelpermod.command.WikiHelperCommand;
import net.tslat.aoawikihelpermod.render.typeadapter.IsoRenderAdapter;
import net.tslat.aoawikihelpermod.util.fakeworld.FakeWorld;
import net.tslat.aoawikihelpermod.util.printer.PrintHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

public class EntityIsoPrinter extends IsometricPrinterScreen {
	protected final ArrayList<IsoRenderAdapter<Entity>> adapters = new ArrayList<>(1);
	protected ResourceLocation entityId;
	protected CompoundTag nbt;
	protected Entity cachedEntity;

	public EntityIsoPrinter(ResourceLocation entityId, @Nullable CompoundTag nbt, int imageSize, float rotationAdjust, CommandSourceStack commandSource, String commandName, Consumer<File> fileConsumer) {
		super(imageSize, rotationAdjust, commandSource, commandName, fileConsumer);

		this.entityId = entityId;
		this.nbt = nbt;

		if (this.nbt != null)
			this.nbt.putString("id", entityId.toString());
	}

	@Override
	protected boolean preRenderingCheck() {
		if (!super.preRenderingCheck())
			return false;

		if (!ForgeRegistries.ENTITY_TYPES.containsKey(this.entityId)) {
			WikiHelperCommand.error(this.commandSource, this.commandName, "Entity by ID: '" + this.entityId + "' does not appear to exist.");

			return false;
		}

		this.cachedEntity = this.nbt == null ? ForgeRegistries.ENTITY_TYPES.getValue(this.entityId).create(FakeWorld.INSTANCE.get()) : EntityType.loadEntityRecursive(this.nbt, FakeWorld.INSTANCE.get(), entity -> entity);

		if (this.cachedEntity == null) {
			WikiHelperCommand.error(this.commandSource, this.commandName, "Unable to instantiate entity of type: '" + this.entityId + "'. Could be an invalid entity or a bug.");

			return false;
		}

		this.cachedEntity.tickCount = 0;
		this.cachedEntity.setXRot(0);
		this.cachedEntity.xRotO = 0;
		this.cachedEntity.setYRot(0);
		this.cachedEntity.yRotO = 0;

		if (cachedEntity instanceof LivingEntity living) {
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
		PoseStack matrix = new PoseStack();

		withAlignedIsometricProjection(matrix, () -> {
			EntityRenderDispatcher renderManager = this.minecraft.getEntityRenderDispatcher();
			MultiBufferSource.BufferSource renderBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

			RenderUtil.setupFakeGuiLighting();
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
			Lighting.setupFor3DItems();
		});
	}

	@Override
	protected void makePreRenderAdjustments(PoseStack matrix) {
		matrix.translate(this.cachedEntity.getBbWidth() / 3.5f, -this.cachedEntity.getBbHeight() / 2f, 0);

		for (IsoRenderAdapter<Entity> adapter : this.adapters) {
			adapter.makePreRenderAdjustments(this.cachedEntity, matrix);
		}
	}

	@Override
	protected File getOutputFile() {
		return PrintHelper.configDir.toPath().resolve("Entity Renders").resolve(ForgeRegistries.ENTITY_TYPES.getKey(cachedEntity.getType()).getNamespace()).resolve(cachedEntity.getDisplayName().getString() + " - " + targetSize + "px.png").toFile();
	}

	protected boolean customRenderEntity(PoseStack matrix, MultiBufferSource.BufferSource renderBuffer) {
		for (IsoRenderAdapter<Entity> adapter : this.adapters) {
			if (adapter.handleCustomRender(this.cachedEntity, matrix, renderBuffer))
				return true;
		}

		return false;
	}
}
