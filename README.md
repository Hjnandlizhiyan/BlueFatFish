<div align="center">

<img src="images/icon.jpg?v=20260911" alt="蓝色大肥鱼" width="120" />

# 🐟 蓝色大肥鱼 (BlueFatFish)

**一款面向 DeepSeek 用户的轻量 Android 助手**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Hjnandlizhiyan/BlueFatFish/releases)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge&logo=apache&logoColor=white)](LICENSE)
[![Release](https://img.shields.io/github/v/release/Hjnandlizhiyan/BlueFatFish?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Hjnandlizhiyan/BlueFatFish/releases)

</div>

---

## ✨ 功能亮点

| 功能 | 说明 |
|------|------|
| 💰 **余额查询** | 查询 DeepSeek 账户余额与历史趋势 |
| 🔔 **低余额提醒** | 余额低于设定阈值时自动通知 |
| 🧭 **价格一览** | 自动爬取 DeepSeek 官网价格规则，实时判断当前处于高峰 / 空闲计费时段，并展示各模型价格 |
| 💬 **AI 聊天** | 支持深度思考模式，畅享智能对话 |
| 🔑 **多 API Key 管理** | 支持多 API Key 加密本地管理 |
| 🌗 **主题切换** | 浅色 / 深色主题自由切换 |
| 🔒 **安全加密** | 所有 Key 本地加密存储，安全无忧 |

---

## 📸 应用截图

### 💰 余额管理

<div align="center">
  <img src="images/show_01.jpg?v=20260911" alt="余额查询" width="30%" />
  <img src="images/show_02.jpg?v=20260911" alt="历史趋势" width="30%" />
  <img src="images/show_03.jpg?v=20260911" alt="余额详情" width="30%" />
</div>

### 🔔 低余额通知

<div align="center">
  <img src="images/show_04.jpg?v=20260911" alt="低余额提醒" width="30%" />
</div>

### 🧭 价格一览

<div align="center">
  <img src="images/show_08.jpg?v=20260911" alt="价格一览 · 高峰/空闲时段判定" width="30%" />
  <img src="images/show_09.jpg?v=20260911" alt="价格一览 · 模型价格参考" width="30%" />
</div>

> 🔄 **规则自动更新**：收费时段规则、模型名称与价格会自动从 DeepSeek 官网爬取并缓存到本地，实时判断当前处于高峰还是空闲计费时段；无网络或官网改版时自动回退内置数据，离线同样可用。

### 💬 AI 聊天

<div align="center">
  <img src="images/show_06.jpg?v=20260911" alt="聊天页面" width="30%" />
  <img src="images/show_07.jpg?v=20260911" alt="历史聊天" width="30%" />
</div>

### 🌙 深色模式

<div align="center">
  <img src="images/show_05.jpg?v=20260911" alt="深色模式" width="30%" />
</div>

---

## 🗂️ 项目结构

> 技术栈：Kotlin + Jetpack Compose (Material 3) + Navigation + DataStore + OkHttp，单模块 `app`。

```text
BlueFatFish
├── app/
│   ├── build.gradle.kts            # app 模块构建脚本
│   ├── release/                    # 基线配置文件 (Baseline Profiles)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/bigfatfish/release/
│       │   │   ├── MainActivity.kt          # 应用入口
│       │   │   ├── data/                    # 数据层
│       │   │   │   ├── Serialization.kt
│       │   │   │   ├── local/               # DataStore 本地持久化
│       │   │   │   ├── model/               # 数据模型
│       │   │   │   ├── notification/        # 低余额通知
│       │   │   │   └── remote/              # 网络请求与官网解析
│       │   │   ├── navigation/              # 页面路由与导航事件
│       │   │   └── ui/                      # 界面层（按页面分包）
│       │   ├── keepRules/                   # 混淆保留规则
│       │   └── res/                         # 图标、主题、字符串等资源
│       ├── test/                            # 单元测试
│       └── androidTest/                     # 仪器测试
├── gradle/wrapper/                 # Gradle Wrapper
├── build.gradle.kts                # 根构建脚本
├── settings.gradle.kts
├── gradle.properties
├── version.properties              # 版本号配置
├── gradlew / gradlew.bat
├── LICENSE
└── .gitignore
```

### 核心模块说明

| 模块 | 职责 |
|------|------|
| `data/local` | DataStore 本地持久化：API Key 加密存储、余额历史、聊天记录、设置项、价格规则缓存 |
| `data/model` | 数据模型定义：余额、聊天请求 / 响应、价格规则与模型价格 |
| `data/remote` | 网络层：DeepSeek 余额与聊天 API，以及官网价格页抓取与 HTML 解析 |
| `data/notification` | 低余额阈值提醒通知 |
| `navigation` | Compose Navigation 路由定义与导航事件总线 |
| `ui/home` | 首页仪表盘：余额卡片与各功能入口 |
| `ui/balance` | 余额历史趋势与详情 |
| `ui/price` | 价格一览：高峰 / 空闲时段判定，自动爬取官网价格规则 |
| `ui/chat`、`ui/history` | AI 聊天与历史对话 |
| `ui/keylist`、`ui/settings` | 多 API Key 管理与应用设置 |
| `ui/groupbuy` | 玩偶参团入口（跳转外部问卷） |
| `ui/theme` | 浅色 / 深色主题与配色 |

---

## 📥 下载

点击右侧 **[Releases](https://github.com/Hjnandlizhiyan/BlueFatFish/releases)**，下载最新版 APK。

> ⚠️ 本应用仅发布在 GitHub，请勿从其他渠道下载以免安全风险。

---

## 📖 关于完整版

> **为什么这里的才是完整版？**
>
> 依据中国相关法规，不具备人工智能生成服务资质的个人，不得在国内网站或应用商城发布含 AI 生成内容的软件（包括调用 AI 公司 API 进行内容生成的场景）。因此，本应用仅在 GitHub 发布完整功能版本。

---

## 📞 联系

| 渠道 | 信息 |
|------|------|
| 🐧 **官方 QQ 群** | **305402575** |

---

<div align="center">

### ⭐ 如果这个项目对你有帮助，欢迎点亮 Star！

</div>