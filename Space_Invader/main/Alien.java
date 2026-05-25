package main;
import java.awt.Color;
import java.awt.Graphics;

public class Alien {
    // 實體空間參數：X與Y座標、碰撞判定寬度與高度
    public int x, y, width = 40, height = 30;
    // 存活狀態，死亡後不再移動與處理碰撞
    public boolean isAlive = true;
    // 爆炸動畫進行幀數紀錄
    public int explosionTimer = 0;
    // 爆炸動畫最大幀數界限
    public final int MAX_EXPLOSION_TIME = 15;
    
    // 定義外星人種類，影響顏色、血量與移動模式
    public int type; 
    // 外星人當前生命值
    public int hp;   
    // 紫色外星人專用的射擊計數器
    public int shootTimer = 0; 

    public Alien(int type) {
        this.type = type;
        // 使用三元運算子判定：類型 3 初始血量為 2，其餘為 1
        this.hp = (type == 3) ? 2 : 1; 
    }

    void draw(Graphics g) {
        // 依據是否存活選擇呼叫繪製正常外觀方法或爆炸特效方法
        if (isAlive) {
            drawNormal(g);
        } else if (explosionTimer < MAX_EXPLOSION_TIME) {
            drawExplosion(g);
            // 每幀推進一次爆炸動畫計時器
            explosionTimer++;
        }
    }

    void drawExplosion(Graphics g) {
        g.setColor(Color.ORANGE);
        // 使用自身固定座標相加作為隨機種子，確保每幀繪製的粒子運動軌跡有一致的推移方向
        java.util.Random rand = new java.util.Random(x + y);
        for (int i = 0; i < 10; i++) {
            // 利用 explosionTimer 將亂數產生的範圍隨時間向外擴大，製造粒子散射視覺效果
            int offsetX = rand.nextInt(40) - 20 + (rand.nextInt(5) * explosionTimer / 2);
            int offsetY = rand.nextInt(30) - 15 + (rand.nextInt(5) * explosionTimer / 2);
            // 在計算後的偏移位置畫出 3x3 像素的方塊
            g.fillRect(x + 20 + offsetX, y + 15 + offsetY, 3, 3);
        }
    }

    void drawNormal(Graphics g) {
        Color bodyColor;
        Color eyeColor = Color.BLACK;

        // 依據 type 賦予不同種類特定的身體顏色與眼睛顏色
        switch(type) {
            case 1: bodyColor = Color.RED; eyeColor = Color.YELLOW; break;
            case 2: bodyColor = Color.BLUE; eyeColor = Color.WHITE; break;
            case 3: bodyColor = Color.GRAY; eyeColor = Color.RED; break;
            case 4: bodyColor = new Color(153, 50, 204); eyeColor = Color.GREEN; break; 
            default: bodyColor = Color.GREEN; break; 
        }

        // 若為類型 3 且已經受傷 (hp從2降為1)，將身體顏色轉深作為視覺回饋
        if (type == 3 && hp == 1) {
            bodyColor = Color.DARK_GRAY;
        }

        // 以下運用 fillRect 繪製像素風格的幾何塊體構成外星人外觀
        g.setColor(bodyColor);
        g.fillRect(x + 5, y + 10, 30, 15); // 主體
        g.fillRect(x + 10, y + 5, 5, 5);   // 左觸角
        g.fillRect(x + 25, y + 5, 5, 5);   // 右觸角
        g.fillRect(x + 5, y + 25, 5, 5);   // 左腳
        g.fillRect(x + 30, y + 25, 5, 5);  // 右腳
        g.fillRect(x + 15, y + 25, 10, 5); // 中間底盤
        
        g.setColor(eyeColor);
        g.fillRect(x + 10, y + 12, 5, 5);  // 左眼
        g.fillRect(x + 25, y + 12, 5, 5);  // 右眼
    }

    public void move(int direction, int wave, int targetX) {
        if (!this.isAlive) return;

        if (type == 1) {
            // 類型 1 (紅色)：強制 Y 軸向下加 2；X 軸速度由 wave 決定，並加入振幅為 3.0 的 Sin 函數產生波浪軌跡
            this.y += 2; 
            double baseSpeed = 5 + wave;
            double oscillator = Math.sin(System.currentTimeMillis() / 200.0 + (y * 0.1)) * 3.0;
            this.x += (direction * baseSpeed) + oscillator;
            
        } else if (type == 2) {
            // 類型 2 (藍色)：強制 Y 軸向下加 2；X 軸無視 direction，單純比較自身與目標玩家 X 座標進行追蹤逼近
            this.y += 2;
            if (this.x < targetX) this.x += 4 + (wave * 0.5);
            else if (this.x > targetX) this.x -= 4 + (wave * 0.5);
            
        } else {
            // 其他類型：依賴方向參數左右移動，基礎速度與波浪振幅均隨著 wave 參數放大，增加難度
            double baseSpeed = 3 + (wave * 0.5);
            double waveAmplitude = 3.0 + (wave * 0.8);
            // 結合系統時間與 y 座標作為 sin 函數角度輸入，使個體間不會完全同步波浪
            double oscillator = Math.sin(System.currentTimeMillis() / 250.0 + (y * 0.05)) * waveAmplitude;
            this.x += (direction * baseSpeed) + oscillator;
        }
    }
}