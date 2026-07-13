package moe.qbyte.compass_plus.config;

import moe.qbyte.compass_plus.CompassPlus;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

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

    public static boolean alwaysShowHud = false;
    public static boolean alwaysShowWorldSpawn = false;
    public static boolean alwaysShowDeathPosition = false;

    private ClientConfig() {}

    /** Called from the loader modules whenever the config (re)loads. */
    public static void cache() {
        alwaysShowHud = CLIENT.alwaysShowHud.get();
        alwaysShowWorldSpawn = CLIENT.alwaysShowWorldSpawn.get();
        alwaysShowDeathPosition = CLIENT.alwaysShowDeathPosition.get();
    }

    public static class ClientConfigTemplate {
        public final ModConfigSpec.BooleanValue alwaysShowHud;
        public final ModConfigSpec.BooleanValue alwaysShowWorldSpawn;
        public final ModConfigSpec.BooleanValue alwaysShowDeathPosition;

        ClientConfigTemplate(ModConfigSpec.Builder builder) {
            builder.push("general");
            alwaysShowHud = builder
                    .comment("Always show the compass HUD with the cardinal directions, even with no compass in your inventory.")
                    .translation("text." + CompassPlus.MODID + ".config.alwaysShowHud")
                    .define("alwaysShowHud", false);
            alwaysShowWorldSpawn = builder
                    .comment("Always show the world spawn marker (overworld only), even with no compass in your inventory.")
                    .translation("text." + CompassPlus.MODID + ".config.alwaysShowWorldSpawn")
                    .define("alwaysShowWorldSpawn", false);
            alwaysShowDeathPosition = builder
                    .comment("Always show the marker for your last death, even with no recovery compass in your inventory.")
                    .translation("text." + CompassPlus.MODID + ".config.alwaysShowDeathPosition")
                    .define("alwaysShowDeathPosition", false);
            builder.pop();
        }
    }
}
