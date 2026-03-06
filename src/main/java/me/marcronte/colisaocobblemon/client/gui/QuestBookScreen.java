package me.marcronte.colisaocobblemon.client.gui;

import me.marcronte.colisaocobblemon.network.payloads.QuestBookPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class QuestBookScreen extends Screen {
    private static final ResourceLocation BOOK_TEXTURE = ResourceLocation.parse("textures/gui/book.png");

    private static final int PAGE_LEFT = 36;
    private static final int PAGE_WIDTH = 116;
    private static final int PAGE_RIGHT = PAGE_LEFT + PAGE_WIDTH;

    private final List<QuestBookPayload.QuestEntry> allQuests;
    private int activeCategory = 0;
    private QuestBookPayload.QuestEntry selectedQuest = null;

    private int scrollY = 0;
    private int maxScroll = 0;

    public QuestBookScreen(List<QuestBookPayload.QuestEntry> quests) {
        super(Component.translatable("gui.colisao-cobblemon.quest_log"));
        this.allQuests = quests;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int bookWidth = 192;
        int bookHeight = 192;
        int startX = (this.width - bookWidth) / 2;
        int startY = (this.height - bookHeight) / 2;

        graphics.blit(BOOK_TEXTURE, startX, startY, 0, 0, bookWidth, bookHeight);

        Component titleText = Component.translatable("gui.colisao-cobblemon.quest_log");
        int titleWidth = this.font.width(titleText);
        int pageCenterX = startX + PAGE_LEFT + (PAGE_WIDTH / 2);
        graphics.drawString(this.font, titleText, pageCenterX - (titleWidth / 2), startY + 15, 0x000000, false);

        if (selectedQuest != null) {
            renderQuestDetails(graphics, startX, startY, mouseX, mouseY);
        } else {
            renderCategoryTabs(graphics, startX, startY, mouseX, mouseY);
            renderQuestList(graphics, startX, startY, mouseX, mouseY);
        }
    }

    private void renderCategoryTabs(GuiGraphics graphics, int startX, int startY, int mouseX, int mouseY) {
        Component[] tabs = {
                Component.translatable("gui.colisao-cobblemon.quest_tab.daily"),
                Component.translatable("gui.colisao-cobblemon.quest_tab.normal"),
                Component.translatable("gui.colisao-cobblemon.quest_tab.lines")
        };

        int tabWidth = 38;
        int maxAvailableWidth = tabWidth - 2;

        for (int i = 0; i < 3; i++) {
            int tabX = startX + PAGE_LEFT + (i * tabWidth);
            int tabY = startY + 30;
            int color = (activeCategory == i) ? 0x005500 : 0x555555;

            boolean hovered = mouseX >= tabX && mouseX <= tabX + maxAvailableWidth && mouseY >= tabY && mouseY <= tabY + 10;
            if (hovered && activeCategory != i) color = 0x0000FF;

            String text = tabs[i].getString();
            int textWidth = this.font.width(text);

            String textToDraw = text;
            int textXOffset = 0;

            if (textWidth > maxAvailableWidth) {
                if (hovered) {
                    int overflow = textWidth - maxAvailableWidth;
                    int pause = 20;
                    int speed = 30;
                    int cycle = (overflow * 2 + pause * 2);

                    int tick = (int) ((net.minecraft.Util.getMillis() / speed) % cycle);

                    if (tick < pause) {
                        textXOffset = 0;
                    } else if (tick < pause + overflow) {
                        textXOffset = tick - pause;
                    } else if (tick < pause * 2 + overflow) {
                        textXOffset = overflow;
                    } else {
                        textXOffset = overflow - (tick - (pause * 2 + overflow));
                    }
                } else {
                    int truncatedWidth = maxAvailableWidth - this.font.width("..");
                    textToDraw = this.font.plainSubstrByWidth(text, truncatedWidth) + "..";
                }
            }

            graphics.enableScissor(tabX, tabY, tabX + maxAvailableWidth, tabY + 12);
            graphics.drawString(this.font, textToDraw, tabX - textXOffset, tabY, color, false);
            graphics.disableScissor();
        }

        graphics.fill(startX + PAGE_LEFT, startY + 42, startX + PAGE_RIGHT, startY + 43, 0xFF000000);
    }

    private void renderQuestList(GuiGraphics graphics, int startX, int startY, int mouseX, int mouseY) {
        List<QuestBookPayload.QuestEntry> filtered = allQuests.stream()
                .filter(q -> q.category() == activeCategory)
                .collect(Collectors.toList());

        int listTop = startY + 50;
        int listBottom = startY + 170;

        graphics.enableScissor(startX + PAGE_LEFT, listTop, startX + PAGE_RIGHT, listBottom);

        int itemHeight = 15;
        maxScroll = Math.max(0, (filtered.size() * itemHeight) - (listBottom - listTop));

        int yOffset = listTop - scrollY;

        for (QuestBookPayload.QuestEntry q : filtered) {
            boolean hovered = mouseX >= startX + PAGE_LEFT && mouseX <= startX + PAGE_RIGHT && mouseY >= yOffset && mouseY < yOffset + itemHeight;

            String fullText = (q.status() == 2 ? "✔ " : "") + q.title();
            int fullTextWidth = this.font.width(fullText);

            String textToDraw = fullText;
            int textXOffset = 0;

            if (fullTextWidth > PAGE_WIDTH) {
                if (hovered) {
                    int overflow = fullTextWidth - PAGE_WIDTH;
                    int pause = 20;
                    int speed = 30;
                    int cycle = (overflow * 2 + pause * 2);

                    int tick = (int) ((net.minecraft.Util.getMillis() / speed) % cycle);

                    if (tick < pause) {
                        textXOffset = 0;
                    } else if (tick < pause + overflow) {
                        textXOffset = tick - pause;
                    } else if (tick < pause * 2 + overflow) {
                        textXOffset = overflow;
                    } else {
                        textXOffset = overflow - (tick - (pause * 2 + overflow));
                    }
                } else {
                    int truncatedWidth = PAGE_WIDTH - this.font.width("...");
                    textToDraw = this.font.plainSubstrByWidth(fullText, truncatedWidth) + "...";
                }
            }

            int color = q.status() == 0 ? 0x888888 : (q.status() == 2 ? 0x008800 : 0x000000);
            if (hovered && mouseY >= listTop && mouseY <= listBottom) color = 0x0000FF;

            graphics.drawString(this.font, textToDraw, startX + PAGE_LEFT - textXOffset, yOffset, color, false);
            yOffset += itemHeight;
        }

        graphics.disableScissor();
    }

    private void renderQuestDetails(GuiGraphics graphics, int startX, int startY, int mouseX, int mouseY) {
        Component backText = Component.translatable("gui.colisao-cobblemon.quest_back");

        int backColor = 0x0000FF;
        boolean backHovered = mouseX >= startX + PAGE_LEFT && mouseX <= startX + PAGE_LEFT + 40 && mouseY >= startY + 30 && mouseY <= startY + 40;
        if (backHovered) backColor = 0x5555FF;

        graphics.drawString(this.font, backText, startX + PAGE_LEFT, startY + 30, backColor, false);
        graphics.fill(startX + PAGE_LEFT, startY + 42, startX + PAGE_RIGHT, startY + 43, 0xFF000000);

        int currentY = startY + 50;

        Component title = Component.literal(selectedQuest.title());
        graphics.drawWordWrap(this.font, title, startX + PAGE_LEFT, currentY, PAGE_WIDTH, 0x000000);

        int titleLines = this.font.split(title, PAGE_WIDTH).size();
        currentY += (titleLines * this.font.lineHeight) + 10;

        Component desc = Component.literal(selectedQuest.description());
        graphics.drawWordWrap(this.font, desc, startX + PAGE_LEFT, currentY, PAGE_WIDTH, 0x333333);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - 192) / 2;
        int startY = (this.height - 192) / 2;

        if (selectedQuest != null) {
            if (mouseX >= startX + PAGE_LEFT && mouseX <= startX + PAGE_LEFT + 40 && mouseY >= startY + 30 && mouseY <= startY + 40) {
                selectedQuest = null;
                return true;
            }
        } else {
            for (int i = 0; i < 3; i++) {
                int tabX = startX + PAGE_LEFT + (i * 38);
                if (mouseX >= tabX && mouseX <= tabX + 36 && mouseY >= startY + 30 && mouseY <= startY + 40) {
                    activeCategory = i;
                    scrollY = 0;
                    return true;
                }
            }

            int listTop = startY + 50;
            int listBottom = startY + 170;
            if (mouseY >= listTop && mouseY <= listBottom && mouseX >= startX + PAGE_LEFT && mouseX <= startX + PAGE_RIGHT) {
                List<QuestBookPayload.QuestEntry> filtered = allQuests.stream()
                        .filter(q -> q.category() == activeCategory).toList();

                int clickedIndex = ((int) mouseY - listTop + scrollY) / 15;
                if (clickedIndex >= 0 && clickedIndex < filtered.size()) {
                    QuestBookPayload.QuestEntry clicked = filtered.get(clickedIndex);
                    if (clicked.status() != 0) {
                        selectedQuest = clicked;
                        scrollY = 0;
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scrollY -= (int) (scrollY * 10);
        this.scrollY = Math.max(0, Math.min(this.scrollY, maxScroll));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}