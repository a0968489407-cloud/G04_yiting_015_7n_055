import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;

public class Ball {
    public static int num = 0;
    public int id;
    public int radius;
    public Vector2D pos;
    public Vector2D velocity;
    public boolean isFreezed;
    public ArrayList<Line> myLines;
    public Color color; 
    
    public boolean isDead;  
    public int deathTick;   
    public boolean hasEnteredGame; 

    public Ball(double startX, double startY, Color assignedColor) {
        num++;
        this.id = num;
        this.radius = 20;
        this.pos = new Vector2D(startX, startY);
        this.velocity = new Vector2D(0, 0);
        this.isFreezed = true;
        this.myLines = new ArrayList<>();
        this.color = assignedColor; 
        this.isDead = false;
        this.deathTick = 0;
        this.hasEnteredGame = false; 
    }

    public void update() {
        if (!isFreezed && !isDead) { 
            pos.add(velocity);
        }
    }

    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillOval((int)pos.x - radius, (int)pos.y - radius, radius * 2, radius * 2);
    }
    
    public int getLineCount() {
        return myLines.size();
    }
}