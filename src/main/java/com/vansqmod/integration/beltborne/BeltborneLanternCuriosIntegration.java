package com.vansqmod.integration.beltborne;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.util.TriState;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioCanEquipEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayer;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayerRegistry;

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
            registerAllLamps();
        });
    }

    /**
     * Tags are empty during common setup, so vanilla lanterns never received {@code registerCurio}
     * there. Re-scan hanging lanterns whenever tags reload so hotbar right-click can equip them.
     */
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (!isEnabled()) {
            return;
        }
        // LampRegistry.init() is triggered inside Beltborne on tags update; we follow up with our extensions.
        registerAllLamps();
    }

    private static void registerAllLamps() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (BeltborneLanternEquipment.isHangingLanternItem(item)) {
                registerLamp(item);
            }
        }
        BuiltInRegistries.ITEM.getTagOrEmpty(BeltborneLanternEquipment.LAMPS_TAG)
                .forEach(holder -> registerLamp(holder.value()));
        for (ResourceLocation id : EXTRA_COMPAT_LAMPS) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item != null && item != Items.AIR) {
                registerLamp(item);
            }
        }
    }

    /**
     * Beltborne rendering needs a block state; Curios right-click equip needs {@link #LAMP_CURIO}.
     */
    private static void registerLamp(Item item) {
        if (item == null || item == Items.AIR) {
            return;
        }
        CuriosApi.registerCurio(item, LAMP_CURIO);
        if (item instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            if (state.hasProperty(BlockStateProperties.HANGING)) {
                state = state.setValue(BlockStateProperties.HANGING, false);
            }
            LampRegistry.register(item, state);
        }
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

    /**
     * Accept hanging lanterns even when they are missing from {@code curios:belt}, and refuse a
     * second lantern while another belt index already holds one.
     */
    @SubscribeEvent
    public static void onCanEquip(CurioCanEquipEvent event) {
        if (!isEnabled()) {
            return;
        }
        SlotContext ctx = event.getSlotContext();
        ItemStack stack = event.getStack();
        if (!BeltborneLanternEquipment.isLamp(stack)
                || !BeltborneLanternEquipment.BELT_SLOT.equals(ctx.identifier())) {
            return;
        }
        event.setEquipResult(BeltborneLanternEquipment.canPlaceLanternInBelt(ctx, stack)
                ? TriState.TRUE
                : TriState.FALSE);
    }

    /**
     * Cursor lantern on an occupied belt slot: reject same-type merges (no flash),
     * and 1-for-1 swap with a different lantern or another belt item (toolbelt, etc.).
     */
    @SubscribeEvent
    public static void onLanternStackedOnBelt(ItemStackedOnOtherEvent event) {
        if (!isEnabled()) {
            return;
        }
        ItemStack carried = event.getCarriedItem();
        ItemStack onSlot = event.getStackedOnItem();
        if (!BeltborneLanternEquipment.isLamp(carried) || onSlot.isEmpty()) {
            return;
        }
        Slot slot = event.getSlot();
        if (!isBeltCurioSlot(slot)) {
            return;
        }
        if (ItemStack.isSameItemSameComponents(carried, onSlot)) {
            event.setCanceled(true);
            return;
        }
        SlotContext ctx = beltSlotContext(slot, event.getPlayer());
        if (ctx == null || !BeltborneLanternEquipment.canPlaceLanternInBelt(ctx, carried)) {
            event.setCanceled(true);
            return;
        }

        ItemStack equipped = carried.copyWithCount(1);
        ItemStack previous = onSlot.copy();
        slot.setByPlayer(equipped);
        ItemStack leftover = carried.copy();
        leftover.shrink(1);
        if (leftover.isEmpty()) {
            event.getCarriedSlotAccess().set(previous);
        } else {
            event.getCarriedSlotAccess().set(leftover);
            Player player = event.getPlayer();
            if (!player.getInventory().add(previous)) {
                player.drop(previous, false);
            }
        }
        event.setCanceled(true);
    }

    private static boolean isBeltCurioSlot(Slot slot) {
        try {
            Object identifier = slot.getClass().getMethod("getIdentifier").invoke(slot);
            return BeltborneLanternEquipment.BELT_SLOT.equals(identifier);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static SlotContext beltSlotContext(Slot slot, Player player) {
        try {
            Object ctx = slot.getClass().getMethod("getSlotContext").invoke(slot);
            if (ctx instanceof SlotContext slotContext) {
                return slotContext;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return new SlotContext(BeltborneLanternEquipment.BELT_SLOT, player, slot.getContainerSlot(), false, true);
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

        if (toLamp && to.getCount() > 1) {
            unstackBeltLantern(player, event.getSlotIndex(), to);
            return;
        }

        if (toLamp) {
            BeltborneLanternSync.applyEquipped(player, to);
        } else if (fromLamp) {
            BeltborneLanternSync.clear(player);
        }
    }

    /**
     * Safety net if anything still writes a stacked lantern into the belt. Keep one on the belt
     * and put leftovers back on the used hotbar slot, not the cursor.
     */
    private static void unstackBeltLantern(ServerPlayer player, int slotIndex, ItemStack stacked) {
        int extra = stacked.getCount() - 1;
        ItemStack one = stacked.copyWithCount(1);
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.setEquippedCurio(BeltborneLanternEquipment.BELT_SLOT, slotIndex, one));
        if (extra > 0) {
            restoreLanternRemainder(player, stacked.copyWithCount(extra));
        }
        BeltborneLanternSync.applyEquipped(player, one);
    }

    private static void restoreLanternRemainder(ServerPlayer player, ItemStack remainder) {
        ItemStack selected = player.getInventory().getSelected();
        if (selected.isEmpty()) {
            player.getInventory().setItem(player.getInventory().selected, remainder);
            return;
        }
        if (ItemStack.isSameItemSameComponents(selected, remainder)) {
            int space = selected.getMaxStackSize() - selected.getCount();
            int merge = Math.min(space, remainder.getCount());
            if (merge > 0) {
                selected.grow(merge);
                remainder.shrink(merge);
            }
            if (remainder.isEmpty()) {
                return;
            }
        }
        ItemStack carried = player.containerMenu.getCarried();
        if (carried.isEmpty()) {
            player.containerMenu.setCarried(remainder);
            return;
        }
        if (ItemStack.isSameItemSameComponents(carried, remainder)) {
            int space = carried.getMaxStackSize() - carried.getCount();
            int merge = Math.min(space, remainder.getCount());
            if (merge > 0) {
                carried.grow(merge);
                remainder.shrink(merge);
            }
            if (remainder.isEmpty()) {
                return;
            }
        }
        player.drop(remainder, false);
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
