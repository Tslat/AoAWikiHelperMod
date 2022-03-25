package net.tslat.aoawikihelpermod.render.typeadapter;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public interface IsoRenderAdapter<T> {
	boolean willHandle(T renderingObject);

	void makePreRenderAdjustments(T renderingObject, MatrixStack matrix);

	boolean handleCustomRender(T renderingObject, MatrixStack matrix, IRenderTypeBuffer buffer);
}
