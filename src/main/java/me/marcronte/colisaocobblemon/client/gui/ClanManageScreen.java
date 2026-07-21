package me.marcronte.colisaocobblemon.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ClanManageScreen extends Screen {

    private static final ResourceLocation TEXTURE = ResourceLocation.parse("colisao-cobblemon:textures/gui/clan_list.png");
    private final int imageWidth = 260;
    private final int imageHeight = 166;

    private final Screen parentScreen;
    private final List<MemberRow> memberRows = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE = 5;

    private String localPlayerName = "";
    private String localPlayerRole = "MEMBRO";

    private static class MemberRow {
        String role;
        String name;
        int confirmState = 0; // 0=Normal, 1=Confirm Promotion, 2=Confirm Demotion, 3=Confirm Kick

        MemberRow(String rawData) {
            String[] parts = rawData.split(" - ", 2);
            this.role = parts[0];
            this.name = parts.length > 1 ? parts[1] : "Desconhecido";
        }
    }

    public ClanManageScreen(Screen parentScreen, List<String> rawMembers) {
        super(Component.literal("Gerenciar Membros"));
        this.parentScreen = parentScreen;
        for (String raw : rawMembers) {
            this.memberRows.add(new MemberRow(raw));
        }
    }

    @Override
    protected void init() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.localPlayerName = this.minecraft.player.getScoreboardName();

            for (MemberRow row : memberRows) {
                if (row.name.equals(this.localPlayerName)) {
                    this.localPlayerRole = row.role;
                    break;
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        int startX = (this.width - imageWidth) / 2;
        int startY = (this.height - imageHeight) / 2;

        graphics.blit(TEXTURE, startX, startY, 0, 0, imageWidth, imageHeight, 260, 166);
        graphics.drawCenteredString(this.font, "§lGerenciar Membros", startX + imageWidth / 2, startY + 15, 0xFFFFFF);

        int listX = startX + 16;
        int listY = startY + 40;

        for (int i = 0; i < MAX_VISIBLE; i++) {
            int idx = scrollOffset + i;
            if (idx >= memberRows.size()) break;

            MemberRow row = memberRows.get(idx);
            int itemY = listY + (i * 22);

            graphics.fill(listX, itemY, startX + 235, itemY + 20, 0xFF222222); // Fundo da linha

            String color = row.role.equals("DONO") ? "§6" : (row.role.equals("GERENTE") ? "§d" : "§7");
            graphics.drawString(this.font, color + "[" + row.role + "] §f" + row.name, listX + 5, itemY + 6, 0xFFFFFF);

            boolean isSelf = row.name.equals(this.localPlayerName);
            boolean canPromoteDemote = this.localPlayerRole.equals("DONO") && !isSelf;
            boolean canKick = !isSelf && (this.localPlayerRole.equals("DONO") || (this.localPlayerRole.equals("GERENTE") && row.role.equals("MEMBRO")));

            if (row.confirmState == 0) {
                if (canPromoteDemote) {
                    boolean isTargetManager = row.role.equals("GERENTE");
                    String arrow = isTargetManager ? "§c↓" : "§a↑";
                    drawHoverBox(graphics, startX + 180, itemY + 2, 20, 16, mouseX, mouseY, arrow);
                }

                if (canKick) {
                    drawHoverBox(graphics, startX + 205, itemY + 2, 20, 16, mouseX, mouseY, "§c✖");
                }
            } else {
                String actionText = row.confirmState == 1 ? "Promover?" : (row.confirmState == 2 ? "Demover?" : "Expulsar?");
                graphics.drawString(this.font, "§e" + actionText, startX + 130, itemY + 6, 0xFFFFFF);

                drawHoverBox(graphics, startX + 180, itemY + 2, 20, 16, mouseX, mouseY, "§a✓");
                drawHoverBox(graphics, startX + 205, itemY + 2, 20, 16, mouseX, mouseY, "§c✖");
            }
        }

        if (memberRows.size() > MAX_VISIBLE) {
            int scrollH = (int) ((MAX_VISIBLE / (float) memberRows.size()) * 110);
            int scrollY = startY + 38 + (int) ((scrollOffset / (float) (memberRows.size() - MAX_VISIBLE)) * (110 - scrollH));
            graphics.fill(startX + 239, scrollY, startX + 244, scrollY + scrollH, 0xFFAAAAAA);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawHoverBox(GuiGraphics graphics, int x, int y, int w, int h, int mx, int my, String text) {
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + h;
        graphics.fill(x, y, x + w, y + h, hovered ? 0xFF555555 : 0xFF333333);
        graphics.drawCenteredString(this.font, text, x + (w / 2), y + 4, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - imageWidth) / 2;
        int startY = (this.height - imageHeight) / 2;
        int listY = startY + 40;

        for (int i = 0; i < MAX_VISIBLE; i++) {
            int idx = scrollOffset + i;
            if (idx >= memberRows.size()) break;

            MemberRow row = memberRows.get(idx);
            int itemY = listY + (i * 22);

            boolean isSelf = row.name.equals(this.localPlayerName);
            boolean canPromoteDemote = this.localPlayerRole.equals("DONO") && !isSelf;
            boolean canKick = !isSelf && (this.localPlayerRole.equals("DONO") || (this.localPlayerRole.equals("GERENTE") && row.role.equals("MEMBRO")));

            if (mouseX >= startX + 180 && mouseX <= startX + 200 && mouseY >= itemY + 2 && mouseY <= itemY + 18) {
                if (row.confirmState == 0) {
                    if (canPromoteDemote) {
                        row.confirmState = row.role.equals("GERENTE") ? 2 : 1; // 2=Demover, 1=Promover
                    }
                } else {
                    executeCommand(row);
                    row.confirmState = 0;
                }
                return true;
            }

            if (mouseX >= startX + 205 && mouseX <= startX + 225 && mouseY >= itemY + 2 && mouseY <= itemY + 18) {
                if (row.confirmState == 0) {
                    if (canKick) {
                        row.confirmState = 3;
                    }
                } else {
                    row.confirmState = 0;
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void executeCommand(MemberRow row) {
        if (this.minecraft == null || this.minecraft.getConnection() == null) return;

        String command = "";
        if (row.confirmState == 1) command = "clan promover " + row.name;
        if (row.confirmState == 2) command = "clan demover " + row.name;
        if (row.confirmState == 3) command = "clan expulsar " + row.name;

        if (!command.isEmpty()) {
            this.minecraft.getConnection().sendCommand(command);
            this.onClose();
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (memberRows.size() > MAX_VISIBLE) {
            if (scrollY > 0 && scrollOffset > 0) scrollOffset--;
            if (scrollY < 0 && scrollOffset < memberRows.size() - MAX_VISIBLE) scrollOffset++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parentScreen);
    }
}