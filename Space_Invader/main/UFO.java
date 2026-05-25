package main;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class UFO {
    // 實體座標與速度
    public int x, y;
    int speed;
    // 標記飛行方向以決定更新座標時做加減
    boolean movingRight;
    // 布林旗標控制此物件是否該被更新運算或渲染
    public boolean active = false; 

    public UFO() {
        spawn();
    }

    public void spawn() {
        // 透過 Math.random() 回傳 0.0~1.0 的特性，大於 0.5 時布林為 true，達到 50% 兩側機率
        movingRight = Math.random() > 0.5;
        // 強制鎖死 Y 軸高度為 50
        y = 50; 
        speed = 3;
        
        // 依據決定好的方向，設定出發點在畫面的左外側或右外側
        if (movingRight) x = -50;       
        else x = 800;                  
        // 狀態啟動
        active = true;
    }

    public void update() {
        if (!active) return;
        
        // 依據方向旗標對 X 軸加上或減去 speed 常數
        if (movingRight) x += speed;
        else x -= speed;

        // 當移動越過左右極限邊界時 (-60 與 850)，自行關閉活動狀態退出迴圈運算
        if (x < -60 || x > 850) {
            active = false;
        }
    }

    public void draw(Graphics g) {
        if (!active) return;

        // 為了將圖形中心拉平至原本的判定點，手動加上負偏移量
        int ufoX = x - 5;
        int ufoWidth = 50;

        // 使用 fillOval 畫出駕駛艙頂圓弧
        g.setColor(Color.CYAN); 
        g.fillOval(ufoX + 15, y, 20, 15);

        // 使用 fillOval 畫出底層機身盤面
        g.setColor(Color.MAGENTA); 
        g.fillOval(ufoX, y + 10, ufoWidth, 15);

        // 使用 fillRect 畫出底部結構增加厚度
        g.setColor(new Color(150, 0, 150)); 
        g.fillRect(ufoX + 5, y + 18, 40, 3);

        // 利用 System.currentTimeMillis() 除以 250(毫秒) 取 2 的餘數
        // 當餘數為 0 或 1 進行切換判斷，能製造每 0.25 秒交換顏色的閃爍燈號視覺
        if ((System.currentTimeMillis() / 250) % 2 == 0) {
            g.setColor(Color.YELLOW);
            g.fillOval(ufoX + 10, y + 20, 5, 5); 
            g.fillOval(ufoX + 35, y + 20, 5, 5); 
            g.setColor(Color.RED);
            g.fillOval(ufoX + 22, y + 20, 6, 6); 
        } else {
            g.setColor(Color.RED);
            g.fillOval(ufoX + 10, y + 20, 5, 5); 
            g.fillOval(ufoX + 35, y + 20, 5, 5);
            g.setColor(Color.YELLOW);
            g.fillOval(ufoX + 22, y + 20, 6, 6);
        }
    
    }

    public Rectangle getBounds() {
        // 利用類別實體的 X,Y 值配合常數 50,20，實例化並回傳 AABB 邊界矩形，提供外部實作intersects判定碰撞
        return new Rectangle(x, y, 50, 20); 
    }
}