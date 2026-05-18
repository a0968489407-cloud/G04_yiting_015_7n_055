import javax.swing.JFrame;

public class Game {
    public static void main(String[] args) {
        // 在遊戲一開始，畫面還沒出來前先載入音效
        SoundManager.init();
        JFrame frame = new JFrame("Ball Survival Game");
        Display display = new Display(1200, 720);
        
        frame.add(display);
        frame.pack(); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); 
        frame.setResizable(false); 
        frame.setVisible(true);
    }
} 