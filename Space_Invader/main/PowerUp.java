package main;
import java.awt.*;

public class PowerUp {
    public int x, y;
    public int type; // 0: 護盾(S), 1: 冰凍(F), 2: 散彈(B), 3: 彈夾(M)
    
    public PowerUp(int x, int y, int type) {
        this.x = x; this.y = y;
        this.type = type;
    }

    public void update() { y += 2; } // 道具緩慢下墜

    public void draw(Graphics g) {
        // 顏色判定
        if (type == 0) g.setColor(Color.BLUE);           // 護盾：藍色
        else if (type == 1) g.setColor(Color.CYAN);      // 冰凍：青藍色
        else if (type == 2) g.setColor(Color.YELLOW);    // 散彈：黃色
        else g.setColor(Color.GREEN);                    // 彈夾：綠色
        
        // 畫實心方塊，中間加上邊框線
        g.fillRect(x, y, 20, 20); 
        g.setColor(Color.BLACK);
        g.drawRect(x, y, 20, 20); 
        
        // 加上簡單文字辨識 (S, F, B, M)
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String label = (type == 0) ? "S" : (type == 1) ? "F" : (type == 2) ? "B" : "M";
        g.drawString(label, x + 6, y + 15);
    }
}