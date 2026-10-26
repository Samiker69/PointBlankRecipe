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
public class ModularWorkbenchMenu extends AbstractContainerMenu {
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

    private static final int DATA_TEMPLATE = 0;
    private static final int DATA_PAGE = 1;
    private static final int DATA_PAGE_COUNT = 2;

    private static final int MATERIALS_START = GRID_SIZE;
    private static final int RESULTS_START = MATERIALS_START + GRID_SIZE;
    private static final int INVENTORY_START = RESULTS_START + RESULTS_PER_PAGE;
    private static final int INVENTORY_END = INVENTORY_START + 36;

    private final ContainerLevelAccess access;
    private final Player player;
    private final SimpleContainer parts = new SimpleContainer(GRID_SIZE);
    private final SimpleContainer materials = new SimpleContainer(GRID_SIZE);
    private final SimpleContainer results = new SimpleContainer(RESULTS_PER_PAGE);
    private final ContainerData data = new SimpleContainerData(3);
    private final List<ModularRecipe> shownRecipes = new ArrayList<>();

    public ModularWorkbenchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ModularWorkbenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModBlocks.MODULAR_WORKBENCH_MENU, containerId);
        this.access = access;
        this.player = playerInventory.player;
        data.set(DATA_PAGE_COUNT, 1);

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

        addDataSlots(data);
        parts.addListener(this::slotsChanged);
        materials.addListener(this::slotsChanged);
    }

    public ModularTemplate template() {
        return ModularTemplate.byIndex(data.get(DATA_TEMPLATE));
    }

    public int page() {
        return data.get(DATA_PAGE);
    }

    public int pageCount() {
        return data.get(DATA_PAGE_COUNT);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.MODULAR_WORKBENCH);
    }

    @Override
    public void slotsChanged(Container container) {
        if (!player.level().isClientSide()) {
            updateResults();
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
                data.set(DATA_PAGE, 0);
            }
            case BUTTON_PREVIOUS_PAGE -> data.set(DATA_PAGE, Math.max(0, page() - 1));
            case BUTTON_NEXT_PAGE -> data.set(DATA_PAGE, Math.min(pageCount() - 1, page() + 1));
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
        data.set(DATA_PAGE_COUNT, pageCount);
        data.set(DATA_PAGE, page);

        shownRecipes.clear();
        for (int i = 0; i < RESULTS_PER_PAGE; i++) {
            int index = page * RESULTS_PER_PAGE + i;
            if (index < matching.size()) {
                ModularRecipe recipe = matching.get(index);
                shownRecipes.add(recipe);
                results.setItem(i, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(recipe.resultItem())), recipe.resultCount()));
            } else {
                shownRecipes.add(null);
                results.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private Map<String, ModularRecipe.Stack> slotStacks(ModularTemplate template) {
        Map<String, ModularRecipe.Stack> slots = new LinkedHashMap<>();
        for (int i = 0; i < template.slots().size(); i++) {
            slots.put(template.slotAt(i), new StackView(parts.getItem(i)));
        }
        return slots;
    }

    private List<ModularRecipe.Stack> gridStacks() {
        List<ModularRecipe.Stack> grid = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            grid.add(new StackView(materials.getItem(i)));
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
                parts.removeItem(i, count);
            }
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            if (taken[i] > 0) {
                materials.removeItem(i, taken[i]);
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

        if (index >= RESULTS_START && index < INVENTORY_START) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index < RESULTS_START) {
            if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, MATERIALS_START, false) && !moveItemStackTo(stack, MATERIALS_START, RESULTS_START, false)) {
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
        access.execute((level, pos) -> {
            clearContainer(player, parts);
            clearContainer(player, materials);
        });
    }

    private class PartSlot extends Slot {
        PartSlot(int index, int x, int y) {
            super(parts, index, x, y);
        }

        public String slotName() {
            return template().slotAt(getContainerSlot());
        }

        @Override
        public boolean isActive() {
            return slotName() != null;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            String name = slotName();
            if (name == null) {
                return false;
            }
            if (name.equals(ModularTemplate.BASE_SLOT)) {
                return true;
            }
            return stack.is(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PointblankRecipe.MOD_ID, "parts/" + name)));
        }
    }

    private class ResultSlot extends Slot {
        ResultSlot(int index, int x, int y) {
            super(results, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        // The server re-checks the recipe, so a stale result can never be taken for free.
        @Override
        public boolean mayPickup(Player player) {
            return player.level().isClientSide() || matches(getContainerSlot());
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (!player.level().isClientSide()) {
                consume(getContainerSlot());
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
