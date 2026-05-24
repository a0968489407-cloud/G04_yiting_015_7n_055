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

    // 血量變數
    int hp = 100;
    final int MAX_HP = 100;

    // 彈藥變數
    int ammo = 35;
    final int MAX_AMMO = 35;
    int ammoRegenTimer = 0;

    // 星星、道具、背包
    ArrayList<Star> stars = new ArrayList<>();
    ArrayList<PowerUp> powerUps = new ArrayList<>();
    ArrayList<Integer> inventory = new ArrayList<>();

    // 道具狀態與獨立計時器
    boolean hasShield = false;
    boolean isFrozen = false;
    boolean spreadShot = false;
    int shieldTimer = 0; // 護盾剩餘時間
    int freezeTimer = 0; // 冰凍剩餘時間
    int spreadTimer = 0; // 散彈剩餘時間

    int wave = 1;
    int waveDisplayTimer = 0;
    int edgeCooldown = 0;

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
        inventory.clear();
        ufo = new UFO();

        score = 0;
        wave = 1;
        alienDirection = 1;
        isGameOver = false;
        isWin = false;
        ufo.active = false;

        // 重設血量與彈藥
        hp = MAX_HP;
        ammo = MAX_AMMO;
        ammoRegenTimer = 0;

        // 重設道具狀態
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

        spawnAliens();
    }

    public void spawnAliens() {
        aliens.clear();
        int rows = 3 + (wave / 2);
        int cols = 6;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double rand = Math.random();
                int type = 0;
                int currentWave = Math.min(wave, 5);

                if (currentWave == 1) {
                    if (rand < 0.80)
                        type = 0;
                    else if (rand < 0.95)
                        type = 1;
                    else
                        type = 3;
                } else if (currentWave == 2) {
                    if (rand < 0.60)
                        type = 0;
                    else if (rand < 0.80)
                        type = 1;
                    else
                        type = 3;
                } else if (currentWave == 3) {
                    if (rand < 0.60)
                        type = 0;
                    else if (rand < 0.75)
                        type = 1;
                    else if (rand < 0.90)
                        type = 2;
                    else
                        type = 3;
                } else if (currentWave == 4) {
                    if (rand < 0.50)
                        type = 0;
                    else if (rand < 0.60)
                        type = 1;
                    else if (rand < 0.75)
                        type = 2;
                    else if (rand < 0.85)
                        type = 3;
                    else
                        type = 4;
                } else {
                    if (rand < 0.30)
                        type = 0;
                    else if (rand < 0.45)
                        type = 1;
                    else if (rand < 0.65)
                        type = 2;
                    else if (rand < 0.80)
                        type = 3;
                    else
                        type = 4;
                }

                Alien a = new Alien(type);
                a.x = 100 + c * 80;
                a.y = -100 + r * 50;
                a.isAlive = true;
                aliens.add(a);
            }
        }
    }

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
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Star s : stars) {
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

            // === 修改這裡：加高框框，並將所有按鍵說明整齊向左對齊 ===
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(210, 410, 380, 170); // 將高度加長為 170，並微調寬度

            g.setFont(new Font("Arial", Font.BOLD, 18));
            // 統一 X 座標為 250，讓排版看起來像是整齊的列表
            g.drawString("MOVE  :  LEFT / RIGHT ARROW", 250, 450);
            g.drawString("FIRE   :  SPACEBAR", 250, 490);
            g.drawString("ITEM   :  Z KEY", 250, 530);
            g.drawString("MENU  :  R KEY", 250, 570); // 補充 R 鍵說明
        } else {
            if (isGameOver) {
                g.setColor(Color.ORANGE);
                g.setFont(new Font("Arial", Font.BOLD, 50));
                String msg = isWin ? "YOU WIN!" : "GAME OVER";
                g.drawString(msg, 250, 250);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.drawString("Press ENTER to Restart", 280, 300);
            } else {
                shooter.draw(g, hasShield);
                for (Bullet b : bullets)
                    b.draw(g);
                for (Alien a : aliens)
                    a.draw(g);
                for (PowerUp p : powerUps)
                    p.draw(g);
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

            // --- 道具欄 UI ---
            int slotWidth = 40, gap = 10;
            int startX = getWidth() - 170, startY = getHeight() - 70;

            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.WHITE);
            g.drawString("ITEMS", startX, startY - 10);

            for (int i = 0; i < 3; i++) {
                g.setColor(Color.BLACK);
                g.fillRect(startX + (i * (slotWidth + gap)), startY, slotWidth, slotWidth);
                g.setColor(Color.GRAY);
                g.drawRect(startX + (i * (slotWidth + gap)), startY, slotWidth, slotWidth);
            }

            // === 修改後的背包道具圖案繪製（等比例放大至 30x30 框內） ===
            for (int i = 0; i < inventory.size(); i++) {
                int type = inventory.get(i);
                int itemX = startX + (i * (slotWidth + gap)) + 5;
                int itemY = startY + 5;

                if (type == 0 && PowerUp.shieldImg != null) {
                    g.drawImage(PowerUp.shieldImg, itemX, itemY, 30, 30, null);
                } else if (type == 1 && PowerUp.iceImg != null) {
                    g.drawImage(PowerUp.iceImg, itemX, itemY, 30, 30, null);
                } else if (type == 2) {
                    // 背包中的散彈圖案：放大版放射線
                    g.setColor(Color.YELLOW);
                    g.drawLine(itemX + 15, itemY + 26, itemX + 5, itemY + 8);
                    g.drawLine(itemX + 15, itemY + 26, itemX + 15, itemY + 4);
                    g.drawLine(itemX + 15, itemY + 26, itemX + 25, itemY + 8);
                    g.fillRect(itemX + 3, itemY + 5, 4, 4);
                    g.fillRect(itemX + 13, itemY + 1, 4, 4);
                    g.fillRect(itemX + 23, itemY + 5, 4, 4);
                } else if (type == 3) {
                    // 背包中的彈夾圖案：放大版綠色子彈
                    g.setColor(Color.GREEN);
                    g.fillRect(itemX + 12, itemY + 11, 6, 14);
                    int[] bx = { itemX + 12, itemX + 15, itemX + 18 };
                    int[] by = { itemY + 11, itemY + 4, itemY + 11 };
                    g.fillPolygon(bx, by, 3);
                }
            }

            // --- 左上角狀態 UI ---
            // 1. 愛心與血條
            int heartX = 15, heartY = 17;
            g.setColor(Color.RED);
            g.fillOval(heartX, heartY, 8, 8);
            g.fillOval(heartX + 6, heartY, 8, 8);
            int[] hX = { heartX, heartX + 7, heartX + 14 };
            int[] hY = { heartY + 4, heartY + 13, heartY + 4 };
            g.fillPolygon(hX, hY, 3);

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

            // 2. 子彈與彈藥條
            int bulletX = 17, bulletY = 40;
            g.setColor(Color.YELLOW);
            g.fillRect(bulletX, bulletY, 4, 8);
            int[] bX = { bulletX, bulletX + 2, bulletX + 4 };
            int[] bY = { bulletY, bulletY - 3, bulletY };
            g.fillPolygon(bX, bY, 3);

            g.setColor(Color.GRAY);
            g.drawRect(35, 35, 150, 15);
            g.setColor(new Color(50, 50, 50));
            g.fillRect(36, 36, 148, 13);

            if (ammo > 0) {
                g.setColor(Color.YELLOW);
                int ammoBarWidth = (int) (148 * ((double) ammo / MAX_AMMO));
                // 防止超過條的長度(因為彈夾道具可突破上限)
                if (ammoBarWidth > 148)
                    ammoBarWidth = 148;
                g.fillRect(36, 36, ammoBarWidth, 13);
            }
            g.setColor(Color.WHITE);
            g.drawString(ammo + " / " + MAX_AMMO, 195, 48);

            // 3. 分數
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 15, 75);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Star s : stars) {
            s.twinkle();
            s.move(); // === 新增這行：讓星星每一幀都往下移動 ===
        }

        if (waveDisplayTimer > 0) {
            bullets.clear();
            repaint();
            return;
        }

        if (!isStarted || isGameOver)
            return;

        // 玩家移動
        if (leftPressed)
            shooter.move(-8);
        if (rightPressed)
            shooter.move(8);

        // 彈藥回復 (每0.5秒1發)
        ammoRegenTimer++;
        if (ammoRegenTimer >= 25) {
            if (ammo < MAX_AMMO)
                ammo++;
            ammoRegenTimer = 0;
        }

        // 玩家射擊 (消耗彈藥邏輯)
        if (shootCooldown > 0)
            shootCooldown--;

        if (spacePressed && shootCooldown == 0 && ammo > 0) {
            if (spreadShot && ammo >= 3) {
                ammo -= 3;
                bullets.add(new Bullet(shooter.x + 22, shooter.y, -4, -35));
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -35));
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 4, -35));
                shootCooldown = 6;
                SoundManager.playShoot();
            } else if (!spreadShot) {
                ammo--;
                bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -35));
                shootCooldown = 6;
                SoundManager.playShoot();
            }
        }

        if (edgeCooldown > 0)
            edgeCooldown--;

        // UFO 生成與移動
        if (!ufo.active) {
            if (Math.random() < 0.005)
                ufo.spawn();
        } else {
            ufo.update();
        }

        // 子彈更新與碰撞
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            boolean hitSomething = false;
            Rectangle bulletRect = new Rectangle(b.x, b.y, 5, 10);

            if (b.isEnemy) {
                // 敵方子彈打玩家
                Rectangle playerRect = new Rectangle(shooter.x, shooter.y, 50, 50);
                if (bulletRect.intersects(playerRect)) {
                    int damage = 10;
                    if (hasShield)
                        damage = (int) (damage * 0.7);
                    hp -= damage;
                    checkGameOver();
                    hitSomething = true;
                }
            } else {
                // 玩家子彈打外星人
                for (int j = aliens.size() - 1; j >= 0; j--) {
                    Alien a = aliens.get(j);
                    if (a.isAlive) {
                        Rectangle alienRect = new Rectangle(a.x, a.y, a.width, a.height);
                        if (bulletRect.intersects(alienRect)) {
                            a.hp--;
                            if (a.hp <= 0) {
                                a.isAlive = false;
                                score += (a.type == 3) ? 30 : 10;
                                SoundManager.playExplosion();
                            }
                            hitSomething = true;
                            break;
                        }
                    }
                }

                // 玩家打 UFO (直接給道具)
                if (!hitSomething && ufo.active && bulletRect.intersects(ufo.getBounds())) {
                    score += 500;
                    ufo.active = false;
                    hitSomething = true;
                    SoundManager.playExplosion();

                    int rType = new java.util.Random().nextInt(4); // 0~3
                    if (inventory.size() < 3) {
                        inventory.add(rType);
                    }
                }
            }

            if (hitSomething || b.y < -50 || b.y > 850) {
                bullets.remove(i);
            }
        }

        // 外星人移動與碰撞邏輯
        boolean hitEdge = false;

        for (Alien a : aliens) {
            if (a.isAlive && waveDisplayTimer == 0) {

                // 冰凍時不移動、不射擊
                if (!isFrozen) {
                    if (edgeCooldown == 0 && (a.x <= 0 || a.x >= 750)) {
                        hitEdge = true;
                    }
                    a.move(alienDirection, wave, shooter.x);

                    if (a.type == 4) {
                        a.shootTimer++;
                        if (a.shootTimer >= 50) {
                            bullets.add(new Bullet(a.x + 18, a.y + a.height, 0, 10, true, Color.MAGENTA));
                            a.shootTimer = 0;
                        }
                    }
                }

                // 外星人撞到底部
                if (a.y + a.height >= 650) {
                    int damage = (a.type == 2 || a.type == 4) ? 10 : 20;
                    if (hasShield)
                        damage = (int) (damage * 0.7);
                    hp -= damage;
                    checkGameOver();
                    a.isAlive = false;
                }
                // 外星人撞到玩家
                else {
                    Rectangle alienRect = new Rectangle(a.x, a.y, a.width, a.height);
                    Rectangle playerRect = new Rectangle(shooter.x, shooter.y, 50, 50);
                    if (alienRect.intersects(playerRect)) {
                        int damage = 30;
                        if (hasShield)
                            damage = (int) (damage * 0.7);
                        hp -= damage;
                        checkGameOver();
                        a.isAlive = false;
                        SoundManager.playExplosion();
                    }
                }
            }
        }

        // 群體碰壁下移
        if (!isFrozen && hitEdge) {
            alienDirection *= -1;
            edgeCooldown = 10;
            for (Alien a : aliens) {
                if (a.isAlive)
                    a.y += 20;
            }
        }

        // 獨立道具倒數計時器
        if (shieldTimer > 0) {
            shieldTimer--;
            if (shieldTimer == 0)
                hasShield = false;
        }
        if (freezeTimer > 0) {
            freezeTimer--;
            if (freezeTimer == 0)
                isFrozen = false;
        }
        if (spreadTimer > 0) {
            spreadTimer--;
            if (spreadTimer == 0)
                spreadShot = false;
        }

        // 清理爆炸完畢的外星人
        for (int j = aliens.size() - 1; j >= 0; j--) {
            Alien a = aliens.get(j);
            if (!a.isAlive && a.explosionTimer >= a.MAX_EXPLOSION_TIME) {
                aliens.remove(j);
            }
        }

        // 檢查通關與下一波
        if (aliens.isEmpty()) {
            // === 修改：打完第5波即為勝利！播放勝利音效 ===
            if (wave >= 5) {
                isGameOver = true;
                isWin = true;
                SoundManager.stopBGM();
                SoundManager.playWin(); // 播放勝利專屬音效
                repaint();
                return;
            }
            wave++;
            waveDisplayTimer = 100;
            spawnAliens();
            ammo = MAX_AMMO;
        }

        repaint();
    }

    public void activatePowerUp(int type) {
        if (type == 0) { // 護盾: 減傷30%
            hasShield = true;
            shieldTimer = 250; // 持續 5 秒 (5 * 50幀)
        } else if (type == 1) { // 冰凍: 完全停止移動與射擊
            isFrozen = true;
            freezeTimer = 75; // 持續 1.5 秒 (1.5 * 50幀)
        } else if (type == 2) { // 散彈: 一次三發
            spreadShot = true;
            spreadTimer = 150; // 持續 3 秒 (3 * 50幀)
        } else if (type == 3) { // 彈夾: 直接增加 25 發，無視上限
            ammo += 25;
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