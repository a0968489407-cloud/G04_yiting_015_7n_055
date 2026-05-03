import java.awt.Color;
import java.awt.Graphics;

public class Alien {
    int x, y, width = 40, height = 30;
    boolean isAlive = true;
    int explosionTimer = 0;
    final int MAX_EXPLOSION_TIME = 15;
    double phaseOffset = Math.random() * Math.PI * 2;
    
    // 新增：類型 (0 = 普通, 1 = 俯衝者)
    int type; 

    // 建構子 (預設為普通)
    public Alien() {
        this.type = 0;
    }

    // 建構子 (可指定類型)
    public Alien(int type) {
        this.type = type;
    }

    void draw(Graphics g) {
        if (isAlive) {
            drawNormal(g);
        } else if (explosionTimer < MAX_EXPLOSION_TIME) {
            drawExplosion(g);
            explosionTimer++;
        }
    }

    void drawExplosion(Graphics g) {
        g.setColor(Color.ORANGE);
        java.util.Random rand = new java.util.Random(x + y);
        for (int i = 0; i < 10; i++) {
            int offsetX = rand.nextInt(40) - 20 + (rand.nextInt(5) * explosionTimer / 2);
            int offsetY = rand.nextInt(30) - 15 + (rand.nextInt(5) * explosionTimer / 2);
            g.fillRect(x + 20 + offsetX, y + 15 + offsetY, 3, 3);
        }
    }

    // 繪圖法畫出飛碟 (UFO)
    void drawUFO(Graphics g) {
        // 為了讓飛碟比普通外星人寬一點點，微調座標
        int ufoX = x - 5;
        int ufoWidth = 50;

        // 1. 畫出半圓形圓頂 (駕駛艙)
        g.setColor(Color.CYAN); // 青色圓頂
        g.fillOval(ufoX + 15, y, 20, 15);

        // 2. 畫出扁平的主機身 (飛盤結構)
        g.setColor(Color.MAGENTA); // 洋紅色機身
        g.fillOval(ufoX, y + 10, ufoWidth, 15);

        // 3. 畫出機身邊緣 (增加厚度感)
        g.setColor(new Color(150, 0, 150)); // 深洋紅色
        g.fillRect(ufoX + 5, y + 18, 40, 3);

        // 4. 畫出下方閃爍的燈光
        // 利用 System.currentTimeMillis() 讓燈光產生交替閃爍感
        if ((System.currentTimeMillis() / 250) % 2 == 0) {
            g.setColor(Color.YELLOW);
            g.fillOval(ufoX + 10, y + 20, 5, 5); // 左燈
            g.fillOval(ufoX + 35, y + 20, 5, 5); // 右燈
            g.setColor(Color.RED);
            g.fillOval(ufoX + 22, y + 20, 6, 6); // 中燈
        } else {
            g.setColor(Color.RED);
            g.fillOval(ufoX + 10, y + 20, 5, 5); 
            g.fillOval(ufoX + 35, y + 20, 5, 5);
            g.setColor(Color.YELLOW);
            g.fillOval(ufoX + 22, y + 20, 6, 6);
        }
    }

    void drawNormal(Graphics g) {

        // --- UFO 造型 (type 2) ---
        if (type == 2) {
            drawUFO(g);
            return; // 畫完 UFO 就結束，不畫普通外星人造型
        }

        // 根據類型決定顏色 (俯衝者是紅色，普通是綠色)
        Color bodyColor = (type == 1) ? Color.RED : Color.GREEN;
        Color eyeColor = (type == 1) ? Color.YELLOW : Color.BLACK;

        // 1. 畫出身體
        g.setColor(bodyColor);
        g.fillRect(x + 5, y + 10, 30, 15);
        
        // 2. 畫出觸角
        g.fillRect(x + 10, y + 5, 5, 5);
        g.fillRect(x + 25, y + 5, 5, 5);
        
        // 3. 畫出腳
        g.fillRect(x + 5, y + 25, 5, 5);
        g.fillRect(x + 30, y + 25, 5, 5);
        g.fillRect(x + 15, y + 25, 10, 5);

        // 4. 畫出眼睛
        g.setColor(eyeColor);
        g.fillRect(x + 10, y + 12, 5, 5);
        g.fillRect(x + 25, y + 12, 5, 5);
    }

    public void move(int direction, int wave) {
        if (!this.isAlive) return;

        if (type == 1) {
            // --- 俯衝者移動邏輯 ---
            this.y += 3; // 直接垂直往下衝
            this.x += (Math.random() - 0.5) * 4; // 帶有一點點左右抖動
        } else {
            // --- 普通外星人移動邏輯 ---
            double baseSpeed = 4 + (wave * 0.5);
            double waveAmplitude = 3.0 + (wave * 0.8);
            double oscillator = Math.sin(System.currentTimeMillis() / 250.0 + (y * 0.05)) * waveAmplitude;
            this.x += (direction * baseSpeed) + oscillator;
        }
    }
}