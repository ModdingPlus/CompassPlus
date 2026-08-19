package moe.qbyte.compass_plus.utils.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public record Sprite(Identifier resourceLocation, int u, int v, int width, int height) {
    public void blit(GuiGraphicsExtractor guiGraphics, int x, int y) {
        this.blit(guiGraphics, x, y, 0xFFFFFFFF);
    }

    /**
     * Draws the sprite tinted with the given ARGB color. The vanilla gui
     * pipeline blends like the old blendFuncSeparate hack did: destination
     * alpha stays opaque, so the pause menu blur does not darken the HUD's
     * translucent pixels into a halo.
     */
    public void blit(GuiGraphicsExtractor guiGraphics, int x, int y, int argb) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.resourceLocation,
                x, y, this.u, this.v, this.width, this.height, 256, 256, argb);
    }
}
