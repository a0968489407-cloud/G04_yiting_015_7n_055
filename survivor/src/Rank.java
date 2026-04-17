import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

public class Rank {
    public static void drawRanking(Graphics g, ArrayList<Ball> balls) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("New Times Roman", Font.BOLD, 40)); 
        g.drawString("Rank", 30, 40); 

        if (balls == null || balls.isEmpty()) return;

        ArrayList<Ball> sortedBalls = new ArrayList<>(balls);
        sortedBalls.sort((b1, b2) -> {
            if (b1.isDead != b2.isDead) return b1.isDead ? 1 : -1; 
            if (b1.isDead && b2.isDead) return b2.deathTick - b1.deathTick;
            int lineDiff = b2.getLineCount() - b1.getLineCount();
            if (lineDiff != 0) return lineDiff; 
            return b1.id - b2.id; 
        });

        int startX = 50, currentY = 80; 

        for (int i = 0; i < sortedBalls.size(); i++) {
            Ball b = sortedBalls.get(i);
            int rank = i + 1;
            int size = (rank == 1) ? 60 : (rank == 2) ? 45 : 30;
            int offset = (60 - size) / 2;

            if (b.isDead) {
                g.setColor(Color.DARK_GRAY); 
                g.fillOval(startX + offset, currentY, size, size);
                g.setColor(b.color);         
                g.drawOval(startX + offset, currentY, size, size);
            } else {
                g.setColor(b.color);         
                g.fillOval(startX + offset, currentY, size, size);
            }
            currentY += size + 20; 
        }
    }
}