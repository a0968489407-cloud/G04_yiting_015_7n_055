package main;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

public class Shooter {
    public int x, y;
    public final int WIDTH = 50;
    public final int HEIGHT = 50;
    public static Image shipImg;

    // 靜態載入飛船圖片
    static {
        try {
            shipImg = ImageIO.read(new File("Space_Invader/pic/spaceship.png"));
        } catch (Exception e) {
            System.out.println("找不到飛船圖片，請確認 Space_Invader/pic/spaceship.png 是否存在");
        }
    }

    public Shooter(int x, int y) { this.x = x; this.y = y; }

    public void move(int dx) {
        x += dx;
        if (x < 0) x = 0;
        if (x > 800 - WIDTH) x = 800 - WIDTH;
    }
    
    public void draw(Graphics g, boolean hasShield) {
        // --- 1. 防護罩效果 ---
        if (hasShield) {
            // 配合飛船放大，將防護罩也稍微擴大一些
            g.setColor(new Color(100, 200, 255, 100));
            g.fillOval(x - 12, y - 12, WIDTH + 24, HEIGHT + 24);
            g.setColor(Color.CYAN);
            g.drawOval(x - 12, y - 12, WIDTH + 24, HEIGHT + 24);
        }

        // --- 2. 飛船實體 ---
        if (shipImg != null) {
            // 【修改：將圖片尺寸從 50x50 放大到 70x70，且中心點自動對齊判定框】
            g.drawImage(shipImg, x - 10, y - 10, WIDTH + 20, HEIGHT + 20, null);
        } else {
            // 備用幾何圖形維持原樣，確保無圖時不崩潰
            int[] xPoints = {x + 25, x + 45, x + 30, x + 25, x + 20, x + 5};
            int[] yPoints = {y, y + 40, y + 45, y + 35, y + 45, y + 40};
            g.setColor(Color.CYAN);
            g.fillPolygon(xPoints, yPoints, 6);
            g.setColor(Color.WHITE);
            g.drawPolygon(xPoints, yPoints, 6);
        }
    }
}