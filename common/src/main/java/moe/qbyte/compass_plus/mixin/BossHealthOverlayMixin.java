package moe.qbyte.compass_plus.mixin;

import moe.qbyte.compass_plus.client.hud.CompassHUD;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pushes the boss bar stack down while the HUD is configured to sit above it
 * (boss_bar_placement = ABOVE_BOSS_BARS) and is currently visible.
 */
@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    @Unique
    private boolean compass_plus$shifted;

    @Inject(method = "render", at = @At("HEAD"))
    private void compass_plus$shiftBelowHud(GuiGraphics guiGraphics, CallbackInfo ci) {
        // reuse the accessor to skip the HUD visibility scan while no bars exist
        boolean hasBars = !((BossHealthOverlayAccessor) this).compass_plus$getEvents().isEmpty();
        int shift = hasBars ? CompassHUD.INSTANCE.bossBarShift() : 0;
        this.compass_plus$shifted = shift != 0;
        if (this.compass_plus$shifted) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, shift, 0);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void compass_plus$restore(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (this.compass_plus$shifted) {
            guiGraphics.pose().popPose();
            this.compass_plus$shifted = false;
        }
    }
}
