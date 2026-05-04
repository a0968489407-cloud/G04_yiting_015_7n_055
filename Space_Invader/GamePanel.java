
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

import main.PowerUp;
import main.UFO;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    Timer timer;
    Shooter shooter;
    ArrayList<Bullet> bullets;
    ArrayList<Alien> aliens;
    UFO ufo;
    int alienDirection = 1; 
    int score = 0; 
    
    
    // 新增：遊戲狀態變數 [cite: 135, 136]
    boolean isStarted = false; // 預設為 false，表示先顯示歡迎畫面
    boolean isGameOver = false;
    boolean isWin = false;
    boolean isSlowed = false;
    int slowCounter = 0; // 用來計算減速剩餘時間

    //星星
    ArrayList<Star> stars = new ArrayList<>();

    // 道具列表
    ArrayList<PowerUp> powerUps = new ArrayList<>();

    // 道具欄：儲存道具的 type (0, 1, 2)
    ArrayList<Integer> inventory = new ArrayList<>();

    // 道具狀態
    boolean hasShield = false;
    boolean spreadShot = false;
    int powerUpTimer = 0; // 道具效果剩餘時間 (幀數)
    int slowTimer = 0; // 減速效果剩餘時間 (幀數)

    // 目前是第幾波
    int wave = 1; 

    // 用來控制回合文字顯示多久
    int waveDisplayTimer = 0; 

    // 碰壁後的冷卻時間，避免外星人過快反彈
    int edgeCooldown = 0; 

    public GamePanel() {
        this.setBackground(Color.BLACK); 
        this.setFocusable(true);
        this.addKeyListener(this); 

        // 呼叫初始化遊戲的方法
        initGame();

        //隨機數取星星位置和大小
        java.util.Random rand = new java.util.Random();

        // 生成 200 顆星星，位置和大小隨機 [cite: 137, 138]
        for (int i = 0; i < 200; i++) {
            int x = rand.nextInt(800);
            int y = rand.nextInt(800);
            int size = rand.nextInt(4) + 1; // 星星大小 1~4
            stars.add(new Star(x, y, size));
        }

        // 遊戲迴圈
        timer = new Timer(20, this); 
        timer.start(); 
    }

    // 新增：將初始化狀態獨立成一個方法，方便重新開始時呼叫 
    public void initGame() {
        shooter = new Shooter(375, 650); 
        bullets = new ArrayList<>();
        aliens = new ArrayList<>();
        powerUps = new ArrayList<>();
        ufo = new UFO();
        
        spawnAliens(); // 直接呼叫這個，不要再手動寫 for 迴圈生 5 隻

        score = 0;
        wave = 1; // 記得重設回合數
        alienDirection = 1; 
        isGameOver = false;
        isWin = false;
        hasShield = false;
        spreadShot = false;
        powerUpTimer = 0;
        slowTimer = 0; // 初始化減速計時器
        ufo.active = false; // 確保 UFO 也被重置
        isSlowed = false;
        slowCounter = 0; // 用來計算減速剩餘時間
}

    public void spawnAliens() {
        aliens.clear();
        
        int rows = 3 + (wave / 2); 
        int cols = 6;
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // 邏輯修改：預設 type 為 0 (普通)
                int type = 0;
                
                // 只有在第 2 輪 (wave >= 2) 開始，才啟用俯衝者生成機率
                if (wave >= 2) {
                    // 隨著回合數增加，俯衝者的出現機率也可以變高 (Wave 2 是 5%，之後每輪增加 5%)
                    double spawnRate = 0.05 + ((wave - 2) * 0.05);
                    type = (Math.random() < spawnRate) ? 1 : 0;
                }
                /* 
                // 在 spawnAliens() 迴圈內，加入機率生成 UFO
                if (wave >= 2 && Math.random() < 1) { 
                    type = 2; // 1% 機率生成 UFO
                }*/
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

       // 1.繪製所有閃爍的星星
        for (Star s : stars) {
            s.draw(g);
        }
        
        if (!isStarted) {

            // 2. 主標題：陰影效果
            g.setFont(new Font("Courier New", Font.BOLD, 70));
            g.setColor(Color.MAGENTA); // 陰影顏色
            g.drawString("SPACE INVADERS", 104, 204); 
            g.setColor(Color.CYAN);    // 主文字顏色
            g.drawString("SPACE INVADERS", 100, 200);   

            // 3. 閃爍提示：Press ENTER
            if ((System.currentTimeMillis() / 600) % 2 == 0) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 25));
                g.drawString(">>> PRESS ENTER TO START <<<", 210, 350);
            }

            // 4. 操作說明欄
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(180, 410, 440, 80); // 畫一個框框包住說明
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("MOVE: LEFT/RIGHT ARROW", 280, 440);
            g.drawString("FIRE: SPACEBAR", 325, 470);
        }

        else {
            if (isGameOver) {
            // 如果遊戲結束，繪製結束畫面 [cite: 140, 141]
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            String msg = isWin ? "YOU WIN!" : "GAME OVER";
            g.drawString(msg, 250, 250); // 顯示勝利或失敗
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press ENTER to Restart", 280, 300); // 提示玩家按 Enter 重新開始
            
            } 
            else {
                // 遊戲進行中，繪製所有元素
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
                
                //繪製過場文字
                if (waveDisplayTimer > 0) {
                    // --- 閃爍邏輯：每 200 毫秒切換一次顯示狀態 ---
                    if ((System.currentTimeMillis() / 200) % 2 == 0) {
                        String waveText = "WAVE " + wave;
                        
                        // 設定超大字體
                        Font waveFont = new Font("Courier New", Font.BOLD, 100); 
                        g.setFont(waveFont);

                        // 1. 繪製白色陰影 (位移 5 像素)
                        g.setColor(Color.WHITE);
                        g.drawString(waveText, 205, 355); 

                        // 2. 繪製橘色主文字
                        g.setColor(Color.ORANGE);
                        g.drawString(waveText, 200, 350); 
                    }
                    
                    waveDisplayTimer--; // 計時器減少
                }

            }
            
            // --- 繪製橫向道具欄 (固定在右下角) ---
            int slotWidth = 40;   // 每個格子寬度
            int gap = 10;         // 格子間距
            int startX = getWidth() - 170; // 讓欄位靠右 (170 是總寬度預留)
            int startY = getHeight() - 70; // 讓欄位靠底

            // 1. 繪製標題
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.WHITE);
            g.drawString("ITEMS", startX, startY - 10);

            // 2. 繪製 3 個空槽位 (固定位置)
            for (int i = 0; i < 3; i++) {
                g.setColor(Color.BLACK);
                g.fillRect(startX + (i * (slotWidth + gap)), startY, slotWidth, slotWidth);
                g.setColor(Color.GRAY);
                g.drawRect(startX + (i * (slotWidth + gap)), startY, slotWidth, slotWidth);
            }

            // 3. 繪製已收集的道具 (橫向排列)
            for (int i = 0; i < inventory.size(); i++) {
                int type = inventory.get(i);
                // 設定顏色
                if (type == 0) g.setColor(Color.BLUE);
                else if (type == 1) g.setColor(Color.WHITE);
                else g.setColor(Color.YELLOW);
                
                // 關鍵：將 i * (slotWidth + gap) 加在 X 座標上，Y 座標固定
                g.fillRect(startX + (i * (slotWidth + gap)) + 5, startY + 5, 30, 30);
            }

            // 分數永遠顯示在左上角 [cite: 16]
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20)); 
            g.drawString("Score: " + score, 15, 30);
            
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 即使遊戲沒開始，星星也要動
        for (Star s : stars) {
            s.twinkle();
        }

        // 如果正在顯示過場文字，清空畫面上的子彈，並跳過物理運算
        if (waveDisplayTimer > 0) {
            bullets.clear(); // 進入下一波時清空殘留子彈
            repaint();
            return; 
        }

        // 如果遊戲還沒開始或已經結束，就不再更新任何物件的移動邏輯
        if (!isStarted || isGameOver) {
            return; 
        }

        // --- 子彈移動 ---
        for (int i = 0; i < bullets.size(); i++) {
            bullets.get(i).update();
        }
        
        if (edgeCooldown > 0){
            edgeCooldown--;
        } 

        // --- UFO 邏輯 ---
        if (!ufo.active) {
            // 給予極低的機率隨機生成，例如每幀 0.1% 機率
            if (Math.random() < 0.001) { 
                ufo.spawn();
            }
        } else {
            ufo.update(); // 讓它移動
        }

        // --- 外星人移動與碰壁反彈 ---
        boolean hitEdge = false;

        // 在迴圈外增加計數器運算
        if (isSlowed) {
            slowTimer--;
            slowCounter++;
        }

        for (Alien a : aliens) {
            if (a.isAlive && waveDisplayTimer == 0) {
                
                // 1. 偵測邊界 (這段一定要在外面，確保每一幀都會偵測)
                if (edgeCooldown == 0 && (a.x <= 0 || a.x >= 750)) {
                    hitEdge = true;
                }

                // 2. 移動邏輯 (只在該移動的幀才呼叫 move)
                if (!isSlowed || (slowCounter % 2 == 0)) {
                    a.move(alienDirection, wave);
                }
            }
        }

        // 重置計數器 (避免數字過大溢位)
        if (slowCounter >= 100) {
            slowCounter = 0;
        }

        // 如果有任一外星人撞到邊緣，全體轉向並下移
        if (hitEdge) {
            alienDirection *= -1;
            edgeCooldown = 10; 
            for (Alien a : aliens) {
                if (a.isAlive) a.y += 20;
            }
        }

        // 當時間耗盡
        if (slowTimer <= 0) {
            isSlowed = false;   // 關閉減速狀態
            slowTimer = 0;      // 重置時間
            slowCounter = 0;    // 重置計數器
        }

        // --- 碰撞檢測 ---
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            Rectangle bulletRect = new Rectangle(b.x, b.y, 5, 10);
            boolean hitSomething = false; // 紀錄這發子彈有沒有打到東西

            // 1. 檢查是否打到外星人
            for (int j = aliens.size() - 1; j >= 0; j--) {
                Alien a = aliens.get(j);
                if (a.isAlive) {
                    Rectangle alienRect = new Rectangle(a.x, a.y, a.width, a.height);
                    if (bulletRect.intersects(alienRect)) {
                        a.isAlive = false; 
                        score += 10;
                        hitSomething = true;
                        break; // 打到就跳出外星人迴圈
                    }
                }
            }

            // 2. 檢查是否打到 UFO (這段要獨立出來，不要包在外星人迴圈裡)
            if (ufo.active && bulletRect.intersects(ufo.getBounds())) {
                score += 500; 
                ufo.active = false; // UFO 消失
                hitSomething = true;
                
                // --- 掉落道具 ---
                int rType = new java.util.Random().nextInt(3); // 0, 1, 2
                powerUps.add(new PowerUp(ufo.x, ufo.y, rType));
            }

            // 3. 如果打到了東西，子彈才消失
            if (hitSomething) {
                bullets.remove(i);
            }
        }

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp p = powerUps.get(i);
            p.update();
            
            // 撿取判斷
            Rectangle playerRect = new Rectangle(shooter.x, shooter.y, 50, 20);
            if (playerRect.intersects(new Rectangle(p.x, p.y, 20, 20))) {
                System.out.println("道具被吃掉了！目前的背包大小: " + inventory.size());
                if (inventory.size() < 3) { // 設定最多持有 3 個
                    inventory.add(p.type);
                    System.out.println("道具已存入！目前數量: " + inventory.size());
                }
                powerUps.remove(i);
                break; // 撞到一個就跳出
             }
        }

        // --- 道具計時器 ---
        if (powerUpTimer > 0) {
            powerUpTimer--;
            if (powerUpTimer == 0) {
                hasShield = false;
                spreadShot = false;
            }
        }

        // --- 清理已經爆炸完成的外星人 ---
        for (int j = aliens.size() - 1; j >= 0; j--) {
            Alien a = aliens.get(j);
            if (!a.isAlive && a.explosionTimer >= a.MAX_EXPLOSION_TIME) {
                aliens.remove(j); // 動畫播完，正式從列表移除
            }
        }

        // --- 新增：遊戲結束條件判定 [cite: 17, 18, 50] ---
        // --- 在 actionPerformed 尾端修正勝利判定 ---
        if (aliens.isEmpty()) {

            //條件一: 全部外星人被消滅，且已完成第5波攻擊，即勝利
            if(wave > 5){
                isGameOver = true;
                isWin = true;   
                return; // 直接結束，不再進入下一波
            }
            wave++;
            waveDisplayTimer = 100; // 顯示約 2 秒 (100 * 20ms)
            spawnAliens();  
        }
        
        // --- 修改為：防護罩防禦邏輯 ---
        for (Alien a : aliens) {
            if (a.isAlive && a.y + a.height >= 650) {
                if (hasShield) {
                    // 防護罩啟動時：撞到保護罩的外星人直接死亡，防護罩消耗
                    a.isAlive = false;
                    
                }    
                else {//外星人到底部，且沒有防護罩技能，遊戲結束 
                    isGameOver = true;
                    isWin = false;
                    break;
                }
            }
        }

        repaint(); 
    }

    public void activatePowerUp(int type) {
        if (type == 0) { // 0 = 防護罩
            hasShield = true;
            powerUpTimer = 500; // 持續 10 秒
        }
        if (type == 1) { // 1 = 減速
            isSlowed = true;
            slowTimer = 400; // 設定減速計時器 8 秒 (400 * 20ms = 8000ms)
            slowCounter = 0; // 重置計數器，確保減速效果從下一幀開始生效
        }
        if (type == 2) {
            spreadShot = true;
            powerUpTimer = 500; // 持續 10 秒
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // 狀態 1：歡迎畫面
        // 如果還沒開始，按 Enter 鍵進入遊戲
        if (!isStarted) {
            if (key == KeyEvent.VK_ENTER) {
                isStarted = true;
                repaint();
            }
            return; // 在歡迎畫面時，不處理移動或射擊
        }
        
        // 狀態 2：遊戲結束
        if (isGameOver) {
         // 遊戲結束時，允許玩家按 Enter 重新開始 
            if (key == KeyEvent.VK_ENTER) {
                initGame();                    
                repaint(); // 立刻重繪畫面清除 Game Over 字樣
            }
        } 
        else {
            // 遊戲進行中時的正常操作
            if (key == KeyEvent.VK_LEFT) {
                shooter.move(-15); 
            } else if (key == KeyEvent.VK_RIGHT) {
                shooter.move(15); 
            } else if (key == KeyEvent.VK_SPACE) {
                if (spreadShot){ 
                    for(int i =0;i<3;i++){
                        // 發射扇形三發子彈
                        bullets.add(new Bullet(shooter.x + 22, shooter.y, -3, -10)); // 左斜
                        bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -10));  // 直線
                        bullets.add(new Bullet(shooter.x + 22, shooter.y, 3, -10));  // 右斜
                    }
                } else {
                    // 普通狀態，只發射直線一發
                    bullets.add(new Bullet(shooter.x + 22, shooter.y, 0, -10));
                }
            }

            // 在 keyPressed(KeyEvent e) 中新增：
            if (key == KeyEvent.VK_Z) { // 按下 Z 鍵使用道具
                if (!inventory.isEmpty()) {
                    int itemToUse = inventory.remove(0); // 拿取並移除第一個道具
                    activatePowerUp(itemToUse); // 呼叫效果
                }
            }
        }
    }
    
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}