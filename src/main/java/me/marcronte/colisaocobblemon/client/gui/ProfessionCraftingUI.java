package me.marcronte.colisaocobblemon.client.gui;

import me.marcronte.colisaocobblemon.network.payloads.ProfessionCraftPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ProfessionCraftingUI extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("colisao-cobblemon:textures/gui/profession_menu.png");

    private static final ResourceLocation XP_BAR_BG = ResourceLocation.parse("hud/experience_bar_background");
    private static final ResourceLocation XP_BAR_PROGRESS = ResourceLocation.parse("hud/experience_bar_progress");

    private final String profession;
    private final String currentRank;
    private final int currentExp;
    private final String recipesJson;

    private final String[] ranks = {"rank_e", "rank_d", "rank_c", "rank_b", "rank_a"};

    public ProfessionCraftingUI(ProfessionCraftPayloads.OpenMenuPayload payload) {
        super(Component.translatable("gui.colisao-cobblemon.profession_menu"));
        this.profession = payload.profession();
        this.currentRank = payload.currentRank();
        this.currentExp = payload.currentExp();
        this.recipesJson = payload.recipesJson();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int imgWidth = 256;
        int imgHeight = 256;
        int startX = (this.width - imgWidth) / 2;
        int startY = (this.height - imgHeight) / 2;

        graphics.blit(TEXTURE, startX, startY, 0, 0, imgWidth, imgHeight, 256, 256);

        String profTitle = profession.substring(0, 1).toUpperCase() + profession.substring(1);
        graphics.drawCenteredString(this.font, "§e" + profTitle, startX + imgWidth / 2, startY + 6, 0xFFFFFF);

        int playerRankIndex = getRankIndex(this.currentRank);

        for (int i = 0; i < 5; i++) {
            int btnX = startX + 18;
            int btnY = startY + 20 + (i * 44);
            int btnWidth = 99;
            int btnHeight = 39;

            boolean isUnlocked = i <= playerRankIndex;
            boolean isHovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight;

            int bgColor = isUnlocked ? (isHovered ? 0x4400FF00 : 0x22FFFFFF) : 0xAA000000;
            graphics.fill(btnX, btnY, btnX + btnWidth, btnY + btnHeight, bgColor);

            String rankName = "Rank " + ranks[i].split("_")[1].toUpperCase();
            String text = isUnlocked ? "§a" + rankName : "§c" + rankName + " 🔒";
            graphics.drawCenteredString(this.font, text, btnX + (btnWidth / 2), btnY + (btnHeight / 2) - 4, 0xFFFFFF);

            if (isUnlocked) {
                int barX = startX + 139;
                int barY = startY + 39 + (i * 44);
                int barWidth = 100;
                int barHeight = 5;

                graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);

                int displayExp = (i < playerRankIndex) ? 100 : Math.min(100, currentExp);
                int fillWidth = (int) ((displayExp / 100.0f) * barWidth);

                graphics.fill(barX, barY, barX + fillWidth, barY + barHeight, 0xFF00AA00);

                graphics.blitSprite(XP_BAR_BG, barX, barY - 1, 102, 7);
                graphics.blitSprite(XP_BAR_PROGRESS, barWidth, 7, 0, 0, barX, barY - 1, fillWidth, 7);

                if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
                    graphics.renderTooltip(this.font, Component.literal("§e" + displayExp + " / 100 XP"), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imgWidth = 256;
        int imgHeight = 256;
        int startX = (this.width - imgWidth) / 2;
        int startY = (this.height - imgHeight) / 2;

        int playerRankIndex = getRankIndex(this.currentRank);

        for (int i = 0; i < 5; i++) {
            int btnX = startX + 18;
            int btnY = startY + 20 + (i * 44);
            int btnWidth = 99;
            int btnHeight = 39;

            if (mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + btnHeight) {
                if (i > playerRankIndex) {
                    return true;
                }

                Minecraft.getInstance().setScreen(new ProfessionTradeScreen(this.profession, ranks[i], this.currentExp, this.recipesJson));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getRankIndex(String rank) {
        for (int i = 0; i < ranks.length; i++) {
            if (ranks[i].equals(rank)) return i;
        }
        return 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}