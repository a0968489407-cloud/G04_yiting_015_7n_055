import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;
import java.util.Random; 

public class GameManager {
    public ArrayList<Ball> balls;
    public ArrayList<Item> items;
    // 使用內部定義的 Enum 控制遊戲流程狀態
    public GameState currentState;
    // 使用內部定義的 Enum 記錄當前遊戲模式
    public GameMode currentMode;

    public double arenaCenterX, arenaCenterY, arenaRadius;
    public Ball pendingBall = null;
    private int playTimeTicks = 0;

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

    // 將所有實體清空，重置遊戲與時間回到初始設定
    public void resetGame() {
        this.balls.clear();
        this.items.clear();
        this.currentState = GameState.SETUP;
        this.pendingBall = null;
        this.playTimeTicks = 0;
        this.itemSpawnTimer = 0; 
        Ball.num = 0;
    }

    // 遍歷所有顏色，回傳一個目前場上還沒有被使用過的新顏色
    public Color getAvailableColor() {
        Color[] allColors = { Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.PINK, Color.ORANGE };
        for (Color c : allColors) {
            boolean isUsed = false;
            for (Ball b : balls) {
                if (b.color.equals(c)) {
                    isUsed = true;
                    break;
                }
            }
            if (!isUsed) return c;
        }
        return Color.WHITE;
    }

    // 計算目標座標與中心點的距離，判定是否在競技場的圓形範圍內
    public boolean isInsideArena(double x, double y) {
        double dx = x - arenaCenterX;
        double dy = y - arenaCenterY;
        return Math.sqrt(dx * dx + dy * dy) <= arenaRadius;
    }

    // 時間累加器，用於實現遊戲加速機制
    private double timeAccumulator = 0.0;
    
    public void update() {
        if (currentState != GameState.PLAYING) return;

        // 每幀將目前的倍速加進時間池
        timeAccumulator += Display.currentSpeedMultiplier;

        // 當時間池超過 1，就進行一次物理更新，以此達到不掉影格的平滑加速
        while (timeAccumulator >= 1.0) {
            timeAccumulator -= 1.0;
            playTimeTicks++;

            // 道具模式：計時生成道具
            if (currentMode == GameMode.ITEM_MODE) {
                itemSpawnTimer++;
                // 每 90 幀 (1.5秒) 產生一個隨機道具
                if (itemSpawnTimer >= 60 * 1.5) { 
                    spawnRandomItem();
                    itemSpawnTimer = 0;      
                }
            }

            if (currentMode == GameMode.ITEM_MODE && items != null && balls != null) {
                applyItemMagnetForce();
            }

            for (Ball b : balls) {
                b.update();
            }

            // 執行一連串的物理碰撞與遊戲邏輯判定
            checkWallCollisions();
            checkBallCollisions();
            checkLineCollisions();
            checkItemCollisions(); 
            checkTinyBallMerge();  
            checkSurvival();
        }
    }

    // 檢查球與道具的碰撞邏輯
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

