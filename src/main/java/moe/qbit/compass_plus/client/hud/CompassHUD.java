package moe.qbit.compass_plus.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import moe.qbit.compass_plus.CompassPlus;
import moe.qbit.compass_plus.api.Color;
import moe.qbit.compass_plus.api.CompassIcon;
import moe.qbit.compass_plus.api.POI;
import moe.qbit.compass_plus.utils.gui.Sprite;
import moe.qbit.compass_plus.utils.item.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = CompassPlus.MODID)
public class CompassHUD extends GuiComponent implements IIngameOverlay {
    public static final int COMPASS_WIDTH = 220;
    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation(CompassPlus.MODID, "textures/gui/gui_icons.png");

    public static final Sprite LINE = new Sprite(GUI_TEXTURES, 0, 0, 256, 16);
    public static final Sprite DIAGONALS_MARK = new Sprite(GUI_TEXTURES, 16, 16, 15, 15);

    public static final Sprite TICK = new Sprite(GUI_TEXTURES, 0*16, 32, 15, 15);
    public static final Sprite HOME = new Sprite(GUI_TEXTURES, 1*16, 32, 15, 15);
    public static final Sprite LODESTONE = new Sprite(GUI_TEXTURES, 2*16, 32, 15, 15);
    public static final Sprite SKULL = new Sprite(GUI_TEXTURES, 3*16, 32, 15, 15);
    public static final Sprite HEART = new Sprite(GUI_TEXTURES, 4*16, 32, 15, 15);
    public static final Sprite EXCLAMATION_POINT = new Sprite(GUI_TEXTURES, 5*16, 32, 15, 15);
    public static final Sprite CROSS = new Sprite(GUI_TEXTURES, 6*16, 32, 15, 15);
    public static final Sprite STAR = new Sprite(GUI_TEXTURES, 7*16, 32, 15, 15);
    public static final Sprite RING = new Sprite(GUI_TEXTURES, 8*16, 32, 15, 15);
    public static final Sprite CIRCLE = new Sprite(GUI_TEXTURES, 9*16, 32, 15, 15);
    public static final Sprite TRIANGLE = new Sprite(GUI_TEXTURES, 10*16, 32, 15, 15);
    public static final Sprite SQUARE = new Sprite(GUI_TEXTURES, 11*16, 32, 15, 15);
    public static final Sprite GATEWAY = new Sprite(GUI_TEXTURES, 12*16, 32, 15, 15);
    public static final Sprite RUBY = new Sprite(GUI_TEXTURES, 13*16, 32, 15, 15);
    public static final Sprite DIAMOND = new Sprite(GUI_TEXTURES, 14*16, 32, 15, 15);

    private final Minecraft minecraft = Minecraft.getInstance();

    @Override
    public void render(ForgeIngameGui gui, PoseStack poseStack, float partialTicks, int width, int height) {
        if (!this.player().getInventory().contains(Items.COMPASS.getDefaultInstance())) return;
        this.minecraft.getProfiler().push("compass");
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        float midX = width/2f;
        float yRot = this.player().getViewYRot(partialTicks);

        // line
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        RenderSystem.enableBlend();
        LINE.blendBlit(poseStack, (int) Math.ceil(midX-(COMPASS_WIDTH/2f)), 2);
        TICK.blendBlit(poseStack, (int) midX-7,0);

        // south marker
        float south = normalizedAngle(yRot);
        if (Math.abs(south) <= COMPASS_WIDTH/2d)
            ForgeIngameGui.drawCenteredString(poseStack, gui.getFont(), "S", (int) (midX - south), 5, 0xFFFFFFFF);

        float east = normalizedAngle(yRot+90);
        if (Math.abs(east) <= COMPASS_WIDTH/2d)
            ForgeIngameGui.drawCenteredString(poseStack, gui.getFont(), "E", (int) (midX - east), 5, 0xFFFFFFFF);

        float north = normalizedAngle(yRot+180);
        if (Math.abs(north) <= COMPASS_WIDTH/2d)
            ForgeIngameGui.drawCenteredString(poseStack, gui.getFont(), "N", (int) (midX - north), 5, 0xFFFFFFFF);

        float west = normalizedAngle(yRot+270);
        if (Math.abs(west) <= COMPASS_WIDTH/2d)
            ForgeIngameGui.drawCenteredString(poseStack, gui.getFont(), "W", (int) (midX - west), 5, 0xFFFFFFFF);

        for (int i=1; i<5; i++) {
            float tickAngle = normalizedAngle(yRot + 45 + 90*i);
            if (Math.abs(tickAngle) <= COMPASS_WIDTH/2d)
                DIAGONALS_MARK.blendBlit(poseStack, (int) (midX-tickAngle-7),5);
        }

        NonNullList<POI> pointsOfInterest = this.getPointsOfInterest();
        pointsOfInterest.forEach(
                poi -> {
                    double angle = this.getAngle(poi.pos(),partialTicks);
                    angle = clamp(angle, COMPASS_WIDTH/-2f, COMPASS_WIDTH/2f);
                    poi.color().setAsShaderColor();
                    getIconSprite(poi.icon()).blendBlit(poseStack, (int) (midX - angle - 7), 2);
                }
        );


        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getProfiler().pop();
    }

