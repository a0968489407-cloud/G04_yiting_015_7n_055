package main;
import java.awt.*;

public class PowerUp {
    public int x, y;
    public int type; // 0: 防護罩, 1: 減速, 2: 大量子彈
    
    public PowerUp(int x, int y, int type) {
        this.x = x; this.y = y;
        this.type = type;
    }

    public void update() { y += 2; } // 道具緩慢下墜

    public void draw(Graphics g) {
    // 顏色判定
    if (type == 0) g.setColor(Color.BLUE);
    else if (type == 1) g.setColor(Color.WHITE);
    else g.setColor(Color.YELLOW);
    
    // 畫實心方塊，中間加上邊框線
    g.fillRect(x, y, 20, 20); // 實心背景
    g.setColor(Color.BLACK);
    g.drawRect(x, y, 20, 20); // 黑色邊框
    
    // 加上簡單文字辨識 (例如 S, D, B)
    g.setFont(new Font("Arial", Font.BOLD, 12));
    String label = (type == 0) ? "S" : (type == 1) ? "D" : "B";
    g.drawString(label, x + 6, y + 15);
    }
}

