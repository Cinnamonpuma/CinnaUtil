package com.example.addon.mixin;

import com.example.addon.CinnaUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class ContainerScreenMixin<T extends ScreenHandler> {

    @Shadow protected T handler;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Log information about the container when it's opened
        CinnaUtil.LOG.info("Container opened: " + handler.getClass().getSimpleName() +
            " with syncId: " + handler.syncId +
            " and " + handler.slots.size() + " slots");
    }
}
