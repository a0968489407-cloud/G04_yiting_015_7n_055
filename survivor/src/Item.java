import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

public class Item {
    public static int num = 0;
    public int radius;
    public Vector2D pos;
    // 道具種類：2:加速, 4:減速, 5:分裂, 6:加命
    public int type; 
    // 是否還在場上可被拾取
    public boolean isAvailable;
    
    // 靜態載入的道具圖示資源
    public static Image speedUpImg;
    public static Image speedDownImg;
    public static Image splitImg;
    public static Image anotherLifeImg; 

    public Item(double x, double y, int type) {
        num++;
        this.radius = 18; 
        this.pos = new Vector2D(x, y);
        this.type = type;
        this.isAvailable = true;
        
        // 第一次建立道具時載入圖片
        if (speedUpImg == null) loadImages();
    }

    private void loadImages() {
        try {
            speedUpImg = ImageIO.read(new File("survivor/pic/lightning.png"));
            speedDownImg = ImageIO.read(new File("survivor/pic/freeze.png"));
            splitImg = ImageIO.read(new File("survivor/pic/cell.png"));
            anotherLifeImg = ImageIO.read(new File("survivor/pic/heart.png")); 
        } catch (Exception e) {
            System.out.println("圖片載入失敗，將使用預設幾何圖形替代");
        }
    }

    // 將各種道具的效果整併在此，針對傳入的目標球產生增益或減益
    public void applyEffect(Ball targetBall) { 
        switch(this.type) {
            case 2: // 加速道具 (增加 15% 速度)
                targetBall.velocity.x *= 1.15;
                targetBall.velocity.y *= 1.15;
                break;
            case 4: // 減速道具 (減少 30% 速度)
                targetBall.velocity.x *= 0.7;
                targetBall.velocity.y *= 0.7;
                break;
            case 6: // 加命道具 (生命值 +1)
                targetBall.lives++;
                break;
            // 註：type 5 (分裂道具) 會改變球的數量與產生新物件，其特殊邏輯在 GameManager 處理
        }
    }

    public void draw(Graphics g) {
        if (!isAvailable) return;

        Image img = null;
        switch (this.type) {
            case 2: img = speedUpImg; break;
            case 4: img = speedDownImg; break;
            case 5: img = splitImg; break;
            case 6: img = anotherLifeImg; break;
        }

        if (img != null) {
            // 將圖片置中繪製於道具座標
            g.drawImage(img, (int)pos.x - radius, (int)pos.y - radius, radius * 2, radius * 2, null);
        } else {
            // 防呆：若無圖片則畫黃色方塊
            g.setColor(Color.YELLOW); 
            g.fillRect((int)pos.x - radius, (int)pos.y - radius, radius * 2, radius * 2);
        }
    }
}