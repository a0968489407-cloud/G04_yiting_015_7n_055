import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;

public class Ball {
    // 靜態變數，用來產生唯一的球體 ID
    public static int num = 0;
    public int id;
    public int radius;
    public Vector2D pos;
    public Vector2D velocity;
    
    // 是否處於凍結狀態 (設定階段不移動)
    public boolean isFreezed;
    // 儲存這顆球身後拉著的所有線段
    public ArrayList<Line> myLines;
    public Color color;
    public int lives; 

    // 存活狀態與死亡時刻紀錄 (用於排行榜排序)
    public boolean isDead;
    public int deathTick;
    // 是否已經碰過牆壁並進入正式遊戲判定
    public boolean hasEnteredGame;

    // 分裂與融合相關屬性
    public boolean isTiny; 
    public long splitTime; 
    public static final long COOLDOWN = 3000; 

    // 紀錄球體最後吃到的道具種類，-1 代表無
    public int currentItemType = -1; 

    public Ball(double startX, double startY, Color assignedColor, boolean isTiny) {
        num++;
        this.id = num;

        this.isTiny = isTiny;
        // 根據是否為分裂後的小球，賦予不同的半徑，並在分裂時記錄時間戳記
        if (this.isTiny) {
            this.radius = 10; 
            this.splitTime = System.currentTimeMillis(); 
        } else {
            this.radius = 20; 
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
        this.lives = 1; 
        this.currentItemType = -1; 
    }

    public void update() {
        // 如果未被凍結且活著，就根據速度向量更新當前座標
        if (!isFreezed && !isDead) {
            pos.x += velocity.x;
            pos.y += velocity.y;
        }
    }

    public void draw(Graphics g) {
        g.setColor(this.color);
        // 以自身座標為中心，畫出實心圓
        g.fillOval((int) pos.x - radius, (int) pos.y - radius, radius * 2, radius * 2);

        // 若擁有多條命，在球體外圍畫出對應數量的綠色保護圈
        if (lives > 1) {
            g.setColor(Color.green.brighter());
            for (int i = 1; i < lives; i++) {
                g.drawOval((int) pos.x - radius - (i * 3), (int) pos.y - radius - (i * 3),
                        radius * 2 + (i * 6), radius * 2 + (i * 6));
            }
        }

        // 若為小球且已經度過融合冷卻時間 (3秒)，在外圍畫出白色發光圈提示玩家
        if (isTiny && (System.currentTimeMillis() - splitTime > COOLDOWN)) {
            g.setColor(Color.WHITE);
            g.drawOval((int) pos.x - radius - 2, (int) pos.y - radius - 2, radius * 2 + 4, radius * 2 + 4);
        }
    }

    // 取得當前持有的線段數量
    public int getLineCount() {
        return myLines.size();
    }
}