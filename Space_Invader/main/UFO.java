package main;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class UFO {
    public int x, y;
    int speed;
    boolean movingRight;
    public boolean active = false; // 用來控制 UFO 是否存在於畫面上

    public UFO() {
        spawn();
    }

    public void spawn() {
        // 隨機決定從左邊還右邊出現
        movingRight = Math.random() > 0.5;
        y = 50; // 固定在螢幕上方
        speed = 3;
        
        if (movingRight) x = -50;       // 從左側外出現
        else x = 800;                  // 從右側外出現
        active = true;
    }

    public void update() {
        if (!active) return;
        
        if (movingRight) x += speed;
        else x -= speed;

        // 如果飛出螢幕範圍，關閉活動狀態
        if (x < -60 || x > 850) {
            active = false;
        }
    }

    public void draw(Graphics g) {
        if (!active) return;

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

    public Rectangle getBounds() {
        // 回傳一個矩形，位置是 (x, y)，寬度 50，高度 20
        // 這裡的數字要對應你在 draw 方法裡畫圓形的寬高 (fillOval(x, y, 50, 20))
        return new Rectangle(x, y, 50, 20); 
    }
}