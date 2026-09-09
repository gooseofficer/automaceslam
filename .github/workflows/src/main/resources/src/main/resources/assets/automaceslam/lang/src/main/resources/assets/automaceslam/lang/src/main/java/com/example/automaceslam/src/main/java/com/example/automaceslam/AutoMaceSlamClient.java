package com.example.automaceslam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoMaceSlamClient implements ClientModInitializer {
    private static KeyBinding slamKey;
    private static int cooldown = 0;

    @Override
    public void onInitializeClient() {
        slamKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.automaceslam.activate",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.automaceslam"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (cooldown > 0) cooldown--;

            if (slamKey.wasPressed() && cooldown == 0) {
                ClientPlayNetworking.send(new AutoMaceSlamMod.SlamPayload());
                cooldown = 20;
            }
        });
    }
}
