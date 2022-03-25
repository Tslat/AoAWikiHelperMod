package net.tslat.aoawikihelpermod.render.typeadapter;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;

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
	public void makePreRenderAdjustments(T renderingObject, MatrixStack matrix) {
		matrix.mulPose(this.axis.rotationDegrees(this.rotation));
	}

	@Override
	public boolean handleCustomRender(T renderingObject, MatrixStack matrix, IRenderTypeBuffer buffer) {
		return false;
	}
}
