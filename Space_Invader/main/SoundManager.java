package main;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import java.io.File;

public class SoundManager {
    // 宣告靜態的 Clip 物件參考，用於保存各音效的音訊串流處理實例
    private static Clip bgmClip;
    private static Clip shootClip;
    private static Clip explosionClip;
    private static Clip gameOverClip; 
    private static Clip winClip; 
    private static Clip powerUpClip;

    public static void init() {
        try {
            // 建立指向實體硬碟 WAV 檔的 File 物件
            File bgmFile = new File("Space_Invader/Sound effect/bgm.wav");
            // 若確認檔案存在，透過 AudioSystem 取得串流並分配給新的 Clip 物件開啟
            if (bgmFile.exists()) {
                AudioInputStream bgmIn = AudioSystem.getAudioInputStream(bgmFile);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(bgmIn);
            }

            File shootFile = new File("Space_Invader/Sound effect/shoot.wav"); 
            if (shootFile.exists()) {
                AudioInputStream shootIn = AudioSystem.getAudioInputStream(shootFile);
                shootClip = AudioSystem.getClip();
                shootClip.open(shootIn);
            }
            
            File explodeFile = new File("Space_Invader/Sound effect/explode.wav"); 
            if (explodeFile.exists()) {
                AudioInputStream explodeIn = AudioSystem.getAudioInputStream(explodeFile);
                explosionClip = AudioSystem.getClip();
                explosionClip.open(explodeIn);
            }

            File gameoverFile = new File("Space_Invader/Sound effect/gameover.wav"); 
            if (gameoverFile.exists()) {
                AudioInputStream gameoverIn = AudioSystem.getAudioInputStream(gameoverFile);
                gameOverClip = AudioSystem.getClip();
                gameOverClip.open(gameoverIn);
            }

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

    public static void playBGM() {
        if (bgmClip == null) return;
        // 若當前正播放則先停止以防重疊
        if (bgmClip.isRunning()) bgmClip.stop();
        // 將播放進度指標歸零 (回到檔頭)
        bgmClip.setFramePosition(0); 
        // 使用 LOOP_CONTINUOUSLY 常數讓音檔完整播放後自動循環
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY); 
    }

    public static void stopBGM() {
        // 檢查指標與狀態，強行中斷音訊串流輸出
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    // 將外部需要觸發音效的公開方法橋接至私有的底層 playClip 處理函式
    public static void playShoot() { playClip(shootClip); }
    public static void playExplosion() { playClip(explosionClip); }
    public static void playGameOver() { playClip(gameOverClip); }
    public static void playWin() { playClip(winClip); }
    public static void playPowerUp() { playClip(powerUpClip); }

    private static void playClip(Clip clip) {
        if (clip == null) return;
        // 建立獨立的 Thread (執行緒)，傳入 Lambda 表達式作為執行區塊
        // 這樣可讓音效播放 IO 操作與主程式 GameLoop (UI) 分離，防止因系統讀取而產生畫面卡頓
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