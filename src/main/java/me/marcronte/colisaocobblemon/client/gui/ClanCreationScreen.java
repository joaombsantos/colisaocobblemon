package me.marcronte.colisaocobblemon.client.gui;

import me.marcronte.colisaocobblemon.network.payloads.ClanPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

public class ClanCreationScreen extends Screen {

    private static final ResourceLocation TEXTURE = ResourceLocation.parse("colisao-cobblemon:textures/gui/clan_creation.png");
    private final int imageWidth = 260;
    private final int imageHeight = 166;

    private EditBox nameBox;
    private EditBox tagBox;
    private Button createBtn;

    private static final String[] COLORS = {"Branco", "Vermelho", "Verde", "Ciano", "Amarelo", "Laranja", "Rosa", "Roxo", "Azul"};
    private static final String[] TYPES = {"Normal", "Fogo", "Água", "Grama", "Elétrico", "Gelo", "Lutador", "Venenoso", "Terra", "Voador", "Psíquico", "Inseto", "Pedra", "Fantasma", "Dragão", "Sombrio", "Aço", "Fada"};

    private int colorIndex = 0;
    private int type1Index = 0;
    private int type2Index = 1;

    private int openDropdown = -1;
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE_ITEMS = 5;

    private boolean isDraggingScrollbar = false;

    public ClanCreationScreen() {
        super(Component.literal("Criar Clan"));
    }

    @Override
    protected void init() {
        int startX = (this.width - imageWidth) / 2;
        int startY = (this.height - imageHeight) / 2;

        this.nameBox = new EditBox(this.font, startX + 30, startY + 30, 200, 20, Component.literal("Nome do Clan"));
        this.nameBox.setMaxLength(20);
        this.nameBox.setFilter(text -> text.matches("^[a-zA-Z0-9_]*$"));
        this.addRenderableWidget(this.nameBox);

        this.tagBox = new EditBox(this.font, startX + 30, startY + 65, 80, 20, Component.literal("TAG"));
        this.tagBox.setMaxLength(3);
        this.tagBox.setFilter(text -> text.matches("^[a-zA-Z0-9_]*$"));
        this.addRenderableWidget(this.tagBox);

        this.createBtn = Button.builder(Component.literal("§aCriar Clan"), btn -> createClan())
                .bounds(startX + 80, startY + 135, 100, 20).build();
        this.addRenderableWidget(this.createBtn);
    }

    private String getColorCode(int index) {
        return switch (index) {
            case 0 -> "§f"; // White
            case 1 -> "§c"; // Red
            case 2 -> "§a"; // Green
            case 3 -> "§b"; // Cyan
            case 4 -> "§e"; // Yellow
            case 5 -> "§6"; // Orange
            case 6 -> "§d"; // Pink
            case 7 -> "§5"; // Purple
            case 8 -> "§9"; // Blue
            default -> "§f";
        };
    }

