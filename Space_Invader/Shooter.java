import java.awt.Color;
import java.awt.Graphics;

public class Shooter {
    int x, y;
    Shooter(int x, int y) { this.x = x; this.y = y; }
    void move(int dx) { x += dx; }
    void draw(Graphics g) { g.setColor(Color.WHITE); g.fillRect(x, y, 50, 20); }
}
