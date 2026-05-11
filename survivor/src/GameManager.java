import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;
import java.util.Random; // 新增：用於道具生成隨機位置

public class GameManager {
    public ArrayList<Ball> balls;
    public ArrayList<Item> items;
    public GameState currentState;
    public GameMode currentMode;

    public double arenaCenterX, arenaCenterY, arenaRadius;
    public Ball pendingBall = null; 
    private int playTimeTicks = 0; 

    // 新增：用於道具生成的計時器與隨機工具
    private Random random = new Random();
    private int itemSpawnTimer = 0;

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
        this.itemSpawnTimer = 0; // 重置計時器
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

        // --- 新增：道具生成邏輯 ---
        if (currentMode == GameMode.ITEM_MODE) {
            itemSpawnTimer++;
            if (itemSpawnTimer > 300) { // 約每 5 秒嘗試生成一個道具
                spawnRandomItem();
                itemSpawnTimer = 0;
            }
        }

        for (Ball b : balls) b.update();

        checkWallCollisions();
        checkBallCollisions();
        checkLineCollisions();
        
        // --- 新增：核心機制處理 ---
        checkItemCollisions(); // 檢查球與道具（加速、減速、分裂）
        checkTinyBallMerge();  // 檢查小球是否可以融合回大球
        
        checkSurvival();
    }

    // 新增：在圓形競技場內隨機生成道具
    private void spawnRandomItem() {
        double angle = random.nextDouble() * Math.PI * 2;
        // 讓道具生成在距離邊界稍遠一點的地方，避免剛生成就在牆壁外
        double r = Math.sqrt(random.nextDouble()) * (arenaRadius - 50);
        double x = arenaCenterX + r * Math.cos(angle);
        double y = arenaCenterY + r * Math.sin(angle);
        
        // 隨機決定道具類型 (2:加速, 4:減速, 5:分裂, 6:生命)
        int[] types = {2, 4, 5, 6};
        int type = types[random.nextInt(types.length)];
        
        // 根據類型建立對應物件
        if (type == 2) items.add(new ItemSpeedUp(x, y));
        else if (type == 4) items.add(new ItemSpeedDown(x, y));
        else if (type == 6) items.add(new ItemLife(x, y)); // 生命道具
        else items.add(new Item(x, y, 5)); // 分裂道具
    }

    // 新增：處理球與道具的碰撞與分裂
    private void checkItemCollisions() {
        ArrayList<Ball> toAdd = new ArrayList<>();
        ArrayList<Ball> toRemove = new ArrayList<>();
        Iterator<Item> itemIter = items.iterator();

        while (itemIter.hasNext()) {
            Item item = itemIter.next();
            for (Ball b : balls) {
                if (b.isDead) continue;

                double dx = b.pos.x - item.pos.x;
                double dy = b.pos.y - item.pos.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < b.radius + item.radius) {
                    if (item.type == 5 && !b.isTiny) { // 觸發分裂：必須是分裂道具且球不是小球
                        toRemove.add(b);
                        
                        // 建立兩顆小球，繼承原本大球的顏色與位置
                        Ball s1 = new Ball(b.pos.x + 5, b.pos.y, b.color, true);
                        Ball s2 = new Ball(b.pos.x - 5, b.pos.y, b.color, true);

                        //小球繼承大球生命
                        s1.lives = b.lives; // 繼承生命
                        s2.lives = b.lives;

                        double speedMultiplier = 1.3;
                        double originalSpeed = Math.sqrt(b.velocity.x * b.velocity.x + b.velocity.y * b.velocity.y);
                        double angle = Math.atan2(b.velocity.y, b.velocity.x);
                        
                        // s1 往左偏 30 度，s2 往右偏 30 度
                        double angle1 = angle + Math.toRadians(30);
                        double angle2 = angle - Math.toRadians(30);

                        // 讓小球繼承大球目前的線段，這樣它們才不會被判定為「沒線而死」
                        s1.myLines = new ArrayList<>(b.myLines); 
                        s2.myLines = new ArrayList<>(b.myLines);
                        
                        s1.velocity.x = Math.cos(angle1) * originalSpeed * speedMultiplier;
                        s1.velocity.y = Math.sin(angle1) * originalSpeed * speedMultiplier;
                        
                        s2.velocity.x = Math.cos(angle2) * originalSpeed * speedMultiplier;
                        s2.velocity.y = Math.sin(angle2) * originalSpeed * speedMultiplier;
                                            
                        s1.isFreezed = s2.isFreezed = false;
                        s1.hasEnteredGame = true; // 確保小球被視為已進入遊戲
                        s2.hasEnteredGame = true;
                        
                        toAdd.add(s1);
                        toAdd.add(s2);
                    } else {
                        // 執行加速或減速效果
                        item.applyEffect(b);
                    }
                    itemIter.remove(); // 道具被吃掉
                    break;
                }
            }
        }
        // 統一更新 Ball 清單，避免 ConcurrentModificationException
        balls.removeAll(toRemove);
        balls.addAll(toAdd);
    }

    // 新增：處理小球融合機制
    private void checkTinyBallMerge() {
        ArrayList<Ball> toAdd = new ArrayList<>();
        ArrayList<Ball> toRemove = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball b1 = balls.get(i);
                Ball b2 = balls.get(j);

                // 融合條件：都是小球、同顏色、皆已過冷卻時間
                if (b1.isTiny && b2.isTiny && b1.color.equals(b2.color)) {
                    if (now - b1.splitTime > Ball.COOLDOWN && now - b2.splitTime > Ball.COOLDOWN) {
                        double dx = b1.pos.x - b2.pos.x;
                        double dy = b1.pos.y - b2.pos.y;
                        double dist = Math.sqrt(dx * dx + dy * dy);

                        if (dist < b1.radius + b2.radius) {
                            toRemove.add(b1);
                            toRemove.add(b2);
                            
                            // 融合回原本的大球
                            Ball merged = new Ball(b1.pos.x, b1.pos.y, b1.color, false);
                            merged.myLines = new ArrayList<>(b1.myLines); // 繼承其中一顆小球的線段
                            merged.velocity = b1.velocity; // 繼承動能
                            merged.isFreezed = false;
                            merged.hasEnteredGame = true;
                            
                            toAdd.add(merged);
                        }
                    }
                }
            }
        }
        balls.removeAll(toRemove);
        balls.addAll(toAdd);
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
                
                // 新增：如果兩顆球正處於可融合狀態，不觸發物理碰撞
                if (b1.isTiny && b2.isTiny && b1.color.equals(b2.color)) {
                    long now = System.currentTimeMillis();
                    if (now - b1.splitTime > Ball.COOLDOWN) continue; 
                }

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
                // 原本條件：if (otherBall.isDead || b == otherBall) continue;
                
                // 修正條件：如果是死球、是自己、或是「同顏色的球」，則不消對方的線
                if (otherBall.isDead || b == otherBall || b.color.equals(otherBall.color)) {
                    continue; 
                }
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
                if (b.lives > 1) {
                    // --- 復活邏輯 ---
                    b.lives--; 
                    // 1. 給予一條從球心指向圓心的虛擬線段，防止下一幀判定死亡
                    double wallX = arenaCenterX; 
                    double wallY = arenaCenterY;
                    b.myLines.add(new Line(wallX, wallY, b));
                    
                    // 2. 稍微彈開球體或改變方向，增加生存機會
                    b.velocity.x *= -0.8; 
                    b.velocity.y *= -0.8;
                    
                    System.out.println("球 " + b.id + " 消耗生命復活！剩餘生命: " + b.lives);
                } else {
                    b.isDead = true;
                    b.deathTick = playTimeTicks; 
                }
            } 
            else {
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