# Space Invader

一款使用 Java Swing 製作的 2D 太空射擊遊戲。玩家操控太空船左右移動、發射子彈擊退外星人，並透過道具與補給撐過多波攻勢。

## 遊戲特色

- Java Swing 視窗遊戲，畫面大小為 800 x 800。
- 星星背景、外星人爆炸效果、UFO、生命值與彈藥 UI。
- 共 5 波敵人，敵人會隨波數增加變得更密集、更有威脅。
- 多種外星人類型，包含一般敵人、快速敵人、追蹤型敵人、高血量敵人與會發射子彈的敵人。
- UFO 隨機出現，擊落後可獲得分數與道具。
- 內建背景音樂與射擊、爆炸、獲勝、失敗、取得道具等音效。

## 操作方式

| 按鍵 | 功能 |
| --- | --- |
| Enter | 開始遊戲 / 遊戲結束後重新開始 |
| Left Arrow | 向左移動 |
| Right Arrow | 向右移動 |
| Space | 發射子彈 |
| Z | 使用道具欄中的第一個道具 |
| R | 回到開始畫面 |

## 道具說明

| 道具 | 效果 |
| --- | --- |
| 護盾 | 暫時降低受到的傷害 |
| 冰凍 | 暫時停止外星人移動 |
| 散彈 | 暫時一次發射三發子彈 |
| 彈藥補給 | 立即補充彈藥 |

道具會存放在右下角的道具欄，最多可持有 3 個，使用時會依照取得順序消耗。

## 專案結構

```text
Space_Invader/
├── main/
│   ├── Main.java
│   ├── GamePanel.java
│   ├── Shooter.java
│   ├── Alien.java
│   ├── Bullet.java
│   ├── PowerUp.java
│   ├── UFO.java
│   ├── Star.java
│   └── SoundManager.java
├── pic/
│   ├── spaceship.png
│   ├── shield.png
│   └── ice.png
└── sound effect/
    ├── bgm.wav
    ├── shoot.wav
    ├── explode.wav
    ├── hit.wav
    ├── getitem.wav
    ├── gameover.wav
    └── win.wav
```

## 執行需求

- JDK 8 或以上版本
- 可執行 Java Swing 的桌面環境

## 編譯與執行

請在 `JAVA project` 這一層資料夾執行指令，因為程式內的圖片與音效路徑會從這一層讀取 `Space_Invader/pic` 與 `Space_Invader/sound effect`。

```powershell
javac Space_Invader\main\*.java
java -cp Space_Invader main.Main
```

如果使用 macOS 或 Linux，可以改用：

```bash
javac Space_Invader/main/*.java
java -cp Space_Invader main.Main
```

## 遊戲目標

擊敗每一波外星人並盡量保住生命值。通過第 5 波後即可獲勝；若生命值歸零，遊戲結束。

## 注意事項

- 執行時請保留 `pic` 與 `sound effect` 資料夾，否則圖片或音效可能無法載入。
- 若音效沒有播放，請確認 `.wav` 檔案仍在 `Space_Invader/sound effect` 資料夾中。
- 若在 macOS 或 Linux 執行，請留意資料夾大小寫。程式中的音效路徑使用 `Space_Invader/Sound effect`，目前資料夾名稱是 `sound effect`；非 Windows 系統可能需要統一大小寫。
