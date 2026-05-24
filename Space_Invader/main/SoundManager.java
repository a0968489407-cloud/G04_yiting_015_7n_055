package main;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import java.io.File;

public class SoundManager {
    // 宣告各種類型的 Clip
    private static Clip bgmClip;
    private static Clip shootClip;
    private static Clip explosionClip;
    private static Clip powerUpClip;

    // 初始化並預先載入所有音效
    public static void init() {
        try {
            // 1. 載入背景音樂 (BGM)
            File bgmFile = new File("Space_Invader/Sound effect/bgm.wav");
            if (bgmFile.exists()) {
                AudioInputStream bgmIn = AudioSystem.getAudioInputStream(bgmFile);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(bgmIn);
            } else {
                System.out.println("找不到 BGM，請確認路徑：" + bgmFile.getPath());
            }

            /* // 2. 預留：載入發射子彈音效
            File shootFile = new File("Space_Invader/Sound effect/shoot.wav"); 
            if (shootFile.exists()) {
                AudioInputStream shootIn = AudioSystem.getAudioInputStream(shootFile);
                shootClip = AudioSystem.getClip();
                shootClip.open(shootIn);
            }
            
            // 3. 預留：載入爆炸音效
            File explosionFile = new File("Space_Invader/Sound effect/explosion.wav"); 
            if (explosionFile.exists()) {
                AudioInputStream explosionIn = AudioSystem.getAudioInputStream(explosionFile);
                explosionClip = AudioSystem.getClip();
                explosionClip.open(explosionIn);
            }
            */

        } catch (Exception e) {
            System.out.println("音效載入失敗，錯誤：" + e.getMessage());
        }
    }

    // --- 播放背景音樂 (BGM) ---
    public static void playBGM() {
        if (bgmClip == null) return;
        
        // 背景音樂不需要一直開新 Thread，直接設定循環即可
        if (bgmClip.isRunning()) {
            bgmClip.stop();
        }
        bgmClip.setFramePosition(0); 
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY); // 無限循環播放
    }

    // --- 停止背景音樂 ---
    public static void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    // --- 播放單次音效的公開方法 ---
    public static void playShoot() {
        playClip(shootClip);
    }

    public static void playExplosion() {
        playClip(explosionClip);
    }

    public static void playPowerUp() {
        playClip(powerUpClip);
    }

    // --- 提取通用的單次播放邏輯 (使用新執行緒防卡頓) ---
    private static void playClip(Clip clip) {
        if (clip == null) return;
        new Thread(() -> {
            try {
                if (clip.isRunning()) clip.stop(); // 如果還在播，先停止
                clip.setFramePosition(0);          // 回到音效開頭
                clip.start();                      // 播放
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}