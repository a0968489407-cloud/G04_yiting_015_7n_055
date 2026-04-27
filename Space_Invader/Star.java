import java.awt.Color;
import java.awt.Graphics;

public class Star {
    int x, y, size;
    int alpha;       // 透明度 (0-255)
    int fadeSpeed;   // 閃爍速度

    Star(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.alpha = new java.util.Random().nextInt(255); // 隨機初始亮度
        this.fadeSpeed = 2 + new java.util.Random().nextInt(5); // 隨機閃爍速度
    }

    void twinkle() {
        alpha += fadeSpeed;
        // 當太亮或太暗時，反轉速度方向
        if (alpha <= 50 || alpha >= 255) {
            fadeSpeed *= -1;
        }
        // 確保不超出範圍
        alpha = Math.max(50, Math.min(255, alpha));
    }

    void draw(Graphics g) {
        g.setColor(new Color(255, 255, 255, alpha)); // 使用帶有透明度的白色
        g.fillOval(x, y, size, size);
    }
}

