package xyz.samiker.pointblank_recipe.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import xyz.samiker.pointblank_recipe.screen.ModularWorkbenchScreenHandler;

import static xyz.samiker.pointblank_recipe.screen.ModularWorkbenchScreenHandler.*;

// Drawn with plain fills for now; a proper texture belongs to the cosmetic part of the roadmap.
public class ModularWorkbenchScreen extends HandledScreen<ModularWorkbenchScreenHandler> {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int SHADOW = 0xFF373737;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int UNUSED_SLOT = 0xFF6B6B6B;
    private static final int TEXT = 0xFF404040;

    public ModularWorkbenchScreen(ModularWorkbenchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundHeight = 190;
        playerInventoryTitleY = backgroundHeight - 94;
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
        addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
            if (client != null && client.interactionManager != null) {
                client.interactionManager.clickButton(handler.syncId, id);
            }
        }).dimensions(x + left, y + top, 12, 12).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, SHADOW);
        context.fill(x + 1, y + 1, x + backgroundWidth - 1, y + backgroundHeight - 1, PANEL);

        int used = handler.template().slots().size();
        for (int i = 0; i < GRID_SIZE; i++) {
            drawSlot(context, PARTS_X + i % 3 * 18, GRID_Y + i / 3 * 18, i < used ? SLOT : UNUSED_SLOT);
            drawSlot(context, MATERIALS_X + i % 3 * 18, GRID_Y + i / 3 * 18, SLOT);
        }
        for (int i = 0; i < RESULTS_PER_PAGE; i++) {
            drawSlot(context, RESULTS_X + i % 2 * 18, GRID_Y + i / 2 * 18, SLOT);
        }
        for (int i = 0; i < 36; i++) {
            int row = i / 9;
            drawSlot(context, 8 + i % 9 * 18, row < 3 ? INVENTORY_Y + row * 18 : INVENTORY_Y + 58, SLOT);
        }
    }

    private void drawSlot(DrawContext context, int slotX, int slotY, int color) {
        int left = x + slotX - 1;
        int top = y + slotY - 1;
        context.fill(left, top, left + 18, top + 18, SHADOW);
        context.fill(left + 1, top + 1, left + 18, top + 18, LIGHT);
        context.fill(left + 1, top + 1, left + 17, top + 17, color);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);
        Text template = Text.translatable("template.pointblank_recipe." + handler.template().name());
        int center = (PARTS_X + MATERIALS_X + 54) / 2;
        context.drawText(textRenderer, template, center - textRenderer.getWidth(template) / 2, 17, TEXT, false);

        String page = (handler.page() + 1) + "/" + handler.pageCount();
        context.drawText(textRenderer, page, RESULTS_X + 18 - textRenderer.getWidth(page) / 2, 17, TEXT, false);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
        Slot slot = focusedSlot;
        String name = slot == null ? null : ModularWorkbenchScreenHandler.slotName(slot);
        if (name != null && !slot.hasStack() && handler.getCursorStack().isEmpty()) {
            context.drawTooltip(textRenderer, Text.translatable("slot.pointblank_recipe." + name), mouseX, mouseY);
            return;
        }
        super.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
