package main;
import java.awt.Color;
import java.awt.Graphics;

public class Star {
    int x, y, size;
    int alpha;       // 透明度 (0-255)
    int fadeSpeed;   // 閃爍速度
    int fallSpeed;   // === 新增：掉落速度 ===

    Star(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.alpha = new java.util.Random().nextInt(255); 
        this.fadeSpeed = 2 + new java.util.Random().nextInt(5); 
        
        // === 新增：隨機設定星星的掉落速度 (1 ~ 3)，營造遠近層次感 ===
        this.fallSpeed = 1 + new java.util.Random().nextInt(3); 
    }

    void twinkle() {
        alpha += fadeSpeed;
        if (alpha <= 50 || alpha >= 255) {
            fadeSpeed *= -1;
        }
        alpha = Math.max(50, Math.min(255, alpha));
    }

    // === 新增：星星往下移動的邏輯 ===
    void move() {
        y += fallSpeed;
        // 如果星星掉出畫面底部 (y > 800)，讓它回到最上面，並隨機換個 X 座標
        if (y > 800) {
            y = 0;
            x = new java.util.Random().nextInt(800);
        }
    }

    void draw(Graphics g) {
        g.setColor(new Color(255, 255, 255, alpha)); 
        g.fillOval(x, y, size, size);
    }
}