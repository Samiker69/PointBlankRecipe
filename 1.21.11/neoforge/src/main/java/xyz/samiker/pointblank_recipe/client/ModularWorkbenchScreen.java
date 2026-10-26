package xyz.samiker.pointblank_recipe.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchMenu;

import static xyz.samiker.pointblank_recipe.screen.ModularWorkbenchMenu.*;

// Drawn with plain fills for now; a proper texture belongs to the cosmetic part of the roadmap.
public class ModularWorkbenchScreen extends AbstractContainerScreen<ModularWorkbenchMenu> {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int SHADOW = 0xFF373737;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int UNUSED_SLOT = 0xFF6B6B6B;
    private static final int TEXT = 0xFF404040;

    public ModularWorkbenchScreen(ModularWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 190;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        addButton("<", BUTTON_PREVIOUS_TEMPLATE, PARTS_X - 1, 15);
        addButton(">", BUTTON_NEXT_TEMPLATE, MATERIALS_X + 43, 15);
        addButton("<", BUTTON_PREVIOUS_PAGE, RESULTS_X - 1, GRID_Y + 56);
        addButton(">", BUTTON_NEXT_PAGE, RESULTS_X + 23, GRID_Y + 56);
    }

    private void addButton(String label, int id, int left, int top) {
        addRenderableWidget(Button.builder(Component.literal(label), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(leftPos + left, topPos + top, 12, 12).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, SHADOW);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, PANEL);

        int used = menu.template().slots().size();
        for (int i = 0; i < GRID_SIZE; i++) {
            drawSlot(graphics, PARTS_X + i % 3 * 18, GRID_Y + i / 3 * 18, i < used ? SLOT : UNUSED_SLOT);
            drawSlot(graphics, MATERIALS_X + i % 3 * 18, GRID_Y + i / 3 * 18, SLOT);
        }
        for (int i = 0; i < RESULTS_PER_PAGE; i++) {
            drawSlot(graphics, RESULTS_X + i % 2 * 18, GRID_Y + i / 2 * 18, SLOT);
        }
        for (int i = 0; i < 36; i++) {
            int row = i / 9;
            drawSlot(graphics, 8 + i % 9 * 18, row < 3 ? INVENTORY_Y + row * 18 : INVENTORY_Y + 58, SLOT);
        }
    }

    private void drawSlot(GuiGraphics graphics, int slotX, int slotY, int color) {
        int left = leftPos + slotX - 1;
        int top = topPos + slotY - 1;
        graphics.fill(left, top, left + 18, top + 18, SHADOW);
        graphics.fill(left + 1, top + 1, left + 18, top + 18, LIGHT);
        graphics.fill(left + 1, top + 1, left + 17, top + 17, color);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        Component template = Component.translatable("template.pointblank_recipe." + menu.template().name());
        int center = (PARTS_X + MATERIALS_X + 54) / 2;
        graphics.drawString(font, template, center - font.width(template) / 2, 17, TEXT, false);

        String page = (menu.page() + 1) + "/" + menu.pageCount();
        graphics.drawString(font, page, RESULTS_X + 18 - font.width(page) / 2, 17, TEXT, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        Slot slot = hoveredSlot;
        String name = slot == null ? null : ModularWorkbenchMenu.slotName(slot);
        if (name != null && !slot.hasItem() && menu.getCarried().isEmpty()) {
            graphics.setTooltipForNextFrame(font, Component.translatable("slot.pointblank_recipe." + name), mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }
}
