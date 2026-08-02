package com.vansqmod.integration.beltborne;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayer;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayerRegistry;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class BeltborneLanternCuriosIntegration {

    private static final BeltborneLanternCurioItem LAMP_CURIO = new BeltborneLanternCurioItem();

    /**
     * Hardcoded Beltborne lamp compatibility list (requested by pack).
     *
     * <p>These entries are registered into Beltborne's {@link LampRegistry} (for rendering/swing)
     * and as Curios belt-curios (for equipping).</p>
     */
    private static final List<ResourceLocation> EXTRA_COMPAT_LAMPS = List.of(
            // Caverns & Chasms copper lanterns
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "copper_lantern"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "cupric_lantern"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "exposed_copper_lantern"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "oxidized_copper_lantern"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "waxed_copper_lantern"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "waxed_exposed_copper_lantern"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "waxed_oxidized_copper_lantern"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "waxed_weathered_copper_lantern"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "weathered_copper_lantern"),

            // Pigsteel lanterns
            ResourceLocation.fromNamespaceAndPath("pigsteel", "corrupted_pigsteel_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "corrupted_pigsteel_soul_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "infected_pigsteel_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "infected_pigsteel_soul_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "pigsteel_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "pigsteel_soul_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "waxed_corrupted_pigsteel_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "waxed_corrupted_pigsteel_soul_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "waxed_infected_pigsteel_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "waxed_infected_pigsteel_soul_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "waxed_pigsteel_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "waxed_pigsteel_soul_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "waxed_zombified_pigsteel_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "waxed_zombified_pigsteel_soul_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "zombified_pigsteel_lantern"),
            ResourceLocation.fromNamespaceAndPath("pigsteel", "zombified_pigsteel_soul_lantern"),

            // Dungeons Delight
            ResourceLocation.fromNamespaceAndPath("dungeonsdelight", "living_lantern")
    );

    private BeltborneLanternCuriosIntegration() {
    }

    public static void init(IEventBus modBus) {
        if (!isEnabled()) {
            return;
        }
        modBus.addListener(BeltborneLanternCuriosIntegration::onCommonSetup);
    }

    private static boolean isEnabled() {
        return ModList.get().isLoaded("beltborne_lanterns") && ModList.get().isLoaded("curios");
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ensureCompatibilityLayerRegistered();
            // Beltborne's own tag-based lamps
            BuiltInRegistries.ITEM.getTagOrEmpty(BeltborneLanternEquipment.LAMPS_TAG)
                    .forEach(holder -> CuriosApi.registerCurio(holder.value(), LAMP_CURIO));

            // Pack hardcoded lamps
            registerExtraCompatLamps();
        });
    }

    /**
     * Ensures lamps in {@link #EXTRA_COMPAT_LAMPS} can be equipped in Curios {@code belt} and have a corresponding
     * Beltborne render state (when they are {@link BlockItem}s).
     */
    private static void registerExtraCompatLamps() {
        for (ResourceLocation id : EXTRA_COMPAT_LAMPS) {
            var item = BuiltInRegistries.ITEM.get(id);
            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                continue;
            }

            CuriosApi.registerCurio(item, LAMP_CURIO);

            // Enable Beltborne rendering/model selection for block-backed lanterns.
            // If it's not a BlockItem, Beltborne can't render it as a block-lantern model.
            if (item instanceof BlockItem blockItem) {
                BlockState state = blockItem.getBlock().defaultBlockState();
                if (state.hasProperty(BlockStateProperties.HANGING)) {
                    state = state.setValue(BlockStateProperties.HANGING, false);
                }
                LampRegistry.register(item, state);
            }
        }
    }

    /**
     * When tags reload, Beltborne re-initializes its lamp registry. Re-apply config-file lamps afterwards.
     * This is important for client-side rendering too.
     */
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (!isEnabled()) {
            return;
        }
        // LampRegistry.init() is triggered inside Beltborne on tags update; we follow up with our extensions.
        registerExtraCompatLamps();
    }

    /**
     * Beltborne loads SPI layers during its own mod init; register ours if that pass ran without Curios/vansq.
     */
    static void ensureCompatibilityLayerRegistered() {
        List<CompatibilityLayer> layers = CompatibilityLayerRegistry.getLayers();
        for (CompatibilityLayer layer : layers) {
            if (BeltborneCuriosCompatibilityLayer.INSTANCE.getModId().equals(layer.getModId())) {
                return;
            }
        }
        layers.add(BeltborneCuriosCompatibilityLayer.INSTANCE);
        BeltborneCuriosCompatibilityLayer.INSTANCE.onInitialize();
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (!isEnabled()) {
            return;
        }
        if (!BeltborneLanternEquipment.BELT_SLOT.equals(event.getIdentifier())) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        ItemStack to = event.getTo();
        ItemStack from = event.getFrom();
        boolean toLamp = BeltborneLanternEquipment.isLamp(to);
        boolean fromLamp = BeltborneLanternEquipment.isLamp(from);

        if (toLamp) {
            BeltborneLanternSync.applyEquipped(player, to);
        } else if (fromLamp) {
            BeltborneLanternSync.clear(player);
        }
    }

    /** After Beltborne restores virtual belt state, move any legacy lamp onto Curios belt. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!isEnabled() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }

        player.level().getServer().execute(() -> migrateLegacyBeltState(player));
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!isEnabled() || !event.isWasDeath()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }

        newPlayer.level().getServer().execute(() -> {
            BeltborneLanternEquipment.getBeltLamp(newPlayer)
                    .ifPresentOrElse(
                            stack -> BeltborneLanternSync.applyEquipped(newPlayer, stack),
                            () -> BeltborneLanternSync.clear(newPlayer)
                    );
        });
    }

    private static void migrateLegacyBeltState(ServerPlayer player) {
        if (BeltborneLanternEquipment.getBeltLamp(player).isPresent()) {
            BeltborneLanternEquipment.getBeltLamp(player).ifPresent(stack -> BeltborneLanternSync.applyEquipped(player, stack));
            return;
        }

        ItemStack virtual = BeltState.getLampStack(player);
        if (virtual != null && !virtual.isEmpty()) {
            BeltborneLanternEquipment.setBeltLamp(player, virtual.copy());
            BeltborneLanternSync.applyEquipped(player, virtual);
        }
    }
}
