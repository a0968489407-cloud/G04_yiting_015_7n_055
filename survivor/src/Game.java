import javax.swing.JFrame;

public class Game {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Ball Survival Game");
        Display display = new Display(1200, 720);
        
        frame.add(display);
        frame.pack(); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); 
        frame.setResizable(false); 
        frame.setVisible(true);
    }
} // test by 7n