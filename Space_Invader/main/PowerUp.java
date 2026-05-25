package main;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class PowerUp {
    // 道具在畫面中的座標 (目前邏輯中主要用於 draw)
    public int x, y;
    // 道具的功能種類代碼
    public int type; 
    
    // 宣告靜態的 Image 變數，整個程式生命週期內共用此圖片資源
    public static Image shieldImg;
    public static Image iceImg;

    // 靜態區塊，當類別第一次被 JVM 載入時立即執行
    static {
        try {
            // 利用 ImageIO 讀取指定路徑的 PNG 圖片檔案放入靜態變數中
            shieldImg = ImageIO.read(new File("Space_Invader/pic/shield.png"));
            iceImg = ImageIO.read(new File("Space_Invader/pic/ice.png"));
        } catch (Exception e) {
            // 捕捉例外並印出提示，防止程式因找不到檔案直接崩潰
            System.out.println("找不到道具圖片，請確認 pic/shield.png 與 pic/ice.png 是否存在");
        }
    }
    
    // 建構子，初始化座標與種類屬性
    public PowerUp(int x, int y, int type) {
        this.x = x; this.y = y;
        this.type = type;
    }

    public void update() { 
        // 賦予道具在畫面上持續向下掉落的固定 Y 軸增量
        y += 2; 
    } 

    public void draw(Graphics g) {
        // 判定種類與圖片是否成功載入，若成立則使用 Graphics 繪製縮放至 20x20 的圖片
        if (type == 0 && shieldImg != null) {
            g.drawImage(shieldImg, x, y, 20, 20, null);
        } 
        else if (type == 1 && iceImg != null) {
            g.drawImage(iceImg, x, y, 20, 20, null);
        } 
        else if (type == 2) {
            // 種類 2 未提供外部圖片，改用 Java Graphics 基本函式拼湊出散彈形狀
            g.setColor(Color.YELLOW);
            // 運用 drawLine 畫出底部同一點向三個不同 X 座標發散的軌跡線
            g.drawLine(x + 10, y + 18, x + 3, y + 5);  
            g.drawLine(x + 10, y + 18, x + 10, y + 2); 
            g.drawLine(x + 10, y + 18, x + 17, y + 5); 
            // 在三條線的前端使用 fillRect 各畫出一個 3x3 像素的小方塊當作子彈端點
            g.fillRect(x + 1, y + 3, 3, 3);
            g.fillRect(x + 9, y + 0, 3, 3);
            g.fillRect(x + 16, y + 3, 3, 3);
        }
        else if (type == 3) {
            // 種類 3 同樣使用基礎幾何圖形拼湊出彈夾內單一子彈的外型
            g.setColor(Color.YELLOW);
            // 繪製子彈主體方塊
            g.fillRect(x + 8, y + 7, 4, 9); 
            // 定義子彈尖端三角形的三個頂點座標陣列
            int[] bx = {x + 8, x + 10, x + 12};
            int[] by = {y + 7, y + 2, y + 7};
            // 透過 fillPolygon 傳入座標點繪製出實心三角形尖端
            g.fillPolygon(bx, by, 3); 
        }
    }
}