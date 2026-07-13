package moe.qbyte.compass_plus.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.qbyte.compass_plus.CompassPlus;
import moe.qbyte.compass_plus.api.Color;
import moe.qbyte.compass_plus.api.CompassIcon;
import moe.qbyte.compass_plus.api.POI;
import moe.qbyte.compass_plus.config.ClientConfig;
import moe.qbyte.compass_plus.config.CompassKind;
import moe.qbyte.compass_plus.config.CompassSlot;
import moe.qbyte.compass_plus.utils.gui.Sprite;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CompassHUD {
    public static final int COMPASS_WIDTH = 220;
    public static final ResourceLocation GUI_TEXTURES = ResourceLocation.fromNamespaceAndPath(CompassPlus.MODID,
            "textures/gui/gui_icons.png");

    public static final Sprite LINE = new Sprite(GUI_TEXTURES, 0, 0, 256, 16);
    public static final Sprite DIAGONALS_MARK = new Sprite(GUI_TEXTURES, 16, 16, 15, 15);

    public static final Sprite TICK = new Sprite(GUI_TEXTURES, 0 * 16, 32, 15, 15);
    public static final Sprite HOME = new Sprite(GUI_TEXTURES, 1 * 16, 32, 15, 15);
    public static final Sprite LODESTONE = new Sprite(GUI_TEXTURES, 2 * 16, 32, 15, 15);
    public static final Sprite SKULL = new Sprite(GUI_TEXTURES, 3 * 16, 32, 15, 15);
    public static final Sprite HEART = new Sprite(GUI_TEXTURES, 4 * 16, 32, 15, 15);
    public static final Sprite EXCLAMATION_POINT = new Sprite(GUI_TEXTURES, 5 * 16, 32, 15, 15);
    public static final Sprite CROSS = new Sprite(GUI_TEXTURES, 6 * 16, 32, 15, 15);
    public static final Sprite STAR = new Sprite(GUI_TEXTURES, 7 * 16, 32, 15, 15);
    public static final Sprite RING = new Sprite(GUI_TEXTURES, 8 * 16, 32, 15, 15);
    public static final Sprite CIRCLE = new Sprite(GUI_TEXTURES, 9 * 16, 32, 15, 15);
    public static final Sprite TRIANGLE = new Sprite(GUI_TEXTURES, 10 * 16, 32, 15, 15);
    public static final Sprite SQUARE = new Sprite(GUI_TEXTURES, 11 * 16, 32, 15, 15);
    public static final Sprite GATEWAY = new Sprite(GUI_TEXTURES, 12 * 16, 32, 15, 15);
    public static final Sprite RUBY = new Sprite(GUI_TEXTURES, 13 * 16, 32, 15, 15);
    public static final Sprite DIAMOND = new Sprite(GUI_TEXTURES, 14 * 16, 32, 15, 15);

    private final Minecraft minecraft = Minecraft.getInstance();

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (this.minecraft.player == null || this.minecraft.level == null)
            return;
        // NeoForge's gui layers are skipped on F1 by vanilla's LayeredDraw; check
        // explicitly so the Fabric render callback behaves the same way.
        if (this.minecraft.options.hideGui)
            return;

        // Classify the compasses in the searched slots in a single pass. Item
        // checks stay item-only (Inventory.contains(ItemStack) compares components
        // since 1.20.5); a compass with a lodestone tracker component is its own kind.
        List<ItemStack> searchedStacks = this.searchedStacks();
        EnumSet<CompassKind> presentKinds = EnumSet.noneOf(CompassKind.class);
        Set<ResourceLocation> searchedItems = new HashSet<>();
        for (ItemStack stack : searchedStacks) {
            if (stack.isEmpty())
                continue;
            if (stack.is(Items.COMPASS)) {
                presentKinds.add(stack.has(DataComponents.LODESTONE_TRACKER)
                        ? CompassKind.LODESTONE_COMPASS
                        : CompassKind.COMPASS);
            } else if (stack.is(Items.RECOVERY_COMPASS)) {
                presentKinds.add(CompassKind.RECOVERY_COMPASS);
            } else {
                searchedItems.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            }
        }

        EnumSet<HudComponent> visibleComponents = EnumSet.noneOf(HudComponent.class);
        if (ClientConfig.cardinalDirections.enabledFor(presentKinds, searchedItems))
            visibleComponents.add(HudComponent.CARDINAL_DIRECTIONS);
        if (ClientConfig.worldSpawn.enabledFor(presentKinds, searchedItems))
            visibleComponents.add(HudComponent.WORLD_SPAWN);
        if (ClientConfig.deathPosition.enabledFor(presentKinds, searchedItems))
            visibleComponents.add(HudComponent.DEATH_POSITION);

        // Lodestone markers are inherent to their compasses (there is no data to
        // show without one), so they keep the HUD visible on their own.
        if (visibleComponents.isEmpty() && !presentKinds.contains(CompassKind.LODESTONE_COMPASS))
            return;

        this.minecraft.getProfiler().push("compass");
        float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        float midX = guiGraphics.guiWidth() / 2f;
        float yRot = this.player().getViewYRot(partialTicks);

        // line
        RenderSystem.enableBlend();
        LINE.blendBlit(guiGraphics, (int) Math.ceil(midX - (COMPASS_WIDTH / 2f)), 2);
        TICK.blendBlit(guiGraphics, (int) midX - 7, 0);

        if (visibleComponents.contains(HudComponent.CARDINAL_DIRECTIONS)) {
            // south marker
            float south = normalizedAngle(yRot);
            if (Math.abs(south) <= COMPASS_WIDTH / 2d)
                guiGraphics.drawCenteredString(this.minecraft.font, "S", (int) (midX - south), 5, 0xFFFFFFFF);

            float east = normalizedAngle(yRot + 90);
            if (Math.abs(east) <= COMPASS_WIDTH / 2d)
                guiGraphics.drawCenteredString(this.minecraft.font, "E", (int) (midX - east), 5, 0xFFFFFFFF);

            float north = normalizedAngle(yRot + 180);
            if (Math.abs(north) <= COMPASS_WIDTH / 2d)
                guiGraphics.drawCenteredString(this.minecraft.font, "N", (int) (midX - north), 5, 0xFFFFFFFF);

            float west = normalizedAngle(yRot + 270);
            if (Math.abs(west) <= COMPASS_WIDTH / 2d)
                guiGraphics.drawCenteredString(this.minecraft.font, "W", (int) (midX - west), 5, 0xFFFFFFFF);

            for (int i = 1; i < 5; i++) {
                float tickAngle = normalizedAngle(yRot + 45 + 90 * i);
                if (Math.abs(tickAngle) <= COMPASS_WIDTH / 2d)
                    DIAGONALS_MARK.blendBlit(guiGraphics, (int) (midX - tickAngle - 7), 5);
            }
        }

        NonNullList<POI> pointsOfInterest = this.getPointsOfInterest(visibleComponents, searchedStacks);
        pointsOfInterest.forEach(
                poi -> {
                    double angle = this.getAngle(poi.pos(), partialTicks);
                    angle = clamp(angle, COMPASS_WIDTH / -2f, COMPASS_WIDTH / 2f);
                    poi.color().setAsShaderColor();
                    getIconSprite(poi.icon()).blendBlit(guiGraphics, (int) (midX - angle - 7), 2);
                });

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

        float dot = viewVector.x * relativePos.x + viewVector.y * relativePos.y;
        float det = viewVector.x * relativePos.y - viewVector.y * relativePos.x;
        double angle = -Math.atan2(det, dot);
        return normalizedAngle((float) Math.toDegrees(angle));
    }

    /** Collects the stacks from the slots enabled in the compass_slots config section. */
    private List<ItemStack> searchedStacks() {
        Inventory inventory = this.player().getInventory();
        Set<CompassSlot> slots = ClientConfig.compassSlots;
        List<ItemStack> stacks = new ArrayList<>();
        // main hand is a subset of the hotbar, which is a subset of the main
        // inventory; only the widest enabled group needs to be collected
        if (slots.contains(CompassSlot.INVENTORY)) {
            stacks.addAll(inventory.items);
        } else if (slots.contains(CompassSlot.HOTBAR)) {
            stacks.addAll(inventory.items.subList(0, Inventory.getSelectionSize()));
        } else if (slots.contains(CompassSlot.MAIN_HAND)) {
            stacks.add(inventory.getSelected());
        }
        if (slots.contains(CompassSlot.OFF_HAND))
            stacks.addAll(inventory.offhand);
        if (slots.contains(CompassSlot.EQUIPMENT))
            stacks.addAll(inventory.armor);
        return stacks;
    }

    @NotNull
    private LocalPlayer player() {
        assert this.minecraft.player != null;
        return this.minecraft.player;
    }

    @NotNull
    private ClientLevel level() {
        assert this.minecraft.level != null;
        return this.minecraft.level;
    }

    public NonNullList<POI> getPointsOfInterest(Set<HudComponent> visibleComponents,
                                                List<ItemStack> searchedStacks) {
        NonNullList<POI> pois = NonNullList.create();

        // World spawn POI
        if (visibleComponents.contains(HudComponent.WORLD_SPAWN)) {
            if (this.level().dimension().equals(Level.OVERWORLD)) {
                pois.add(POI.from(this.level().getSharedSpawnPos(), CompassIcon.STAR, new Color(0.6f, 0.6f, 1f, 1f)));
            }
        }

        // For every lodestone compass in a searched slot, add a POI
        for (ItemStack stack : searchedStacks) {
            if (stack.getItem() instanceof CompassItem) {
                CompassHUD.getLodestonePosition(this.level(), stack)
                        .ifPresent(blockPos -> pois
                                .add(POI.from(blockPos, CompassIcon.LODESTONE, new Color(1f, 0.6f, 0.6f, 1f))));
            }
        }

        // Last death POI
        if (visibleComponents.contains(HudComponent.DEATH_POSITION)) {
            Optional<GlobalPos> deathPos = this.player().getLastDeathLocation();
            if (deathPos.isPresent()) {
                if (deathPos.get().dimension().equals(this.level().dimension())) {
                    pois.add(POI.from(deathPos.get().pos(), CompassIcon.SKULL, new Color(0.3f, 0.0f, 0.56f, 1f)));
                }
            }
        }

        return pois;
    }

    private static Optional<BlockPos> getLodestonePosition(Level level, ItemStack stack) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null)
            return Optional.empty();

        return tracker.target()
                .filter(pos -> pos.dimension() == level.dimension())
                .map(GlobalPos::pos);
    }

    public static float normalizedAngle(float rotation) {
        float angle = rotation % 360;
        angle = angle >= 180 ? angle - 360 : angle;
        angle = angle < -180 ? angle + 360 : angle;
        return angle;
    }

    public static double clamp(double in, double min, double max) {
        return Math.max(min, Math.min(max, in));
    }
}
