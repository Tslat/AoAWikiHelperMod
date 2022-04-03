package net.tslat.aoawikihelpermod.render.typeadapter;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface IsoRenderAdapter<T> {
	boolean willHandle(T renderingObject);

	void makePreRenderAdjustments(T renderingObject, PoseStack matrix);

	boolean handleCustomRender(T renderingObject, PoseStack matrix, MultiBufferSource buffer);
}
