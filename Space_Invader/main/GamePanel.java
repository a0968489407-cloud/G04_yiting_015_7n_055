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
    Timer timer;
    Shooter shooter;
    ArrayList<Bullet> bullets;
    ArrayList<Alien> aliens;
    UFO ufo;
    int alienDirection = 1;
    int score = 0;

    // 遊戲狀態變數
    boolean isStarted = false;
    boolean isGameOver = false;
    boolean isWin = false;
    boolean isSlowed = false;
    int slowCounter = 0;

    // === 新增：血量變數 (以 100 點為準) ===
    int hp = 100;
    final int MAX_HP = 100;

    // === 新增：彈藥變數 ===
    int ammo = 35;
    final int MAX_AMMO = 35;
    int ammoRegenTimer = 0; // 用來計算0.5秒回復的計時器

    // 星星
    ArrayList<Star> stars = new ArrayList<>();

    // 道具列表
    ArrayList<PowerUp> powerUps = new ArrayList<>();

    // 道具欄
    ArrayList<Integer> inventory = new ArrayList<>();

    // 道具狀態
    boolean hasShield = false;
    boolean spreadShot = false;
    int powerUpTimer = 0;
    int slowTimer = 0;

    // 目前是第幾波
    int wave = 1;
    int waveDisplayTimer = 0;
    int edgeCooldown = 0;

    // 按鍵狀態紀錄與射擊冷卻
    boolean leftPressed = false;
    boolean rightPressed = false;
    boolean spacePressed = false;
    int shootCooldown = 0;

    public GamePanel() {
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);

        initGame();
        SoundManager.init();

        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 200; i++) {
            int x = rand.nextInt(800);
            int y = rand.nextInt(800);
            int size = rand.nextInt(4) + 1;
            stars.add(new Star(x, y, size));
        }

        timer = new Timer(20, this);
        timer.start();
    }

    public void initGame() {
        shooter = new Shooter(375, 650);
        bullets = new ArrayList<>();
        aliens = new ArrayList<>();
        powerUps = new ArrayList<>();
        ufo = new UFO();

        spawnAliens();

        score = 0;
        wave = 1;
        alienDirection = 1;
        isGameOver = false;
        isWin = false;
        hasShield = false;
        spreadShot = false;
        powerUpTimer = 0;
        slowTimer = 0;
        ufo.active = false;
        isSlowed = false;
        slowCounter = 0;

        // === 新增：重新開始時重設血量 ===
        hp = MAX_HP;

        ammo = MAX_AMMO;
        ammoRegenTimer = 0;

        leftPressed = false;
        rightPressed = false;
        spacePressed = false;
        shootCooldown = 0;
    }

    public void spawnAliens() {
        aliens.clear();
        int rows = 3 + (wave / 2);
        int cols = 6;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double rand = Math.random(); 
                int type = 0;
                int currentWave = Math.min(wave, 5); // 超過第5關就以第5關的機率生成

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
                } else { // 第5關
                    if (rand < 0.30) type = 0;
                    else if (rand < 0.45) type = 1;
                    else if (rand < 0.65) type = 2;
                    else if (rand < 0.80) type = 3;
                    else type = 4;
                }

                Alien a = new Alien(type);
                a.x = 100 + c * 80;
                a.y = -100 + r * 50;
                a.isAlive = true;
                aliens.add(a);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Star s : stars) {
            s.twinkle();
            s.draw(g);
        }

        if (!isStarted) {
            g.setFont(new Font("Courier New", Font.BOLD, 70));
            g.setColor(Color.MAGENTA);
            g.drawString("SPACE INVADERS", 104, 204);
            g.setColor(Color.CYAN);
            g.drawString("SPACE INVADERS", 100, 200);

            if ((System.currentTimeMillis() / 600) % 2 == 0) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 25));
                g.drawString(">>> PRESS ENTER TO START <<<", 210, 350);
            }

            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(180, 410, 440, 80);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("MOVE: LEFT/RIGHT ARROW", 280, 440);
            g.drawString("FIRE: SPACEBAR", 325, 470);
        } else {
            if (isGameOver) {
                g.setColor(Color.ORANGE);
                g.setFont(new Font("Arial", Font.BOLD, 50));
                String msg = isWin ? "YOU WIN!" : "GAME OVER";
                g.drawString(msg, 250, 250);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.drawString("Press ENTER to Restart", 280, 300);
                g.drawString("Press R to Main Menu", 295, 330);
            } else {
                shooter.draw(g, hasShield);
                for (Bullet b : bullets) {
                    b.draw(g);
                }
                for (Alien a : aliens) {
                    a.draw(g);
                }
                for (PowerUp p : powerUps) {
                    p.draw(g);
                }
                ufo.draw(g);

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

            // 橫向道具欄 (固定在右下角)
            int slotWidth = 40;
            int gap = 10;
            int startX = getWidth() - 170;
            int startY = getHeight() - 70;

            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.WHITE);
            g.drawString("ITEMS", startX, startY - 10);

            for (int i = 0; i < 3; i++) {
                g.setColor(Color.BLACK);
                g.fillRect(startX + (i * (slotWidth + gap)), startY, slotWidth, slotWidth);
                g.setColor(Color.GRAY);
                g.drawRect(startX + (i * (slotWidth + gap)), startY, slotWidth, slotWidth);
            }

            for (int i = 0; i < inventory.size(); i++) {
                int type = inventory.get(i);
                if (type == 0)
                    g.setColor(Color.BLUE);
                else if (type == 1)
                    g.setColor(Color.WHITE);
                else
                    g.setColor(Color.YELLOW);
                g.fillRect(startX + (i * (slotWidth + gap)) + 5, startY + 5, 30, 30);
            }

            // === 修改：左上角 UI 區域 (分數與血量條) ===
            // 1. 繪製前面的小愛心
            int heartX = 15;
            int heartY = 17;
            g.setColor(Color.RED);
            g.fillOval(heartX, heartY, 8, 8);          
            g.fillOval(heartX + 6, heartY, 8, 8);      
            int[] hX = {heartX, heartX + 7, heartX + 14};
            int[] hY = {heartY + 4, heartY + 13, heartY + 4};
            g.fillPolygon(hX, hY, 3);                  

            // 2. 繪製紅色血量條
            g.setColor(Color.GRAY);
            g.drawRect(35, 15, 150, 15); 
            g.setColor(new Color(50, 50, 50));
            g.fillRect(36, 16, 148, 13); 

            if (hp > 0) {
                g.setColor(Color.RED); 
                int barWidth = (int) (148 * ((double) hp / MAX_HP));
                g.fillRect(36, 16, barWidth, 13);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(hp + " / " + MAX_HP, 195, 28);

            // --- 3. 新增：繪製前面的小子彈圖示 ---
            int bulletX = 17;
            int bulletY = 40;
            g.setColor(Color.YELLOW);
            g.fillRect(bulletX, bulletY, 4, 8);        // 子彈主體
            int[] bX = {bulletX, bulletX + 2, bulletX + 4};
            int[] bY = {bulletY, bulletY - 3, bulletY};
            g.fillPolygon(bX, bY, 3);                  // 子彈尖端

            // --- 4. 新增：繪製黃色彈藥條 (在血條下方) ---
            g.setColor(Color.GRAY);
            g.drawRect(35, 35, 150, 15); // 外框向下平移 20 像素
            g.setColor(new Color(50, 50, 50));
            g.fillRect(36, 36, 148, 13); // 暗灰色背景

            if (ammo > 0) {
                g.setColor(Color.YELLOW); 
                int ammoBarWidth = (int) (148 * ((double) ammo / MAX_AMMO));
                g.fillRect(36, 36, ammoBarWidth, 13);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(ammo + " / " + MAX_AMMO, 195, 48);

            // 5. 繪製分數 (再往下平移到彈藥條下方)
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 15, 75);
        }
    }

    // === 新增：統一檢查血量是否歸零與死亡處理 ===
    private void checkGameOver() {
        if (hp <= 0) {
            hp = 0;
            isGameOver = true;
            isWin = false;
            SoundManager.stopBGM();     
            SoundManager.playGameOver(); 
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // --- 1. 背景星星動畫 ---
        for (Star s : stars) {
            s.twinkle();
        }

        // --- 2. 過場狀態檢測 ---
        // 如果正在顯示過場文字，清空畫面上的子彈，並跳過物理運算
        if (waveDisplayTimer > 0) {
            bullets.clear(); 
            repaint();
            return;
        }

        // 如果遊戲還沒開始或已經結束，就不再更新任何物件的移動邏輯
        if (!isStarted || isGameOver) {
            return;
        }

        // --- 3. 玩家移動 ---
        if (leftPressed) shooter.move(-8);
        if (rightPressed) shooter.move(8);

        // --- 4. 彈藥回復邏輯 (每 0.5 秒回復 1 發) ---
        // 遊戲迴圈是 20ms 一幀，25 幀剛好是 500ms (0.5秒)
        ammoRegenTimer++;
        if (ammoRegenTimer >= 25) {
            if (ammo < MAX_AMMO) {
                ammo++;
            }
            ammoRegenTimer = 0; // 重置計時器
        }

        // --- 5. 玩家射擊邏輯 ---
        if (shootCooldown > 0) {
            shootCooldown--;
        }

        // 需要有彈藥 (ammo > 0) 且冷卻完畢才能發射
        if (spacePressed && shootCooldown == 0 && ammo > 0) {
            ammo--; // 扣除 1 發彈藥

            // 子彈速度加快，將垂直速度設為 -35
            if (spreadShot) {
                bullets.add(new Bullet(shooter.x + 22, shooter.y, -4, -35));
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -35));
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 4, -35));
            } else {
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -35));
            }
            
            // 發射速度減慢，將冷卻時間設為 12 幀
            shootCooldown = 5; 
            SoundManager.playShoot();
        }

        if (edgeCooldown > 0) {
            edgeCooldown--;
        }

        // --- 6. UFO 邏輯 ---
        if (!ufo.active) {
            if (Math.random() < 0.001) {
                ufo.spawn();
            }
        } else {
            ufo.update();
        }

        // --- 7. 所有子彈移動與碰撞 (包含敵我) ---
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            boolean hitSomething = false; 
            Rectangle bulletRect = new Rectangle(b.x, b.y, 5, 10);

            if (b.isEnemy) {
                // 【敵方子彈打玩家】
                Rectangle playerRect = new Rectangle(shooter.x, shooter.y, 50, 50);
                if (bulletRect.intersects(playerRect)) {
                    if (!hasShield) {
                        hp -= 10; // 紫色外星人子彈傷害：10
                        checkGameOver();
                    }
                    hitSomething = true;
                }
            } else {
                // 【玩家子彈打外星人】
                for (int j = aliens.size() - 1; j >= 0; j--) {
                    Alien a = aliens.get(j);
                    if (a.isAlive) {
                        Rectangle alienRect = new Rectangle(a.x, a.y, a.width, a.height);
                        if (bulletRect.intersects(alienRect)) {
                            a.hp--; // 扣除外星人血量 (灰色要兩次)
                            if (a.hp <= 0) {
                                a.isAlive = false;
                                score += (a.type == 3) ? 30 : 10; // 打倒裝甲給多一點分數
                                SoundManager.playExplosion();
                            }
                            hitSomething = true;
                            break; 
                        }
                    }
                }

                // 【玩家子彈打 UFO】
                if (!hitSomething && ufo.active && bulletRect.intersects(ufo.getBounds())) {
                    score += 500;
                    ufo.active = false; 
                    hitSomething = true;
                    SoundManager.playExplosion();
                    int rType = new java.util.Random().nextInt(3); 
                    powerUps.add(new PowerUp(ufo.x, ufo.y, rType));
                }
            }

            // 如果打到了東西，或是飛出邊界，子彈消失
            if (hitSomething || b.y < -50 || b.y > 850) {
                bullets.remove(i);
            }
        }

        // --- 8. 外星人移動、射擊與撞擊判定 ---
        boolean hitEdge = false;
        if (isSlowed) {
            slowTimer--;
            slowCounter++;
        }

        for (Alien a : aliens) {
            if (a.isAlive && waveDisplayTimer == 0) {
                
                // 邊界判定
                if (edgeCooldown == 0 && (a.x <= 0 || a.x >= 750)) {
                    hitEdge = true;
                }

                // 呼叫移動 (傳遞玩家 X 座標讓藍色可以追蹤)
                if (!isSlowed || (slowCounter % 2 == 0)) {
                    a.move(alienDirection, wave, shooter.x);
                }

                // 紫色外星人：每秒(50幀)射一發子彈
                if (a.type == 4 && (!isSlowed || slowCounter % 2 == 0)) {
                    a.shootTimer++;
                    if (a.shootTimer >= 50) {
                        // isEnemy = true, 顏色 = 紫紅, 垂直速度 = 10
                        bullets.add(new Bullet(a.x + 18, a.y + a.height, 0, 10, true, Color.MAGENTA));
                        a.shootTimer = 0;
                    }
                }

                // 碰撞判定：是否撞到底部
                if (a.y + a.height >= 650) {
                    if (!hasShield) {
                        // 藍色與紫色碰底 -10，其他 -20
                        int damage = (a.type == 2 || a.type == 4) ? 10 : 20;
                        hp -= damage;
                        checkGameOver();
                    }
                    a.isAlive = false; 
                } 
                // 碰撞判定：是否撞到玩家本體
                else {
                    Rectangle alienRect = new Rectangle(a.x, a.y, a.width, a.height);
                    Rectangle playerRect = new Rectangle(shooter.x, shooter.y, 50, 50);
                    if (alienRect.intersects(playerRect)) {
                        if (!hasShield) {
                            hp -= 30; // 任何外星人直接撞擊玩家都是 -30
                            checkGameOver();
                        }
                        a.isAlive = false;
                        SoundManager.playExplosion();
                    }
                }
            }
        }

        // 處理群體碰壁反彈與下移
        if (slowCounter >= 100) {
            slowCounter = 0;
        }

        if (hitEdge) {
            alienDirection *= -1;
            edgeCooldown = 10;
            for (Alien a : aliens) {
                if (a.isAlive) a.y += 20;
            }
        }

        if (slowTimer <= 0) {
            isSlowed = false; 
            slowTimer = 0; 
            slowCounter = 0; 
        }

        // --- 9. 道具更新與撿取 ---
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp p = powerUps.get(i);
            p.update();

            Rectangle playerRect = new Rectangle(shooter.x, shooter.y, 50, 20);
            if (playerRect.intersects(new Rectangle(p.x, p.y, 20, 20))) {
                if (inventory.size() < 3) { 
                    inventory.add(p.type);
                }
                powerUps.remove(i);
                break; 
            }
        }

        // 道具效果倒數
        if (powerUpTimer > 0) {
            powerUpTimer--;
            if (powerUpTimer == 0) {
                hasShield = false;
                spreadShot = false;
            }
        }

        // --- 10. 清理已經爆炸完成的外星人 ---
        for (int j = aliens.size() - 1; j >= 0; j--) {
            Alien a = aliens.get(j);
            if (!a.isAlive && a.explosionTimer >= a.MAX_EXPLOSION_TIME) {
                aliens.remove(j); 
            }
        }

        // --- 11. 遊戲過關或進入下一波條件判定 ---
        if (aliens.isEmpty()) {
            if (wave > 5) {
                isGameOver = true;
                isWin = true;
                SoundManager.stopBGM(); 
                return; 
            }
            wave++;
            waveDisplayTimer = 100; 
            spawnAliens();

            // 每一波開始時，將子彈補滿
            ammo = MAX_AMMO; 
        }

        // --- 12. 重新繪製畫面 ---
        repaint();
    }

    public void activatePowerUp(int type) {
        if (type == 0) {
            hasShield = true;
            powerUpTimer = 500;
        }
        if (type == 1) {
            isSlowed = true;
            slowTimer = 400;
            slowCounter = 0;
        }
        if (type == 2) {
            spreadShot = true;
            powerUpTimer = 500;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (!isStarted) {
            if (key == KeyEvent.VK_ENTER) {
                initGame();
                isStarted = true;
                SoundManager.playBGM();
                repaint();
            }
            return;
        }

        if (isGameOver) {
            if (key == KeyEvent.VK_ENTER) {
                initGame();
                SoundManager.playBGM();
                repaint();
            }
        } else {
            if (key == KeyEvent.VK_LEFT)
                leftPressed = true;
            if (key == KeyEvent.VK_RIGHT)
                rightPressed = true;
            if (key == KeyEvent.VK_SPACE)
                spacePressed = true;

            if (key == KeyEvent.VK_Z) {
                if (!inventory.isEmpty()) {
                    int itemToUse = inventory.remove(0);
                    activatePowerUp(itemToUse);
                }
            }
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
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT)
            leftPressed = false;
        if (key == KeyEvent.VK_RIGHT)
            rightPressed = false;
        if (key == KeyEvent.VK_SPACE)
            spacePressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}