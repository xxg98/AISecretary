# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于 unibest 框架的跨平台移动端应用（KaiLei AI 秘书），支持 H5、微信小程序、APP 三端。技术栈：uni-app 3 + Vue 3 + TypeScript + Vite 5 + UnoCSS + wot-ui + alova + Pinia。

## 常用命令

```bash
# 开发（开发输出到 dist/dev/<platform>）
pnpm dev              # 运行 H5 开发服务器（默认端口 9000），等同于 pnpm dev:h5
pnpm dev:mp           # 运行微信小程序开发，输出 dist/dev/mp-weixin
pnpm dev:app          # 运行 APP 开发，输出 dist/dev/app
pnpm dev:h5:ssr       # 运行 H5 SSR 模式

# 构建（生产输出到 dist/build/<platform>）
pnpm build:h5          # 构建 H5，输出 dist/build/h5
pnpm build:mp          # 构建微信小程序，输出 dist/build/mp-weixin
pnpm build:app         # 构建 APP，输出 dist/build/app

# 代码质量
pnpm lint              # ESLint 检查
pnpm lint:fix          # ESLint 自动修复
pnpm type-check        # TypeScript 类型检查（vue-tsc --noEmit）

# 环境模式（test / production）
pnpm dev:mp:test       # 等同于 pnpm dev:mp --mode test
pnpm build:mp:prod     # 等同于 pnpm build:mp --mode production

# API 类型生成
pnpm openapi           # 根据 OpenAPI 文档生成 TS 类型（openapi-ts）

# 微信小程序
pnpm upload:mp         # 上传微信小程序代码（miniprogram-ci）
SKIP_OPEN_DEVTOOLS=true pnpm dev:mp  # 跳过自动打开微信开发者工具

# 版本管理
pnpm upload:changeset  # 生成 changeset 并更新版本
```

## 核心架构

### 约定式路由

页面路由由文件系统自动生成，无需手动配置 `pages.json`。页面文件放在 `src/pages/` 下，文件名即路由路径。使用 `definePage` 宏设置页面标题等元信息（必须放在 `<script setup>` 最顶部）：

```vue
<script setup lang="ts">
definePage({ navigationBarTitleText: '页面标题' })
// 其他逻辑...
</script>
```

分包配置在 `vite.config.ts` 的 `UniPages` 插件中通过 `subPackages` 指定。

### SFC 组件结构规范

`<script setup lang="ts">` → `<template>` → `<style scoped>` 的顺序不可变。

### 根组件与启动流程

- `src/main.ts` — 入口，创建 SSR App，注册 store、路由拦截器、请求拦截器
- `src/App.ku.vue` — 全局根组件，包含 `<KuRootView />`（渲染当前页面）和条件性的 `<FgTabbar />`
- `src/App.vue` — `App.ku.vue` 的外层包裹（全局样式、启动逻辑）

### HTTP 请求层（双轨制）

项目同时存在两套 HTTP 体系：

1. **简单请求拦截器** (`src/http/http.ts` + `interceptor.ts`)：通过 `uni.addInterceptor('request', ...)` 全局拦截 uni.request，自动拼接 baseUrl、注入 token
2. **alova 实例** (`src/http/alova.ts`)：使用 alova 库，支持动态域名（`DEFAULT` / `SECONDARY`）、token 自动刷新、统一错误处理

API 接口定义在 `src/api/`，按功能模块拆分文件。

### 登录与路由拦截

- `src/router/config.ts` — 登录策略配置（黑名单/白名单模式 `LOGIN_STRATEGY`）、登录页路径、排除列表
- `src/router/interceptor.ts` — 通过 `uni.addInterceptor` 拦截 `navigateTo`/`reLaunch`/`redirectTo`/`switchTab`，根据登录状态和策略决定放行或重定向到登录页
- 小程序默认走平台自带登录，不走 H5 登录逻辑

### 环境变量

环境变量文件统一放在 `./env/` 目录（非项目根目录）：
- `.env` — 公共配置
- `.env.development` / `.env.production` / `.env.test`

