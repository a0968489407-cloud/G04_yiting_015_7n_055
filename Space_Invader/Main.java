//package Space_Invader;

import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invader");
        frame.setSize(800, 800);
        frame.setLocation(150, 50);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new GamePanel());
        frame.setVisible(true);

        
    }
}