    private void createClan() {
        String name = this.nameBox.getValue().trim();
        String tag = this.tagBox.getValue().trim();

        boolean isValidName = name.matches("^[a-zA-Z0-9_]+$");
        boolean isValidTag = tag.matches("^[a-zA-Z0-9_]{3}$");

        if (!isValidName || !isValidTag || type1Index == type2Index) {
            return;
        }

        String colorCode = getColorCode(colorIndex);

        ClientPlayNetworking.send(new ClanPayloads.CreateClanPayload(
                name, tag, colorCode /*+ COLORS[colorIndex]*/, TYPES[type1Index], TYPES[type2Index]
        ));

        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        int startX = (this.width - imageWidth) / 2;
        int startY = (this.height - imageHeight) / 2;

        graphics.blit(TEXTURE, startX, startY, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        graphics.drawCenteredString(this.font, "Fundar Novo Clan", startX + imageWidth / 2, startY + 10, 0xFFFFFF);
        graphics.drawString(this.font, "Nome:", startX + 30, startY + 20, 0xFFFFFF);
        graphics.drawString(this.font, "TAG (3 Letras):", startX + 30, startY + 55, 0xFFFFFF);

        if (type1Index == type2Index) {
            graphics.drawCenteredString(this.font, "§cOs tipos devem ser diferentes!", startX + imageWidth / 2, startY + 123, 0xFF0000);
        }

        this.nameBox.setEditable(openDropdown == -1);
        this.tagBox.setEditable(openDropdown == -1);
        this.createBtn.active = (openDropdown == -1 && type1Index != type2Index);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderDropdownHeader(graphics, 0, getColorCode(colorIndex) + COLORS[colorIndex], startX + 120, startY + 65, 110, 20, mouseX, mouseY);
        renderDropdownHeader(graphics, 1, "Principal: " + TYPES[type1Index], startX + 30, startY + 100, 95, 20, mouseX, mouseY);
        renderDropdownHeader(graphics, 2, "Secundário: " + TYPES[type2Index], startX + 135, startY + 100, 95, 20, mouseX, mouseY);

        renderDropdownList(graphics, 0, COLORS, colorIndex, startX + 120, startY + 65, 110, mouseX, mouseY);
        renderDropdownList(graphics, 1, TYPES, type1Index, startX + 30, startY + 100, 95, mouseX, mouseY);
        renderDropdownList(graphics, 2, TYPES, type2Index, startX + 135, startY + 100, 95, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    private void renderDropdownHeader(GuiGraphics graphics, int id, String text, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        graphics.fill(x, y, x + w, y + h, hovered ? 0xFF777777 : 0xFF333333);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF111111);

        int textWidth = this.font.width(text);
        int maxTextWidth = w - 20;
        int textX = x + 5;

        if (textWidth > maxTextWidth) {
            graphics.enableScissor(x + 5, y, x + w - 15, y + h);

            int shift = 0;
            if (hovered) {
                long time = Util.getMillis();
                float cycle = (time % 4000) / 4000.0f;
                float pingPong = cycle < 0.5f ? cycle * 2.0f : (1.0f - cycle) * 2.0f;
                float smooth = Math.max(0.0f, Math.min(1.0f, (pingPong - 0.1f) / 0.8f));

                shift = (int) (smooth * (textWidth - maxTextWidth));
            }

            graphics.drawString(this.font, text, textX - shift, y + 6, 0xFFFFFF);
            graphics.disableScissor();
        } else {
            graphics.drawString(this.font, text, textX, y + 6, 0xFFFFFF);
        }

        graphics.drawString(this.font, openDropdown == id ? "▲" : "▼", x + w - 10, y + 6, 0xAAAAAA);
    }

    private void renderDropdownList(GuiGraphics graphics, int id, String[] options, int selected, int x, int y, int w, int mouseX, int mouseY) {
        if (openDropdown != id) return;

        int listH = Math.min(options.length, MAX_VISIBLE_ITEMS) * 15;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);

        graphics.fill(x, y + 20, x + w, y + 20 + listH, 0xFF555555);
        graphics.fill(x + 1, y + 21, x + w - 1, y + 20 + listH - 1, 0xFF000000);

        for (int i = 0; i < MAX_VISIBLE_ITEMS; i++) {
            int idx = scrollOffset + i;
            if (idx >= options.length) break;

            int itemY = y + 21 + (i * 15);
            boolean hovered = mouseX >= x && mouseX <= x + w - 5 && mouseY >= itemY && mouseY < itemY + 15;

            if (hovered) graphics.fill(x + 1, itemY, x + w - 5, itemY + 15, 0xFF444444);

            String displayText = (id == 0) ? getColorCode(idx) + options[idx] : options[idx];
            int color = (idx == selected) ? 0xFF55FF55 : 0xFFDDDDDD;

            graphics.drawString(this.font, displayText, x + 5, itemY + 4, color);
        }

        if (options.length > MAX_VISIBLE_ITEMS) {
            int scrollBarH = (int) ((MAX_VISIBLE_ITEMS / (float) options.length) * listH);
            int scrollBarY = y + 21 + (int) ((scrollOffset / (float) (options.length - MAX_VISIBLE_ITEMS)) * (listH - scrollBarH - 2));
            graphics.fill(x + w - 4, y + 21, x + w - 1, y + 20 + listH - 1, 0xFF222222);
            graphics.fill(x + w - 4, scrollBarY, x + w - 1, scrollBarY + scrollBarH, isDraggingScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA);
        }
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - imageWidth) / 2;
        int startY = (this.height - imageHeight) / 2;

        if (openDropdown != -1) {
            String[] currentArr = getOpenDropdownArray();
            int listH = Math.min(currentArr.length, MAX_VISIBLE_ITEMS) * 15;
            int dx = getDropdownX(openDropdown, startX);
            int dy = getDropdownY(openDropdown, startY) + 20;
            int dw = getDropdownW(openDropdown);

            if (currentArr.length > MAX_VISIBLE_ITEMS) {
                int scrollBarX = dx + dw - 4;
                int scrollBarW = 3;
                if (mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarW && mouseY >= dy && mouseY <= dy + listH) {
                    this.isDraggingScrollbar = true;
                    updateScrollbarByMouse(mouseY, dy, listH, currentArr.length);
                    return true;
                }
            }

            if (mouseX >= dx && mouseX <= dx + dw - 5 && mouseY >= dy && mouseY <= dy + listH) {
                int clickedIndex = scrollOffset + (int) ((mouseY - dy) / 15);
                if (clickedIndex < currentArr.length) {
                    if (openDropdown == 0) colorIndex = clickedIndex;
                    else if (openDropdown == 1) type1Index = clickedIndex;
                    else if (openDropdown == 2) type2Index = clickedIndex;
                }
            }

            openDropdown = -1;
            return true;
        }

        if (isHoveringHeader(startX + 120, startY + 65, 110, 20, mouseX, mouseY)) { openDropdown = 0; scrollOffset = 0; return true; }
        if (isHoveringHeader(startX + 30, startY + 100, 95, 20, mouseX, mouseY)) { openDropdown = 1; scrollOffset = 0; return true; }
        if (isHoveringHeader(startX + 135, startY + 100, 95, 20, mouseX, mouseY)) { openDropdown = 2; scrollOffset = 0; return true; }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDraggingScrollbar && openDropdown != -1) {
            int startY = (this.height - imageHeight) / 2;
            String[] currentArr = getOpenDropdownArray();
            int listH = Math.min(currentArr.length, MAX_VISIBLE_ITEMS) * 15;
            int dy = getDropdownY(openDropdown, startY) + 20;

            updateScrollbarByMouse(mouseY, dy, listH, currentArr.length);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isDraggingScrollbar) {
            this.isDraggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateScrollbarByMouse(double mouseY, int dy, int listH, int optionsLength) {
        int scrollTrackH = listH - 2;
        int scrollBarH = (int) ((MAX_VISIBLE_ITEMS / (float) optionsLength) * listH);

        double relativeY = mouseY - dy - (scrollBarH / 2.0);
        double scrollPercentage = Math.max(0, Math.min(1, relativeY / (scrollTrackH - scrollBarH)));

        this.scrollOffset = (int) Math.round(scrollPercentage * (optionsLength - MAX_VISIBLE_ITEMS));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (openDropdown != -1) {
            String[] currentArr = getOpenDropdownArray();
            if (currentArr.length > MAX_VISIBLE_ITEMS) {
                if (scrollY > 0 && scrollOffset > 0) scrollOffset--;
                if (scrollY < 0 && scrollOffset < currentArr.length - MAX_VISIBLE_ITEMS) scrollOffset++;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isHoveringHeader(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private String[] getOpenDropdownArray() { return openDropdown == 0 ? COLORS : TYPES; }
    private int getDropdownX(int id, int startX) { return id == 0 ? startX + 120 : (id == 1 ? startX + 30 : startX + 135); }
    private int getDropdownY(int id, int startY) { return id == 0 ? startY + 65 : startY + 100; }
    private int getDropdownW(int id) { return id == 0 ? 110 : 95; }
}