                // 碰撞判定成立
                if (dist < b.radius + item.radius) {
                    // 根據種類播放對應音效
                    if (item.type == 2) SoundManager.playSpeedUp();
                    else if (item.type == 4) SoundManager.playSpeedDown();
                    else if (item.type == 5) SoundManager.playSplit();
                    else SoundManager.playExtraLife();

                    // 大球吃到分裂道具：一分為二
                    if (item.type == 5 && !b.isTiny) {
                        toRemove.add(b);

                        // 建立兩顆微小位移的新小球，並複製原有的生命值
                        Ball s1 = new Ball(b.pos.x + 5, b.pos.y, b.color, true);
                        Ball s2 = new Ball(b.pos.x - 5, b.pos.y, b.color, true);
                        s1.lives = s2.lives = b.lives;
                        s1.currentItemType = s2.currentItemType = 5;

                        // 速度略微提升 1.3 倍，角度互相偏離 30 度
                        double speedMultiplier = 1.3;
                        double originalSpeed = Math.sqrt(b.velocity.x * b.velocity.x + b.velocity.y * b.velocity.y);
                        double angle = Math.atan2(b.velocity.y, b.velocity.x);
                        double angle1 = angle + Math.toRadians(30);
                        double angle2 = angle - Math.toRadians(30);

                        // 複製線段並把 targetBall 指向新分裂的球體
                        for (Line oldLine : b.myLines) {
                            s1.myLines.add(new Line(oldLine.startX, oldLine.startY, s1));
                            s2.myLines.add(new Line(oldLine.startX, oldLine.startY, s2));
                        }

                        s1.velocity.x = Math.cos(angle1) * originalSpeed * speedMultiplier;
                        s1.velocity.y = Math.sin(angle1) * originalSpeed * speedMultiplier;
                        s2.velocity.x = Math.cos(angle2) * originalSpeed * speedMultiplier;
                        s2.velocity.y = Math.sin(angle2) * originalSpeed * speedMultiplier;

                        s1.isFreezed = s2.isFreezed = false;
                        s1.hasEnteredGame = s2.hasEnteredGame = true;

                        toAdd.add(s1);
                        toAdd.add(s2);

                    // 已分裂的小球吃到新道具：與另一顆夥伴球合體變回大球
                    } else if (b.isTiny) {
                        Ball partner = null;
                        for (Ball other : balls) {
                            if (other != b && other.color.equals(b.color) && !other.isDead && other.isTiny) {
                                partner = other;
                                break;
                            }
                        }

                        // 隨機選一顆球作為重生的基準點
                        Ball luckyBall = b;
                        Ball absorbedBall = partner;
                        if (partner != null && random.nextBoolean()) {
                            luckyBall = partner;
                            absorbedBall = b;
                        }

                        toRemove.add(luckyBall);
                        if (absorbedBall != null) toRemove.add(absorbedBall);

                        // 合體成為普通大球
                        Ball bigBall = new Ball(luckyBall.pos.x, luckyBall.pos.y, luckyBall.color, false);
                        bigBall.velocity = new Vector2D(luckyBall.velocity.x, luckyBall.velocity.y);
                        bigBall.lives = luckyBall.lives;
                        bigBall.isFreezed = false;
                        bigBall.hasEnteredGame = true;
                        bigBall.currentItemType = item.type; 

                        // 兩顆小球的線全部彙整到新大球上
                        for (Line oldLine : luckyBall.myLines) {
                            bigBall.myLines.add(new Line(oldLine.startX, oldLine.startY, bigBall));
                        }
                        if (absorbedBall != null) {
                            for (Line oldLine : absorbedBall.myLines) {
                                bigBall.myLines.add(new Line(oldLine.startX, oldLine.startY, bigBall));
                            }
                        }

                        toAdd.add(bigBall);

                    // 普通大球吃到普通道具：直接產生效用
                    } else {
                        b.currentItemType = item.type; 
                        item.applyEffect(b);
                    }

                    itemIter.remove(); 
                    break;
                }
            }
        }
        balls.removeAll(toRemove);
        balls.addAll(toAdd);
    }

    // 檢查兩顆同色小球是否撞在一起觸發融合
    private void checkTinyBallMerge() {
        ArrayList<Ball> toAdd = new ArrayList<>();
        ArrayList<Ball> toRemove = new ArrayList<>();

        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball b1 = balls.get(i);
                Ball b2 = balls.get(j);

                if (b1.isDead || b2.isDead) continue;

                // 若都是同色小球
                if (b1.isTiny && b2.isTiny && b1.color.equals(b2.color)) {
                    long now = System.currentTimeMillis();
                    // 必須超過分裂後的冷卻時間
                    if (now - b1.splitTime > Ball.COOLDOWN && now - b2.splitTime > Ball.COOLDOWN) {
                        double dx = b2.pos.x - b1.pos.x;
                        double dy = b2.pos.y - b1.pos.y;
                        double dist = Math.sqrt(dx * dx + dy * dy);

                        // 距離夠近則合體
                        if (dist < b1.radius + b2.radius) {
                            toRemove.add(b1);
                            toRemove.add(b2);
                            
                            Ball merged = new Ball(b1.pos.x, b1.pos.y, b1.color, false);
                            merged.myLines = new ArrayList<>(b1.myLines); 
                            merged.velocity = b1.velocity; 
                            merged.isFreezed = false;
                            merged.hasEnteredGame = true;
                            // 繼承兩者中較高的生命值
                            merged.lives = Math.max(b1.lives, b2.lives); 
                            
                            toAdd.add(merged);
                        }
                    }
                }
            }
        }
        balls.removeAll(toRemove);
        balls.addAll(toAdd);
    }

    // 計算道具對附近球體的向心吸引力
    private void applyItemMagnetForce() {
        double maxRange = 120.0; 

        for (Item item : items) {
            for (Ball b : balls) {
                if (b.isDead || b.isFreezed) continue;

                double dx = item.pos.x - b.pos.x;
                double dy = item.pos.y - b.pos.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                // 只吸附範圍內的球體
                if (dist > 0 && dist <= maxRange) {
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double force = 0.0;

                    // 越近吸引力越大
                    if (dist <= 40.0) force = 0.50; 
                    else if (dist <= 80.0) force = 0.35; 
                    else force = 0.20; 

                    b.velocity.x += nx * force;
                    b.velocity.y += ny * force;
                }
            }
        }
    }

    // 在競技場內隨機生成一件道具
    private void spawnRandomItem() {
        double angle = random.nextDouble() * Math.PI * 2;
        double r = Math.sqrt(random.nextDouble()) * (arenaRadius - 50);
        double x = arenaCenterX + r * Math.cos(angle);
        double y = arenaCenterY + r * Math.sin(angle);

        // 改寫為統一使用 Item 建構子，並隨機賦予 Type 種類
        int[] types = { 2, 4, 5, 6 };
        int type = types[random.nextInt(types.length)];
        items.add(new Item(x, y, type));
    }

    // 按下快捷鍵時，在空中一鍵隨機生成多顆球
    public void spawnBatchRandomBalls(int count) {
        if (currentState != GameState.SETUP) return;

        this.balls.clear();
        this.pendingBall = null;
        Ball.num = 0; 

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double r = Math.sqrt(random.nextDouble()) * (arenaRadius - 60); 
            double x = arenaCenterX + r * Math.cos(angle);
            double y = arenaCenterY + r * Math.sin(angle);

            Color assignedColor = getAvailableColor();
            Ball b = new Ball(x, y, assignedColor, false);

            double moveAngle = random.nextDouble() * Math.PI * 2;
            double speed = 2.5 + random.nextDouble() * 2.5;
            b.velocity.x = Math.cos(moveAngle) * speed;
            b.velocity.y = Math.sin(moveAngle) * speed;

            // 確保生成時未解鎖攻擊狀態，觸牆後才正式啟動遊戲邏輯
            b.isFreezed = false;
            b.hasEnteredGame = false; 
            b.myLines.clear();        
            b.lives = 1;

            this.balls.add(b);
        }
        System.out.println("成功隨機生成 " + count + " 顆純懸空球。第一次碰牆後將會激活拉線與攻擊能力！");
    }

    // 計算球撞擊圓形競技場牆壁的邏輯與反彈
    private void checkWallCollisions() {
        for (Ball b : balls) {
            if (b.isDead) continue;

            double dx = b.pos.x - arenaCenterX;
            double dy = b.pos.y - arenaCenterY;
            double distanceToCenter = Math.sqrt(dx * dx + dy * dy);

            // 當距離圓心超過半徑時，表示撞牆
            if (distanceToCenter + b.radius >= arenaRadius) {
                double nx = dx / distanceToCenter;
                double ny = dy / distanceToCenter;

                // 強制拉回牆內避免出界
                b.pos.x = arenaCenterX + nx * (arenaRadius - b.radius);
                b.pos.y = arenaCenterY + ny * (arenaRadius - b.radius);

                // 計算入射角與法向量的內積，以鏡射公式改變反彈速度方向
                double dotProduct = b.velocity.x * nx + b.velocity.y * ny;
                b.velocity.x = b.velocity.x - 2 * dotProduct * nx;
                b.velocity.y = b.velocity.y - 2 * dotProduct * ny;

                // 從撞擊點拉出一條新線段
                double wallX = arenaCenterX + nx * arenaRadius;
                double wallY = arenaCenterY + ny * arenaRadius;
                b.myLines.add(new Line(wallX, wallY, b));

                // 標記該球已正式進入遊戲，可受到死亡判定
                b.hasEnteredGame = true;
                SoundManager.playHit();
            }
        }
    }

    // 檢查並處理球與球之間的彈性碰撞
    private void checkBallCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball b1 = balls.get(i);
                Ball b2 = balls.get(j);

                if (b1.isDead || b2.isDead) continue;

                // 若為冷卻中的分裂小球，忽略碰撞使其能重疊融合
                if (b1.isTiny && b2.isTiny && b1.color.equals(b2.color)) {
                    long now = System.currentTimeMillis();
                    if (now - b1.splitTime > Ball.COOLDOWN) continue;
                }

                double dx = b2.pos.x - b1.pos.x;
                double dy = b2.pos.y - b1.pos.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                // 當兩圓心距離小於半徑和，產生重疊
                if (dist < b1.radius + b2.radius) {
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double overlap = (b1.radius + b2.radius) - dist;
                    // 將兩者往相反方向推擠出重疊區塊
                    b1.pos.x -= nx * overlap / 2;
                    b1.pos.y -= ny * overlap / 2;
                    b2.pos.x += nx * overlap / 2;
                    b2.pos.y += ny * overlap / 2;

                    // 交換動量速度
                    double kx = b1.velocity.x - b2.velocity.x;
                    double ky = b1.velocity.y - b2.velocity.y;
                    double p = 2.0 * (nx * kx + ny * ky) / 2.0;

                    b1.velocity.x -= p * nx;
                    b1.velocity.y -= p * ny;
                    b2.velocity.x += p * nx;
                    b2.velocity.y += p * ny;

                    SoundManager.playHit();
                }
            }
        }
    }

    // 檢查是否有球切過其他人的線段 (核心玩法)
    private void checkLineCollisions() {
        for (Ball b : balls) {
            if (b.isDead || b.myLines.isEmpty()) continue;

            for (Ball otherBall : balls) {
                // 不能切斷自己的線，也不能切斷同隊(同色)的線
                if (otherBall.isDead || b == otherBall || b.color.equals(otherBall.color)) {
                    continue;
                }
                Iterator<Line> iterator = otherBall.myLines.iterator();
                while (iterator.hasNext()) {
                    Line line = iterator.next();
                    // 運算球心到線段的最短幾何距離
                    double distToLine = pointToSegmentDistance(b.pos.x, b.pos.y, line.startX, line.startY,
                            otherBall.pos.x, otherBall.pos.y);

                    // 若距離小於半徑即判定切斷
                    if (distToLine <= b.radius) {
                        iterator.remove(); 
                        SoundManager.playKnife();
                    }
                }
            }
        }
    }

    // 檢查死亡條件與遊戲結束判定
    private void checkSurvival() {
        ArrayList<Color> aliveColors = new ArrayList<>();
        boolean someoneEntered = false;

        for (Ball b : balls) {
            if (b.isDead) continue;

            // 已經開始遊戲但身上的線都被切光了
            if (b.hasEnteredGame && b.myLines.isEmpty()) {
                if (b.lives > 1) {
                    b.lives--; // 消耗生命
                    double wallX = arenaCenterX;
                    double wallY = arenaCenterY;
                    // 在中心點復活重拉一條線
                    b.myLines.add(new Line(wallX, wallY, b));
                    b.velocity.x *= -0.8;
                    b.velocity.y *= -0.8;
                    System.out.println("球 " + b.id + " 消耗生命復活！剩餘生命: " + b.lives);

                    if (!aliveColors.contains(b.color)) {
                        aliveColors.add(b.color);
                    }
                } else {
                    // 無命可扣，判定死亡
                    b.isDead = true;
                    b.deathTick = playTimeTicks;
                }
            } else {
                if (!aliveColors.contains(b.color)) {
                    aliveColors.add(b.color);
                }
                if (b.hasEnteredGame) someoneEntered = true;
            }
        }

        // 若場上只有一種顏色存活，遊戲結束
        if (balls.size() >= 2 && someoneEntered && aliveColors.size() <= 1) {
            currentState = GameState.GAME_OVER;
        }
    }

    // 數學公式：點到線段的最短距離計算
    private double pointToSegmentDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1, B = py - y1, C = x2 - x1, D = y2 - y1;
        double dot = A * C + B * D, len_sq = C * C + D * D, param = -1;
        if (len_sq != 0) param = dot / len_sq;

        double xx, yy;
        // 投影點落在線段起點外側
        if (param < 0) { xx = x1; yy = y1; } 
        // 投影點落在線段終點外側
        else if (param > 1) { xx = x2; yy = y2; } 
        // 投影點落在線段上
        else { xx = x1 + param * C; yy = y1 + param * D; }

        double dx = px - xx, dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

// ----------------------------------------------------
// 【整併】將原本的兩個小檔案 Enum 直接合併至 GameManager 下方
// 省去檔案切換的麻煩，並保持 package-private 的存取層級
// ----------------------------------------------------
enum GameMode {
    CLASSIC, ITEM_MODE
}

enum GameState {
    SETUP, PLAYING, GAME_OVER
}