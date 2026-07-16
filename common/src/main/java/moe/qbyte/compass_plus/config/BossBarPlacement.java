package moe.qbyte.compass_plus.config;

import moe.qbyte.compass_plus.CompassPlus;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;

import java.util.Locale;

/** Where the HUD goes while boss bars are shown. */
public enum BossBarPlacement implements TranslatableEnum {
    /** The HUD moves down below the boss bar stack; boss bars stay in their vanilla spot. */
    BELOW_BOSS_BARS,
    /** The HUD stays at the top of the screen; the boss bar stack is pushed down below it. */
    ABOVE_BOSS_BARS;

    @Override
    public Component getTranslatedName() {
        return Component.translatable(
                "text." + CompassPlus.MODID + ".config.bossBarPlacement." + this.name().toLowerCase(Locale.ROOT));
    }
}
