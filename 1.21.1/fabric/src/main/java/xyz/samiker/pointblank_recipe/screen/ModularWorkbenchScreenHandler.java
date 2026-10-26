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
import xyz.samiker.pointblank_recipe.modular.ModularRecipe;
import xyz.samiker.pointblank_recipe.modular.ModularTemplate;
import xyz.samiker.pointblank_recipe.resource.ModularRecipeLoader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parts grid (slots of the selected template) + materials grid; every recipe the contents match is listed in the
 * result slots, the player takes the one they want. Matching only runs on the server, results reach the client
 * through normal slot sync.
 */
public class ModularWorkbenchScreenHandler extends ScreenHandler {
    public static final int GRID_SIZE = 9;
    public static final int RESULTS_PER_PAGE = 6;

    public static final int PARTS_X = 8;
    public static final int MATERIALS_X = 70;
    public static final int RESULTS_X = 134;
    public static final int GRID_Y = 30;
    public static final int INVENTORY_Y = 108;

    public static final int BUTTON_PREVIOUS_TEMPLATE = 0;
    public static final int BUTTON_NEXT_TEMPLATE = 1;
    public static final int BUTTON_PREVIOUS_PAGE = 2;
    public static final int BUTTON_NEXT_PAGE = 3;

    private static final int PROPERTY_TEMPLATE = 0;
    private static final int PROPERTY_PAGE = 1;
    private static final int PROPERTY_PAGE_COUNT = 2;

    private static final int MATERIALS_START = GRID_SIZE;
    private static final int RESULTS_START = MATERIALS_START + GRID_SIZE;
    private static final int INVENTORY_START = RESULTS_START + RESULTS_PER_PAGE;
    private static final int INVENTORY_END = INVENTORY_START + 36;

    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    private final SimpleInventory parts = new SimpleInventory(GRID_SIZE);
    private final SimpleInventory materials = new SimpleInventory(GRID_SIZE);
    private final SimpleInventory results = new SimpleInventory(RESULTS_PER_PAGE);
    private final PropertyDelegate properties = new ArrayPropertyDelegate(3);
    private final List<ModularRecipe> shownRecipes = new ArrayList<>();

