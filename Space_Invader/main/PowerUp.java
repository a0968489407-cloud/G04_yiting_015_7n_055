package main;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class PowerUp {
    public int x, y;
    public int type; // 0: 護盾(S), 1: 冰凍(F), 2: 散彈(B), 3: 彈夾(M)
    
    public static Image shieldImg;
    public static Image iceImg;

    // 靜態載入道具圖片
    static {
        try {
            shieldImg = ImageIO.read(new File("Space_Invader/pic/shield.png"));
            iceImg = ImageIO.read(new File("Space_Invader/pic/ice.png"));
        } catch (Exception e) {
            System.out.println("找不到道具圖片，請確認 pic/shield.png 與 pic/ice.png 是否存在");
        }
    }
    
    public PowerUp(int x, int y, int type) {
        this.x = x; this.y = y;
        this.type = type;
    }

    public void update() { y += 2; } 

    public void draw(Graphics g) {
        if (type == 0 && shieldImg != null) {
            g.drawImage(shieldImg, x, y, 20, 20, null);
        } 
        else if (type == 1 && iceImg != null) {
            g.drawImage(iceImg, x, y, 20, 20, null);
        } 
        // === 修改：散彈道具手繪圖案 (3條放射狀) ===
        else if (type == 2) {
            g.setColor(Color.YELLOW);
            // 繪製放射狀軌跡線
            g.drawLine(x + 10, y + 18, x + 3, y + 5);  // 左斜
            g.drawLine(x + 10, y + 18, x + 10, y + 2); // 中直
            g.drawLine(x + 10, y + 18, x + 17, y + 5); // 右斜
            // 繪製前方的三顆彈丸
            g.fillRect(x + 1, y + 3, 3, 3);
            g.fillRect(x + 9, y + 0, 3, 3);
            g.fillRect(x + 16, y + 3, 3, 3);
        }
        // === 修改：彈匣道具手繪圖案 (畫一顆子彈) ===
        else if (type == 3) {
            g.setColor(Color.YELLOW);
            g.fillRect(x + 8, y + 7, 4, 9); // 子彈主體
            int[] bx = {x + 8, x + 10, x + 12};
            int[] by = {y + 7, y + 2, y + 7};
            g.fillPolygon(bx, by, 3); // 子彈尖端
        }
    }
}