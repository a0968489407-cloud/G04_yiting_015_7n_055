import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

public class Item {
    public static int num = 0;
    public int radius;
    public Vector2D pos;
    public int type; 
    public boolean isAvailable;
    
    // 新增圖片變數
    private static Image speedUpImg;
    private static Image speedDownImg;
    private static Image splitImg;
    private static Image anotherLifeImg; // 生命道具圖片

    public Item(double x, double y, int type) {
        num++;
        this.radius = 18; // 配合圖片大小稍微調大
        this.pos = new Vector2D(x, y);
        this.type = type;
        this.isAvailable = true;
        
        // 靜態載入圖片（只需載入一次）
        if (speedUpImg == null) loadImages();
    }

    private void loadImages() {
        try {
            // 請確保你的專案目錄下有這些圖檔，或換成你的路徑
            speedUpImg = ImageIO.read(new File("survivor/src/lightning.png"));
            speedDownImg = ImageIO.read(new File("survivor/src/freeze.png"));
            splitImg = ImageIO.read(new File("survivor/src/cell.png"));
            anotherLifeImg = ImageIO.read(new File("survivor/src/heart.png")); // 生命道具圖片
        } catch (Exception e) {
            System.out.println("圖片載入失敗，將使用預設幾何圖形替代");
        }
    }

    public void applyEffect(Ball targetBall) { }

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
            // 繪製圖片，並將中心點對準 pos
            g.drawImage(img, (int)pos.x - radius, (int)pos.y - radius, radius * 2, radius * 2, null);
        } else {
            // 如果圖片載入失敗的備用方案
            g.setColor(Color.YELLOW); 
            g.fillRect((int)pos.x - radius, (int)pos.y - radius, radius * 2, radius * 2);
        }
    }
}