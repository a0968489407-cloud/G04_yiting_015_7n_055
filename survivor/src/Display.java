import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Display extends JPanel implements ActionListener {
    private GameManager gameManager;
    private Timer timer;
    private Btn btnMode;
    private Btn btnStart;
    private Btn btnRule;
    private Btn btnExit;

    // 滑鼠當前座標
    private int currentMouseX = 0;
    private int currentMouseY = 0;

    // 記錄當前時間倍速
    public static double currentSpeedMultiplier = 1.0;
    // 計時器用於 UI 特效的閃爍運算
    private int animationTick = 0; 

    public Display(int width, int height) {
        this.setBackground(Color.BLACK);
        // 設定接收鍵盤焦點
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(width, height));

        // 設定圓形競技場的中心與大小
        double centerX = (double) width / 2;
        double centerY = (double) height / 2;
        double arenaRadius = (double) height / 2 * 0.9;
        gameManager = new GameManager(centerX, centerY, arenaRadius);

        // 初始化右側的 UI 按鈕
        int btnWidth = 150;
        int btnHeight = 50;
        int btnX = (int) (width * 0.83);
        int btnY = (int) (height * 0.55);
        int btnYOffset = btnHeight + 20;

        btnMode = new Btn(btnX, btnY, btnWidth, btnHeight, "Classic");
        btnStart = new Btn(btnX, btnY + btnYOffset, btnWidth, btnHeight, "Start");
        btnRule = new Btn(btnX, btnY + btnYOffset * 2, btnWidth, btnHeight, "Rule");
        btnExit = new Btn(btnX, btnY + btnYOffset * 3, btnWidth, btnHeight, "Exit");

        setupInputListeners();

        // 設定 JPanel 刷新 Timer，每 16 毫秒觸發一次 (約 60 FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    private void setupInputListeners() {
        // --- 滑鼠點擊事件 ---
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                if (e.getButton() == MouseEvent.BUTTON1) { // 左鍵
                    // 判斷按鈕點擊
                    if (btnMode.contains(mx, my)) { toggleGameMode(); } 
                    else if (btnStart.contains(mx, my)) { handleStartRestart(); } 
                    else if (btnRule.contains(mx, my)) { showRules(); } 
                    else if (btnExit.contains(mx, my)) { System.exit(0); } 
                    else {
                        // 點擊場景時的處理
                        if (gameManager.currentState != GameState.SETUP) return;

                        if (gameManager.pendingBall == null) {
                            // 若還沒拉弓，且未達數量上限，則建立一顆新球等待賦予速度
                            if (gameManager.balls.size() >= 6) return;
                            if (gameManager.isInsideArena(mx, my)) {
                                Color newColor = gameManager.getAvailableColor();
                                Ball newBall = new Ball(mx, my, newColor, false);
                                gameManager.balls.add(newBall);
                                gameManager.pendingBall = newBall;
                            }
                        } else {
                            // 拖曳放開後，根據與滑鼠游標的距離設定初速度與方向
                            Ball b = gameManager.pendingBall;
                            double dx = mx - b.pos.x;
                            double dy = my - b.pos.y;
                            double length = Math.sqrt(dx * dx + dy * dy);
                            
                            if (length > 0) {
                                double speed = 5.0;
                                b.velocity.x = (dx / length) * speed;
                                b.velocity.y = (dy / length) * speed;
                            } else {
                                b.velocity.x = 0;
                                b.velocity.y = -5;
                            }
                            b.isFreezed = false;
                            gameManager.pendingBall = null;
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) { // 右鍵
                    // 在 SETUP 狀態下，右鍵可刪除點擊的球
                    if (gameManager.currentState == GameState.SETUP) {
                        boolean ballRemoved = false;
                        for (int i = gameManager.balls.size() - 1; i >= 0; i--) {
                            Ball b = gameManager.balls.get(i);
                            double dx = mx - b.pos.x;
                            double dy = my - b.pos.y;
                            if (Math.sqrt(dx * dx + dy * dy) <= b.radius + 5) {
                                if (b == gameManager.pendingBall) gameManager.pendingBall = null;
                                gameManager.balls.remove(i);
                                ballRemoved = true;
                                break;
                            }
                        }
                        // 若無點擊到任何實體且正在拉弓狀態，則放棄該次拉弓操作
                        if (!ballRemoved && gameManager.pendingBall != null) {
                            gameManager.balls.remove(gameManager.pendingBall);
                            gameManager.pendingBall = null;
                        }
                    }
                }
            }
        });

        // --- 滑鼠移動事件 ---
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                currentMouseX = e.getX();
                currentMouseY = e.getY();
                // 檢查是否懸停於按鈕
                btnMode.isHovered = btnMode.contains(currentMouseX, currentMouseY);
                btnStart.isHovered = btnStart.contains(currentMouseX, currentMouseY);
                btnRule.isHovered = btnRule.contains(currentMouseX, currentMouseY);
                btnExit.isHovered = btnExit.contains(currentMouseX, currentMouseY);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                currentMouseX = e.getX();
                currentMouseY = e.getY();
            }
        });

        // --- 鍵盤事件 ---
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) toggleGameMode();
                else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleStartRestart();
                }
                else if (e.getKeyCode() == KeyEvent.VK_R) showRules();
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
                
                // 動態調整遊戲倍率，增加可玩性
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    currentSpeedMultiplier += 0.5;
                    if (currentSpeedMultiplier > 3.0) currentSpeedMultiplier = 3.0;
                }
                else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    currentSpeedMultiplier -= 0.5;
                    if (currentSpeedMultiplier < 1.0) currentSpeedMultiplier = 1.0;
                }
                // 使用鍵盤上方數字區或右側九宮格區，一鍵呼叫 GameManager 的批次產生隨機球功能
                else if (gameManager.currentState == GameState.SETUP) {
                    int keyCode = e.getKeyCode();
                    if (keyCode >= KeyEvent.VK_2 && keyCode <= KeyEvent.VK_6) {
                        int ballCount = keyCode - KeyEvent.VK_0; 
                        gameManager.spawnBatchRandomBalls(ballCount);
                        repaint();
                    }
                    else if (keyCode >= KeyEvent.VK_NUMPAD2 && keyCode <= KeyEvent.VK_NUMPAD6) {
                        int ballCount = keyCode - KeyEvent.VK_NUMPAD0; 
                        gameManager.spawnBatchRandomBalls(ballCount);
                        repaint();
                    }
                }
            }
        });
    }

    private void handleStartRestart() {
        if (gameManager.currentState == GameState.SETUP) {
            if (gameManager.balls.size() >= 2) {
                gameManager.currentState = GameState.PLAYING;
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please add at least 2 balls to start!",
                        "Not Enough Balls",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            // 已在遊戲中則執行重置
            gameManager.resetGame();
            currentSpeedMultiplier = 1.0; 
        }
    }

    private void toggleGameMode() {
        // 僅能在未啟動時切換模式
        if (gameManager.currentState != GameState.SETUP) return;

        if (gameManager.currentMode == GameMode.CLASSIC) {
            gameManager.currentMode = GameMode.ITEM_MODE;
            btnMode.text = "Item";
        } else {
            gameManager.currentMode = GameMode.CLASSIC;
            btnMode.text = "Classic";
        }
    }

    private void showRules() {
        String page1 =
                "🏆 1. 經典模式 (Classic) 🏆\n" +
                "• 獲勝條件：割斷對手的線！活到最後的球獲勝。\n" +
                "• 初始機制：球剛生成時皆為安全狀態。\n" +
                "            必須在「第一次撞牆」後才會有攻擊能力和生命(線)！\n" +
                "• 淘汰機制：當球體身上的線段數歸零時，該顏色立即淘汰。\n\n" +
                
                "🎁 2. 道具模式 (Item) 🎁\n" +
                "• 獲勝條件：割斷對手的線！活到最後的球獲勝。\n" +
                "• 道具生成：每 1.5 秒隨機掉落道具\n" +
                "• 初始機制：同經典模式\n" +
                "• 黑洞磁力：道具自帶重力圈，會將半徑 120px 內的球高速吸向中心。\n" +
                "• 道具效果：\n" +
                "  ⚡ 閃電：大幅增加球體的移動速度。\n" +
                "  ❄️ 冰凍：大幅降低球體速度，使其短暫減速。\n" +
                "  ❤️ 生命：生命+1。復活時從中間接線\n" +
                "  🧬 分裂：大球分裂為 2 顆快速小球，完整複製並繼承所有線段。\n" +
                "  道具非累加!!!\n" +
                "【1/2】";

        JOptionPane.showMessageDialog(this, page1, "遊戲規則與機制 (第 1 頁 / 共 2 頁)", JOptionPane.INFORMATION_MESSAGE);

        String page2 =
                "🕹️ 3. 操作指南 (Control) 🕹️\n" +
                "【滑鼠操作 — 準備階段 (SETUP)】\n" +
                "• 左鍵點擊空地：新增球與指定方向\n" +
                "• 右鍵點擊球體：刪除該顆球\n\n" +
                "【鍵盤快捷鍵】\n" +
                "• 空白鍵  ：切換遊戲模式(CLASSIC / ITEM)\n" +
                "• ENTER   ：開始遊戲 / 重新重置開局\n" +
                "• 2 ~ 6 鍵：一鍵隨機生成對應球數與噴射方向\n" +
                "• R 鍵：規則說明\n" +
                "• 方向鍵▶：遊戲時間流速加快 (+0.5)，最高 x3.0\n" +
                "• 方向鍵◀：遊戲時間流速減慢 (-0.5)，最低 x1.0\n" +
                "• ESC 鍵  ：直接強行關閉並退出遊戲\n\n" +
                "【2/2】";

        JOptionPane.showMessageDialog(this, page2, "按鍵與操作設定 (第 2 頁 / 共 2 頁)", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 主迴圈心跳更新
        gameManager.update();
        // 動畫時間計算要一併乘上倍數以配合視覺快進
        animationTick += Display.currentSpeedMultiplier;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. 繪製圓形競技場外框
        g.setColor(Color.WHITE);
        g.drawOval((int) (gameManager.arenaCenterX - gameManager.arenaRadius),
                (int) (gameManager.arenaCenterY - gameManager.arenaRadius),
                (int) (gameManager.arenaRadius * 2), (int) (gameManager.arenaRadius * 2));

        // 繪製道具磁力圈特效
        if (gameManager.currentMode == GameMode.ITEM_MODE && gameManager.items != null) {
            for (Item item : gameManager.items) {
                if (!item.isAvailable) continue;

                // 運用 mod 計算，使波紋呈現連續往內收縮的動畫視覺
                int baseOffset = (animationTick * 4) % 120;
                int radius1 = 120 - baseOffset; 
                int radius2 = 120 - ((baseOffset + 60) % 120); 

                // 亮藍色波紋
                g.setColor(new Color(0, 191, 255, 100)); 
                if (radius1 > 0) {
                    g.drawOval((int) item.pos.x - radius1, (int) item.pos.y - radius1, radius1 * 2, radius1 * 2);
                }
                if (radius2 > 0) {
                    g.drawOval((int) item.pos.x - radius2, (int) item.pos.y - radius2, radius2 * 2, radius2 * 2);
                }

                // 畫出影響極限的淺色底圈 (120px)
                g.setColor(new Color(100, 100, 100, 40));
                g.drawOval((int) item.pos.x - 120, (int) item.pos.y - 120, 240, 240);
            }
        }

        // 2. 繪製實體球與線段
        if (gameManager.balls != null) {
            for (Ball b : gameManager.balls) {
                if (b.isDead) continue;

                if (b.myLines != null) {
                    for (Line l : b.myLines) l.draw(g);
                }
                b.draw(g);

                // 在 SETUP 狀態下預先畫出方向箭頭方便玩家視覺確認
                if (gameManager.currentState == GameState.SETUP && b != gameManager.pendingBall) {
                    drawVelocityArrow(g, b);
                }
            }
        }

        // 若有正在拖曳中的球體，畫出灰色的拉弓瞄準虛線
        if (gameManager.pendingBall != null) {
            g.setColor(Color.GRAY);
            g.drawLine((int) gameManager.pendingBall.pos.x,
                    (int) gameManager.pendingBall.pos.y,
                    currentMouseX, currentMouseY);
        }

        // 3. 繪製道具圖示
        if (gameManager.items != null) {
            for (Item item : gameManager.items) {
                item.draw(g);
            }
        }

        // 繪製所有的 UI 介面
        drawUI(g);
    }

    // 將速度向量轉為視覺箭頭畫在畫面上
    private void drawVelocityArrow(Graphics g, Ball b) {
        if (b.velocity.x == 0 && b.velocity.y == 0) return;

        double len = Math.sqrt(b.velocity.x * b.velocity.x + b.velocity.y * b.velocity.y);
        double nx = b.velocity.x / len;
        double ny = b.velocity.y / len;

        int startX = (int) b.pos.x;
        int startY = (int) b.pos.y;
        int lineLen = 35;
        int endX = startX + (int) (nx * lineLen);
        int endY = startY + (int) (ny * lineLen);

        g.setColor(b.color);
        g.drawLine(startX, startY, endX, endY);

        int arrowSize = 6;
        double px = -ny;
        double py = nx;

        // 計算箭頭的三個頂點形成 Polygon 三角形
        int[] xPoints = {
                endX + (int) (nx * arrowSize),
                endX - (int) (nx * arrowSize) + (int) (px * arrowSize),
                endX - (int) (nx * arrowSize) - (int) (px * arrowSize)
        };
        int[] yPoints = {
                endY + (int) (ny * arrowSize),
                endY - (int) (ny * arrowSize) + (int) (py * arrowSize),
                endY - (int) (ny * arrowSize) - (int) (py * arrowSize)
        };
        g.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawUI(Graphics g) {
        // 委派靜態類別繪製左上角排行榜
        Rank.drawRanking(g, gameManager.balls, gameManager.currentMode);

        if (btnStart != null) {
            if (gameManager.currentState == GameState.SETUP) {
                btnStart.text = "Start";
            } else {
                btnStart.text = "Restart";
            }
        }

        if (btnMode != null) btnMode.draw(g);
        if (btnStart != null) btnStart.draw(g);
        if (btnRule != null) btnRule.draw(g);
        if (btnExit != null) btnExit.draw(g);

        drawSpeedIndicator(g);

        // 畫面正中間顯示結束文字
        if (gameManager.currentState == GameState.GAME_OVER) {
            g.setColor(Color.WHITE);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 50));
            g.drawString("GAME OVER", (int) gameManager.arenaCenterX - 150, (int) gameManager.arenaCenterY);
        }
    }

    // 繪製右上角的倍數狀態指示器 (與快進閃爍特效)
    private void drawSpeedIndicator(Graphics g) {
        if (gameManager.currentState == GameState.GAME_OVER || currentSpeedMultiplier <= 1.0) {
            return;
        }

        int uiX = getWidth() - 250; 
        int uiY = 50; 

        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 30));

        if (currentSpeedMultiplier > 1.0) {
            g.setColor(Color.CYAN);
        } else {
            g.setColor(Color.WHITE);
        }

        String speedStr = "x" + currentSpeedMultiplier;
        g.drawString(speedStr, uiX, uiY);

        // 利用 animationTick 計算箭頭亮起的順序，製造流水燈感
        int arrowStartX = uiX + 85;
        int step = (animationTick / 4) % 4;

        for (int i = 0; i < 3; i++) {
            if (currentSpeedMultiplier > 1.0) {
                if (i == step - 1 || (step == 0 && i == 2)) {
                    g.setColor(Color.CYAN.brighter());
                } else {
                    g.setColor(Color.CYAN.darker().darker());
                }
            } else {
                g.setColor(Color.DARK_GRAY); 
            }
            g.drawString(">", arrowStartX + (i * 20), uiY);
        }
    }
}
