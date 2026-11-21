# Duo 前端

给 Retroid Pocket Duo（上下双屏安卓掌机）做的 3DS 风格游戏前端。

- **上屏**（1920×1080）：选中平台时显示主机图；选中游戏时显示模糊封面背景、游戏 logo、平台与模拟器标签、简介，带缓慢的 3D 摇摆。
- **下屏**（1280×960，Android `Presentation`）：平台文件夹和可缩放的游戏图标网格，支持手柄和触屏。
- **直接复用天马G（Pegasus）的游戏目录**：读取 `Roms/<平台>/metadata.pegasus.txt`，沿用其中的 `launch:` 启动命令和 `media/` 素材，不改动任何文件。
- **游戏图标从 ROM 里读**：NDS 读文件头的 banner 图标，3DS 读 `.cci/.3ds` 中 ExeFS 的 SMDH 图标（需已解密）；GB/GBA 用天马的封面。
- **按游戏 / 按平台选模拟器**（Y 键）：3DS 可选 AzaharPlus 或 Citra MMJ，GB/GBA 可选不同的 RetroArch 核心。

## 按键

| 键 | 作用 |
|---|---|
| 十字键 / 左摇杆 | 移动 |
| A | 打开 / 开始游戏 |
| B | 返回 |
| L / R | 切换平台 |
| X | 图标大小 |
| Y | 选择模拟器 |
| SELECT | 排序（默认 / 名称） |

## 编译

不需要 Android Studio 或 Gradle，只用 Android SDK 的命令行工具（build-tools 35、platform android-35、JDK 21）：

```powershell
powershell -File build.ps1            # 生成 DuoLauncher.apk
powershell -File build.ps1 -Install   # 并通过 adb 安装到掌机、授予文件访问权限、启动
```

第一次编译时会自动生成调试签名 `debug.keystore`（不入库）。

## 主机图片

`assets/consoles/<平台>.png` 会被打包进 APK，用于上屏的主机大图和下屏文件夹；缺图的平台用代码画的示意图。
现有照片来自 Wikimedia Commons，摄影 Evan-Amos，公有领域（Public domain）：
`Nintendo-3DS-AquaOpen.png`、`Nintendo-Game-Boy-Color-FL.png`。白底的照片由脚本把背景抠成透明并缩到 1100px。

## 启动方式备忘

- AzaharPlus 只接受它已获授权的文件夹下的 tree URI：
  `content://com.android.externalstorage.documents/tree/<卷>%3ARoms%2F3DS/document/<卷>%3ARoms%2F3DS%2F<文件名>`
- Citra MMJ：`org.citra.emu/org.citra.emu.ui.EmulationActivity`，`-e GamePath <路径>`
- DraStic、RetroArch：沿用天马G元数据里的命令。
