import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

public class Btn {
    public int x, y, width, height;
    public String text;
    public boolean isHovered;

    public Btn(int x, int y, int width, int height, String text) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.text = text; this.isHovered = false;
    }

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void draw(Graphics g) {
        if (isHovered) g.setColor(Color.DARK_GRAY);
        else g.setColor(Color.WHITE);

        g.setFont(new Font("New Times Roman", Font.BOLD, 50));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(text, textX, textY);
    }
}