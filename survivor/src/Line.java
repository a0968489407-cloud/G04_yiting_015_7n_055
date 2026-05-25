import java.awt.Graphics;
import java.awt.Color;

public class Line {
    // 紀錄線段的起點座標 (通常是球撞擊牆壁的位置)
    public double startX, startY; 
    // 紀錄這條線綁定的目標球體
    public Ball targetBall;       

    public Line(double startX, double startY, Ball targetBall) {
        this.startX = startX;
        this.startY = startY;
        this.targetBall = targetBall;
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        // 畫出一條從起點連接到目標球體當前座標的直線
        g.drawLine((int)startX, (int)startY, (int)targetBall.pos.x, (int)targetBall.pos.y);
    }
}