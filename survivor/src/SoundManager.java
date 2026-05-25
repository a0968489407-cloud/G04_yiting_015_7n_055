import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import java.io.File;

public class SoundManager {
    // 建立各音效的串流實例
    private static Clip hitClip;
    private static Clip speedUpClip;
    private static Clip speedDownClip;
    private static Clip splitClip;
    private static Clip extraLifeClip;
    private static Clip knifeClip;

    public static void init() {
        try {
            // 讀取並開啟撞球碰撞音效
            File soundFile = new File("survivor/sound effect/pool ball sound.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            hitClip = AudioSystem.getClip();
            hitClip.open(audioIn);

            // 讀取加速道具音效
            File speedUpFile = new File("survivor/sound effect/speedup.wav"); 
            AudioInputStream speedUpIn = AudioSystem.getAudioInputStream(speedUpFile);
            speedUpClip = AudioSystem.getClip();
            speedUpClip.open(speedUpIn);

            // 讀取減速道具音效
            File speedDownFile = new File("survivor/sound effect/speedDown.wav"); 
            AudioInputStream speedDownIn = AudioSystem.getAudioInputStream(speedDownFile);
            speedDownClip = AudioSystem.getClip();
            speedDownClip.open(speedDownIn);

            // 讀取分裂道具音效
            File splitFile = new File("survivor/sound effect/split.wav"); 
            AudioInputStream splitIn = AudioSystem.getAudioInputStream(splitFile);
            splitClip = AudioSystem.getClip();
            splitClip.open(splitIn);

            // 讀取加命道具音效
            File extraLifeFile = new File("survivor/sound effect/extralLife.wav"); 
            AudioInputStream extraLifeIn = AudioSystem.getAudioInputStream(extraLifeFile);
            extraLifeClip = AudioSystem.getClip();
            extraLifeClip.open(extraLifeIn);

            // 讀取斷線音效
            File knifeFile = new File("survivor/sound effect/knife sound.wav"); 
            AudioInputStream knifeIn = AudioSystem.getAudioInputStream(knifeFile);
            knifeClip = AudioSystem.getClip();
            knifeClip.open(knifeIn);

        } catch (Exception e) {
            System.out.println("音效載入失敗，請檢查路徑與檔案格式 (.wav)。錯誤：" + e.getMessage());
        }
    }

    // 播放碰撞音效，為防止高頻率碰撞阻塞畫面，利用 Thread 非同步處理
    public static void playHit() {
        if (hitClip == null) return;
        new Thread(() -> {
            try {
                if (hitClip.isRunning()) {
                    hitClip.stop();
                }
                hitClip.setFramePosition(0); 
                hitClip.start();            
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void playSpeedUp() { playClip(speedUpClip); }
    public static void playSpeedDown() { playClip(speedDownClip); }
    public static void playSplit() { playClip(splitClip); }
    public static void playExtraLife() { playClip(extraLifeClip); }
    public static void playKnife() { playClip(knifeClip); }

    // 提取通用播放邏輯，重置播放點並於新執行緒啟動
    private static void playClip(Clip clip) {
        if (clip == null) return;
        new Thread(() -> {
            try {
                if (clip.isRunning()) clip.stop();
                clip.setFramePosition(0);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}