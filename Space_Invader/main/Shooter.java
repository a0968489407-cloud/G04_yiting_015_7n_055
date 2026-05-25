package main;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

public class Shooter {
    // 玩家本體左上角座標
    public int x, y;
    // 用於限制邊界與 AABB 碰撞運算的邏輯長寬數值
    public final int WIDTH = 50;
    public final int HEIGHT = 50;
    // 宣告靜態的 Image 變數以便共用圖檔，減少 IO 重複讀取
    public static Image shipImg;

    // 靜態區塊，執行時讀取玩家飛船的圖片並賦值
    static {
        try {
            shipImg = ImageIO.read(new File("Space_Invader/pic/spaceship.png"));
        } catch (Exception e) {
            System.out.println("找不到飛船圖片，請確認 Space_Invader/pic/spaceship.png 是否存在");
        }
    }

    public Shooter(int x, int y) { 
        this.x = x; this.y = y; 
    }

    public void move(int dx) {
        // 將外部傳入的方向增量加入 X 座標
        x += dx;
        // 執行畫面邊界夾擠(Clamp)限制：左極限為 0
        if (x < 0) x = 0;
        // 右極限為畫面預設寬度 800 減掉自身寬度，確保機體完全不超出右邊界
        if (x > 800 - WIDTH) x = 800 - WIDTH;
    }
    
    public void draw(Graphics g, boolean hasShield) {
        // 若具備護盾狀態，繪製一個半透明覆蓋區
        if (hasShield) {
            // 使用 Color 的第四個參數 (Alpha 100) 建立半透明水藍色
            g.setColor(new Color(100, 200, 255, 100));
            // 往左上偏移並放大尺寸，以產生包覆於飛船外的填滿橢圓形
            g.fillOval(x - 12, y - 12, WIDTH + 24, HEIGHT + 24);
            g.setColor(Color.CYAN);
            // 畫出同樣尺寸的無填滿外框增強邊界視覺
            g.drawOval(x - 12, y - 12, WIDTH + 24, HEIGHT + 24);
        }

        // 若圖片順利載入
        if (shipImg != null) {
            // 利用 drawImage 將圖片寬高拉伸至 WIDTH+20 等尺寸，並補償 x,y 偏移使其置中於邏輯座標
            g.drawImage(shipImg, x - 10, y - 10, WIDTH + 20, HEIGHT + 20, null);
        } else {
            // 無圖檔防呆機制：使用預先定義的六個頂點座標 (xPoints, yPoints)
            int[] xPoints = {x + 25, x + 45, x + 30, x + 25, x + 20, x + 5};
            int[] yPoints = {y, y + 40, y + 45, y + 35, y + 45, y + 40};
            g.setColor(Color.CYAN);
            // 利用 fillPolygon 繪製出一個幾何飛機形狀替代圖片
            g.fillPolygon(xPoints, yPoints, 6);
            g.setColor(Color.WHITE);
            // 利用 drawPolygon 畫出外側白框使其清楚可見
            g.drawPolygon(xPoints, yPoints, 6);
        }
    }
}