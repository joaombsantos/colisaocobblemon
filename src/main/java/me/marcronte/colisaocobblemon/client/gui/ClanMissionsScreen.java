package me.marcronte.colisaocobblemon.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class ClanMissionsScreen extends Screen {

    private static final ResourceLocation TEXTURE = ResourceLocation.parse("colisao-cobblemon:textures/gui/clan_list.png");
    private final int imageWidth = 260;
    private final int imageHeight = 166;

    private final Screen parentScreen;
    private final List<String> missions;
    private final long nextResetTimestamp;
    private int scrollOffset = 0;

    private String cachedTimeStr = "";
    private long lastSecondProcessed = 0;

    public ClanMissionsScreen(Screen parentScreen, List<String> missions, long nextResetTimestamp) {
        super(Component.literal("Missões do Clã"));
        this.parentScreen = parentScreen;
        this.missions = missions;
        this.nextResetTimestamp = nextResetTimestamp;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        int startX = (this.width - imageWidth) / 2;
        int startY = (this.height - imageHeight) / 2;

        graphics.blit(TEXTURE, startX, startY, 0, 0, imageWidth, imageHeight, 260, 166);

        graphics.drawCenteredString(this.font, "§lMissões Semanais", startX + imageWidth / 2, startY + 10, 0xFFFFFF);

        graphics.drawCenteredString(this.font, "§lTempo Restante: " + getLiveTimeRemaining(), startX + imageWidth / 2, startY + 22, 0xAAAAAA);

        int listX = startX + 16;
        int listY = startY + 42;
        int maxVisible = 5;

        for (int i = 0; i < maxVisible; i++) {
            int idx = scrollOffset + i;
            if (idx >= missions.size()) break;

            int itemY = listY + (i * 20);
            graphics.fill(listX, itemY, startX + 235, itemY + 18, 0xFF333333); // Fundo do item
            graphics.drawString(this.font, missions.get(idx), listX + 5, itemY + 5, 0xFFFFFF);
            graphics.drawString(this.font, "§b+143 XP", startX + 180, itemY + 5, 0x55FFFF);
        }

        if (missions.size() > maxVisible) {
            int scrollH = (int) ((maxVisible / (float) missions.size()) * 110);
            int scrollY = startY + 38 + (int) ((scrollOffset / (float) (missions.size() - maxVisible)) * (110 - scrollH));
            graphics.fill(startX + 239, scrollY, startX + 244, scrollY + scrollH, 0xFFAAAAAA);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private String getLiveTimeRemaining() {
        long now = System.currentTimeMillis();
        long currentSecond = now / 1000;

        if (currentSecond != lastSecondProcessed) {
            lastSecondProcessed = currentSecond;
            long remainingMs = this.nextResetTimestamp - now;

            if (remainingMs <= 0) {
                cachedTimeStr = "0 dias 00:00:00";
            } else {
                long days = remainingMs / (1000 * 60 * 60 * 24);
                long hours = (remainingMs / (1000 * 60 * 60)) % 24;
                long minutes = (remainingMs / (1000 * 60)) % 60;
                long seconds = (remainingMs / 1000) % 60;
                cachedTimeStr = String.format("%d dias %02d:%02d:%02d", days, hours, minutes, seconds);
            }
        }
        return cachedTimeStr;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (missions.size() > 5) {
            if (scrollY > 0 && scrollOffset > 0) scrollOffset--;
            if (scrollY < 0 && scrollOffset < missions.size() - 5) scrollOffset++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parentScreen);
    }
}