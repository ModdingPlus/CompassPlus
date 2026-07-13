package moe.qbyte.compass_plus.config;

import moe.qbyte.compass_plus.CompassPlus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

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
    public static Set<ResourceLocation> customCompassItems = Set.of();

    private ClientConfig() {}

    /** Called from the loader modules whenever the config (re)loads. */
    public static void cache() {
        cardinalDirections = CLIENT.cardinalDirections.snapshot();
        worldSpawn = CLIENT.worldSpawn.snapshot();
        deathPosition = CLIENT.deathPosition.snapshot();
        customCompassItems = CLIENT.customCompassItems.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Which compass kinds make a HUD component visible. */
    public record ComponentSettings(boolean always, boolean compass, boolean lodestoneCompass,
                                    boolean recoveryCompass, boolean customCompasses) {
        public boolean enabledFor(boolean hasCompass, boolean hasLodestoneCompass,
                                  boolean hasRecoveryCompass, boolean hasCustomCompass) {
            return this.always
                    || (this.compass && hasCompass)
                    || (this.lodestoneCompass && hasLodestoneCompass)
                    || (this.recoveryCompass && hasRecoveryCompass)
                    || (this.customCompasses && hasCustomCompass);
        }
    }

    public static class ClientConfigTemplate {
        static final ComponentSettings CARDINAL_DIRECTIONS_DEFAULTS = new ComponentSettings(false, true, true, true, true);
        static final ComponentSettings WORLD_SPAWN_DEFAULTS = new ComponentSettings(false, true, false, false, false);
        static final ComponentSettings DEATH_POSITION_DEFAULTS = new ComponentSettings(false, false, false, true, false);

        public final ComponentTemplate cardinalDirections;
        public final ComponentTemplate worldSpawn;
        public final ComponentTemplate deathPosition;
        public final ModConfigSpec.ConfigValue<List<? extends String>> customCompassItems;

        ClientConfigTemplate(ModConfigSpec.Builder builder) {
            cardinalDirections = new ComponentTemplate(builder, "cardinal_directions",
                    "the cardinal direction letters and diagonal marks", CARDINAL_DIRECTIONS_DEFAULTS);
            worldSpawn = new ComponentTemplate(builder, "world_spawn",
                    "the world spawn marker (overworld only)", WORLD_SPAWN_DEFAULTS);
            deathPosition = new ComponentTemplate(builder, "death_position",
                    "the marker for your last death (same dimension only)", DEATH_POSITION_DEFAULTS);

            builder.push("custom_compasses");
            customCompassItems = builder
                    .comment("Item ids that count as the \"custom compasses\" kind in the sections above, e.g. [\"somemod:gps\"].")
                    .translation("text." + CompassPlus.MODID + ".config.customCompassItems")
                    .defineListAllowEmpty("items", List.of(), () -> "",
                            o -> o instanceof String s && ResourceLocation.tryParse(s) != null);
            builder.pop();
        }
    }

    public static class ComponentTemplate {
        public final ModConfigSpec.BooleanValue always;
        public final ModConfigSpec.BooleanValue compass;
        public final ModConfigSpec.BooleanValue lodestoneCompass;
        public final ModConfigSpec.BooleanValue recoveryCompass;
        public final ModConfigSpec.BooleanValue customCompasses;

        ComponentTemplate(ModConfigSpec.Builder builder, String section, String what, ComponentSettings defaults) {
            builder.push(section);
            always = builder
                    .comment("Always show " + what + ", regardless of the compasses in your inventory.")
                    .translation(key("always"))
                    .define("always", defaults.always());
            compass = builder
                    .comment("Show " + what + " while a regular compass is in your inventory.")
                    .translation(key("compass"))
                    .define("compass", defaults.compass());
            lodestoneCompass = builder
                    .comment("Show " + what + " while a lodestone-bound compass is in your inventory.")
                    .translation(key("lodestoneCompass"))
                    .define("lodestone_compass", defaults.lodestoneCompass());
            recoveryCompass = builder
                    .comment("Show " + what + " while a recovery compass is in your inventory.")
                    .translation(key("recoveryCompass"))
                    .define("recovery_compass", defaults.recoveryCompass());
            customCompasses = builder
                    .comment("Show " + what + " while one of the configured custom compass items is in your inventory.")
                    .translation(key("customCompasses"))
                    .define("custom_compasses", defaults.customCompasses());
            builder.pop();
        }

        private static String key(String name) {
            return "text." + CompassPlus.MODID + ".config." + name;
        }

        ComponentSettings snapshot() {
            return new ComponentSettings(always.get(), compass.get(), lodestoneCompass.get(),
                    recoveryCompass.get(), customCompasses.get());
        }
    }
}
