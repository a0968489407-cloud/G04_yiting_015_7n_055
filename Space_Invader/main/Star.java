package main;
import java.awt.Color;
import java.awt.Graphics;

public class Star {
    // 座標與大小
    int x, y, size;
    // 保存該星星當前的 Color Alpha(透明度) 值
    int alpha;       
    // 每幀 alpha 的改變量 (正為變亮，負為變暗)
    int fadeSpeed;   
    // 每幀 Y 軸往下位移的像素數
    int fallSpeed;   

    Star(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        // 使用 java.util.Random 進行 0~254 亂數決定初始透明狀態
        this.alpha = new java.util.Random().nextInt(255); 
        // 給予 2~6 之間的亂數決定明暗變化的頻率速率
        this.fadeSpeed = 2 + new java.util.Random().nextInt(5); 
        // 給予 1~3 之間的亂數決定下墜速度，藉此在視覺上產生物件前後遠近的視差效果
        this.fallSpeed = 1 + new java.util.Random().nextInt(3); 
    }

    void twinkle() {
        // 對當前透明度進行線性加減
        alpha += fadeSpeed;
        // 檢查極值，若透明度小於等於50或大於等於255，則將 fadeSpeed 正負號反轉，產生回彈效果
        if (alpha <= 50 || alpha >= 255) {
            fadeSpeed *= -1;
        }
        // 使用 Math.max 與 Math.min 強制將數值限制在 50~255 範圍區間，防止丟出 IllegalArgumentException
        alpha = Math.max(50, Math.min(255, alpha));
    }

    void move() {
        // 利用初始決定的速度增量更新座標
        y += fallSpeed;
        // 檢查如果掉落至預設的視窗 Y 軸最底端 800 外
        if (y > 800) {
            // 則將 Y 強制拉回頂端 (0)
            y = 0;
            // 並且隨機分配一個全新的 X 座標，使下墜效果無窮迴圈且自然
            x = new java.util.Random().nextInt(800);
        }
    }

    void draw(Graphics g) {
        // 實例化帶有 Alpha 色版的 Color 物件，產生半透明筆刷繪製填滿圓形
        g.setColor(new Color(255, 255, 255, alpha)); 
        g.fillOval(x, y, size, size);
    }
}