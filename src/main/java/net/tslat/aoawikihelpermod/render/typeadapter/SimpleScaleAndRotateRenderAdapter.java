package net.tslat.aoawikihelpermod.render.typeadapter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.function.Predicate;

public class SimpleScaleAndRotateRenderAdapter<T> implements IsoRenderAdapter<T> {
	private final Predicate<T> predicate;
	private final float scale;
	private final Axis axis;
	private final float rotation;

	public SimpleScaleAndRotateRenderAdapter(Predicate<T> predicate, float scale, Axis axis, float amount) {
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
	public void makePreRenderAdjustments(T renderingObject, PoseStack matrix) {
		matrix.scale(this.scale, this.scale, this.scale);
		matrix.mulPose(this.axis.rotationDegrees(this.rotation));
	}

	@Override
	public boolean handleCustomRender(T renderingObject, PoseStack matrix, MultiBufferSource buffer) {
		return false;
	}
}
