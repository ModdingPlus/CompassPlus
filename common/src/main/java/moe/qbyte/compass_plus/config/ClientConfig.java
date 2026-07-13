package moe.qbyte.compass_plus.config;

import moe.qbyte.compass_plus.CompassPlus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
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
    public static SlotSettings compassSlots = ClientConfigTemplate.COMPASS_SLOTS_DEFAULTS;

    private ClientConfig() {}

    /** Called from the loader modules whenever the config (re)loads. */
    public static void cache() {
        cardinalDirections = CLIENT.cardinalDirections.snapshot();
        worldSpawn = CLIENT.worldSpawn.snapshot();
        deathPosition = CLIENT.deathPosition.snapshot();
        compassSlots = CLIENT.compassSlots.snapshot();
    }

    /** Which compass kinds make a HUD component visible. */
    public record ComponentSettings(boolean always, boolean compass, boolean lodestoneCompass,
                                    boolean recoveryCompass, Set<ResourceLocation> customCompasses) {
        public boolean enabledFor(boolean hasCompass, boolean hasLodestoneCompass,
                                  boolean hasRecoveryCompass, Set<ResourceLocation> searchedItems) {
            return this.always
                    || (this.compass && hasCompass)
                    || (this.lodestoneCompass && hasLodestoneCompass)
                    || (this.recoveryCompass && hasRecoveryCompass)
                    || !Collections.disjoint(this.customCompasses, searchedItems);
        }
    }

    /** Which slots are searched for compasses. */
    public record SlotSettings(boolean mainHand, boolean offHand, boolean hotbar, boolean inventory,
                               boolean equipment) {}

    public static class ClientConfigTemplate {
        static final ComponentSettings CARDINAL_DIRECTIONS_DEFAULTS = new ComponentSettings(false, true, true, true, Set.of());
        static final ComponentSettings WORLD_SPAWN_DEFAULTS = new ComponentSettings(false, true, false, false, Set.of());
        static final ComponentSettings DEATH_POSITION_DEFAULTS = new ComponentSettings(false, false, false, true, Set.of());
        static final SlotSettings COMPASS_SLOTS_DEFAULTS = new SlotSettings(true, true, true, true, true);

        public final ComponentTemplate cardinalDirections;
        public final ComponentTemplate worldSpawn;
        public final ComponentTemplate deathPosition;
        public final SlotsTemplate compassSlots;

        ClientConfigTemplate(ModConfigSpec.Builder builder) {
            cardinalDirections = new ComponentTemplate(builder, "cardinal_directions",
                    "the cardinal direction letters and diagonal marks", CARDINAL_DIRECTIONS_DEFAULTS);
            worldSpawn = new ComponentTemplate(builder, "world_spawn",
                    "the world spawn marker (overworld only)", WORLD_SPAWN_DEFAULTS);
            deathPosition = new ComponentTemplate(builder, "death_position",
                    "the marker for your last death (same dimension only)", DEATH_POSITION_DEFAULTS);
            compassSlots = new SlotsTemplate(builder);
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
                    .define("compass", defaults.compass());
            lodestoneCompass = builder
                    .comment("Show " + what + " while a lodestone-bound compass is in a searched slot.")
                    .translation(key("lodestoneCompass"))
                    .define("lodestone_compass", defaults.lodestoneCompass());
            recoveryCompass = builder
                    .comment("Show " + what + " while a recovery compass is in a searched slot.")
                    .translation(key("recoveryCompass"))
                    .define("recovery_compass", defaults.recoveryCompass());
            customCompasses = builder
                    .comment("Item ids that also enable " + what + " from a searched slot, e.g. [\"somemod:gps\"].")
                    .translation(key("customCompasses"))
                    .defineListAllowEmpty("custom_compasses", List.of(), () -> "",
                            o -> o instanceof String s && ResourceLocation.tryParse(s) != null);
            builder.pop();
        }

        ComponentSettings snapshot() {
            return new ComponentSettings(always.get(), compass.get(), lodestoneCompass.get(), recoveryCompass.get(),
                    customCompasses.get().stream()
                            .map(ResourceLocation::tryParse)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toUnmodifiableSet()));
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

        SlotSettings snapshot() {
            return new SlotSettings(mainHand.get(), offHand.get(), hotbar.get(), inventory.get(), equipment.get());
        }
    }

    private static String key(String name) {
        return "text." + CompassPlus.MODID + ".config." + name;
    }
}
