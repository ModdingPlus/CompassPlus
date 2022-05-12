package moe.qbit.compass_plus.utils.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

public record Sprite(ResourceLocation resourceLocation, int u, int v, int width, int height) {
    public void blit(PoseStack poseStack, int x, int y) {
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        GuiComponent.blit(poseStack, x, y, this.u, this.v, this.width, this.height, 256, 256);
    }

    public void blendBlit(PoseStack poseStack, int x, int y) {
        RenderSystem.enableBlend();
        this.blit(poseStack, x, y);
    }
}
