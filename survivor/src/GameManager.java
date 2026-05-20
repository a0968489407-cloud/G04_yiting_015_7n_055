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
        Color[] allColors = { Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.PINK, Color.ORANGE };
        for (Color c : allColors) {
            boolean isUsed = false;
            for (Ball b : balls) {
                if (b.color.equals(c)) {
                    isUsed = true;
                    break;
                }
            }
            if (!isUsed)
                return c;
        }
        return Color.WHITE;
    }

    public boolean isInsideArena(double x, double y) {
        double dx = x - arenaCenterX;
        double dy = y - arenaCenterY;
        return Math.sqrt(dx * dx + dy * dy) <= arenaRadius;
    }

    public void update() {
        if (currentState != GameState.PLAYING)
            return;
        playTimeTicks++;

        // --- 新增：道具生成邏輯 ---
        if (currentMode == GameMode.ITEM_MODE) {
            itemSpawnTimer++;

            // 遊戲剛開始時（此時 items 為空），3 秒 (180 幀) 就生成第一個道具
            if (items.isEmpty() && itemSpawnTimer >= 180) {
                spawnRandomItem();
                itemSpawnTimer = 0;
            }
            // 之後維持每 5 秒 (300 幀) 嘗試生成一個新道具
            else if (!items.isEmpty() && itemSpawnTimer > 300) {
                spawnRandomItem();
                itemSpawnTimer = 0;
            }
        }

        // --- 新增處：計算道具磁力吸引效果 ---
        if (currentMode == GameMode.ITEM_MODE && items != null && balls != null) {
            applyItemMagnetForce();
        }

        for (Ball b : balls)
            b.update();

        checkWallCollisions();
        checkBallCollisions();
        checkLineCollisions();

        checkSurvival();
    }

    // --- 新增方法：計算分層磁力物理邏輯 ---
    private void applyItemMagnetForce() {
        double maxRange = 120.0; // 3顆大球直徑 (3 * 40 = 120 像素)

        for (Item item : items) {
            for (Ball b : balls) {
                if (b.isDead || b.isFreezed)
                    continue;

                // 計算球與道具之間的距離向量
                double dx = item.pos.x - b.pos.x;
                double dy = item.pos.y - b.pos.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                // 只影響範圍 120 像素內的球
                if (dist > 0 && dist <= maxRange) {
                    // 計算吸引力的單位方向向量
                    double nx = dx / dist;
                    double ny = dy / dist;

                    double force = 0.0;

                    // 分層吸引力判定
                    if (dist <= 40.0) {
                        force = 0.25; // 1單位距離：吸引力最大
                    } else if (dist <= 80.0) {
                        force = 0.15; // 2單位距離：吸引力其次
                    } else {
                        force = 0.05; // 3單位距離：吸引力最後
                    }

                    // 將吸引力直接加到球的速度向量上，使其往道具靠攏
                    b.velocity.x += nx * force;
                    b.velocity.y += ny * force;
                }
            }
        }
    }

    // 新增：在圓形競技場內隨機生成道具
    private void spawnRandomItem() {
        double angle = random.nextDouble() * Math.PI * 2;
        // 讓道具生成在距離邊界稍遠一點的地方，避免剛生成就在牆壁外
        double r = Math.sqrt(random.nextDouble()) * (arenaRadius - 50);
        double x = arenaCenterX + r * Math.cos(angle);
        double y = arenaCenterY + r * Math.sin(angle);

        // 隨機決定道具類型 (2:加速, 4:減速, 5:分裂, 6:生命)
        int[] types = { 2, 4, 5, 6 };
        int type = types[random.nextInt(types.length)];

        // 根據類型建立對應物件
        if (type == 2)
            items.add(new ItemSpeedUp(x, y));
        else if (type == 4)
            items.add(new ItemSpeedDown(x, y));
        else if (type == 6)
            items.add(new ItemLife(x, y)); // 生命道具
        else
            items.add(new Item(x, y, 5)); // 分裂道具
    }

    private void checkWallCollisions() {
        for (Ball b : balls) {
            if (b.isDead)
                continue;

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

                // --- 新增：播放撞牆音效 ---
                SoundManager.playHit();
            }
        }
    }

    private void checkBallCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball b1 = balls.get(i);
                Ball b2 = balls.get(j);

                if (b1.isDead || b2.isDead)
                    continue;

                // 新增：如果兩顆球正處於可融合狀態，不觸發物理碰撞
                if (b1.isTiny && b2.isTiny && b1.color.equals(b2.color)) {
                    long now = System.currentTimeMillis();
                    if (now - b1.splitTime > Ball.COOLDOWN)
                        continue;
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

                    // --- 新增：播放互撞音效 ---
                    SoundManager.playHit();
                }
            }
        }
    }

    private void checkLineCollisions() {
        for (Ball b : balls) {
            if (b.isDead || b.myLines.isEmpty())
                continue;

            for (Ball otherBall : balls) {
                // 原本條件：if (otherBall.isDead || b == otherBall) continue;

                // 修正條件：如果是死球、是自己、或是「同顏色的球」，則不消對方的線
                if (otherBall.isDead || b == otherBall || b.color.equals(otherBall.color)) {
                    continue;
                }
                Iterator<Line> iterator = otherBall.myLines.iterator();
                while (iterator.hasNext()) {
                    Line line = iterator.next();
                    double distToLine = pointToSegmentDistance(b.pos.x, b.pos.y, line.startX, line.startY,
                            otherBall.pos.x, otherBall.pos.y);

                    if (distToLine <= b.radius) {
                        iterator.remove(); // 這裡將別人的線切斷了

                        // ====================================================
                        // 【新增這行】每次線斷掉的時候，播放「唰」的飛刀割裂音效
                        SoundManager.playKnife();
                        // ====================================================
                    }
                }
            }
        }
    }

    private void checkSurvival() {
        // 改用一個清單來記錄目前還活著的「顏色」
        ArrayList<Color> aliveColors = new ArrayList<>();
        boolean someoneEntered = false;

        for (Ball b : balls) {
            if (b.isDead)
                continue;

            // 沒線時的生命消耗與復活邏輯
            if (b.hasEnteredGame && b.myLines.isEmpty()) {
                if (b.lives > 1) {
                    b.lives--;
                    double wallX = arenaCenterX;
                    double wallY = arenaCenterY;
                    b.myLines.add(new Line(wallX, wallY, b));
                    b.velocity.x *= -0.8;
                    b.velocity.y *= -0.8;
                    System.out.println("球 " + b.id + " 消耗生命復活！剩餘生命: " + b.lives);

                    // 復活成功，這種類型依然活著，記錄顏色
                    if (!aliveColors.contains(b.color)) {
                        aliveColors.add(b.color);
                    }
                } else {
                    b.isDead = true;
                    b.deathTick = playTimeTicks;
                }
            } else {
                // 球體正常存活，記錄顏色（若清單中沒有就加進去）
                if (!aliveColors.contains(b.color)) {
                    aliveColors.add(b.color);
                }
                if (b.hasEnteredGame)
                    someoneEntered = true;
            }
        }

        // --- 關鍵修正：當總球數大於等於 2，且有人碰過牆壁，但活著的「顏色種類」小於等於 1 時結束 ---
        if (balls.size() >= 2 && someoneEntered && aliveColors.size() <= 1) {
            currentState = GameState.GAME_OVER;
        }

    }

    private double pointToSegmentDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1, B = py - y1, C = x2 - x1, D = y2 - y1;
        double dot = A * C + B * D, len_sq = C * C + D * D, param = -1;
        if (len_sq != 0)
            param = dot / len_sq;

        double xx, yy;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx, dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}