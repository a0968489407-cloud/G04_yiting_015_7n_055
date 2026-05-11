import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Rank {
    private static class ColorGroup {
        Color color;
        int totalLines;
        int totalLives; // 新增：記錄該顏色的總生命值
        boolean isAllDead;
        int latestDeathTick;
        int minId;

        ColorGroup(Color color) {
            this.color = color;
            this.totalLines = 0;
            this.totalLives = 0;
            this.isAllDead = true;
            this.latestDeathTick = -1;
            this.minId = Integer.MAX_VALUE;
        }
    }

    public static void drawRanking(Graphics g, ArrayList<Ball> balls) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("New Times Roman", Font.BOLD, 40)); 
        g.drawString("Rank", 30, 40); 

        if (balls == null || balls.isEmpty()) return;

        // 1. 按顏色統計
        Map<Color, ColorGroup> groups = new HashMap<>();
        for (Ball b : balls) {
            ColorGroup group = groups.computeIfAbsent(b.color, k -> new ColorGroup(b.color));
            group.totalLines += b.getLineCount();
            
            if (!b.isDead) {
                group.isAllDead = false;
                // 累加活著的小球生命值
                group.totalLives += b.lives; 
            }
            
            if (b.isDead) {
                group.latestDeathTick = Math.max(group.latestDeathTick, b.deathTick);
            }
            group.minId = Math.min(group.minId, b.id);
        }

        // 2. 排序 (與之前邏輯相同)
        ArrayList<ColorGroup> sortedGroups = new ArrayList<>(groups.values());
        sortedGroups.sort((g1, g2) -> {
            if (g1.isAllDead != g2.isAllDead) return g1.isAllDead ? 1 : -1; 
            if (g1.isAllDead && g2.isAllDead) return g2.latestDeathTick - g1.latestDeathTick;
            int lineDiff = g2.totalLines - g1.totalLines;
            if (lineDiff != 0) return lineDiff; 
            return g1.minId - g2.minId; 
        });

        // 3. 繪製
        int startX = 50, currentY = 80; 
        for (int i = 0; i < sortedGroups.size(); i++) {
            ColorGroup group = sortedGroups.get(i);
            int rank = i + 1;
            int size = (rank == 1) ? 60 : (rank == 2) ? 45 : 30;
            int offset = (60 - size) / 2;

            // 畫球的圓圈
            if (group.isAllDead) {
                g.setColor(Color.DARK_GRAY); 
                g.fillOval(startX + offset, currentY, size, size);
                g.setColor(group.color);         
                g.drawOval(startX + offset, currentY, size, size);
            } else {
                g.setColor(group.color);         
                g.fillOval(startX + offset, currentY, size, size);

                // --- 新增：在排名旁顯示生命值 ---
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                // 畫出生命值，例如 "x3" 代表還有三條命
                String lifeText = "x" + group.totalLives;
                g.drawString(lifeText, startX + 70, currentY + (size / 2) + 7);
                
                // 如果你想畫愛心圖示，可以在這裡使用 g.fillPolygon 或 drawImage
            }
            currentY += size + 20; 
        }
    }
}