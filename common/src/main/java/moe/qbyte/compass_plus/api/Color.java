package moe.qbyte.compass_plus.api;

public record Color(float red, float green, float blue, float alpha) {
    /** This color packed as ARGB, the format the gui blit methods tint with. */
    public int argb() {
        return Math.round(this.alpha * 255f) << 24
                | Math.round(this.red * 255f) << 16
                | Math.round(this.green * 255f) << 8
                | Math.round(this.blue * 255f);
    }
}
