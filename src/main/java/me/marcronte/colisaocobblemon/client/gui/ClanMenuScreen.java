package me.marcronte.colisaocobblemon.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import org.joml.Matrix4f;

import java.util.List;

public class ClanMenuScreen extends Screen {

    private static final ResourceLocation TEXTURE = ResourceLocation.parse("colisao-cobblemon:textures/gui/clan_menu.png");
    private final int imageWidth = 300;
    private final int imageHeight = 166;

    private final String clanName;
    private final int clanLevel;
    private final int clanXp;
    private final int clanXpNeeded;
    private final List<String> members;
    private final List<String> perks;
    private final boolean isManager;
    private final long nextResetTimestamp;

    private int memberScroll = 0;
    private int perkScroll = 0;
    private final List<String> missions;
    private final String timeRemaining;

    public ClanMenuScreen(String clanName, int clanLevel, int clanXp, int clanXpNeeded, List<String> members, List<String> perks, List<String> missions, String timeRemaining, long nextResetTimestamp, boolean isManager) {
        super(Component.literal("Menu do Clan"));
        this.clanName = clanName;
        this.clanLevel = clanLevel;
        this.clanXp = clanXp;
        this.clanXpNeeded = clanXpNeeded;
        this.members = members;
        this.perks = perks;
        this.missions = missions;
        this.timeRemaining = timeRemaining;
        this.nextResetTimestamp = nextResetTimestamp;
        this.isManager = isManager;
    }



    @Override
    protected void init() {
        int startX = (this.width - imageWidth) / 2;
        int startY = (this.height - imageHeight) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Missões"), btn -> openMissions())
                .bounds(startX + 97, startY + 127, 106, 28).build());