    public ModularWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ModularWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModBlocks.MODULAR_WORKBENCH_SCREEN, syncId);
        this.context = context;
        this.player = playerInventory.player;
        properties.set(PROPERTY_PAGE_COUNT, 1);

        for (int i = 0; i < GRID_SIZE; i++) {
            addSlot(new PartSlot(i, PARTS_X + i % 3 * 18, GRID_Y + i / 3 * 18));
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            addSlot(new Slot(materials, i, MATERIALS_X + i % 3 * 18, GRID_Y + i / 3 * 18));
        }
        for (int i = 0; i < RESULTS_PER_PAGE; i++) {
            addSlot(new ResultSlot(i, RESULTS_X + i % 2 * 18, GRID_Y + i / 2 * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, INVENTORY_Y + 58));
        }

        addProperties(properties);
        parts.addListener(this::onContentChanged);
        materials.addListener(this::onContentChanged);
    }

    public ModularTemplate template() {
        return ModularTemplate.byIndex(properties.get(PROPERTY_TEMPLATE));
    }

    public int page() {
        return properties.get(PROPERTY_PAGE);
    }

    public int pageCount() {
        return properties.get(PROPERTY_PAGE_COUNT);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, ModBlocks.MODULAR_WORKBENCH);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        if (!player.getWorld().isClient) {
            updateResults();
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
                properties.set(PROPERTY_PAGE, 0);
            }
            case BUTTON_PREVIOUS_PAGE -> properties.set(PROPERTY_PAGE, Math.max(0, page() - 1));
            case BUTTON_NEXT_PAGE -> properties.set(PROPERTY_PAGE, Math.min(pageCount() - 1, page() + 1));
            default -> {
                return false;
            }
        }
        updateResults();
        return true;
    }

    private void updateResults() {
        ModularTemplate template = template();
        Map<String, ModularRecipe.Stack> slots = slotStacks(template);
        List<ModularRecipe.Stack> grid = gridStacks();

        List<ModularRecipe> matching = ModularRecipeLoader.recipes().stream()
                .filter(recipe -> recipe.template().equals(template.name()) && recipe.match(slots, grid) != null)
                .sorted(Comparator.comparing(ModularRecipe::resultItem))
                .toList();

        int pageCount = Math.max(1, (matching.size() + RESULTS_PER_PAGE - 1) / RESULTS_PER_PAGE);
        int page = Math.min(page(), pageCount - 1);
        properties.set(PROPERTY_PAGE_COUNT, pageCount);
        properties.set(PROPERTY_PAGE, page);

        shownRecipes.clear();
        for (int i = 0; i < RESULTS_PER_PAGE; i++) {
            int index = page * RESULTS_PER_PAGE + i;
            if (index < matching.size()) {
                ModularRecipe recipe = matching.get(index);
                shownRecipes.add(recipe);
                results.setStack(i, new ItemStack(Registries.ITEM.get(Identifier.of(recipe.resultItem())), recipe.resultCount()));
            } else {
                shownRecipes.add(null);
                results.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    private Map<String, ModularRecipe.Stack> slotStacks(ModularTemplate template) {
        Map<String, ModularRecipe.Stack> slots = new LinkedHashMap<>();
        for (int i = 0; i < template.slots().size(); i++) {
            slots.put(template.slotAt(i), new StackView(parts.getStack(i)));
        }
        return slots;
    }

    private List<ModularRecipe.Stack> gridStacks() {
        List<ModularRecipe.Stack> grid = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            grid.add(new StackView(materials.getStack(i)));
        }
        return grid;
    }

    private boolean matches(int resultIndex) {
        ModularRecipe recipe = resultIndex < shownRecipes.size() ? shownRecipes.get(resultIndex) : null;
        return recipe != null && recipe.match(slotStacks(template()), gridStacks()) != null;
    }

    /** Takes the ingredients of the recipe shown in a result slot; false if the contents no longer match. */
    private boolean consume(int resultIndex) {
        ModularRecipe recipe = resultIndex < shownRecipes.size() ? shownRecipes.get(resultIndex) : null;
        if (recipe == null) {
            return false;
        }
        ModularTemplate template = template();
        Map<String, ModularRecipe.Stack> slots = slotStacks(template);
        int[] taken = recipe.match(slots, gridStacks());
        if (taken == null) {
            return false;
        }
        for (int i = 0; i < template.slots().size(); i++) {
            int count = recipe.partCount(template.slotAt(i), slots.get(template.slotAt(i)));
            if (count > 0) {
                parts.removeStack(i, count);
            }
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            if (taken[i] > 0) {
                materials.removeStack(i, taken[i]);
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

        if (index >= RESULTS_START && index < INVENTORY_START) {
            if (!insertItem(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickTransfer(stack, original);
        } else if (index < RESULTS_START) {
            if (!insertItem(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!insertItem(stack, 0, MATERIALS_START, false) && !insertItem(stack, MATERIALS_START, RESULTS_START, false)) {
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
        context.run((world, pos) -> {
            dropInventory(player, parts);
            dropInventory(player, materials);
        });
    }

    private class PartSlot extends Slot {
        PartSlot(int index, int x, int y) {
            super(parts, index, x, y);
        }

        public String slotName() {
            return template().slotAt(getIndex());
        }

        @Override
        public boolean isEnabled() {
            return slotName() != null;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            String name = slotName();
            if (name == null) {
                return false;
            }
            if (name.equals(ModularTemplate.BASE_SLOT)) {
                return true;
            }
            return stack.isIn(TagKey.of(RegistryKeys.ITEM, Identifier.of(PointblankRecipe.MOD_ID, "parts/" + name)));
        }
    }

    private class ResultSlot extends Slot {
        ResultSlot(int index, int x, int y) {
            super(results, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        // The server re-checks the recipe, so a stale result can never be taken for free.
        @Override
        public boolean canTakeItems(PlayerEntity player) {
            return player.getWorld().isClient || matches(getIndex());
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            if (!player.getWorld().isClient) {
                consume(getIndex());
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
            return stack.isIn(TagKey.of(RegistryKeys.ITEM, Identifier.of(tagId)));
        }

        @Override
        public int count() {
            return stack.getCount();
        }
    }
}
