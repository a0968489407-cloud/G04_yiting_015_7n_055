# Survivor

一款使用 Java Swing 製作的 2D 球類生存競技遊戲。玩家可以在圓形場地中放置多顆球，設定初始方向後開始遊戲。球會在場地內碰撞、反彈並留下線段，其他顏色的球碰到線段時會破壞該線段；當某顆球失去所有線段後就會淘汰，最後存活的顏色獲勝。

## 遊戲特色

- Java Swing 視窗遊戲，畫面大小為 1200 x 720。
- 圓形競技場，球會與牆面、其他球產生碰撞與反彈。
- 玩家可手動放置球，也可使用快捷鍵一次隨機生成 2 到 6 顆球。
- Classic 模式依照存活狀態與線段數進行排名。
- Item 模式會定期生成道具，讓比賽更有變化。
- 支援速度倍率調整，可加快遊戲節奏。
- 內建碰撞、道具、分裂、線段破壞等音效。

## 遊戲模式

| 模式 | 說明 |
| --- | --- |
| Classic | 基本生存模式。球反彈後留下線段，其他顏色的球可以破壞線段。失去所有線段的球會淘汰。 |
| Item | 道具模式。玩法與 Classic 類似，但場上會定期生成道具，球接觸道具後會獲得特殊效果。 |

## 操作方式

| 操作 | 功能 |
| --- | --- |
| 滑鼠左鍵 | 在 SETUP 階段放置球；再次點擊可設定該球的移動方向 |
| 滑鼠右鍵 | 在 SETUP 階段移除球或取消正在設定的球 |
| Start 按鈕 / Enter | 開始遊戲 |
| Restart 按鈕 / Enter | 遊戲中或結束後重新開始 |
| Classic / Item 按鈕 | 切換遊戲模式 |
| Space | 切換 Classic / Item 模式 |
| Rule 按鈕 / R | 顯示規則說明 |
| Exit 按鈕 / Esc | 離開遊戲 |
| 2 到 6 | 在 SETUP 階段隨機生成指定數量的球 |
| 方向鍵右 | 提高遊戲速度倍率，最高 x3.0 |
| 方向鍵左 | 降低遊戲速度倍率，最低 x1.0 |

## 道具說明

Item 模式中，場上會定期出現道具。道具附近會有吸引範圍，球靠近後會被吸向道具。

| 道具 | 圖片 | 效果 |
| --- | --- | --- |
| 加速 | `lightning.png` | 提高球的移動速度 |
| 減速 | `freeze.png` | 降低球的移動速度 |
| 分裂 | `cell.png` | 將一般球分裂成兩顆小球 |
| 生命 | `heart.png` | 增加球的生命次數 |

## 勝負規則

- 球碰到牆壁後會新增一條屬於自己的線段。
- 不同顏色的球碰到其他球的線段時，該線段會被破壞。
- 球進入遊戲後，如果沒有剩下任何線段，就會失去生命或被淘汰。
- 若球有額外生命，會先消耗生命並繼續遊戲。
- 當場上只剩下一種顏色仍存活時，遊戲結束。

## 專案結構

```text
survivor/
├── README.md
├── src/
│   ├── Game.java
│   ├── Display.java
│   ├── GameManager.java
│   ├── Ball.java
│   ├── Item.java
│   ├── Line.java
│   ├── Rank.java
│   ├── Btn.java
│   ├── SoundManager.java
│   └── Vector2D.java
├── pic/
│   ├── cell.png
│   ├── freeze.png
│   ├── heart.png
│   └── lightning.png
└── sound effect/
    ├── extralLife.wav
    ├── knife sound.wav
    ├── pool ball sound.wav
    ├── speedDown.wav
    ├── speedup.wav
    └── split.wav
```

## 執行需求

- JDK 8 或以上版本
- 可執行 Java Swing 的桌面環境

## 編譯與執行

請在 `JAVA project` 這一層資料夾執行指令，因為程式內的圖片與音效路徑會從這一層讀取 `survivor/pic` 與 `survivor/sound effect`。

```powershell
javac survivor\src\*.java
java -cp survivor\src Game
```

如果使用 macOS 或 Linux，可以改用：

```bash
javac survivor/src/*.java
java -cp survivor/src Game
```

## 注意事項

- 執行時請保留 `pic` 與 `sound effect` 資料夾，否則圖片或音效可能無法載入。
- 音效檔案名稱包含空格與大小寫差異，非 Windows 系統請確認檔名與程式中的路徑完全一致。
- `sound effect/edit file` 內是音效編輯用檔案，不是遊戲執行必需檔案。
