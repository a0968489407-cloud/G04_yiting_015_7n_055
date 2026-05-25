import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

public class Btn {
    // 按鈕的座標與寬高
    public int x, y, width, height;
    // 按鈕上顯示的文字
    public String text;
    // 紀錄滑鼠是否正懸停於按鈕上方
    public boolean isHovered;

    public Btn(int x, int y, int width, int height, String text) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.text = text; this.isHovered = false;
    }

    // 檢查傳入的滑鼠座標是否落在按鈕的矩形範圍內
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void draw(Graphics g) {
        // 根據懸停狀態切換按鈕文字與外框顏色 (懸停時變深灰，平時為白)
        if (isHovered) g.setColor(Color.DARK_GRAY);
        else g.setColor(Color.WHITE);

        g.setFont(new Font("New Times Roman", Font.BOLD, 50));
        // 使用 FontMetrics 來計算字串繪製時的確切像素寬度與高度，以便將文字完美置中
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        
        g.drawString(text, textX, textY);
    }
}