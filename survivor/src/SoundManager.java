import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import java.io.File;


public class SoundManager {
    private static Clip hitClip;
    private static Clip speedUpClip;
    private static Clip speedDownClip;
    private static Clip splitClip;
    private static Clip extraLifeClip;

    // 初始化並預先載入音效
    public static void init() {
        try {
            // 1. 載入撞牆音效 (Type 1)
            File soundFile = new File("survivor/src/pool ball sound.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            hitClip = AudioSystem.getClip();
            hitClip.open(audioIn);

            // 2. 載入加速道具音效 (Type 2)
            File speedUpFile = new File("survivor/src/speedup.wav"); 
            AudioInputStream speedUpIn = AudioSystem.getAudioInputStream(speedUpFile);
            speedUpClip = AudioSystem.getClip();
            speedUpClip.open(speedUpIn);

            // 3. 載入減速道具音效 (Type 3)
            File speedDownFile = new File("survivor/src/speedDown.wav"); 
            AudioInputStream speedDownIn = AudioSystem.getAudioInputStream(speedDownFile);
            speedDownClip = AudioSystem.getClip();
            speedDownClip.open(speedDownIn);

            // 4. 載入分裂道具音效 (Type 4)
            File splitFile = new File("survivor/src/split.wav"); 
            AudioInputStream splitIn = AudioSystem.getAudioInputStream(splitFile);
            splitClip = AudioSystem.getClip();
            splitClip.open(splitIn);

            // 5. 載入加命道具音效 (Type 5)
            File extraLifeFile = new File("survivor/src/extralLife.wav"); 
            AudioInputStream extraLifeIn = AudioSystem.getAudioInputStream(extraLifeFile);
            extraLifeClip = AudioSystem.getClip();
            extraLifeClip.open(extraLifeIn);

        } catch (Exception e) {
            System.out.println("音效載入失敗，請檢查路徑與檔案格式 (.wav)。錯誤：" + e.getMessage());
        }
    }

    // 播放碰撞音效
    public static void playHit() {
        if (hitClip == null) return;

        // 使用新執行緒播放，防止多顆球同時撞擊時造成主畫面卡頓
        new Thread(() -> {
            try {
                // 如果上一次的聲音還沒播完，先重置到開頭
                if (hitClip.isRunning()) {
                    hitClip.stop();
                }
                hitClip.setFramePosition(0); // 回到最前端
                hitClip.start();            // 開始播放
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 播放加速音效 (Type 2)
    public static void playSpeedUp() {
        playClip(speedUpClip);
    }

    // 播放減速音效 (Type 3)
    public static void playSpeedDown() {
        playClip(speedDownClip);
    }

    // 播放分裂音效 (Type 4)
    public static void playSplit() {
        playClip(splitClip);
    }

    // 播放加命音效 (Type 5)
    public static void playExtraLife() {
        playClip(extraLifeClip);
    }

    // 提取通用的播放邏輯，避免重複寫 Thread
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