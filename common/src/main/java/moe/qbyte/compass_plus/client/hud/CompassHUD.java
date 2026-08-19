package moe.qbyte.compass_plus.client.hud;

import moe.qbyte.compass_plus.CompassPlus;
import moe.qbyte.compass_plus.mixin.BossHealthOverlayAccessor;
import moe.qbyte.compass_plus.api.Color;
import moe.qbyte.compass_plus.api.CompassIcon;
import moe.qbyte.compass_plus.api.POI;
import moe.qbyte.compass_plus.config.BossBarPlacement;
import moe.qbyte.compass_plus.config.ClientConfig;
import moe.qbyte.compass_plus.config.CompassKind;
import moe.qbyte.compass_plus.config.CompassSlot;
import moe.qbyte.compass_plus.utils.gui.Sprite;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
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
    public static final Identifier GUI_TEXTURES = Identifier.fromNamespaceAndPath(CompassPlus.MODID,
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

    private static final ResourceKey<MapDecorationType> RED_X_KEY =
            MapDecorationTypes.RED_X.unwrapKey().orElseThrow();

    /** How far the boss bars are pushed down while the HUD sits above them. */
    public static final int BOSS_BAR_SHIFT = 18;

    public static final CompassHUD INSTANCE = new CompassHUD();

    private final Minecraft minecraft = Minecraft.getInstance();

    private CompassHUD() {}

    /** Everything one frame of the HUD shows, derived from the searched slots. */
    private record HudState(EnumSet<HudComponent> visibleComponents, List<ItemStack> searchedStacks,
                            List<Vec2> treasureTargets, List<Vec2> explorerTargets) {}

    private HudState computeState() {
        // Classify the compasses in the searched slots in a single pass. Item
        // checks stay item-only (Inventory.contains(ItemStack) compares components
        // since 1.20.5); a compass with a lodestone tracker component is its own kind.
        List<ItemStack> searchedStacks = this.searchedStacks();
        EnumSet<CompassKind> presentKinds = EnumSet.noneOf(CompassKind.class);
        Set<Identifier> searchedItems = new HashSet<>();
        List<Vec2> treasureTargets = new ArrayList<>();
        List<Vec2> explorerTargets = new ArrayList<>();
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
                if (stack.is(Items.FILLED_MAP))
                    this.collectMapTargets(stack, treasureTargets, explorerTargets);
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

        // Marker components are inherent to their items (there is no data to show
        // without one); they are visible whenever their toggle is on and a source
        // item is in a searched slot, and keep the HUD visible on their own.
        Set<HudComponent> markers = ClientConfig.enabledMarkers;
        if (markers.contains(HudComponent.LODESTONES) && presentKinds.contains(CompassKind.LODESTONE_COMPASS))
            visibleComponents.add(HudComponent.LODESTONES);
        if (markers.contains(HudComponent.TREASURE_MAPS) && !treasureTargets.isEmpty())
            visibleComponents.add(HudComponent.TREASURE_MAPS);
        if (markers.contains(HudComponent.EXPLORER_MAPS) && !explorerTargets.isEmpty())
            visibleComponents.add(HudComponent.EXPLORER_MAPS);

        return new HudState(visibleComponents, searchedStacks, treasureTargets, explorerTargets);
    }

    /** Whether the HUD draws anything this frame. */
    private boolean isVisible() {
        if (this.minecraft.player == null || this.minecraft.level == null)
            return false;
        if (this.minecraft.options.hideGui)
            return false;
        return !this.computeState().visibleComponents().isEmpty();
    }

    /**
     * How far BossHealthOverlayMixin pushes the boss bars down this frame; zero
     * unless the HUD is configured to sit above them and is currently visible.
     */
    public int bossBarShift() {
        if (ClientConfig.bossBarPlacement != BossBarPlacement.ABOVE_BOSS_BARS)
            return 0;
        return this.isVisible() ? BOSS_BAR_SHIFT : 0;
    }

    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (this.minecraft.player == null || this.minecraft.level == null)
            return;
        // NeoForge's gui layers are skipped on F1 by vanilla's LayeredDraw; check
        // explicitly so the Fabric render callback behaves the same way.
        if (this.minecraft.options.hideGui)
            return;

        HudState state = this.computeState();
        EnumSet<HudComponent> visibleComponents = state.visibleComponents();
        List<ItemStack> searchedStacks = state.searchedStacks();
        List<Vec2> treasureTargets = state.treasureTargets();
        List<Vec2> explorerTargets = state.explorerTargets();

        if (visibleComponents.isEmpty())
            return;

        Profiler.get().push("compass");
        float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);

        float midX = guiGraphics.guiWidth() / 2f;
        float yRot = this.player().getViewYRot(partialTicks);

        // shift below the boss bar stack instead of overlapping it
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(0, this.bossBarOffset());

        // line
        LINE.blit(guiGraphics, (int) Math.ceil(midX - (COMPASS_WIDTH / 2f)), 2);
        TICK.blit(guiGraphics, (int) midX - 7, 0);

        if (visibleComponents.contains(HudComponent.CARDINAL_DIRECTIONS)) {
            // south marker
            float south = normalizedAngle(yRot);
            if (Math.abs(south) <= COMPASS_WIDTH / 2d)
                this.drawCenteredString(guiGraphics, "S", (int) (midX - south), 5, 0xFFFFFFFF);

            float east = normalizedAngle(yRot + 90);
            if (Math.abs(east) <= COMPASS_WIDTH / 2d)
                this.drawCenteredString(guiGraphics, "E", (int) (midX - east), 5, 0xFFFFFFFF);

            float north = normalizedAngle(yRot + 180);
            if (Math.abs(north) <= COMPASS_WIDTH / 2d)
                this.drawCenteredString(guiGraphics, "N", (int) (midX - north), 5, 0xFFFFFFFF);

            float west = normalizedAngle(yRot + 270);
            if (Math.abs(west) <= COMPASS_WIDTH / 2d)
                this.drawCenteredString(guiGraphics, "W", (int) (midX - west), 5, 0xFFFFFFFF);

            for (int i = 1; i < 5; i++) {
                float tickAngle = normalizedAngle(yRot + 45 + 90 * i);
                if (Math.abs(tickAngle) <= COMPASS_WIDTH / 2d)
                    DIAGONALS_MARK.blit(guiGraphics, (int) (midX - tickAngle - 7), 5);
            }
        }

        NonNullList<POI> pointsOfInterest = this.getPointsOfInterest(visibleComponents, searchedStacks,
                treasureTargets, explorerTargets);
        pointsOfInterest.forEach(
                poi -> {
                    double angle = this.getAngle(poi.pos(), partialTicks);
                    angle = clamp(angle, COMPASS_WIDTH / -2f, COMPASS_WIDTH / 2f);
                    getIconSprite(poi.icon()).blit(guiGraphics, (int) (midX - angle - 7), 2,
                            poi.color().argb());
                });

        guiGraphics.pose().popMatrix();
        Profiler.get().pop();
    }

    /**
     * The boss bar stack shares the HUD's top center anchor, so the HUD moves
     * below it while any bars are shown (unless it is configured to sit above
     * them instead). Mirrors BossHealthOverlay.render: bars start at y = 12
     * with their name just above, advance 10 + line height per bar, and stop
     * once the next row would reach a third of the screen height.
     */
    private int bossBarOffset() {
        if (ClientConfig.bossBarPlacement != BossBarPlacement.BELOW_BOSS_BARS)
            return 0;
        int bars = ((BossHealthOverlayAccessor) this.minecraft.gui.getBossOverlay())
                .compass_plus$getEvents().size();
        if (bars == 0)
            return 0;
        int limit = this.minecraft.getWindow().getGuiScaledHeight() / 3;
        int top = 12;
        for (int bar = 1; bar < bars; bar++) {
            int next = top + 10 + this.minecraft.font.lineHeight;
            if (next >= limit)
                break;
            top = next;
        }
        // the last bar's texture is 5 tall; keep a small gap below it
        return top + 5 + 3;
    }

    /** drawCenteredString is gone since 26.1; center over the plain text() primitive. */
    private void drawCenteredString(GuiGraphicsExtractor guiGraphics, String text, int centerX, int y, int color) {
        guiGraphics.text(this.minecraft.font, text, centerX - this.minecraft.font.width(text) / 2, y, color);
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

    /**
     * Collects the world positions of a map's target decorations. Vanilla writes
     * them onto the item as the MAP_DECORATIONS component with world coordinates
     * (see MapItemSavedData.addTargetDecoration); the red X of buried treasure
     * maps counts as treasure, every other target as an explorer map target.
     */
    private void collectMapTargets(ItemStack stack, List<Vec2> treasureTargets, List<Vec2> explorerTargets) {
        MapDecorations decorations = stack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);
        if (decorations.decorations().isEmpty())
            return;
        if (!this.mapDimensionMatches(stack))
            return;
        for (MapDecorations.Entry entry : decorations.decorations().values()) {
            Vec2 target = new Vec2((float) entry.x(), (float) entry.z());
            if (entry.type().is(RED_X_KEY))
                treasureTargets.add(target);
            else
                explorerTargets.add(target);
        }
    }

    private boolean mapDimensionMatches(ItemStack stack) {
        MapId mapId = stack.get(DataComponents.MAP_ID);
        MapItemSavedData mapData = mapId != null ? this.level().getMapData(mapId) : null;
        if (mapData != null)
            return mapData.dimension.equals(this.level().dimension());
        // the map's saved data may not be synced for maps that were never held;
        // vanilla treasure and explorer maps target the overworld
        return this.level().dimension().equals(Level.OVERWORLD);
    }

    /** Collects the stacks from the slots enabled in the compass_slots config section. */
    private List<ItemStack> searchedStacks() {
        Inventory inventory = this.player().getInventory();
        Set<CompassSlot> slots = ClientConfig.compassSlots;
        List<ItemStack> stacks = new ArrayList<>();
        // main hand is a subset of the hotbar, which is a subset of the main
        // inventory; only the widest enabled group needs to be collected
        if (slots.contains(CompassSlot.INVENTORY)) {
            stacks.addAll(inventory.getNonEquipmentItems());
        } else if (slots.contains(CompassSlot.HOTBAR)) {
            stacks.addAll(inventory.getNonEquipmentItems().subList(0, Inventory.getSelectionSize()));
        } else if (slots.contains(CompassSlot.MAIN_HAND)) {
            stacks.add(inventory.getSelectedItem());
        }
        if (slots.contains(CompassSlot.OFF_HAND))
            stacks.add(this.player().getItemBySlot(EquipmentSlot.OFFHAND));
        if (slots.contains(CompassSlot.EQUIPMENT))
            for (EquipmentSlot slot : EquipmentSlot.values())
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
                    stacks.add(this.player().getItemBySlot(slot));
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
                                                List<ItemStack> searchedStacks,
                                                List<Vec2> treasureTargets, List<Vec2> explorerTargets) {
        NonNullList<POI> pois = NonNullList.create();

        // World spawn POI (the respawn data knows its dimension; vanilla's is the overworld)
        if (visibleComponents.contains(HudComponent.WORLD_SPAWN)) {
            LevelData.RespawnData spawn = this.level().getRespawnData();
            if (this.level().dimension().equals(spawn.dimension())) {
                pois.add(POI.from(spawn.pos(), CompassIcon.STAR, new Color(0.6f, 0.6f, 1f, 1f)));
            }
        }

        // For every lodestone compass in a searched slot, add a POI
        if (visibleComponents.contains(HudComponent.LODESTONES)) {
            for (ItemStack stack : searchedStacks) {
                if (stack.getItem() instanceof CompassItem) {
                    CompassHUD.getLodestonePosition(this.level(), stack)
                            .ifPresent(blockPos -> pois
                                    .add(POI.from(blockPos, CompassIcon.LODESTONE, new Color(1f, 0.6f, 0.6f, 1f))));
                }
            }
        }

        // Map target POIs (buried treasure and explorer maps)
        if (visibleComponents.contains(HudComponent.TREASURE_MAPS)) {
            for (Vec2 target : treasureTargets)
                pois.add(new POI(target, CompassIcon.CROSS, new Color(0.9f, 0.25f, 0.25f, 1f)));
        }
        if (visibleComponents.contains(HudComponent.EXPLORER_MAPS)) {
            for (Vec2 target : explorerTargets)
                pois.add(new POI(target, CompassIcon.TRIANGLE, new Color(0.5f, 0.85f, 0.5f, 1f)));
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
