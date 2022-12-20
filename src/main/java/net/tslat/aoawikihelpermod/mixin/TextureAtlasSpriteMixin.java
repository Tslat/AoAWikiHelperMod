package net.tslat.aoawikihelpermod.mixin;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.tslat.aoawikihelpermod.render.RenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteMixin {
	@Inject(
			method = "createTicker",
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lnet/minecraft/client/renderer/texture/SpriteContents;createTicker()Lnet/minecraft/client/renderer/texture/SpriteTicker;",
					shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILEXCEPTION
	)
	public void collectTicker(CallbackInfoReturnable<TextureAtlasSprite.Ticker> callback, SpriteTicker ticker) {
		if (ticker instanceof SpriteContents.Ticker spriteTicker)
			RenderUtil.addSpriteTicker(spriteTicker);
	}
}