    public static Sprite getIconSprite(CompassIcon icon) {
        return switch (icon) {
            case TICK -> TICK;
            case HOME -> HOME;
            case LODESTONE -> LODESTONE;
            case SKULL -> SKULL;
            case HEART -> HEART;
            case EXCLAMATION_POINT -> EXCLAMATION_POINT;
            case CROSS -> CROSS;
            case STAR -> STAR;
            case RING -> RING;
            case CIRCLE -> CIRCLE;
            case TRIANGLE -> TRIANGLE;
            case SQUARE -> SQUARE;
            case GATEWAY -> GATEWAY;
            case RUBY -> RUBY;
            case DIAMOND -> DIAMOND;
        };
    }

    public float getAngle(Vec2 pos, float partialTicks) {
        Vec3 player3dPos = this.player().getPosition(partialTicks);
        Vec2 playerPos = new Vec2((float) player3dPos.x, (float) player3dPos.z);

        Vec2 relativePos = pos.add(playerPos.scale(-1));

        Vec3 viewVector3d = this.player().getViewVector(partialTicks);
        Vec2 viewVector = new Vec2((float) viewVector3d.x, (float) viewVector3d.z);

        float dot = viewVector.x*relativePos.x + viewVector.y*relativePos.y;
        float det = viewVector.x*relativePos.y - viewVector.y*relativePos.x;
        double angle = -Math.atan2(det, dot);
        return normalizedAngle((float) Math.toDegrees(angle));
    }

    @Nonnull
    private LocalPlayer player() {
        assert this.minecraft.player != null;
        return this.minecraft.player;
    }

    @Nonnull
    private ClientLevel level() {
        assert this.minecraft.level != null;
        return this.minecraft.level;
    }

    public NonNullList<POI> getPointsOfInterest() {
        NonNullList<POI> pois = NonNullList.create();
        if (this.level().dimension().equals(Level.OVERWORLD)) {
            pois.add(POI.from(this.level().getSharedSpawnPos(), CompassIcon.STAR, new Color(0.6f, 0.6f, 1f,1f)));
        }
        for (ItemStack stack: InventoryUtil.all(this.player().getInventory())) {
            if (stack.getItem() instanceof CompassItem) {
                CompassHUD.getLodestonePosition(this.level(), stack)
                        .ifPresent(blockPos -> pois.add(POI.from(blockPos, CompassIcon.LODESTONE, new Color(1f, 0.6f, 0.6f, 1f))));
            }
        }

        return pois;
    }

    private static Optional<BlockPos> getLodestonePosition(Level level, ItemStack stack) {
        if (!stack.hasTag()) return Optional.empty();
        CompoundTag tag = stack.getTag();
        if (tag.contains("LodestonePos") && tag.contains("LodestoneDimension")) {
            Optional<ResourceKey<Level>> optional = CompassItem.getLodestoneDimension(tag);
            if (optional.isPresent() && level.dimension() == optional.get()) {
                return Optional.of(NbtUtils.readBlockPos(tag.getCompound("LodestonePos")));
            }
        }

        return Optional.empty();
    }

    public static float normalizedAngle(float rotation) {
        float angle = rotation % 360;
        angle = angle >= 180 ? angle - 360 : angle;
        angle = angle < -180 ? angle + 360 : angle;
        return angle;
    }

    public static double clamp(double in, double min, double max) {
        return Math.max(min,Math.min(max, in));
    }
}
