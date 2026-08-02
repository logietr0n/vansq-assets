package com.vansqmod.mixin.backpacks;

import com.vansqmod.integration.backpacks.BackpackEquipment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Spyglass Improvements + Backpacks: use spyglass stored in a Curios {@code back} backpack.
 * Backpacks' own mixin only checks the chest slot; this mirrors that inject with {@link BackpackEquipment}.
 */
@Mixin(targets = "me.juancarloscp52.spyglass_improvements.client.SpyglassImprovementsClient")
public abstract class BackpackSpyglassMixin {

    @Shadow
    public static KeyMapping useSpyglass;

    @Shadow
    protected abstract void forceUseSpyglass(LocalPlayer player);

    @Inject(method = "onClientTick", at = @At("HEAD"))
    private void vansqmod$spyglassFromBackBackpack(Minecraft client, CallbackInfo ci) {
        if (client.player == null
                || client.gameMode == null
                || !useSpyglass.isDown()
                || ((MinecraftItemUseCooldownInvoker) client).vansqmod$getItemUseCooldown() != 0
                || client.player.isUsingItem()) {
            return;
        }

        ItemStack backpack = BackpackEquipment.getEquippedBackpack(client.player).orElse(ItemStack.EMPTY);
        if (!BackpackEquipment.isBackpack(backpack)
                || !backpack.has(DataComponents.CONTAINER)
                || backpack.get(DataComponents.CONTAINER).stream().noneMatch(stack -> stack.is(Items.SPYGLASS))) {
            return;
        }

        forceUseSpyglass(client.player);
    }
}
