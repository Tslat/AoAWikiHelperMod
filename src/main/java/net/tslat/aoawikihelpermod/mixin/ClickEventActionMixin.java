package net.tslat.aoawikihelpermod.mixin;

import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClickEvent.Action.class)
public class ClickEventActionMixin {
    @Inject(method = "isAllowedFromServer", at = @At("HEAD"), cancellable = true)
    public void forceClickable(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
