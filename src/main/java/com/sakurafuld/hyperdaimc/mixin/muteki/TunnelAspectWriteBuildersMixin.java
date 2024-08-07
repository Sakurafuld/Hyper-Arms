package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.cyclops.integratedtunnels.part.aspect.TunnelAspectWriteBuilders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

@Pseudo
@Mixin(TunnelAspectWriteBuilders.class)
public abstract class TunnelAspectWriteBuildersMixin {
    @ModifyVariable(method = "getEntity", at = @At("STORE"), name = "entities", remap = false)
    private static List<Entity> getEntityMutekiModify$entities(List<Entity> entities){
        return entities.stream()
                .filter(entity -> !(entity instanceof LivingEntity living) || CuriosApi.getCuriosHelper().findCurios(living, ModItems.MUTEKI.get()).isEmpty())
                .toList();
    }
}
