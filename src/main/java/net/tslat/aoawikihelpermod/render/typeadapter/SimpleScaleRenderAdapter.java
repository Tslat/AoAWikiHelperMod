package net.tslat.aoawikihelpermod.render.typeadapter;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;

import java.util.function.Predicate;

public class SimpleScaleRenderAdapter<T> implements IsoRenderAdapter<T> {
	private final Predicate<T> predicate;
	private final float scale;

	public SimpleScaleRenderAdapter(Predicate<T> predicate, float scale) {
		this.predicate = predicate;
		this.scale = scale;
	}

	@Override
	public boolean willHandle(T renderingObject) {
		return this.predicate.test(renderingObject);
	}

	@Override
	public void makePreRenderAdjustments(T renderingObject, MatrixStack matrix) {
		matrix.scale(this.scale, this.scale, this.scale);
	}

	@Override
	public boolean handleCustomRender(T renderingObject, MatrixStack matrix, IRenderTypeBuffer buffer) {
		return false;
	}
}
