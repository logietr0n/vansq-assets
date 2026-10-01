package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.integration.charm.CharmTotemEquipment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Puts the Totem of Undying chest renderer on extra Charm totems.
 * Does not call {@link CuriosRendererRegistry#load()} — that re-instantiates every
 * Curios renderer and crashes Artifacts if model layers are not ready yet.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class CharmTotemCuriosClient {

    private CharmTotemCuriosClient() {
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        registerCharmRenderers();
    }

    static void registerCharmRenderers() {
        if (!ModList.get().isLoaded("curios")) {
            return;
        }
        for (ResourceLocation id : CharmTotemEquipment.EXTRA_CHEST_TOTEMS) {
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(CharmTotemCuriosClient::installTotemRenderer);
        }
    }

    private static void installTotemRenderer(Item item) {
        CuriosRendererRegistry.register(item, CharmTotemCurioRenderer::new);
        putLiveRenderer(item, new CharmTotemCurioRenderer());
    }

    @SuppressWarnings("unchecked")
    private static void putLiveRenderer(Item item, ICurioRenderer renderer) {
        try {
            Field field = CuriosRendererRegistry.class.getDeclaredField("RENDERERS");
            field.setAccessible(true);
            Map<Item, ICurioRenderer> renderers = (Map<Item, ICurioRenderer>) field.get(null);
            renderers.put(item, renderer);
        } catch (ReflectiveOperationException e) {
            VansqMod.LOGGER.warn("Could not install charm totem renderer for {}", BuiltInRegistries.ITEM.getKey(item), e);
        }
    }
}
