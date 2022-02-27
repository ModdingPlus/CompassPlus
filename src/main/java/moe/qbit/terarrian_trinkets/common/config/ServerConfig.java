package moe.qbit.terarrian_trinkets.common.config;

import moe.qbit.terarrian_trinkets.TerrarianTrinkets;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = TerrarianTrinkets.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig
{
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ServerConfigTemplate SERVER;

    static
    {
        final Pair<ServerConfigTemplate, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfigTemplate::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static int multiJumps = 0;
    public static int coyoteTime = 0;
    public static double stepHeight = 0d;
    public static double stepHeightSneaking = 0d;
    public static double jumpHeightBoost = 0d;

    // Any config that has to deal with datapack stuffs
    public static class ServerConfigTemplate
    {
        public final ForgeConfigSpec.IntValue multiJumps;
        public final ForgeConfigSpec.IntValue coyoteTime;
        public final ForgeConfigSpec.DoubleValue stepHeight;
        public final ForgeConfigSpec.DoubleValue stepHeightSneaking;
        public final ForgeConfigSpec.DoubleValue jumpHeightBoost;

        ServerConfigTemplate(ForgeConfigSpec.Builder builder)
        {
            builder.push("general");
            multiJumps = builder
                    .comment("The base amount of midair jumps every player has.")
                    .translation("text."+ TerrarianTrinkets.MODID+".config.multiJumps")
                    .defineInRange("multiJumps", 0, 0, 1024);
            coyoteTime = builder
                    .comment("The base coyote time every player has in ticks.")
                    .translation("text."+ TerrarianTrinkets.MODID+".config.coyoteTime")
                    .defineInRange("coyoteTime", 3, 0, 65536);
            stepHeight = builder
                    .comment("The base step height every player has in blocks.")
                    .translation("text."+ TerrarianTrinkets.MODID+".config.stepHeight")
                    .defineInRange("stepHeight", 0.6d, 0d, 8d);
            stepHeightSneaking = builder
                    .comment("The base step height every player has in blocks when sneaking. It's highly recommended to keep this at the default value.")
                    .translation("text."+ TerrarianTrinkets.MODID+".config.stepHeightSneaking")
                    .defineInRange("stepHeightSneaking", 0.6d, 0d, 8d);
            jumpHeightBoost = builder
                    .comment("Additional base jump height in equivalent levels of the jump boost potion effect. Negative values possible.")
                    .translation("text."+ TerrarianTrinkets.MODID+".config.jumpHeightBoost")
                    .defineInRange("jumpHeightBoost", 0.0d, -128d, 127d);
            builder.pop();
        }
    }

    @SubscribeEvent
    public static void modConfig(ModConfigEvent event)
    {
        ModConfig config = event.getConfig();
        if (config.getSpec() == SERVER_SPEC) {
            multiJumps = SERVER.multiJumps.get();
            coyoteTime = SERVER.coyoteTime.get();
            stepHeight = SERVER.stepHeight.get();
            stepHeightSneaking = SERVER.stepHeightSneaking.get();
            jumpHeightBoost = SERVER.jumpHeightBoost.get();
        }
    }
}