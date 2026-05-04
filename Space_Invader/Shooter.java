import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

public class Shooter {
    int x, y;
    final int WIDTH = 50;
    final int HEIGHT = 50;

    Shooter(int x, int y) { this.x = x; this.y = y; }

    void move(int dx) {
        x += dx;
        if (x < 0) x = 0;
        if (x > 800 - WIDTH) x = 800 - WIDTH;
    }
    
 void draw(Graphics g, boolean hasShield) {
        // --- 1. 防護罩 (不變) ---
        if (hasShield) {
            g.setColor(new Color(100, 200, 255, 100));
            g.fillOval(x - 5, y - 5, WIDTH + 10, HEIGHT + 10);
            g.setColor(Color.CYAN);
            g.drawOval(x - 5, y - 5, WIDTH + 10, HEIGHT + 10);
        }

        // --- 2. 修改後的太空梭本體 (更流線的箭頭形狀) ---
        // 點的順序：頭部 -> 右翼尖 -> 右引擎內側 -> 尾部中心 -> 左引擎內側 -> 左翼尖
        int[] xPoints = {x + 25, x + 45, x + 30, x + 25, x + 20, x + 5};
        int[] yPoints = {y, y + 40, y + 45, y + 35, y + 45, y + 40};
        
        Polygon shuttle = new Polygon(xPoints, yPoints, 6);

        // 畫出主體
        g.setColor(Color.CYAN);
        g.fillPolygon(shuttle);
        
        // 畫出邊框
        g.setColor(Color.WHITE);
        g.drawPolygon(shuttle);

        // 畫出駕駛艙細節 (在中間畫個深色的小三角，增加立體感)
        g.setColor(new Color(0, 100, 150));
        int[] cockpitX = {x + 25, x + 32, x + 18};
        int[] cockpitY = {y + 10, y + 25, y + 25};
        g.fillPolygon(cockpitX, cockpitY, 3);
    }
}