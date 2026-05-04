import java.awt.Color;
import java.awt.Graphics;   

public class Bullet {
    int x, y;
    int vx, vy; // 新增水平與垂直速度

    // 修改建構子，接收速度參數
    public Bullet(int x, int y, int vx, int vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public void update() {
        x += vx; // 子彈會根據水平速度偏移
        y += vy; // 子彈根據垂直速度移動
    }

    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, 5, 10);
    }
}