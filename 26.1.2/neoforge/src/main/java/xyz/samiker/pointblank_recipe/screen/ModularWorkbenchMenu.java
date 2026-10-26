package xyz.samiker.pointblank_recipe.screen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.block.ModBlocks;
import xyz.samiker.pointblank_recipe.modular.ModularLayout;
import xyz.samiker.pointblank_recipe.modular.ModularRecipe;
import xyz.samiker.pointblank_recipe.modular.ModularTemplate;
import xyz.samiker.pointblank_recipe.resource.ModularRecipeLoader;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One slot per part of the selected template, placed like on a gun, and one result slot. When several guns use the
 * same parts the player flips through them with arrows. Matching only runs on the server, the result reaches the
 * client through normal slot sync.
 */
public class ModularWorkbenchMenu extends AbstractContainerMenu {
    public static final int BUTTON_PREVIOUS_TEMPLATE = 0;
    public static final int BUTTON_NEXT_TEMPLATE = 1;
    public static final int BUTTON_PREVIOUS_RESULT = 2;
    public static final int BUTTON_NEXT_RESULT = 3;

    private static final int DATA_TEMPLATE = 0;
    private static final int DATA_SELECTED = 1;
    private static final int DATA_MATCHES = 2;

    private static final int PART_COUNT = ModularLayout.SLOTS.size();
    private static final int RESULT_INDEX = PART_COUNT;
    private static final int INVENTORY_START = RESULT_INDEX + 1;
    private static final int INVENTORY_END = INVENTORY_START + 36;

    private final ContainerLevelAccess access;
    private final Player player;
    private final SimpleContainer parts = new WatchedContainer();
    private final SimpleContainer result = new SimpleContainer(1);
    private final ContainerData data = new SimpleContainerData(3);
    private ModularRecipe shownRecipe;

    public ModularWorkbenchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ModularWorkbenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModBlocks.MODULAR_WORKBENCH_MENU, containerId);
        this.access = access;
        this.player = playerInventory.player;

        for (int i = 0; i < PART_COUNT; i++) {
            int[] position = ModularLayout.slotPosition(ModularLayout.SLOTS.get(i));
            addSlot(new PartSlot(i, position[0], position[1]));
        }
        addSlot(new ResultSlot(ModularLayout.RESULT_X, ModularLayout.RESULT_Y));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, ModularLayout.INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, ModularLayout.INVENTORY_Y + 58));
        }

        addDataSlots(data);
    }

    public ModularTemplate template() {
        return ModularTemplate.byIndex(data.get(DATA_TEMPLATE));
    }

    /** Which of the matching guns is shown, 0-based. */
    public int selected() {
        return data.get(DATA_SELECTED);
    }

    /** How many guns the parts in the slots can make. */
    public int matches() {
        return data.get(DATA_MATCHES);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.MODULAR_WORKBENCH);
    }

    @Override
    public void slotsChanged(Container container) {
        if (!player.level().isClientSide()) {
            updateResult();
        }
        super.slotsChanged(container);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case BUTTON_PREVIOUS_TEMPLATE, BUTTON_NEXT_TEMPLATE -> {
                // Parts only fit the slots of their template, so hand them back before switching.
                clearContainer(player, parts);
                int step = id == BUTTON_NEXT_TEMPLATE ? 1 : -1;
                data.set(DATA_TEMPLATE, Math.floorMod(data.get(DATA_TEMPLATE) + step, ModularTemplate.ALL.size()));
                data.set(DATA_SELECTED, 0);
            }
            case BUTTON_PREVIOUS_RESULT, BUTTON_NEXT_RESULT -> {
                if (matches() > 0) {
                    int step = id == BUTTON_NEXT_RESULT ? 1 : -1;
                    data.set(DATA_SELECTED, Math.floorMod(selected() + step, matches()));
                }
            }
            default -> {
                return false;
            }
        }
        updateResult();
        return true;
    }

    private List<ModularRecipe> matchingRecipes() {
        ModularTemplate template = template();
        Map<String, ModularRecipe.Stack> slots = slotStacks(template);
        return ModularRecipeLoader.recipes().stream()
                .filter(recipe -> recipe.template().equals(template.name()) && recipe.match(slots, List.of()) != null)
                .sorted(Comparator.comparing(ModularRecipe::resultItem))
                .toList();
    }

    private void updateResult() {
        List<ModularRecipe> matching = matchingRecipes();
        int selected = matching.isEmpty() ? 0 : Math.min(selected(), matching.size() - 1);
        data.set(DATA_MATCHES, matching.size());
        data.set(DATA_SELECTED, selected);

        shownRecipe = matching.isEmpty() ? null : matching.get(selected);
        result.setItem(0, shownRecipe == null
                ? ItemStack.EMPTY
                : new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(shownRecipe.resultItem())), shownRecipe.resultCount()));
    }

    private Map<String, ModularRecipe.Stack> slotStacks(ModularTemplate template) {
        Map<String, ModularRecipe.Stack> slots = new LinkedHashMap<>();
        for (String slot : template.slots()) {
            slots.put(slot, new StackView(parts.getItem(ModularLayout.SLOTS.indexOf(slot))));
        }
        return slots;
    }

    private boolean resultStillMatches() {
        return shownRecipe != null && shownRecipe.match(slotStacks(template()), List.of()) != null;
    }

    /** Takes the parts of the shown recipe; false if the slots no longer match it. */
    private boolean consume() {
        if (!resultStillMatches()) {
            return false;
        }
        // Taking parts re-runs the matching and replaces shownRecipe, so keep the recipe being crafted.
        ModularRecipe recipe = shownRecipe;
        Map<String, ModularRecipe.Stack> slots = slotStacks(template());
        for (Map.Entry<String, ModularRecipe.Stack> slot : slots.entrySet()) {
            int count = recipe.partCount(slot.getKey(), slot.getValue());
            if (count > 0) {
                parts.removeItem(ModularLayout.SLOTS.indexOf(slot.getKey()), count);
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == RESULT_INDEX) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index < RESULT_INDEX) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, RESULT_INDEX, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, parts));
    }

    // Containers no longer take listeners, so report changes the way vanilla menus do.
    private class WatchedContainer extends SimpleContainer {
        WatchedContainer() {
            super(PART_COUNT);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            slotsChanged(this);
        }
    }

    private class PartSlot extends Slot {
        PartSlot(int index, int x, int y) {
            super(parts, index, x, y);
        }

        public String slotName() {
            return ModularLayout.SLOTS.get(getContainerSlot());
        }

        @Override
        public boolean isActive() {
            return template().has(slotName());
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!isActive()) {
                return false;
            }
            if (slotName().equals(ModularTemplate.BASE_SLOT)) {
                return true;
            }
            return stack.is(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "parts/" + slotName())));
        }
    }

    private class ResultSlot extends Slot {
        ResultSlot(int x, int y) {
            super(result, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        // The server re-checks the recipe, so a stale result can never be taken for free.
        @Override
        public boolean mayPickup(Player player) {
            return player.level().isClientSide() || resultStillMatches();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (!player.level().isClientSide()) {
                consume();
            }
            super.onTake(player, stack);
        }
    }

    public static String slotName(Slot slot) {
        return slot instanceof ModularWorkbenchMenu.PartSlot part ? part.slotName() : null;
    }

    private record StackView(ItemStack stack) implements ModularRecipe.Stack {
        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public String itemId() {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        }

        @Override
        public boolean isIn(String tagId) {
            return stack.is(TagKey.create(Registries.ITEM, Identifier.parse(tagId)));
        }

        @Override
        public int count() {
            return stack.getCount();
        }
    }
}
