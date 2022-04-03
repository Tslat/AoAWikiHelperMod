package net.tslat.aoawikihelpermod.render.typeadapter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.function.Predicate;

public class SimpleRotationRenderAdapter<T> implements IsoRenderAdapter<T> {
	private final Predicate<T> predicate;
	private final Vector3f axis;
	private final float rotation;

	public SimpleRotationRenderAdapter(Predicate<T> predicate, Vector3f axis, float amount) {
		this.predicate = predicate;
		this.axis = axis;
		this.rotation = amount;
	}

	@Override
	public boolean willHandle(T renderingObject) {
		return this.predicate.test(renderingObject);
	}

	@Override
	public void makePreRenderAdjustments(T renderingObject, PoseStack matrix) {
		matrix.mulPose(this.axis.rotationDegrees(this.rotation));
	}

	@Override
	public boolean handleCustomRender(T renderingObject, PoseStack matrix, MultiBufferSource buffer) {
		return false;
	}
}
