import java.awt.Graphics;
import java.awt.Color;

public class Line {
    public double startX, startY; 
    public Ball targetBall;       

    public Line(double startX, double startY, Ball targetBall) {
        this.startX = startX;
        this.startY = startY;
        this.targetBall = targetBall;
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawLine((int)startX, (int)startY, (int)targetBall.pos.x, (int)targetBall.pos.y);
    }
}