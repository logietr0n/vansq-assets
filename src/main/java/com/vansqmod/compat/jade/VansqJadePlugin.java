package com.vansqmod.compat.jade;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade plugin loaded only when EnchantWithMob is present ({@code @WailaPlugin} value).
 */
@WailaPlugin("enchantwithmob")
public class VansqJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(MobEnchantJadeProvider.INSTANCE, LivingEntity.class);
    }
}
