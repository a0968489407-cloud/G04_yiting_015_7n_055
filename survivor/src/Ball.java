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
    public int lives; // 新增生命值屬性 
    
    public boolean isDead;  
    public int deathTick;   
    public boolean hasEnteredGame; 

    // 分裂與融合相關屬性
    public boolean isTiny;      // 是否為分裂後的小球
    public long splitTime;      // 記錄分裂發生的時間點（毫秒）
    public static final long COOLDOWN = 3000; // 分裂後 3 秒才能融合
     

    public Ball(double startX, double startY, Color assignedColor, boolean isTiny) {
        num++;
        this.id = num;
        
        // --- 修改處：根據是否為小球決定半徑 ---
        this.isTiny = isTiny;
        if (this.isTiny) {
            this.radius = 10; // 小球半徑
            this.splitTime = System.currentTimeMillis(); // 記錄分裂時間
        } else {
            this.radius = 20; // 一般球半徑
            this.splitTime = 0;
        }
        
        this.pos = new Vector2D(startX, startY);
        this.velocity = new Vector2D(0, 0);
        this.isFreezed = true;
        this.myLines = new ArrayList<>();
        this.color = assignedColor; 
        this.isDead = false;
        this.deathTick = 0;
        this.hasEnteredGame = false; 
        this.lives = 1; // 預設 1 條命

    }

    public void update() {
        if (!isFreezed && !isDead) { 
            pos.add(velocity);
        }
    }

    public void draw(Graphics g) {
        g.setColor(this.color);
        // 使用成員變數 radius 繪製，這樣大小球就會有視覺差異
        g.fillOval((int)pos.x - radius, (int)pos.y - radius, radius * 2, radius * 2);

        // 如果有額外的生命，畫一個外圈提示
        if (lives > 1) {
            g.setColor(Color.green.brighter());
            for (int i = 1; i < lives; i++) {
                g.drawOval((int)pos.x - radius - (i * 3), (int)pos.y - radius - (i * 3), 
                        radius * 2 + (i * 6), radius * 2 + (i * 6));
            }
        }
        
        // 可選：如果想讓可以融合的小球有發光效果，可以在這裡加邏輯
        if (isTiny && (System.currentTimeMillis() - splitTime > COOLDOWN)) {
            g.setColor(Color.WHITE);
            g.drawOval((int)pos.x - radius - 2, (int)pos.y - radius - 2, radius * 2 + 4, radius * 2 + 4);
        }
    }
    
    public int getLineCount() {
        return myLines.size();
    }
}