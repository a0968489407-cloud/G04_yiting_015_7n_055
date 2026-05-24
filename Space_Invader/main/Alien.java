package main;
import java.awt.Color;
import java.awt.Graphics;

public class Alien {
    public int x, y, width = 40, height = 30;
    public boolean isAlive = true;
    public int explosionTimer = 0;
    public final int MAX_EXPLOSION_TIME = 15;
    
    public int type; // 0=綠, 1=紅, 2=藍, 3=灰, 4=紫
    public int hp;   // 外星人血量
    public int shootTimer = 0; // 紫色專用射擊計時器

    public Alien(int type) {
        this.type = type;
        this.hp = (type == 3) ? 2 : 1; // 灰色裝甲型需要2次攻擊
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

    void drawNormal(Graphics g) {
        Color bodyColor;
        Color eyeColor = Color.BLACK;

        // 依據類型設定顏色
        switch(type) {
            case 1: bodyColor = Color.RED; eyeColor = Color.YELLOW; break;
            case 2: bodyColor = Color.BLUE; eyeColor = Color.WHITE; break;
            case 3: bodyColor = Color.GRAY; eyeColor = Color.RED; break;
            case 4: bodyColor = new Color(153, 50, 204); eyeColor = Color.GREEN; break; // 紫色
            default: bodyColor = Color.GREEN; break; // 0: 綠色基本型
        }

        // --- 若灰色裝甲型受損 (hp == 1)，改變外觀提示玩家 ---
        if (type == 3 && hp == 1) {
            bodyColor = Color.DARK_GRAY;
        }

        // 畫身體
        g.setColor(bodyColor);
        g.fillRect(x + 5, y + 10, 30, 15);
        // 觸角
        g.fillRect(x + 10, y + 5, 5, 5);
        g.fillRect(x + 25, y + 5, 5, 5);
        // 腳
        g.fillRect(x + 5, y + 25, 5, 5);
        g.fillRect(x + 30, y + 25, 5, 5);
        g.fillRect(x + 15, y + 25, 10, 5);
        // 眼睛
        g.setColor(eyeColor);
        g.fillRect(x + 10, y + 12, 5, 5);
        g.fillRect(x + 25, y + 12, 5, 5);
    }

    // 新增 targetX (玩家位置) 作為追蹤依據
    public void move(int direction, int wave, int targetX) {
        if (!this.isAlive) return;

        if (type == 1) {
            // 紅色 (綠色延伸)：快速下降，左右波浪範圍極大且極快
            this.y += 3; 
            double baseSpeed = 8 + wave;
            double oscillator = Math.sin(System.currentTimeMillis() / 200.0 + (y * 0.1)) * 5.0;
            this.x += (direction * baseSpeed) + oscillator;
            
        } else if (type == 2) {
            // 藍色 (紅色延伸)：快速下降，並水平追蹤玩家
            this.y += 3;
            if (this.x < targetX) this.x += 4 + (wave * 0.5);
            else if (this.x > targetX) this.x -= 4 + (wave * 0.5);
            
        } else {
            // 綠、灰、紫：標準移動
            double baseSpeed = 4 + (wave * 0.5);
            double waveAmplitude = 3.0 + (wave * 0.8);
            double oscillator = Math.sin(System.currentTimeMillis() / 250.0 + (y * 0.05)) * waveAmplitude;
            this.x += (direction * baseSpeed) + oscillator;
        }
    }
}