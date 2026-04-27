import java.awt.Color;
import java.awt.Graphics;


public class Alien {
    int x, y, width = 40, height = 30;
    boolean isAlive = true;      // 是否活著
    int explosionTimer = 0;      // 爆炸動畫計時器
    final int MAX_EXPLOSION_TIME = 15; // 爆炸持續的幀數 (約 0.3 秒)
    double phaseOffset = Math.random() * Math.PI * 2; // 每個外星人初始相位不同

    void draw(Graphics g) {
        if (isAlive) {
            drawNormal(g); // 畫你原本設計的外星人造型
        } else if (explosionTimer < MAX_EXPLOSION_TIME) {
            drawExplosion(g); // 畫爆炸粒子
            explosionTimer++;
        }
    }

    // 這裡用程式畫出「碎裂」的效果
    void drawExplosion(Graphics g) {
        g.setColor(Color.ORANGE);
        java.util.Random rand = new java.util.Random(x + y); // 固定種子讓粒子不會亂閃
        for (int i = 0; i < 10; i++) {
            // 讓粒子隨著時間往外擴散
            int offsetX = rand.nextInt(40) - 20 + (rand.nextInt(5) * explosionTimer / 2);
            int offsetY = rand.nextInt(30) - 15 + (rand.nextInt(5) * explosionTimer / 2);
            g.fillRect(x + 20 + offsetX, y + 15 + offsetY, 3, 3);
        }
    }

    /*void move(int direction) {
    if (this.isAlive) { // 只有活著才更新座標
        this.x += direction * 5;
    }
    }*/
    void drawNormal(Graphics g) {
        // 1. 畫出身體 (主體)
        g.setColor(Color.GREEN);
        g.fillRect(x + 5, y + 10, 30, 15); 
        
        // 2. 畫出觸角 (頭部)
        g.fillRect(x + 10, y + 5, 5, 5);
        g.fillRect(x + 25, y + 5, 5, 5);
        
        // 3. 畫出腳
        g.fillRect(x + 5, y + 25, 5, 5);
        g.fillRect(x + 30, y + 25, 5, 5);
        g.fillRect(x + 15, y + 25, 10, 5);

        // 4. 畫出眼睛 (黑色)
        g.setColor(Color.BLACK);
        g.fillRect(x + 10, y + 12, 5, 5);
        g.fillRect(x + 25, y + 12, 5, 5);
    }

    public void move(int direction, int wave) {
        if (!this.isAlive) return;

        // 基礎速度隨回合提升
        double baseSpeed = 4 + (wave * 0.5);
        
        // 相位偏移 (第一輪就有的蛇行感)
        double waveAmplitude = 3.0 + (wave * 0.8); // 隨回合晃得越來越大
        double oscillator = Math.sin(System.currentTimeMillis() / 250.0 + (y * 0.05)) * waveAmplitude;

        // 更新座標
        this.x += (direction * baseSpeed) + oscillator;
    }

}


