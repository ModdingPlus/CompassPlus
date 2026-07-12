package moe.qbyte.compass_plus.utils.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public record Sprite(ResourceLocation resourceLocation, int u, int v, int width, int height) {
    public void blit(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(this.resourceLocation, x, y, this.u, this.v, this.width, this.height, 256, 256);
    }

    public void blendBlit(GuiGraphics guiGraphics, int x, int y) {
        RenderSystem.enableBlend();
        this.blit(guiGraphics, x, y);
    }
}
