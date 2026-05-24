package main;
import java.awt.Color;
import java.awt.Graphics;   

public class Bullet {
    public int x, y;
    public int vx, vy; 
    public boolean isEnemy; // 判斷是否為外星人子彈
    public Color color;     // 子彈顏色

    // 新增完整版建構子 (支援設定是否為敵方與顏色)
    public Bullet(int x, int y, int vx, int vy, boolean isEnemy, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.isEnemy = isEnemy;
        this.color = color;
    }

    // 為了不破壞舊的程式碼，保留原本的建構子，預設為玩家子彈 (黃色)
    public Bullet(int x, int y, int vx, int vy) {
        this(x, y, vx, vy, false, Color.YELLOW);
    }

    public void update() {
        x += vx; 
        y += vy; 
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, 5, 10);
    }
}