import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

public class GameManager {
    public ArrayList<Ball> balls;
    public ArrayList<Item> items;
    public GameState currentState;
    public GameMode currentMode;

    public double arenaCenterX, arenaCenterY, arenaRadius;
    public Ball pendingBall = null; 
    private int playTimeTicks = 0; 

    public GameManager(double cx, double cy, double r) {
        this.balls = new ArrayList<>();
        this.items = new ArrayList<>();
        this.currentState = GameState.SETUP;
        this.currentMode = GameMode.CLASSIC;
        this.arenaCenterX = cx;
        this.arenaCenterY = cy;
        this.arenaRadius = r;
    }

    public void resetGame() {
        this.balls.clear();
        this.items.clear();
        this.currentState = GameState.SETUP;
        this.pendingBall = null;
        this.playTimeTicks = 0;
        Ball.num = 0; 
    }

    public Color getAvailableColor() {
        Color[] allColors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.PINK, Color.ORANGE};
        for (Color c : allColors) {
            boolean isUsed = false;
            for (Ball b : balls) {
                if (b.color.equals(c)) { isUsed = true; break; }
            }
            if (!isUsed) return c;
        }
        return Color.WHITE; 
    }

    public boolean isInsideArena(double x, double y) {
        double dx = x - arenaCenterX;
        double dy = y - arenaCenterY;
        return Math.sqrt(dx * dx + dy * dy) <= arenaRadius;
    }

    public void update() {
        if (currentState != GameState.PLAYING) return;
        playTimeTicks++; 

        for (Ball b : balls) b.update();

        checkWallCollisions();
        checkBallCollisions();
        checkLineCollisions();
        checkSurvival();
    }

    private void checkWallCollisions() {
        for (Ball b : balls) {
            if (b.isDead) continue; 

            double dx = b.pos.x - arenaCenterX;
            double dy = b.pos.y - arenaCenterY;
            double distanceToCenter = Math.sqrt(dx * dx + dy * dy);

            if (distanceToCenter + b.radius >= arenaRadius) {
                double nx = dx / distanceToCenter;
                double ny = dy / distanceToCenter;

                b.pos.x = arenaCenterX + nx * (arenaRadius - b.radius);
                b.pos.y = arenaCenterY + ny * (arenaRadius - b.radius);

                double dotProduct = b.velocity.x * nx + b.velocity.y * ny;
                b.velocity.x = b.velocity.x - 2 * dotProduct * nx;
                b.velocity.y = b.velocity.y - 2 * dotProduct * ny;

                double wallX = arenaCenterX + nx * arenaRadius;
                double wallY = arenaCenterY + ny * arenaRadius;
                b.myLines.add(new Line(wallX, wallY, b));
                
                b.hasEnteredGame = true; 
            }
        }
    }

    private void checkBallCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball b1 = balls.get(i);
                Ball b2 = balls.get(j);

                if (b1.isDead || b2.isDead) continue; 

                double dx = b2.pos.x - b1.pos.x;
                double dy = b2.pos.y - b1.pos.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < b1.radius + b2.radius) {
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double overlap = (b1.radius + b2.radius) - dist;
                    b1.pos.x -= nx * overlap / 2;
                    b1.pos.y -= ny * overlap / 2;
                    b2.pos.x += nx * overlap / 2;
                    b2.pos.y += ny * overlap / 2;

                    double kx = b1.velocity.x - b2.velocity.x;
                    double ky = b1.velocity.y - b2.velocity.y;
                    double p = 2.0 * (nx * kx + ny * ky) / 2.0;

                    b1.velocity.x -= p * nx;
                    b1.velocity.y -= p * ny;
                    b2.velocity.x += p * nx;
                    b2.velocity.y += p * ny;
                }
            }
        }
    }

    private void checkLineCollisions() {
        for (Ball b : balls) {
            if (b.isDead || b.myLines.isEmpty()) continue; 

            for (Ball otherBall : balls) {
                if (otherBall.isDead || b == otherBall) continue; 

                Iterator<Line> iterator = otherBall.myLines.iterator();
                while (iterator.hasNext()) {
                    Line line = iterator.next();
                    double distToLine = pointToSegmentDistance(b.pos.x, b.pos.y, line.startX, line.startY, otherBall.pos.x, otherBall.pos.y);
                    
                    if (distToLine <= b.radius) iterator.remove();
                }
            }
        }
    }

    private void checkSurvival() {
        int aliveCount = 0;
        boolean someoneEntered = false; 

        for (Ball b : balls) {
            if (b.isDead) continue; 

            if (b.hasEnteredGame && b.myLines.isEmpty()) {
                b.isDead = true;
                b.deathTick = playTimeTicks; 
            } else {
                aliveCount++;
                if (b.hasEnteredGame) someoneEntered = true; 
            }
        }
        
        if (balls.size() >= 2 && someoneEntered && aliveCount <= 1) { 
            currentState = GameState.GAME_OVER;
        }
    }

    private double pointToSegmentDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1, B = py - y1, C = x2 - x1, D = y2 - y1;
        double dot = A * C + B * D, len_sq = C * C + D * D, param = -1;
        if (len_sq != 0) param = dot / len_sq;

        double xx, yy;
        if (param < 0) { xx = x1; yy = y1; } 
        else if (param > 1) { xx = x2; yy = y2; } 
        else { xx = x1 + param * C; yy = y1 + param * D; }

        double dx = px - xx, dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}