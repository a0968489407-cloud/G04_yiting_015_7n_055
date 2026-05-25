import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.Image;

public class Rank {
    // 內部類別：用於依顏色分組統計各球體的狀態
    private static class ColorGroup {
        Color color;
        int totalLines;
        boolean isAllDead;
        int latestDeathTick;
        int minId;
        int activeItemType = -1; 

        ColorGroup(Color color) {
            this.color = color;
            this.totalLines = 0;
            this.isAllDead = true;
            this.latestDeathTick = -1;
            this.minId = Integer.MAX_VALUE;
        }
    }

    public static void drawRanking(Graphics g, ArrayList<Ball> balls, GameMode mode) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("New Times Roman", Font.BOLD, 40));
        g.drawString("Rank", 30, 40);

        if (balls == null || balls.isEmpty()) return;

        // 1. 建立 Map，以顏色為 Key 將相同顏色的球聚合統計
        Map<Color, ColorGroup> groups = new HashMap<>();
        for (Ball b : balls) {
            // 若該顏色不存在則建立新群組
            ColorGroup group = groups.computeIfAbsent(b.color, k -> new ColorGroup(b.color));
            group.totalLines += b.getLineCount(); // 累加線段總數

            if (!b.isDead) {
                group.isAllDead = false; // 只要有一顆活著，該顏色就算存活
                if (b.currentItemType != -1) {
                    group.activeItemType = b.currentItemType; // 紀錄最新獲得的道具種類
                }
            } else {
                // 記錄最晚死亡的時間戳記，用於死亡後的同分排序
                group.latestDeathTick = Math.max(group.latestDeathTick, b.deathTick);
            }
            // 記錄該顏色的最早加入 ID，用於順序不變時的排序依據
            group.minId = Math.min(group.minId, b.id);
        }

        // 2. 將統計結果轉為 List 進行排序
        ArrayList<ColorGroup> sortedGroups = new ArrayList<>(groups.values());
        if (mode == GameMode.CLASSIC) {
            // 經典模式：根據存活狀態、線段數量進行動態排名
            sortedGroups.sort((g1, g2) -> {
                if (g1.isAllDead != g2.isAllDead) return g1.isAllDead ? 1 : -1;
                if (g1.isAllDead && g2.isAllDead) return g2.latestDeathTick - g1.latestDeathTick;
                int lineDiff = g2.totalLines - g1.totalLines;
                if (lineDiff != 0) return lineDiff;
                return g1.minId - g2.minId;
            });
        } else if (mode == GameMode.ITEM_MODE) {
            // 道具模式：取消動態變換排名，固定依據生成的先後順序顯示
            sortedGroups.sort((g1, g2) -> g1.minId - g2.minId);
        }

        // 3. 繪製排行榜 UI
        int startX = 50, currentY = 80;
        for (int i = 0; i < sortedGroups.size(); i++) {
            ColorGroup group = sortedGroups.get(i);
            // 依據排名與模式決定圓形圖示的大小
            int size = (mode == GameMode.ITEM_MODE) ? 45 : ((i == 0) ? 60 : (i == 1) ? 45 : 30);
            int offset = (60 - size) / 2;

            if (group.isAllDead) {
                // 若全滅，畫灰底加上顏色邊框
                g.setColor(Color.DARK_GRAY);
                g.fillOval(startX + offset, currentY, size, size);
                g.setColor(group.color);
                g.drawOval(startX + offset, currentY, size, size);
            } else {
                // 若存活，畫全滿顏色
                g.setColor(group.color);
                g.fillOval(startX + offset, currentY, size, size);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 20));

                if (mode == GameMode.CLASSIC) {
                    // 經典模式顯示線段數
                    String lineText = group.totalLines + " lines";
                    g.drawString(lineText, startX + 70, currentY + (size / 2) + 7);
                } else if (mode == GameMode.ITEM_MODE) {
                    // 道具模式顯示持有道具的小圖示
                    if (group.activeItemType != -1) {
                        Image itemImg = null;
                        switch (group.activeItemType) {
                            case 2: itemImg = Item.speedUpImg; break;
                            case 4: itemImg = Item.speedDownImg; break;
                            case 5: itemImg = Item.splitImg; break;
                            case 6: itemImg = Item.anotherLifeImg; break;
                        }

                        if (itemImg != null) {
                            g.drawImage(itemImg, startX + 65, currentY + 20, 24, 24, null);
                        }
                    }
                }
            }
            currentY += size + 20;
        }
    }
}