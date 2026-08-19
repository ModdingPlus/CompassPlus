package moe.qbyte.compass_plus.config;

import moe.qbyte.compass_plus.CompassPlus;
import moe.qbyte.compass_plus.client.hud.HudComponent;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Client-only display config. The spec is registered per loader (natively on NeoForge,
 * via Forge Config API Port on Fabric). The plain static fields below are what the HUD
 * reads; they are cached from the spec by the loader modules' config events.
 */
public final class ClientConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ClientConfigTemplate CLIENT;

    static {
        final Pair<ClientConfigTemplate, ModConfigSpec> specPair =
                new ModConfigSpec.Builder().configure(ClientConfigTemplate::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static ComponentSettings cardinalDirections = ClientConfigTemplate.CARDINAL_DIRECTIONS_DEFAULTS;
    public static ComponentSettings worldSpawn = ClientConfigTemplate.WORLD_SPAWN_DEFAULTS;
    public static ComponentSettings deathPosition = ClientConfigTemplate.DEATH_POSITION_DEFAULTS;
    public static Set<HudComponent> enabledMarkers = ClientConfigTemplate.MARKERS_DEFAULTS;
    public static Set<CompassSlot> compassSlots = ClientConfigTemplate.COMPASS_SLOTS_DEFAULTS;
    public static BossBarPlacement bossBarPlacement = ClientConfigTemplate.BOSS_BAR_PLACEMENT_DEFAULT;

    private ClientConfig() {}

    /** Called from the loader modules whenever the config (re)loads. */
    public static void cache() {
        cardinalDirections = CLIENT.cardinalDirections.snapshot();
        worldSpawn = CLIENT.worldSpawn.snapshot();
        deathPosition = CLIENT.deathPosition.snapshot();
        enabledMarkers = CLIENT.markers.snapshot();
        compassSlots = CLIENT.compassSlots.snapshot();
        bossBarPlacement = CLIENT.bossBarPlacement.get();
    }

    /** Which compass kinds make a HUD component visible. */
    public record ComponentSettings(boolean always, Set<CompassKind> enabledKinds,
                                    Set<Identifier> customCompasses) {
        public boolean enabledFor(Set<CompassKind> presentKinds, Set<Identifier> searchedItems) {
            return this.always
                    || !Collections.disjoint(this.enabledKinds, presentKinds)
                    || !Collections.disjoint(this.customCompasses, searchedItems);
        }
    }

    public static class ClientConfigTemplate {
        static final ComponentSettings CARDINAL_DIRECTIONS_DEFAULTS =
                new ComponentSettings(false, EnumSet.allOf(CompassKind.class), Set.of());
        static final ComponentSettings WORLD_SPAWN_DEFAULTS =
                new ComponentSettings(false, EnumSet.of(CompassKind.COMPASS), Set.of());
        static final ComponentSettings DEATH_POSITION_DEFAULTS =
                new ComponentSettings(false, EnumSet.of(CompassKind.RECOVERY_COMPASS), Set.of());
        static final Set<HudComponent> MARKERS_DEFAULTS =
                EnumSet.of(HudComponent.LODESTONES, HudComponent.TREASURE_MAPS, HudComponent.EXPLORER_MAPS);
        static final Set<CompassSlot> COMPASS_SLOTS_DEFAULTS = EnumSet.allOf(CompassSlot.class);
        static final BossBarPlacement BOSS_BAR_PLACEMENT_DEFAULT = BossBarPlacement.BELOW_BOSS_BARS;

        public final ComponentTemplate cardinalDirections;
        public final ComponentTemplate worldSpawn;
        public final ComponentTemplate deathPosition;
        public final MarkersTemplate markers;
        public final SlotsTemplate compassSlots;
        public final ModConfigSpec.EnumValue<BossBarPlacement> bossBarPlacement;

        ClientConfigTemplate(ModConfigSpec.Builder builder) {
            cardinalDirections = new ComponentTemplate(builder, "cardinal_directions",
                    "the cardinal direction letters and diagonal marks", CARDINAL_DIRECTIONS_DEFAULTS);
            worldSpawn = new ComponentTemplate(builder, "world_spawn",
                    "the world spawn marker (overworld only)", WORLD_SPAWN_DEFAULTS);
            deathPosition = new ComponentTemplate(builder, "death_position",
                    "the marker for your last death (same dimension only)", DEATH_POSITION_DEFAULTS);
            markers = new MarkersTemplate(builder);
            compassSlots = new SlotsTemplate(builder);
            bossBarPlacement = builder
                    .comment("Where the HUD goes while boss bars are shown: BELOW_BOSS_BARS moves the",
                            "HUD down below the boss bar stack, ABOVE_BOSS_BARS keeps the HUD at the",
                            "top of the screen and pushes the boss bars down instead.")
                    .translation(key("bossBarPlacement"))
                    .defineEnum("boss_bar_placement", BOSS_BAR_PLACEMENT_DEFAULT);
        }
    }

    public static class ComponentTemplate {
        public final ModConfigSpec.BooleanValue always;
        public final ModConfigSpec.BooleanValue compass;
        public final ModConfigSpec.BooleanValue lodestoneCompass;
        public final ModConfigSpec.BooleanValue recoveryCompass;
        public final ModConfigSpec.ConfigValue<List<? extends String>> customCompasses;

        ComponentTemplate(ModConfigSpec.Builder builder, String section, String what, ComponentSettings defaults) {
            builder.push(section);
            always = builder
                    .comment("Always show " + what + ", regardless of the compasses in your inventory.")
                    .translation(key("always"))
                    .define("always", defaults.always());
            compass = builder
                    .comment("Show " + what + " while a regular compass is in a searched slot.")
                    .translation(key("compass"))
                    .define("compass", defaults.enabledKinds().contains(CompassKind.COMPASS));
            lodestoneCompass = builder
                    .comment("Show " + what + " while a lodestone-bound compass is in a searched slot.")
                    .translation(key("lodestoneCompass"))
                    .define("lodestone_compass", defaults.enabledKinds().contains(CompassKind.LODESTONE_COMPASS));
            recoveryCompass = builder
                    .comment("Show " + what + " while a recovery compass is in a searched slot.")
                    .translation(key("recoveryCompass"))
                    .define("recovery_compass", defaults.enabledKinds().contains(CompassKind.RECOVERY_COMPASS));
            customCompasses = builder
                    .comment("Item ids that also enable " + what + " from a searched slot, e.g. [\"somemod:gps\"].")
                    .translation(key("customCompasses"))
                    .defineListAllowEmpty("custom_compasses", List.of(), () -> "",
                            o -> o instanceof String s && Identifier.tryParse(s) != null);
            builder.pop();
        }

        ComponentSettings snapshot() {
            EnumSet<CompassKind> kinds = EnumSet.noneOf(CompassKind.class);
            if (compass.get())
                kinds.add(CompassKind.COMPASS);
            if (lodestoneCompass.get())
                kinds.add(CompassKind.LODESTONE_COMPASS);
            if (recoveryCompass.get())
                kinds.add(CompassKind.RECOVERY_COMPASS);
            return new ComponentSettings(always.get(), kinds,
                    customCompasses.get().stream()
                            .map(Identifier::tryParse)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toUnmodifiableSet()));
        }
    }

    public static class MarkersTemplate {
        public final ModConfigSpec.BooleanValue lodestones;
        public final ModConfigSpec.BooleanValue treasureMaps;
        public final ModConfigSpec.BooleanValue explorerMaps;

        MarkersTemplate(ModConfigSpec.Builder builder) {
            builder.push("markers");
            lodestones = builder
                    .comment("Show markers for the targets of lodestone-bound compasses in searched slots.")
                    .translation(key("lodestones"))
                    .define("lodestones", true);
            treasureMaps = builder
                    .comment("Show markers for the red X of buried treasure maps in searched slots.")
                    .translation(key("treasureMaps"))
                    .define("treasure_maps", true);
            explorerMaps = builder
                    .comment("Show markers for the structure targets of explorer maps (woodland mansion, ocean monument, trial chambers) in searched slots.")
                    .translation(key("explorerMaps"))
                    .define("explorer_maps", true);
            builder.pop();
        }

        Set<HudComponent> snapshot() {
            EnumSet<HudComponent> markers = EnumSet.noneOf(HudComponent.class);
            if (lodestones.get())
                markers.add(HudComponent.LODESTONES);
            if (treasureMaps.get())
                markers.add(HudComponent.TREASURE_MAPS);
            if (explorerMaps.get())
                markers.add(HudComponent.EXPLORER_MAPS);
            return markers;
        }
    }

    public static class SlotsTemplate {
        public final ModConfigSpec.BooleanValue mainHand;
        public final ModConfigSpec.BooleanValue offHand;
        public final ModConfigSpec.BooleanValue hotbar;
        public final ModConfigSpec.BooleanValue inventory;
        public final ModConfigSpec.BooleanValue equipment;

        SlotsTemplate(ModConfigSpec.Builder builder) {
            builder.push("compass_slots");
            mainHand = builder
                    .comment("Search the main hand for compasses. The main hand is part of the hotbar; this toggle matters when \"hotbar\" and \"inventory\" are disabled.")
                    .translation(key("mainHand"))
                    .define("main_hand", true);
            offHand = builder
                    .comment("Search the off hand for compasses.")
                    .translation(key("offHand"))
                    .define("off_hand", true);
            hotbar = builder
                    .comment("Search the hotbar for compasses. The hotbar is part of the main inventory; this toggle matters when \"inventory\" is disabled.")
                    .translation(key("hotbar"))
                    .define("hotbar", true);
            inventory = builder
                    .comment("Search the whole main inventory (including the hotbar) for compasses.")
                    .translation(key("inventory"))
                    .define("inventory", true);
            equipment = builder
                    .comment("Search the armor slots for compasses, e.g. custom compasses built into helmets.")
                    .translation(key("equipment"))
                    .define("equipment", true);
            builder.pop();
        }

        Set<CompassSlot> snapshot() {
            EnumSet<CompassSlot> slots = EnumSet.noneOf(CompassSlot.class);
            if (mainHand.get())
                slots.add(CompassSlot.MAIN_HAND);
            if (offHand.get())
                slots.add(CompassSlot.OFF_HAND);
            if (hotbar.get())
                slots.add(CompassSlot.HOTBAR);
            if (inventory.get())
                slots.add(CompassSlot.INVENTORY);
            if (equipment.get())
                slots.add(CompassSlot.EQUIPMENT);
            return slots;
        }
    }

    private static String key(String name) {
        return "text." + CompassPlus.MODID + ".config." + name;
    }
}
