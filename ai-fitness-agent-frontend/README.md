# AI 健身大师前端项目

## 🚀 快速启动

### 前提条件
- Node.js v18+ (检查：`node --version`)
- npm v8+ (检查：`npm --version`)
- Spring Boot 后端运行在端口 8123

### 启动步骤

1. **安装依赖**（首次运行）
   ```bash
   npm install --registry=https://registry.npmmirror.com
   ```

2. **启动开发服务器**
   ```bash
   npm run dev
   ```

3. **访问应用**
   
   浏览器打开：http://localhost:3000

---

## ✨ 功能特性

### 1. 聊天室界面
- ✅ **AI 消息在左侧**（带🤖头像）
- ✅ **用户消息在右侧**（带👤头像）
- ✅ 渐变紫色主题设计
- ✅ 响应式布局

### 2. AI 开场白
进入页面自动显示：
> "您好！我是您的专属健康管家。在制定方案前，我需要先了解您的身体状况。您方便用 30 秒描述最近一次运动后的身体感受吗？"

### 3. SSE 实时通信
- 使用 EventSource 连接 `/api/ai/manus/chat`
- 实时流式显示 AI 回复
- 打字加载动画

---

## 🔧 技术栈

- **Vue 3.4** - Composition API
- **Vite 5** - 快速构建工具
- **Axios** - HTTP 客户端（用于检测后端）
- **EventSource** - SSE 客户端

---

## 📋 后端接口要求

### 接口地址
```
GET http://localhost:8123/api/ai/manus/chat?message={用户输入}
```

### SSE 返回格式
```
data: {"content": "回复内容片段"}

event: end
data: {}
```

---

## ⚠️ 注意事项

1. **Node.js 版本**：必须 >= 18.0.0
   - 如果是 v12.x，请升级到 v20.x
   
2. **后端服务**：确保 Spring Boot 已启动
   ```java
   @RestController
   @RequestMapping("/ai")
   public class AiController {
       @GetMapping("/manus/chat")
       public SseEmitter doChatWithManus(String message) {
           YuManus yuManus = new YuManus(allTools, dashscopeChatModel);
           return yuManus.runStream(message);
       }
   }
   ```

3. **跨域配置**：vite.config.js 已配置代理

---

## 🎯 常见问题

### Q: 启动时报 "SyntaxError: Unexpected reserved word"
**A:** Node.js 版本太旧，请升级到 v18+

### Q: 无法连接后端
**A:** 检查 Spring Boot 是否运行在 8123 端口

### Q: 中文乱码
**A:** start.bat 已设置 UTF-8 编码，如仍有问题请检查系统区域设置

---

**项目创建时间**: 2026-03-25  
**适用系统**: Windows 10/11
