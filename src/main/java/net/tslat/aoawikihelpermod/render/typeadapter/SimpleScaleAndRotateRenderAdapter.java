package net.tslat.aoawikihelpermod.render.typeadapter;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;

import java.util.function.Predicate;

public class SimpleScaleAndRotateRenderAdapter<T> implements IsoRenderAdapter<T> {
	private final Predicate<T> predicate;
	private final float scale;
	private final Vector3f axis;
	private final float rotation;

	public SimpleScaleAndRotateRenderAdapter(Predicate<T> predicate, float scale, Vector3f axis, float amount) {
		this.predicate = predicate;
		this.scale = scale;
		this.axis = axis;
		this.rotation = amount;
	}

	@Override
	public boolean willHandle(T renderingObject) {
		return this.predicate.test(renderingObject);
	}

	@Override
	public void makePreRenderAdjustments(T renderingObject, MatrixStack matrix) {
		matrix.scale(this.scale, this.scale, this.scale);
		matrix.mulPose(this.axis.rotationDegrees(this.rotation));
	}

	@Override
	public boolean handleCustomRender(T renderingObject, MatrixStack matrix, IRenderTypeBuffer buffer) {
		return false;
	}
}
