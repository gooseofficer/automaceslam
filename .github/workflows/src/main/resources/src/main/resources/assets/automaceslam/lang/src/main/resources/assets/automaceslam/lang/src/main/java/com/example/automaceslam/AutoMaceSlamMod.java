package com.example.automaceslam;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import java.util.List;

public class AutoMaceSlamMod implements ModInitializer {
    public static final Identifier SLAM_PACKET_ID = Identifier.of("automaceslam", "slam_packet");

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(SlamPayload.ID, SlamPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SlamPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                PlayerEntity player = context.player();
                if (hasMaceInHotbar(player)) {
                    LivingEntity target = findNearestTarget(player);
                    if (target != null) {
                        if (target.isBlocking()) {
                            target.disableShield(true);
                        }

                        int maceSlot = findMaceSlot(player);
                        int currentSlot = player.getInventory().selectedSlot;
                        player.getInventory().selectedSlot = maceSlot;

                        player.attack(target);
                        player.swingHand(Hand.MAIN_HAND);

                        player.getInventory().selectedSlot = currentSlot;
                    }
                }
            });
        });
    }

    private boolean hasMaceInHotbar(PlayerEntity player) {
        return findMaceSlot(player) != -1;
    }

    private int findMaceSlot(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(Items.MACE)) {
                return i;
            }
        }
        return -1;
    }

    private LivingEntity findNearestTarget(PlayerEntity player) {
        World world = player.getWorld();
        Box box = player.getBoundingBox().expand(4.0);
        List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && e.isAttackable());

        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (LivingEntity entity : entities) {
            double dist = entity.squaredDistanceTo(player);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = entity;
            }
        }
        return nearest;
    }

    public record SlamPayload() implements CustomPayload {
        public static final CustomPayload.Id<SlamPayload> ID = new CustomPayload.Id<>(SLAM_PACKET_ID);
        public static final PacketCodec<PacketByteBuf, SlamPayload> CODEC = PacketCodec.unit(new SlamPayload());

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