关键变量：`VITE_SERVER_BASEURL`（API 地址）、`VITE_APP_PROXY_ENABLE`（H5 代理开关）、`VITE_AUTH_MODE`（认证模式 single/double）、`VITE_WX_APPID`

### Tabbar 策略

`src/tabbar/config.ts` 中 `selectedTabbarStrategy` 控制三种模式：
- `NO_TABBAR (0)` — 无底部栏
- `NATIVE_TABBAR (1)` — 原生 tabbar，配置在 `nativeTabbarList`
- `CUSTOM_TABBAR (2)` — 自定义 tabbar（支持鼓包按钮、角色过滤），配置在 `customTabbarList`

### UnoCSS 与样式

- 预设：`presetUni`（uni-app 兼容）+ `presetIcons`（iconify + 本地 SVG）+ `presetLegacyCompat`（低端安卓兼容）
- 自定义规则：`p-safe` / `pt-safe` / `pb-safe`（安全区域 padding）
- 自定义快捷方式：`center` → `flex justify-center items-center`
- 动态图标需在 `safelist` 中注册
- CSS 预处理器使用 Sass（SCSS 语法），样式写在 `<style scoped>` 中

### 语音通话模块（WebSocket 实时通信）

语音通话是本项目的核心功能之一，通过 WebSocket 与后端实时交互，实现语音采集→ASR→LLM→TTS 全链路：

- `src/websocket/index.ts` — WebSocket 连接管理（建立连接、心跳、重连、消息收发）
- `src/websocket/types.ts` — 协议定义：客户端消息（`audio.chunk`/`audio.end`/`interrupt`）、服务器事件（`vad.*`/`asr.*`/`llm.delta`/`tts.*`）、连接状态/通话状态枚举
- `src/store/voiceCall.ts` — 通话状态管理（非持久化），管理连接状态、通话状态流转、ASR/AI 文本、音频电平
- `src/pages/voiceCall/` — 通话页面及子组件（VoiceStatusBar/VoiceWaveform/VoiceControlBar）

通话状态机流转：`LISTENING → USER_SPEAKING → THINKING → ASSISTANT_SPEAKING → LISTENING`，打断时进入 `INTERRUPTED` 状态。

### manifest.config.ts

应用清单配置（对应 `manifest.json`），可配置各平台参数（如 `h5.router.base` 用于非根目录部署）、权限、特性开关等。

### 自动导入

- **组件自动导入**（easycom）：`wd-*`（wot-ui）、`kl-*`（自定义 kl-ui）、`fg-*`（通用业务组件）、`z-paging*`（分页组件）
- **API 自动导入**（unplugin-auto-import）：Vue API（`ref`/`computed`/`watch` 等）、uni-app API（`uni.request` 等）无需手动 import
- **Hooks 自动导入**：`src/hooks/` 下的 composables（`useRequest`/`useScroll`/`useUpload`）自动导入

自动生成的类型声明文件在 `src/types/` 目录下（`auto-import.d.ts`/`components.d.ts`/`uni-pages.d.ts` 等），不要手动编辑。

### 路径别名

- `@` → `./src`
- `@img` → `./src/static/images`

### 平台条件编译

使用 uni-app 的条件注释语法：
- `// #ifdef H5` / `// #ifndef H5`
- `// #ifdef MP-WEIXIN`
- `<!-- #ifdef H5 -->` 在 template 中同样可用

### 状态管理

使用 Pinia + `pinia-plugin-persistedstate` 持久化。核心 store：
- `src/store/token.ts` — token 管理（含有效期判断）
- `src/store/user.ts` — 用户信息
- `src/store/voiceCall.ts` — 语音通话状态（非持久化），管理 WebSocket 连接状态、通话状态机、ASR/AI 文本、音频电平
- `src/tabbar/store.ts` — tabbar 状态

## 注意事项

- 不使用 `any` 类型，import type 导入类型
- 环境要求 Node >= 20、pnpm >= 9
- 组件库使用 `@wot-ui/ui`（wot-design-uni v2），通过 easycom 自动按需引入，无需手动 import
- `.vscode/settings.json` 中已配置保存时自动 ESLint 修复
- commit 遵循 conventional commits 规范（由 commitlint 检查）