        if (isManager) {
            this.addRenderableWidget(Button.builder(Component.literal("⚙"), btn -> openManage())
                    .bounds(startX + 80, startY + 9, 10, 10).build());
        }
    }

    private void openMissions() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new ClanMissionsScreen(this, this.missions, this.nextResetTimestamp));
        }
    }

    private void openManage() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new ClanManageScreen(this, members));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        int startX = (this.width - imageWidth) / 2;
        int startY = (this.height - imageHeight) / 2;

        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        graphics.blit(TEXTURE, startX, startY, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        float cx = startX + 149.5f;
        float cy = startY + 65.5f;
        float radius = 48.5f;
        float thickness = 8.0f;

        drawCircularArc(graphics, cx, cy, radius, thickness, 0.0f, 1.0f, 0xFF8B8B8B);

        drawCircularArc(graphics, cx, cy, radius, thickness, -0.005f, 0.005f, 0xFF373737);
        drawCircularArc(graphics, cx, cy, radius, thickness, 0.995f, 1.005f, 0xFF373737);

        float progress = (this.clanXpNeeded > 0) ? Math.min(1.0f, (float) this.clanXp / this.clanXpNeeded) : 1.0f;
        drawCircularArc(graphics, cx, cy, radius, thickness, 0.0f, progress, 0xFF44D5DA);


        graphics.pose().pushPose();
        graphics.pose().translate(startX + 149, startY + 54, 0);
        graphics.pose().scale(4.0F, 4.0F, 2.0F);
        String lvlText = String.valueOf(clanLevel);
        graphics.drawCenteredString(this.font, lvlText, 0, -2, 0xFFD700);
        graphics.pose().popPose();

        graphics.drawCenteredString(this.font, "§l" + clanName, startX + 149, startY + 85, 0xFFFFFF);

        graphics.drawString(this.font, "§nMembros", startX + 9, startY + 10, 0xAAAAAA);
        int memY = startY + 22;
        for (int i = memberScroll; i < members.size() && i < memberScroll + 12; i++) {
            String rawMember = members.get(i);
            String displayMember = rawMember;

            if (rawMember.contains(" - ")) {
                String[] parts = rawMember.split(" - ", 2);
                displayMember = parts[1] + " - " + parts[0];
            }

            renderScrollingText(graphics, displayMember, startX + 9, memY, 70, mouseX, mouseY, 0xFFFFFF);
            memY += 10;
        }

        graphics.drawString(this.font, "§nPerks & Bônus", startX + 211, startY + 30, 0xAAAAAA);
        int perkY = startY + 42;
        for (int i = perkScroll; i < perks.size() && i < perkScroll + 8; i++) {
            renderScrollingText(graphics, perks.get(i), startX + 211, perkY, 72, mouseX, mouseY, 0x55FF55);
            perkY += 10;
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        double dx = mouseX - (startX + 149.5);
        double dy = mouseY - (startY + 65.5);
        double distanceToCenter = Math.sqrt(dx * dx + dy * dy);

        if (distanceToCenter <= 50) {
            String xpText = (this.clanXpNeeded > 0) ? (this.clanXp + " / " + this.clanXpNeeded + " XP") : (this.clanXp + " XP");
            graphics.renderTooltip(this.font, Component.literal(xpText), mouseX, mouseY);
        }
    }

    private void drawCircularArc(GuiGraphics graphics, float centerX, float centerY, float radius, float thickness, float startProgress, float endProgress, int color) {
        float progressDelta = endProgress - startProgress;
        if (progressDelta <= 0) return;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = graphics.pose().last().pose();

        int a = (color >> 24) & 255;
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;

        int segments = 100;
        float baseStartAngle = (float) (Math.PI / 2.0);

        float anglePerProgress = (float) Math.PI * 2.0f;
        float actualStartAngle = baseStartAngle + (startProgress * anglePerProgress);
        float actualEndAngle = baseStartAngle + (endProgress * anglePerProgress);

        int activeSegments = Math.max(1, (int) (segments * progressDelta));

        float innerR = radius - (thickness / 2.0f);
        float outerR = radius + (thickness / 2.0f);

        for (int i = 0; i < activeSegments; i++) {
            float theta1 = actualStartAngle + ((actualEndAngle - actualStartAngle) * i / activeSegments);
            float theta2 = actualStartAngle + ((actualEndAngle - actualStartAngle) * (i + 1) / activeSegments);

            float x1_out = centerX + outerR * (float) Math.cos(theta1);
            float y1_out = centerY + outerR * (float) Math.sin(theta1);
            float x1_in  = centerX + innerR * (float) Math.cos(theta1);
            float y1_in  = centerY + innerR * (float) Math.sin(theta1);

            float x2_out = centerX + outerR * (float) Math.cos(theta2);
            float y2_out = centerY + outerR * (float) Math.sin(theta2);
            float x2_in  = centerX + innerR * (float) Math.cos(theta2);
            float y2_in  = centerY + innerR * (float) Math.sin(theta2);

            buffer.addVertex(matrix, x1_out, y1_out, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x2_out, y2_out, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x2_in, y2_in, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1_in, y1_in, 0).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderScrollingText(GuiGraphics graphics, String text, int x, int y, int maxWidth, int mouseX, int mouseY, int color) {
        int textWidth = this.font.width(text);
        int textHeight = 9;

        boolean hovered = mouseX >= x && mouseX <= x + maxWidth && mouseY >= y && mouseY < y + textHeight;

        if (textWidth > maxWidth) {
            graphics.enableScissor(x, y, x + maxWidth, y + textHeight + 2);

            int shift = 0;
            if (hovered) {
                long time = Util.getMillis();
                float cycle = (time % 4000) / 4000.0f;
                float pingPong = cycle < 0.5f ? cycle * 2.0f : (1.0f - cycle) * 2.0f;
                float smooth = Math.max(0.0f, Math.min(1.0f, (pingPong - 0.1f) / 0.8f));

                shift = (int) (smooth * (textWidth - maxWidth));
            }

            graphics.drawString(this.font, text, x - shift, y, color);
            graphics.disableScissor();
        } else {
            graphics.drawString(this.font, text, x, y, color);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int startX = (this.width - imageWidth) / 2;

        if (mouseX > startX && mouseX < startX + 90) {
            if (scrollY > 0 && memberScroll > 0) memberScroll--;
            if (scrollY < 0 && memberScroll < Math.max(0, members.size() - 12)) memberScroll++;
            return true;
        }

        if (mouseX > startX + 205 && mouseX < startX + 295) {
            if (scrollY > 0 && perkScroll > 0) perkScroll--;
            if (scrollY < 0 && perkScroll < Math.max(0, perks.size() - 8)) perkScroll++;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}