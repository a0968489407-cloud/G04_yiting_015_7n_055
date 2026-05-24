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
    private static Clip gameOverClip; // 新增：遊戲結束音效
    private static Clip winClip; // 新增：勝利音效
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
            }

            // 2. 載入發射子彈音效
            File shootFile = new File("Space_Invader/Sound effect/shoot.wav"); 
            if (shootFile.exists()) {
                AudioInputStream shootIn = AudioSystem.getAudioInputStream(shootFile);
                shootClip = AudioSystem.getClip();
                shootClip.open(shootIn);
            }
            
            // 3. 載入爆炸音效
            File explodeFile = new File("Space_Invader/Sound effect/explode.wav"); 
            if (explodeFile.exists()) {
                AudioInputStream explodeIn = AudioSystem.getAudioInputStream(explodeFile);
                explosionClip = AudioSystem.getClip();
                explosionClip.open(explodeIn);
            }

            // 4. 載入遊戲結束音效
            File gameoverFile = new File("Space_Invader/Sound effect/gameover.wav"); 
            if (gameoverFile.exists()) {
                AudioInputStream gameoverIn = AudioSystem.getAudioInputStream(gameoverFile);
                gameOverClip = AudioSystem.getClip();
                gameOverClip.open(gameoverIn);
            }

            // === 新增：載入勝利音效 ===
            File winFile = new File("Space_Invader/Sound effect/win.wav"); 
            if (winFile.exists()) {
                winClip = AudioSystem.getClip();
                winClip.open(AudioSystem.getAudioInputStream(winFile));
            }

            File getItemFile = new File("Space_Invader/Sound effect/getitem.wav"); 
            if (getItemFile.exists()) {
                powerUpClip = AudioSystem.getClip();
                powerUpClip.open(AudioSystem.getAudioInputStream(getItemFile));
            }
            
        } catch (Exception e) {
            System.out.println("音效載入失敗，錯誤：" + e.getMessage());
        }
    }

    // --- 播放與停止背景音樂 (BGM) ---
    public static void playBGM() {
        if (bgmClip == null) return;
        if (bgmClip.isRunning()) bgmClip.stop();
        bgmClip.setFramePosition(0); 
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY); 
    }

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

    public static void playGameOver() {
        playClip(gameOverClip);
    }

    public static void playWin() {
        playClip(winClip);
    }

    public static void playPowerUp() {
        playClip(powerUpClip);
    }

    // --- 提取通用的單次播放邏輯 (使用新執行緒防卡頓) ---
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