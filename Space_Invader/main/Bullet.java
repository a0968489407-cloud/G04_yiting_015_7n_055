package main;
import java.awt.Color;
import java.awt.Graphics;   

public class Bullet {
    // 子彈左上角座標位置
    public int x, y;
    // 子彈在每幀更新時於 X 軸與 Y 軸上的移動增量
    public int vx, vy; 
    // 定義陣營旗標，供主程式判定要對玩家還是外星人進行碰撞傷害
    public boolean isEnemy; 
    // 繪製時使用的色彩物件
    public Color color;     

    // 完整的建構子，接收座標、速度、陣營判別與顏色參數，直接賦值給類別變數
    public Bullet(int x, int y, int vx, int vy, boolean isEnemy, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.isEnemy = isEnemy;
        this.color = color;
    }

    // 重載建構子，只需傳入座標與速度，內部透過 this() 呼叫完整建構子並自動設定為黃色的玩家子彈
    public Bullet(int x, int y, int vx, int vy) {
        this(x, y, vx, vy, false, Color.YELLOW);
    }

    public void update() {
        // 在每一幀中，將速度向量值累加至當前座標上，改變實體位置
        x += vx; 
        y += vy; 
    }

    public void draw(Graphics g) {
        // 設定 Graphics 顏色為自身顏色屬性，繪出固定寬 5 像素、高 10 像素的直立矩形
        g.setColor(color);
        g.fillRect(x, y, 5, 10);
    }
}