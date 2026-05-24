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
                int type = 0;
                if (wave >= 2) {
                    double spawnRate = 0.05 + ((wave - 2) * 0.05);
                    type = (Math.random() < spawnRate) ? 1 : 0;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Star s : stars) {
            s.twinkle();
        }

        if (waveDisplayTimer > 0) {
            bullets.clear();
            repaint();
            return;
        }

        if (!isStarted || isGameOver) {
            return;
        }

        if (leftPressed)
            shooter.move(-8);
        if (rightPressed)
            shooter.move(8);

        // --- 新增：彈藥回復邏輯 (每 0.5 秒回復 1 發) ---
        // 遊戲迴圈是 20ms 一幀，25 幀剛好是 500ms (0.5秒)
        ammoRegenTimer++;
        if (ammoRegenTimer >= 25) {
            if (ammo < MAX_AMMO) {
                ammo++;
            }
            ammoRegenTimer = 0; // 重置計時器
        }

        if (shootCooldown > 0) {
            shootCooldown--;
        }

        if (spacePressed && shootCooldown == 0 && ammo > 0) {
            
            ammo--; // 扣除 1 發彈藥

            // === 修改：子彈速度加快，將垂直速度從 -10 改為 -20 ===
            if (spreadShot) {
                bullets.add(new Bullet(shooter.x + 22, shooter.y, -4, -35));
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -35));
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 4, -35));
            } else {
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -35));
            }
            
            // === 修改：發射速度減慢，將冷卻時間從 3 加長為 6 ===
            shootCooldown = 6; 
            
            SoundManager.playShoot();
        }

        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
        }

        if (edgeCooldown > 0) {
            edgeCooldown--;
        }

        if (!ufo.active) {
            if (Math.random() < 0.001) {
                ufo.spawn();
            }
        } else {
            ufo.update();
        }

        boolean hitEdge = false;

        if (isSlowed) {
            slowTimer--;
            slowCounter++;
        }

        for (Alien a : aliens) {
            if (a.isAlive && waveDisplayTimer == 0) {
                if (edgeCooldown == 0 && (a.x <= 0 || a.x >= 750)) {
                    hitEdge = true;
                }
                if (!isSlowed || (slowCounter % 2 == 0)) {
                    a.move(alienDirection, wave);
                }
            }
        }

        if (slowCounter >= 100) {
            slowCounter = 0;
        }

        if (hitEdge) {
            alienDirection *= -1;
            edgeCooldown = 10;
            for (Alien a : aliens) {
                if (a.isAlive)
                    a.y += 20;
            }
        }

        if (slowTimer <= 0) {
            isSlowed = false;
            slowTimer = 0;
            slowCounter = 0;
        }

        // 碰撞檢測
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            Rectangle bulletRect = new Rectangle(b.x, b.y, 5, 10);
            boolean hitSomething = false;

            for (int j = aliens.size() - 1; j >= 0; j--) {
                Alien a = aliens.get(j);
                if (a.isAlive) {
                    Rectangle alienRect = new Rectangle(a.x, a.y, a.width, a.height);
                    if (bulletRect.intersects(alienRect)) {
                        a.isAlive = false;
                        score += 10;
                        hitSomething = true;
                        SoundManager.playExplosion();
                        break;
                    }
                }
            }

            if (ufo.active && bulletRect.intersects(ufo.getBounds())) {
                score += 500;
                ufo.active = false;
                hitSomething = true;
                SoundManager.playExplosion();
                int rType = new java.util.Random().nextInt(3);
                powerUps.add(new PowerUp(ufo.x, ufo.y, rType));
            }

            if (hitSomething) {
                bullets.remove(i);
            }
        }

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

        if (powerUpTimer > 0) {
            powerUpTimer--;
            if (powerUpTimer == 0) {
                hasShield = false;
                spreadShot = false;
            }
        }

        for (int j = aliens.size() - 1; j >= 0; j--) {
            Alien a = aliens.get(j);
            if (!a.isAlive && a.explosionTimer >= a.MAX_EXPLOSION_TIME) {
                aliens.remove(j);
            }
        }

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

            ammo = MAX_AMMO; // 每波開始時補滿彈藥
        }

        // === 修改：外星人到達底部的扣血與傷害判定邏輯 ===
        for (Alien a : aliens) {
            if (a.isAlive && a.y + a.height >= 650) {
                if (hasShield) {
                    // 有防護罩時：外星人撞擊死亡，玩家不扣血
                    a.isAlive = false;
                } else {
                    // 沒有防護罩時：根據外星人種類決定不同傷害值
                    int damage = 15; // 預設普通外星人 (type 0) 傷害 15 點

                    if (a.type == 1) {
                        damage = 35; // 紅色俯衝者 (type 1) 傷害更高，扣 35 點
                    }

                    hp -= damage; // 扣除血量
                    a.isAlive = false; // 讓攻擊過的外星人死亡消失，避免在下一幀重複扣血

                    // 檢查玩家血量是否歸零
                    if (hp <= 0) {
                        hp = 0;
                        isGameOver = true;
                        isWin = false;
                        SoundManager.stopBGM(); // 停止背景音樂
                        SoundManager.playGameOver(); // 播放失敗音效
                        break;
                    }
                }
            }
        }

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