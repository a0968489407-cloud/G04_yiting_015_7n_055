
// === src/Display.java ===
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

    private int currentMouseX = 0;
    private int currentMouseY = 0;

    // --- 新增：倍率控制與動態箭頭計時器 ---
    public static double currentSpeedMultiplier = 1.0;
    private int animationTick = 0; // 用於控制 >>> 箭頭的動態閃爍

    public Display(int width, int height) {
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        this.setPreferredSize(new Dimension(width, height));

        double centerX = (double) width / 2;
        double centerY = (double) height / 2;
        double arenaRadius = (double) height / 2 * 0.9;
        gameManager = new GameManager(centerX, centerY, arenaRadius);

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

        timer = new Timer(16, this);
        timer.start();
    }

    private void setupInputListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (btnMode.contains(mx, my)) {
                        toggleGameMode();
                    } else if (btnStart.contains(mx, my)) {
                        handleStartRestart();
                    } else if (btnRule.contains(mx, my)) {
                        showRules();
                    } else if (btnExit.contains(mx, my)) {
                        System.exit(0);
                    } else {
                        if (gameManager.currentState != GameState.SETUP)
                            return;

                        if (gameManager.pendingBall == null) {
                            if (gameManager.balls.size() >= 6)
                                return;
                            if (gameManager.isInsideArena(mx, my)) {
                                Color newColor = gameManager.getAvailableColor();
                                Ball newBall = new Ball(mx, my, newColor, false);
                                gameManager.balls.add(newBall);
                                gameManager.pendingBall = newBall;
                            }
                        } else {
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
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (gameManager.currentState == GameState.SETUP) {
                        boolean ballRemoved = false;
                        for (int i = gameManager.balls.size() - 1; i >= 0; i--) {
                            Ball b = gameManager.balls.get(i);
                            double dx = mx - b.pos.x;
                            double dy = my - b.pos.y;
                            if (Math.sqrt(dx * dx + dy * dy) <= b.radius + 5) {
                                if (b == gameManager.pendingBall)
                                    gameManager.pendingBall = null;
                                gameManager.balls.remove(i);
                                ballRemoved = true;
                                break;
                            }
                        }
                        if (!ballRemoved && gameManager.pendingBall != null) {
                            gameManager.balls.remove(gameManager.pendingBall);
                            gameManager.pendingBall = null;
                        }
                    }
                }
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                currentMouseX = e.getX();
                currentMouseY = e.getY();
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

        //
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE)
                    toggleGameMode();
                else if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    handleStartRestart();
                else if (e.getKeyCode() == KeyEvent.VK_R)
                    showRules();
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);

                // --- 新增：按下鍵盤右鍵，開啟所有球的加速 ---
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    currentSpeedMultiplier += 0.5;
                    if (currentSpeedMultiplier > 3.0) {
                        currentSpeedMultiplier = 3.0;
                    }
                }
                // --- 修改處：按左鍵減 0.5 倍速，最低 1.0 ---
                else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    currentSpeedMultiplier -= 0.5;
                    if (currentSpeedMultiplier < 1.0) {
                        currentSpeedMultiplier = 1.0;
                    }
                }
            }
        });

    }

    private void handleStartRestart() {
        if (gameManager.currentState == GameState.SETUP) {
            // 至少兩顆球才能開始
            if (gameManager.balls.size() >= 2) {
                gameManager.currentState = GameState.PLAYING;
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please add at least 2 balls to start!",
                        "Not Enough Balls",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            gameManager.resetGame();
            currentSpeedMultiplier = 1.0; // 重置倍率
        }
    }

    private void toggleGameMode() {
        if (gameManager.currentMode == GameMode.CLASSIC) {
            gameManager.currentMode = GameMode.ITEM_MODE;
            btnMode.text = "Item";
        } else {
            gameManager.currentMode = GameMode.CLASSIC;
            btnMode.text = "Classic";
        }
    }

    private void showRules() {
        String rules = "Game Rules:\n" +
                "1. Use left mouse button to add a ball or set direction.\n" +
                "2. Use right mouse button to remove a ball.\n" +
                "3. Press SPACE to switch game mode.\n" +
                "4. Press ENTER to start or restart the game.\n" +
                "5. Avoid colliding with other balls and stay within the arena.\n" +
                "6. Press ESC to exit the game.";
        JOptionPane.showMessageDialog(this, rules, "Game Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gameManager.update();

        // --- 修改處：讓動畫計時器累加時也乘以倍率，使動畫跟隨時間流速一起變快 ---
        animationTick += Display.currentSpeedMultiplier;

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. 畫出中央大圓圈
        g.setColor(Color.WHITE);
        g.drawOval((int) (gameManager.arenaCenterX - gameManager.arenaRadius),
                (int) (gameManager.arenaCenterY - gameManager.arenaRadius),
                (int) (gameManager.arenaRadius * 2), (int) (gameManager.arenaRadius * 2));

        if (gameManager.currentMode == GameMode.ITEM_MODE && gameManager.items != null) {
            for (Item item : gameManager.items) {
                if (!item.isAvailable)
                    continue;

                // ---- 調整點 1：縮的速度加快 ----
                // 將 animationTick 乘數從 2 加大到 4，讓波紋向內收縮的速度變為原本的 2 倍快！
                int baseOffset = (animationTick * 4) % 120;

                // ---- 調整點 2：用兩圈（前後錯開 60 像素） ----
                int radius1 = 120 - baseOffset; // 第一圈（外圈）
                int radius2 = 120 - ((baseOffset + 60) % 120); // 第二圈（內圈，錯開半個週期）

                // ---- 調整點 3：顏色再亮一點 ----
                // 將最後一個參數（不透明度 Alpha）從 30 調高到 100，光圈視覺上會亮非常多！
                g.setColor(new Color(0, 191, 255, 100)); // 改用亮深天藍 (DeepSkyBlue)，亮度更強

                // 畫出第一圈
                if (radius1 > 0) {
                    g.drawOval((int) item.pos.x - radius1, (int) item.pos.y - radius1,
                            radius1 * 2, radius1 * 2);
                }

                // 畫出第二圈
                if (radius2 > 0) {
                    g.drawOval((int) item.pos.x - radius2, (int) item.pos.y - radius2,
                            radius2 * 2, radius2 * 2);
                }

                // 靜態的最大邊界提示線（也稍微調亮一點點，Alpha 設為 40）
                g.setColor(new Color(100, 100, 100, 40));
                g.drawOval((int) item.pos.x - 120, (int) item.pos.y - 120, 240, 240);
            }
        }

        // 2. 畫出所有球與線
        if (gameManager.balls != null) {
            for (Ball b : gameManager.balls) {
                if (b.isDead)
                    continue;

                if (b.myLines != null) {
                    for (Line l : b.myLines)
                        l.draw(g);
                }
                b.draw(g);

                // 畫出已經設定好方向的箭頭
                if (gameManager.currentState == GameState.SETUP && b != gameManager.pendingBall) {
                    drawVelocityArrow(g, b);
                }
            }
        }

        // 畫出正在拖曳的灰色瞄準線
        if (gameManager.pendingBall != null) {
            g.setColor(Color.GRAY);
            g.drawLine((int) gameManager.pendingBall.pos.x,
                    (int) gameManager.pendingBall.pos.y,
                    currentMouseX, currentMouseY);
        }

        // 3. 畫出道具
        if (gameManager.items != null) {
            for (Item item : gameManager.items) {
                item.draw(g);
            }
        }

        drawUI(g);
    }

    // 畫出方向箭頭
    private void drawVelocityArrow(Graphics g, Ball b) {
        if (b.velocity.x == 0 && b.velocity.y == 0)
            return;

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
        // 排行榜
        Rank.drawRanking(g, gameManager.balls, gameManager.currentMode);

        // 動態改變 Start/Restart 文字
        if (btnStart != null) {
            if (gameManager.currentState == GameState.SETUP) {
                btnStart.text = "Start";
            } else {
                btnStart.text = "Restart";
            }
        }

        // 按鈕
        if (btnMode != null)
            btnMode.draw(g);
        if (btnStart != null)
            btnStart.draw(g);
        if (btnRule != null)
            btnRule.draw(g);
        if (btnExit != null)
            btnExit.draw(g);

        // --- 修改處：在右上角繪製動態倍率與箭頭指標 ---
        drawSpeedIndicator(g);

        // 遊戲結束文字
        if (gameManager.currentState == GameState.GAME_OVER) {
            g.setColor(Color.WHITE);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 50));
            g.drawString("GAME OVER", (int) gameManager.arenaCenterX - 150, (int) gameManager.arenaCenterY);
        }
    }

    // --- 新增方法：繪製右上角動態倍速特效 ---
    private void drawSpeedIndicator(Graphics g) {
        if (gameManager.currentState == GameState.GAME_OVER || currentSpeedMultiplier <= 1.0) {
            return;
        }

        int uiX = getWidth() - 250; // 右上角起點 X
        int uiY = 50; // 右上角起點 Y

        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 30));

        // 1. 根據是否加速決定顏色 (原速白色，加速用亮綠色/青色突出)
        if (currentSpeedMultiplier > 1.0) {
            g.setColor(Color.CYAN);
        } else {
            g.setColor(Color.WHITE);
        }

        // 2. 畫出文字，例如 "x1.5"、"x2.0"
        String speedStr = "x" + currentSpeedMultiplier;
        g.drawString(speedStr, uiX, uiY);

        // 3. 畫出三個動態流水燈箭頭 >>>
        int arrowStartX = uiX + 85;
        // 【優化點】原本是 animationTick / 10，改成 / 4 讓切換速度縮短到原本的 2.5 倍快！
        int step = (animationTick / 4) % 4;

        for (int i = 0; i < 3; i++) {
            // 如果是大於 1 倍速，讓箭頭有亮度的流動感；若為 1.0 倍則灰暗不閃爍
            if (currentSpeedMultiplier > 1.0) {
                if (i == step - 1 || (step == 0 && i == 2)) {
                    g.setColor(Color.CYAN.brighter());
                } else {
                    g.setColor(Color.CYAN.darker().darker());
                }
            } else {
                g.setColor(Color.DARK_GRAY); // 原速時顯示暗色靜態箭頭
            }
            g.drawString(">", arrowStartX + (i * 20), uiY);
        }
    }
}