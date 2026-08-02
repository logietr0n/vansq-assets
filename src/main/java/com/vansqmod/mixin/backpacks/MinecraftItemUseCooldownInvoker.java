package com.vansqmod.mixin.backpacks;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftItemUseCooldownInvoker {

    @Invoker("getItemUseCooldown")
    int vansqmod$getItemUseCooldown();
}
