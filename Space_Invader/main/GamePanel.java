package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.Timer;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // 遊戲主迴圈計時器，控制更新與重繪頻率
    Timer timer;
    // 玩家實體
    Shooter shooter;
    // 儲存畫面上所有子彈的集合 (包含玩家與敵方)
    ArrayList<Bullet> bullets;
    // 儲存畫面上所有一般外星人的集合
    ArrayList<Alien> aliens;
    // 特殊幽浮實體 (畫面上方偶爾出現)
    UFO ufo;
    // 控制外星人群體水平移動的方向 (1 為向右，-1 為向左)
    int alienDirection = 1;
    // 玩家獲得的總分
    int score = 0;

    // 遊戲流程狀態旗標
    boolean isStarted = false; // 是否已離開標題畫面進入遊戲
    boolean isGameOver = false;// 玩家血量是否歸零
    boolean isWin = false;     // 是否已通關所有波數

    // 玩家血量系統
    int hp = 100;
    final int MAX_HP = 100; // 血量上限基準

    // 玩家彈藥系統
    int ammo = 35;
    final int MAX_AMMO = 35; // 彈藥自然回復的上限
    int ammoRegenTimer = 0;  // 計算幀數以控制自然回復的頻率

    // 背景裝飾與道具系統
    ArrayList<Star> stars = new ArrayList<>();       // 背景閃爍的星星
    ArrayList<PowerUp> powerUps = new ArrayList<>(); // 畫面上掉落的道具 (此版本中未實際使用，道具改為直接進背包)
    ArrayList<Integer> inventory = new ArrayList<>();// 玩家背包，儲存道具種類的整數代碼 (最大容量3)

    // 玩家身上的增益狀態旗標
    boolean hasShield = false;  // 受到傷害時是否減傷
    boolean isFrozen = false;   // 敵方是否全體停止移動與攻擊
    boolean spreadShot = false; // 開火時是否一次發射三發子彈
    
    // 增益狀態的倒數計時器 (單位為遊戲幀數)
    int shieldTimer = 0; 
    int freezeTimer = 0; 
    int spreadTimer = 0; 

    // 波數與關卡控制
    int wave = 1;              // 當前關卡波數
    int waveDisplayTimer = 0;  // 顯示「WAVE X」過場字樣的剩餘幀數，大於0時遊戲暫停更新
    int edgeCooldown = 0;      // 避免外星人卡在邊界連續觸發反轉向下移動的冷卻機制

    // 鍵盤輸入狀態紀錄
    boolean leftPressed = false;
    boolean rightPressed = false;
    boolean spacePressed = false;
    int shootCooldown = 0;     // 限制連續開火的最短間隔幀數

    public GamePanel() {
        // 設定 JPanel 屬性，背景黑底並允許接收鍵盤輸入事件
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);

        initGame();
        SoundManager.init();

        // 預先生成 200 顆座標與大小隨機的星星作為背景
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 200; i++) {
            int x = rand.nextInt(800);
            int y = rand.nextInt(800);
            int size = rand.nextInt(4) + 1;
            stars.add(new Star(x, y, size));
        }

        // 啟動主迴圈，每 20 毫秒觸發一次 actionPerformed (約 50 FPS)
        timer = new Timer(20, this);
        timer.start();
    }

    public void initGame() {
        // 初始化或重置玩家與各實體陣列
        shooter = new Shooter(375, 650);
        bullets = new ArrayList<>();
        aliens = new ArrayList<>();
        powerUps = new ArrayList<>();
        inventory.clear();
        ufo = new UFO();

        // 數值與狀態歸零
        score = 0;
        wave = 1;
        alienDirection = 1;
        isGameOver = false;
        isWin = false;
        ufo.active = false;

        hp = MAX_HP;
        ammo = MAX_AMMO;
        ammoRegenTimer = 0;

        hasShield = false;
        isFrozen = false;
        spreadShot = false;
        shieldTimer = 0;
        freezeTimer = 0;
        spreadTimer = 0;

        leftPressed = false;
        rightPressed = false;
        spacePressed = false;
        shootCooldown = 0;

        // 依據初始波數生成敵人
        spawnAliens();
    }

    public void spawnAliens() {
        aliens.clear();
        // 隨著 wave 增加，外星人的列數 (rows) 會增加
        int rows = 3 + (wave / 2);
        int cols = 6;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double rand = Math.random();
                int type = 0;
                // 將 currentWave 限制在最大 5，避免超出機率表定義
                int currentWave = Math.min(wave, 5);

                // 根據當前波數，設定 0~4 號外星人出現的機率分佈區間
                if (currentWave == 1) {
                    if (rand < 0.80) type = 0;
                    else if (rand < 0.95) type = 1;
                    else type = 3;
                } else if (currentWave == 2) {
                    if (rand < 0.60) type = 0;
                    else if (rand < 0.80) type = 1;
                    else type = 3;
                } else if (currentWave == 3) {
                    if (rand < 0.60) type = 0;
                    else if (rand < 0.75) type = 1;
                    else if (rand < 0.90) type = 2;
                    else type = 3;
                } else if (currentWave == 4) {
                    if (rand < 0.50) type = 0;
                    else if (rand < 0.60) type = 1;
                    else if (rand < 0.75) type = 2;
                    else if (rand < 0.85) type = 3;
                    else type = 4;
                } else {
                    if (rand < 0.30) type = 0;
                    else if (rand < 0.45) type = 1;
                    else if (rand < 0.65) type = 2;
                    else if (rand < 0.80) type = 3;
                    else type = 4;
                }

                // 以決定的 type 建立外星人，並利用行列索引計算其初始座標，使其從畫面外上方依序排好
                Alien a = new Alien(type);
                a.x = 100 + c * 80;
                a.y = -100 + r * 50;
                a.isAlive = true;
                aliens.add(a);
            }
        }
    }

    private void checkGameOver() {
        // 檢查玩家血量是否小於等於 0，若是則觸發死亡狀態並切換音效
        if (hp <= 0) {
            hp = 0;
            isGameOver = true;
            isWin = false;
            SoundManager.stopBGM();
            SoundManager.playGameOver();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 優先繪製背景星星
        for (Star s : stars) {
            s.draw(g);
        }

        if (!isStarted) {
            // --- 標題選單畫面繪製 ---
            // 繪製帶有錯位陰影效果的大標題
            g.setFont(new Font("Courier New", Font.BOLD, 70));
            g.setColor(Color.MAGENTA);
            g.drawString("SPACE INVADERS", 104, 204);
            g.setColor(Color.CYAN);
            g.drawString("SPACE INVADERS", 100, 200);

            // 利用系統時間除以 600 毫秒的奇偶數來製造文字閃爍效果
            //if ((System.currentTimeMillis() / 600) % 2 == 0) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 25));
                g.drawString(">>> PRESS ENTER TO START <<<", 210, 350);
            //}

            // 繪製操作說明外框與內容
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(210, 410, 380, 170); 

            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("MOVE  :  LEFT / RIGHT ARROW", 250, 450);
            g.drawString("FIRE   :  SPACEBAR", 250, 490);
            g.drawString("ITEM   :  Z KEY", 250, 530);
            g.drawString("MENU  :  R KEY", 250, 570); 
        } else {
            // --- 遊戲進行中或結束畫面繪製 ---
            if (isGameOver) {
                // 根據 isWin 狀態繪製勝利或失敗字樣
                g.setColor(Color.ORANGE);
                g.setFont(new Font("Arial", Font.BOLD, 50));
                String msg = isWin ? "YOU WIN!" : "GAME OVER";
                g.drawString(msg, 250, 250);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.drawString("Press ENTER to Restart", 280, 300);
            } else {
                // 正常遊戲中，呼叫各實體自身的繪製方法
                shooter.draw(g, hasShield);
                for (Bullet b : bullets) b.draw(g);
                for (Alien a : aliens) a.draw(g);
                for (PowerUp p : powerUps) p.draw(g);
                ufo.draw(g);

                // 若正處於波數過場階段，繪製閃爍的大型 WAVE 字樣
                if (waveDisplayTimer > 0) {
                    if ((System.currentTimeMillis() / 200) % 2 == 0) {
                        String waveText = "WAVE " + wave;
                        Font waveFont = new Font("Courier New", Font.BOLD, 100);
                        g.setFont(waveFont);
                        g.setColor(Color.WHITE);
                        g.drawString(waveText, 205, 355);
                        g.setColor(Color.ORANGE);
                        g.drawString(waveText, 200, 350);
                    }
                    waveDisplayTimer--;
                }
            }

            // --- UI：右下角道具背包欄繪製 ---
            int slotWidth = 40, gap = 10;
            int startX = getWidth() - 170, startY = getHeight() - 70;

            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.WHITE);
            g.drawString("ITEMS", startX, startY - 10);

            // 繪製 3 個固定位置的空格底框
            for (int i = 0; i < 3; i++) {
                g.setColor(Color.GRAY);
                g.drawRect(startX + (i * (slotWidth + gap)), startY, slotWidth, slotWidth);
            }

            // 根據背包陣列中的 type 值，將對應的縮圖繪製至空格內
            for (int i = 0; i < inventory.size(); i++) {
                int type = inventory.get(i);
                int itemX = startX + (i * (slotWidth + gap)) + 5;
                int itemY = startY + 5;

                if (type == 0 && PowerUp.shieldImg != null) {
                    g.drawImage(PowerUp.shieldImg, itemX, itemY, 30, 30, null);
                } else if (type == 1 && PowerUp.iceImg != null) {
                    g.drawImage(PowerUp.iceImg, itemX, itemY, 30, 30, null);
                } else if (type == 2) {
                    // 手繪版散彈圖示 (三條線代表散射軌跡，三個方塊代表子彈)
                    g.setColor(Color.YELLOW);
                    g.drawLine(itemX + 15, itemY + 26, itemX + 5, itemY + 8);
                    g.drawLine(itemX + 15, itemY + 26, itemX + 15, itemY + 4);
                    g.drawLine(itemX + 15, itemY + 26, itemX + 25, itemY + 8);
                    g.fillRect(itemX + 3, itemY + 5, 4, 4);
                    g.fillRect(itemX + 13, itemY + 1, 4, 4);
                    g.fillRect(itemX + 23, itemY + 5, 4, 4);
                } else if (type == 3) {
                    // 手繪版彈匣圖示 (矩形+多邊形組成的子彈)
                    g.setColor(Color.GREEN);
                    g.fillRect(itemX + 12, itemY + 11, 6, 14);
                    int[] bx = { itemX + 12, itemX + 15, itemX + 18 };
                    int[] by = { itemY + 11, itemY + 4, itemY + 11 };
                    g.fillPolygon(bx, by, 3);
                }
            }

            // --- UI：左上角血量與彈藥狀態條繪製 ---
            
            // 愛心圖示繪製 (由兩個圓形與一個倒三角形組成)
            int heartX = 15, heartY = 17;
            g.setColor(Color.RED);
            g.fillOval(heartX, heartY, 8, 8);
            g.fillOval(heartX + 6, heartY, 8, 8);
            int[] hX = { heartX, heartX + 7, heartX + 14 };
            int[] hY = { heartY + 4, heartY + 13, heartY + 4 };
            g.fillPolygon(hX, hY, 3);

            // 繪製血條外框與背景底色
            g.setColor(Color.GRAY);
            g.drawRect(35, 15, 150, 15);
            g.setColor(new Color(50, 50, 50));
            g.fillRect(36, 16, 148, 13);

            // 按比例計算並繪製當前血量進度條
            if (hp > 0) {
                g.setColor(Color.RED);
                int barWidth = (int) (148 * ((double) hp / MAX_HP));
                g.fillRect(36, 16, barWidth, 13);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(hp + " / " + MAX_HP, 195, 28);

            // 子彈圖示繪製 (矩形+三角形組成)
            int bulletX = 17, bulletY = 40;
            g.setColor(Color.YELLOW);
            g.fillRect(bulletX, bulletY, 4, 8);
            int[] bX = { bulletX, bulletX + 2, bulletX + 4 };
            int[] bY = { bulletY, bulletY - 3, bulletY };
            g.fillPolygon(bX, bY, 3);

            // 繪製彈藥條外框與背景底色
            g.setColor(Color.GRAY);
            g.drawRect(35, 35, 150, 15);
            g.setColor(new Color(50, 50, 50));
            g.fillRect(36, 36, 148, 13);

            // 按比例計算當前彈藥進度條
            if (ammo > 0) {
                g.setColor(Color.YELLOW);
                int ammoBarWidth = (int) (148 * ((double) ammo / MAX_AMMO));
                // 因彈藥道具可使 ammo 突破 MAX_AMMO，強制限制長度避免畫出框外
                if (ammoBarWidth > 148) ammoBarWidth = 148;
                g.fillRect(36, 36, ammoBarWidth, 13);
            }
            g.setColor(Color.WHITE);
            g.drawString(ammo + " / " + MAX_AMMO, 195, 48);

            // 分數文字繪製
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 15, 75);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 背景星星獨立於遊戲狀態，每一幀都會更新閃爍與下落位置
        for (Star s : stars) {
            s.twinkle();
            s.move(); 
        }

        // 若處於波數過場等待期，清空場上殘留子彈並強制重新繪製，不進行實體更新
        if (waveDisplayTimer > 0) {
            bullets.clear();
            repaint();
            return;
        }

        if (!isStarted || isGameOver) return;

        // 依照鍵盤布林狀態，直接修改玩家座標
        if (leftPressed) shooter.move(-8);
        if (rightPressed) shooter.move(8);

        // 彈藥每 25 幀 (約 0.5 秒) 自動回復 1 發，直到達 MAX_AMMO
        ammoRegenTimer++;
        if (ammoRegenTimer >= 25) {
            if (ammo < MAX_AMMO) ammo++;
            ammoRegenTimer = 0;
        }

        // 遞減玩家的射擊冷卻時間
        if (shootCooldown > 0) shootCooldown--;

        // 判定玩家開火條件：按住空白鍵、無冷卻且有彈藥
        if (spacePressed && shootCooldown == 0 && ammo > 0) {
            if (spreadShot && ammo >= 3) {
                // 散彈模式：一次扣除 3 發彈藥，並新增三發具有不同 X 軸速度的子彈
                ammo -= 3;
                bullets.add(new Bullet(shooter.x + 22, shooter.y, -4, -35));
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -35));
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 4, -35));
                shootCooldown = 6;
                SoundManager.playShoot();
            } else if (!spreadShot) {
                // 一般模式：扣除 1 發，新增一發直向子彈
                ammo--;
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -35));
                shootCooldown = 6;
                SoundManager.playShoot();
            }
        }

        // 遞減外星人群體碰壁反轉的冷卻時間
        if (edgeCooldown > 0) edgeCooldown--;

        // 幽浮隨機生成邏輯：若不在場上則每一幀有 0.5% 機率產生，若在場上則呼叫其更新邏輯
        if (!ufo.active) {
            if (Math.random() < 0.005) ufo.spawn();
        } else {
            ufo.update();
        }

        // 從陣列尾部開始疊代更新子彈，避免在移除元素時發生索引錯亂
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            boolean hitSomething = false;
            // 建立子彈的 AABB 碰撞矩形 (位置 x,y 寬 5 高 10)
            Rectangle bulletRect = new Rectangle(b.x, b.y, 5, 10);

            if (b.isEnemy) {
                // 敵方子彈：檢查是否與玩家的 AABB 矩形重疊
                Rectangle playerRect = new Rectangle(shooter.x, shooter.y, 50, 50);
                if (bulletRect.intersects(playerRect)) {
                    int damage = 10;
                    // 若有護盾則傷害乘以 0.7 減輕
                    if (hasShield) damage = (int) (damage * 0.7);
                    hp -= damage;
                    checkGameOver();
                    hitSomething = true;
                }
            } else {
                // 玩家子彈：遍歷所有存活的外星人檢查碰撞
                for (int j = aliens.size() - 1; j >= 0; j--) {
                    Alien a = aliens.get(j);
                    if (a.isAlive) {
                        Rectangle alienRect = new Rectangle(a.x, a.y, a.width, a.height);
                        if (bulletRect.intersects(alienRect)) {
                            a.hp--; // 扣除外星人血量
                            if (a.hp <= 0) {
                                a.isAlive = false;
                                // 若擊殺類型 3 獲得 30 分，其餘獲得 10 分
                                score += (a.type == 3) ? 30 : 10;
                                SoundManager.playExplosion();
                            }
                            hitSomething = true;
                            break;
                        }
                    }
                }

                // 若未擊中一般敵人且幽浮存在，檢查是否擊中幽浮
                if (!hitSomething && ufo.active && bulletRect.intersects(ufo.getBounds())) {
                    score += 500;
                    ufo.active = false;
                    hitSomething = true;
                    SoundManager.playExplosion();

                    // 擊中幽浮隨機給予 0~3 號道具，若背包未滿則加入背包並播放音效
                    int rType = new java.util.Random().nextInt(4); 
                    if (inventory.size() < 3) {
                        inventory.add(rType);
                        SoundManager.playPowerUp();
                    }
                }
            }

            // 若擊中目標或飛出畫面上下邊界，將此子彈從 List 中移除
            if (hitSomething || b.y < -50 || b.y > 850) {
                bullets.remove(i);
            }
        }

        boolean hitEdge = false;

        // 處理外星人移動與碰撞玩家
        for (Alien a : aliens) {
            if (a.isAlive && waveDisplayTimer == 0) {

                // 檢查冰凍狀態，若未冰凍才允許移動與射擊
                if (!isFrozen) {
                    // 若任意存活的外星人 X 座標碰到左右極限 (0 或 750)，且無冷卻，標記全體需要反轉
                    if (edgeCooldown == 0 && (a.x <= 0 || a.x >= 750)) {
                        hitEdge = true;
                    }
                    // 傳入方向、波數與玩家目前 X 座標進行移動運算
                    a.move(alienDirection, wave, shooter.x);

                    // 若外星人為類型 4 (紫色)，累積其專屬射擊計時器，達標後發射敵方子彈
                    if (a.type == 4) {
                        a.shootTimer++;
                        if (a.shootTimer >= 50) {
                            bullets.add(new Bullet(a.x + 18, a.y + a.height, 0, 10, true, Color.MAGENTA));
                            a.shootTimer = 0;
                        }
                    }
                }

                // 判定外星人 Y 軸下緣是否碰到底部防線 (Y=650)
                if (a.y + a.height >= 650) {
                    int damage = (a.type == 2 || a.type == 4) ? 10 : 20;
                    if (hasShield) damage = (int) (damage * 0.7);
                    hp -= damage;
                    checkGameOver();
                    a.isAlive = false; // 外星人觸底即判定死亡
                    SoundManager.playExplosion();
                }
                // 判定外星人實體是否直接重疊玩家實體
                else {
                    Rectangle alienRect = new Rectangle(a.x, a.y, a.width, a.height);
                    Rectangle playerRect = new Rectangle(shooter.x, shooter.y, 50, 50);
                    if (alienRect.intersects(playerRect)) {
                        int damage = 30;
                        if (hasShield) damage = (int) (damage * 0.7);
                        hp -= damage;
                        checkGameOver();
                        a.isAlive = false;
                        SoundManager.playExplosion();
                    }
                }
            }
        }

        // 若有外星人碰壁且未在冰凍狀態下，執行群體反向與向下逼近邏輯
        if (!isFrozen && hitEdge) {
            alienDirection *= -1; // 改變水平移動方向
            edgeCooldown = 10;    // 給予冷卻幀數避免連續觸發卡牆
            for (Alien a : aliens) {
                if (a.isAlive) a.y += 20; // 存活個體全體 Y 軸向下增加 20 像素
            }
        }

        // 遞減各增益狀態的計時器，歸零時解除狀態
        if (shieldTimer > 0) {
            shieldTimer--;
            if (shieldTimer == 0) hasShield = false;
        }
        if (freezeTimer > 0) {
            freezeTimer--;
            if (freezeTimer == 0) isFrozen = false;
        }
        if (spreadTimer > 0) {
            spreadTimer--;
            if (spreadTimer == 0) spreadShot = false;
        }

        // 從陣列尾端檢查死亡且爆炸特效已經播放完畢的外星人，將其徹底從記憶體 List 移除
        for (int j = aliens.size() - 1; j >= 0; j--) {
            Alien a = aliens.get(j);
            if (!a.isAlive && a.explosionTimer >= a.MAX_EXPLOSION_TIME) {
                aliens.remove(j);
            }
        }

        // 當 aliens List 為空時，代表波數清空
        if (aliens.isEmpty()) {
            // 若清空的是第 5 波，判定通關勝利，鎖死狀態並播放音效
            if (wave >= 5) {
                isGameOver = true;
                isWin = true;
                SoundManager.stopBGM();
                SoundManager.playWin(); 
                repaint();
                return;
            }
            // 未滿 5 波則波數+1，設定過場計時，補滿彈藥並重新生成新一波敵人
            wave++;
            waveDisplayTimer = 100;
            spawnAliens();
            ammo = MAX_AMMO;
        }

        // 通知 JPanel 呼叫 paintComponent 進行畫面更新
        repaint();
    }

    public void activatePowerUp(int type) {
        // 依據傳入的道具種類整數，賦予對應增益並重置計時器 (50幀約等於1秒)
        if (type == 0) { 
            hasShield = true;
            shieldTimer = 250; 
        } else if (type == 1) { 
            isFrozen = true;
            freezeTimer = 75; 
        } else if (type == 2) { 
            spreadShot = true;
            spreadTimer = 150; 
        } else if (type == 3) { 
            // 彈夾道具直接增加 25 發，不設冷卻時間，允許突破 MAX_AMMO
            ammo += 25;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // 未開始時只處理 ENTER 鍵
        if (!isStarted) {
            if (key == KeyEvent.VK_ENTER) {
                initGame();
                isStarted = true;
                SoundManager.playBGM();
                repaint();
            }
            return;
        }

        // 遊戲結束時只處理 ENTER 鍵重啟
        if (isGameOver) {
            if (key == KeyEvent.VK_ENTER) {
                initGame();
                SoundManager.playBGM();
                repaint();
            }
        } else {
            // 遊戲進行中設定布林狀態以支援多鍵連按
            if (key == KeyEvent.VK_LEFT) leftPressed = true;
            if (key == KeyEvent.VK_RIGHT) rightPressed = true;
            if (key == KeyEvent.VK_SPACE) spacePressed = true;

            // Z 鍵：若背包不為空，取出索引 0 的第一項道具 (FIFO) 並執行 activatePowerUp
            if (key == KeyEvent.VK_Z) {
                if (!inventory.isEmpty()) {
                    int itemToUse = inventory.remove(0);
                    activatePowerUp(itemToUse);
                }
            }
            // R 鍵：強制重置回標題畫面
            if (key == KeyEvent.VK_R) {
                initGame();
                isStarted = false;
                SoundManager.stopBGM();
                repaint();
                return;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // 放開按鍵時解除布林狀態停止對應動作
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_SPACE) spacePressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // 因實作 KeyListener 而必須存在，未提供實作邏輯
    }
}