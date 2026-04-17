import java.awt.Graphics;
import java.awt.Color;

public class Item {
    public static int num = 0;
    public int radius;
    public Vector2D pos;
    public int type; 
    public boolean isAvailable;

    public Item(double x, double y, int type) {
        num++;
        this.radius = 10;
        this.pos = new Vector2D(x, y);
        this.type = type;
        this.isAvailable = true;
    }

    public void applyEffect(Ball targetBall) { }

    public void draw(Graphics g) {
        g.setColor(Color.YELLOW); 
        g.fillRect((int)pos.x - radius, (int)pos.y - radius, radius * 2, radius * 2);
    }
}