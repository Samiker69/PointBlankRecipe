package xyz.samiker.pointblank_recipe.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import xyz.samiker.pointblank_recipe.PointblankRecipe;
import xyz.samiker.pointblank_recipe.block.ModBlocks;
import xyz.samiker.pointblank_recipe.modular.ModularLayout;
import xyz.samiker.pointblank_recipe.modular.ModularRecipe;
import xyz.samiker.pointblank_recipe.modular.ModularTemplate;
import xyz.samiker.pointblank_recipe.resource.ModularRecipeLoader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One slot per part of the selected template, placed like on a gun, and one result slot. When several guns use the
 * same parts the player flips through them with arrows. Matching only runs on the server, the result reaches the
 * client through normal slot sync.
 */
public class ModularWorkbenchScreenHandler extends ScreenHandler {
    public static final int BUTTON_PREVIOUS_TEMPLATE = 0;
    public static final int BUTTON_NEXT_TEMPLATE = 1;
    public static final int BUTTON_PREVIOUS_RESULT = 2;
    public static final int BUTTON_NEXT_RESULT = 3;

    private static final int PROPERTY_TEMPLATE = 0;
    private static final int PROPERTY_SELECTED = 1;
    private static final int PROPERTY_MATCHES = 2;

    private static final int PART_COUNT = ModularLayout.SLOTS.size();
    private static final int RESULT_INDEX = PART_COUNT;
    private static final int INVENTORY_START = RESULT_INDEX + 1;
    private static final int INVENTORY_END = INVENTORY_START + 36;

    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    private final SimpleInventory parts = new SimpleInventory(PART_COUNT);
    private final SimpleInventory result = new SimpleInventory(1);
    private final PropertyDelegate properties = new ArrayPropertyDelegate(3);
    private ModularRecipe shownRecipe;

    public ModularWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ModularWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModBlocks.MODULAR_WORKBENCH_SCREEN, syncId);
        this.context = context;
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

        addProperties(properties);
        parts.addListener(this::onContentChanged);
    }

    public ModularTemplate template() {
        return ModularTemplate.byIndex(properties.get(PROPERTY_TEMPLATE));
    }

    /** Which of the matching guns is shown, 0-based. */
    public int selected() {
        return properties.get(PROPERTY_SELECTED);
    }

    /** How many guns the parts in the slots can make. */
    public int matches() {
        return properties.get(PROPERTY_MATCHES);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, ModBlocks.MODULAR_WORKBENCH);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        if (!player.getWorld().isClient) {
            updateResult();
        }
        super.onContentChanged(inventory);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        switch (id) {
            case BUTTON_PREVIOUS_TEMPLATE, BUTTON_NEXT_TEMPLATE -> {
                // Parts only fit the slots of their template, so hand them back before switching.
                dropInventory(player, parts);
                int step = id == BUTTON_NEXT_TEMPLATE ? 1 : -1;
                properties.set(PROPERTY_TEMPLATE, Math.floorMod(properties.get(PROPERTY_TEMPLATE) + step, ModularTemplate.ALL.size()));
                properties.set(PROPERTY_SELECTED, 0);
            }
            case BUTTON_PREVIOUS_RESULT, BUTTON_NEXT_RESULT -> {
                if (matches() > 0) {
                    int step = id == BUTTON_NEXT_RESULT ? 1 : -1;
                    properties.set(PROPERTY_SELECTED, Math.floorMod(selected() + step, matches()));
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
        properties.set(PROPERTY_MATCHES, matching.size());
        properties.set(PROPERTY_SELECTED, selected);

        shownRecipe = matching.isEmpty() ? null : matching.get(selected);
        result.setStack(0, shownRecipe == null
                ? ItemStack.EMPTY
                : new ItemStack(Registries.ITEM.get(new Identifier(shownRecipe.resultItem())), shownRecipe.resultCount()));
    }

    private Map<String, ModularRecipe.Stack> slotStacks(ModularTemplate template) {
        Map<String, ModularRecipe.Stack> slots = new LinkedHashMap<>();
        for (String slot : template.slots()) {
            slots.put(slot, new StackView(parts.getStack(ModularLayout.SLOTS.indexOf(slot))));
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
                parts.removeStack(ModularLayout.SLOTS.indexOf(slot.getKey()), count);
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        if (index == RESULT_INDEX) {
            if (!insertItem(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(stack, original);
        } else if (index < RESULT_INDEX) {
            if (!insertItem(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!insertItem(stack, 0, RESULT_INDEX, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTakeItem(player, stack);
        return original;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        context.run((world, pos) -> dropInventory(player, parts));
    }

    private class PartSlot extends Slot {
        PartSlot(int index, int x, int y) {
            super(parts, index, x, y);
        }

        public String slotName() {
            return ModularLayout.SLOTS.get(getIndex());
        }

        @Override
        public boolean isEnabled() {
            return template().has(slotName());
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            if (!isEnabled()) {
                return false;
            }
            if (slotName().equals(ModularTemplate.BASE_SLOT)) {
                return true;
            }
            return stack.isIn(TagKey.of(RegistryKeys.ITEM, Identifier.of(PointblankRecipe.MOD_ID, "parts/" + slotName())));
        }
    }

    private class ResultSlot extends Slot {
        ResultSlot(int x, int y) {
            super(result, 0, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        // The server re-checks the recipe, so a stale result can never be taken for free.
        @Override
        public boolean canTakeItems(PlayerEntity player) {
            return player.getWorld().isClient || resultStillMatches();
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            if (!player.getWorld().isClient) {
                consume();
            }
            super.onTakeItem(player, stack);
        }
    }

    public static String slotName(Slot slot) {
        return slot instanceof ModularWorkbenchScreenHandler.PartSlot part ? part.slotName() : null;
    }

    private record StackView(ItemStack stack) implements ModularRecipe.Stack {
        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public String itemId() {
            return Registries.ITEM.getId(stack.getItem()).toString();
        }

        @Override
        public boolean isIn(String tagId) {
            return stack.isIn(TagKey.of(RegistryKeys.ITEM, new Identifier(tagId)));
        }

        @Override
        public int count() {
            return stack.getCount();
        }
    }
}
