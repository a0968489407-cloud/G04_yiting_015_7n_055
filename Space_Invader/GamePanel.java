
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
    int alienDirection = 1; 
    int score = 0; 
    
    // 新增：遊戲狀態變數 [cite: 135, 136]
    boolean isStarted = false; // 預設為 false，表示先顯示歡迎畫面
    boolean isGameOver = false;
    boolean isWin = false;

    //星星
    ArrayList<Star> stars = new ArrayList<>();

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
        shooter = new Shooter(375, 500); 
        bullets = new ArrayList<>();
        aliens = new ArrayList<>();
        
        spawnAliens(); // 直接呼叫這個，不要再手動寫 for 迴圈生 5 隻

        score = 0;
        wave = 1; // 記得重設回合數
        alienDirection = 1; 
        isGameOver = false;
        isWin = false;
}

    public void spawnAliens() {
        aliens.clear(); // 清空舊的（包含爆炸殘骸）
        
        // 假設每回合增加難度，我們用 wave 來決定排數
        // 例如：第 1 回合 3 排，第 2 回合 4 排...
        int rows = 3 + (wave / 2); 
        int cols = 6; // 每排 6 隻
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Alien a = new Alien();
                a.x = 100 + c * 80;  // 間距 80
                a.y = 30 + r * 50;   // 每排垂直間距 50
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
                shooter.draw(g); 
                for (Bullet b : bullets) {
                    b.draw(g); 
                }
                for (Alien a : aliens) {
                    a.draw(g); 
                }
                
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

        // --- 外星人移動與碰壁反彈 ---
        boolean hitEdge = false;
        for (Alien a : aliens) {
            if (a.isAlive && waveDisplayTimer == 0) {
                a.move(alienDirection, wave);
                // 只有在冷卻時間結束後，才檢查邊界
                if (edgeCooldown == 0 && (a.x <= 0 || a.x >= 750)) {
                    hitEdge = true;
                }
            }
        }

        // 如果有任一「活著」的外星人撞到邊緣，全體轉向並下移
        if (hitEdge) {
            alienDirection *= -1;
            edgeCooldown = 10; // 觸發後鎖定 10 幀 (約 0.2 秒) 不再偵測邊界
            for (Alien a : aliens) {
                if (a.isAlive) a.y += 20;
            }
        }

        // --- 碰撞檢測 ---
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            for (int j = aliens.size() - 1; j >= 0; j--) {
                Alien a = aliens.get(j);
                
                // 只跟還活著的外星人進行碰撞檢測
                if (a.isAlive) {
                    Rectangle bulletRect = new Rectangle(b.x, b.y, 5, 10);
                    Rectangle alienRect = new Rectangle(a.x, a.y, a.width, a.height);

                    if (bulletRect.intersects(alienRect)) {
                        a.isAlive = false; // 標記為死亡，啟動爆炸動畫
                        score += 10;
                        bullets.remove(i); // 子彈消失
                        break;
                    }
                }
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
            wave++;
            waveDisplayTimer = 100; // 顯示約 2 秒 (100 * 20ms)
            spawnAliens();  
        }
        
        // 條件二：外星人到達底部 (接近玩家 y=500 的位置)
        for (Alien a : aliens) {
            if (a.y + a.height >= 480) {
                isGameOver = true;
                isWin = false;
                break;
            }
        }

        repaint(); 
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
                bullets.add(new Bullet(shooter.x + 22, shooter.y)); 
            }
        }
    }
    
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}