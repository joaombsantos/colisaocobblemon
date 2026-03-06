package me.marcronte.colisaocobblemon.client.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.marcronte.colisaocobblemon.network.payloads.StylistPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.List;

public class StylistMenuScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("colisao-cobblemon:textures/gui/stylist_menu.png");

    private final List<String> categories;
    private int scrollOffset = 0;

    public StylistMenuScreen(StylistPayloads.OpenMenuPayload payload) {
        super(Component.translatable("gui.colisao-cobblemon.stylist_menu"));

        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType();
        this.categories = gson.fromJson(payload.categoriesJson(), type);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int imgWidth = 176;
        int imgHeight = 166;
        int startX = (this.width - imgWidth) / 2;
        int startY = (this.height - imgHeight) / 2;

        graphics.blit(TEXTURE, startX, startY, 0, 0, imgWidth, imgHeight, 176, 166);
        graphics.drawCenteredString(this.font, "§dCategorias", startX + (imgWidth / 2), startY + 8, 0xFFFFFF);

        int maxScroll = Math.max(0, categories.size() - 5);
        drawScrollbar(graphics, startX + 145, startY + 25, 6, 120, scrollOffset, maxScroll);

        for (int i = 0; i < 5; i++) {
            int index = i + scrollOffset;
            if (index >= categories.size()) break;

            String category = categories.get(index);

            int btnX = startX + 25;
            int btnY = startY + 25 + (i * 24);
            int btnWidth = 110;

            boolean isHovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + 20;

            graphics.fill(btnX, btnY, btnX + btnWidth, btnY + 20, isHovered ? 0x55FF88FF : 0x44000000);

            String formattedName = category.substring(0, 1).toUpperCase() + category.substring(1);
            graphics.drawCenteredString(this.font, formattedName, btnX + (btnWidth / 2), btnY + 6, 0xFFFFFF);
        }
    }

    private void drawScrollbar(GuiGraphics graphics, int x, int y, int width, int height, int currentOffset, int maxOffset) {
        graphics.fill(x, y, x + width, y + height, 0x55000000);
        if (maxOffset > 0) {
            int handleHeight = Math.max(15, height / (maxOffset + 1));
            int handleY = y + (int) (((float) currentOffset / maxOffset) * (height - handleHeight));
            graphics.fill(x, handleY, x + width, handleY + handleHeight, 0xFFFF88FF);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, categories.size() - 5);
        if (scrollY > 0) scrollOffset = Math.max(0, scrollOffset - 1);
        if (scrollY < 0) scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - 176) / 2;
        int startY = (this.height - 166) / 2;

        for (int i = 0; i < 5; i++) {
            int index = i + scrollOffset;
            if (index >= categories.size()) break;

            int btnX = startX + 25;
            int btnY = startY + 25 + (i * 24);

            if (mouseX >= btnX && mouseX <= btnX + 110 && mouseY >= btnY && mouseY <= btnY + 20) {
                ClientPlayNetworking.send(new StylistPayloads.SelectCategoryPayload(categories.get(index)));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean isPauseScreen() { return false; }
}