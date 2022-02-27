package moe.qbit.terarrian_trinkets.common.items.component;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClimbingComponent extends ComponentType.Double {
    public static final ClimbingComponent INSTANCE = new ClimbingComponent();

    private ClimbingComponent() { }

    @Override
    public void tick(ItemStack stack, java.lang.Double value, Entity entity) {
        if (value > 0 && entity instanceof LivingEntity) {
            Vec3 pos = entity.position();
            Vec3 look = Vec3.directionFromRotation(0, entity.getRotationVector().y).normalize().multiply(.5, .5, .5);
            if (look.lengthSqr()==0) return;

            Vec3 target = pos.add(look);

            BlockHitResult r= entity.level.clip(new ClipContext(pos, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            if (r.getType() == HitResult.Type.BLOCK) {
                BlockState state= entity.level.getBlockState(r.getBlockPos());
                if(state.isFaceSturdy(entity.level, r.getBlockPos(), r.getDirection())){
                    if(entity.isCrouching()){
                        Vec3 deltaMovement = entity.getDeltaMovement();

                        if (((LivingEntity) entity).zza > 0F) {
                            entity.setDeltaMovement(new Vec3(deltaMovement.x, Math.max(deltaMovement.y, value), deltaMovement.z));
                        }else{
                            entity.setDeltaMovement(new Vec3(deltaMovement.x, Math.max(deltaMovement.y, 0), deltaMovement.z));
                        }

                        entity.fallDistance = 0;
                    }
                }
            }
        }
    }
}
