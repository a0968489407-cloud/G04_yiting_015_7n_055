import java.awt.Color;
import java.awt.Graphics;


public class Bullet {
    int x, y;
    Bullet(int x, int y) { this.x = x; this.y = y; }
    void update() { y -= 10; }
    void draw(Graphics g) { g.setColor(Color.RED); g.fillRect(x, y, 5, 10); }

}
