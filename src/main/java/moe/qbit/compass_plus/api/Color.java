package moe.qbit.compass_plus.api;

import com.mojang.blaze3d.systems.RenderSystem;

public record Color(float red, float green, float blue, float alpha) {
    public void setAsShaderColor() {
        RenderSystem.setShaderColor(this.red,this.green, this.blue, this.alpha);
    }
}
