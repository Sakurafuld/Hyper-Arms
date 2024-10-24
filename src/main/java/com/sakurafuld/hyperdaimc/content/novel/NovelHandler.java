package com.sakurafuld.hyperdaimc.content.novel;

import com.sakurafuld.hyperdaimc.api.ILivingEntityNovel;
import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.novel.C2SNovelKill;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.Deets.*;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class NovelHandler {
    @SubscribeEvent(receiveCanceled = true)
    @OnlyIn(Dist.CLIENT)
    public static void novel(InputEvent.ClickInputEvent event){
        Minecraft mc = Minecraft.getInstance();
        LOG.debug("isAttack={}-isNovel={}", event.isAttack(), mc.player.getMainHandItem().is(ModItems.NOVEL.get()));
        if(event.isAttack() && mc.player.getMainHandItem().is(ModItems.NOVEL.get())){
            event.setCanceled(true);
            Vec3 view = mc.player.getViewVector(1f);
            Vec3 eye = mc.player.getEyePosition();
            double reach = Math.max(mc.player.getReachDistance(), mc.player.getAttackRange());
            if(mc.player.isShiftKeyDown()) {
                List<Entity> entities = rayTraceEntities(mc.player, eye, eye.add(view.x() * reach, view.y() * reach, view.z() * reach), mc.player.getBoundingBox().expandTowards(view.scale(reach)).inflate(1), 0);
                entities.stream()
                        .filter(entity -> entity.getPose() != Pose.DYING)
                        .min(Comparator.comparingDouble(entity -> entity.position().distanceToSqr(eye)))
                        .ifPresent(entity -> {
                            novelKill(mc.player, entity);
//                            if(entity.getPose() == Pose.DYING) {
//                                List<Entity> noDies = rayTraceEntities(mc.player, eye, eye.add(view.x() * reach, view.y() * reach, view.z() * reach), mc.player.getBoundingBox().expandTowards(view.scale(reach)).inflate(1), 0);
//                                noDies.stream()
//                                        .filter(noDied-> noDied.getPose() != Pose.DYING)
//                                        .min(Comparator.comparingDouble(noDied-> noDied.position().distanceToSqr(eye)))
//                                        .ifPresent(noDied -> novelKill(mc.player, noDied));
//                            }
                        });
            } else {
                List<Entity> entities = rayTraceEntities(mc.player, eye.subtract(view.x() , view.y() , view.z()), eye.add(view.x() * reach, view.y() * reach, view.z() * reach), mc.player.getBoundingBox().expandTowards(view.scale(reach)).inflate(1), 0.5f);
                for(Entity entity : entities){
                    novelKill(mc.player, entity);
                }
            }
        }
    }

    public static void novelKill(LivingEntity from, Entity entity){
        LOG.debug("{}-novelKill={}", side(), entity.getDisplayName().getString());
        if(!(entity instanceof ILivingEntityNovel target)){
            if(entity instanceof PartEntity<?> part){
                entity = part.getParent();
                novelKill(from, entity);
                LOG.debug("{}-Part", side());
            }
            LOG.debug("{}-Entity", side());
            entity.kill();
            if(side().isClient())
                PacketHandler.INSTANCE.sendToServer(new C2SNovelKill(entity.getId()));
            return;
        }
        target.novelKill(from);
        if(side().isClient())
            PacketHandler.INSTANCE.sendToServer(new C2SNovelKill(entity.getId()));
    }
    public static List<Entity> rayTraceEntities(Entity owner, Vec3 start, Vec3 end, AABB area, float adjust) {
        List<Entity> entities = new ArrayList<>();

        for(Entity entity : owner.getLevel().getEntities(owner, area, entity -> true)) {
            AABB aabb = entity.getBoundingBox().inflate(adjust);
            Optional<Vec3> optional = aabb.clip(start, end);
            if (optional.isPresent()) {
                entities.add(entity);
            }
        }
        return entities;
    }
}
