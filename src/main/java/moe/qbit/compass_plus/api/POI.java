package moe.qbit.compass_plus.api;

import com.google.common.annotations.Beta;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@Beta
public record POI(Vec2 pos, CompassIcon icon, Color color) {
    public static POI from(BlockPos pos, CompassIcon icon, Color color){
        return POI.from(new Vec3(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f), icon, color);
    }
    public static POI from(Vec3 pos, CompassIcon icon, Color color){
        return new POI(new Vec2((float) pos.x, (float) pos.z), icon, color);
    }

}
