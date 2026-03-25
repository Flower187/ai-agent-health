<template>
  <div class="chat-container">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <h1>AI 健身大师</h1>
      <p class="subtitle">您的专属健康管家</p>
    </div>

    <!-- 聊天记录区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <!-- AI 开场白 -->
      <div v-if="messages.length === 0" class="message ai-message">
        <div class="avatar ai-avatar">🤖</div>
        <div class="message-content">
          <div class="message-bubble ai">
            您好！我是您的专属健康管家。在制定方案前，我需要先了解您的身体状况。您方便用 30 秒描述最近一次运动后的身体感受吗？
          </div>
          <div class="message-time">{{ currentTime }}</div>
        </div>
      </div>

      <!-- 聊天消息列表 -->
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message', message.type === 'user' ? 'user-message' : 'ai-message']"
      >
        <!-- AI 头像（左侧） -->
        <div v-if="message.type === 'ai'" class="avatar ai-avatar">🤖</div>
        
        <!-- 消息内容 -->
        <div class="message-content">
          <div :class="['message-bubble', message.type]">
            {{ message.content }}
          </div>
          <div class="message-time">{{ message.time }}</div>
        </div>

        <!-- 用户头像（右侧） -->
        <div v-if="message.type === 'user'" class="avatar user-avatar">👤</div>
      </div>

      <!-- 加载中提示 -->
      <div v-if="isLoading" class="message ai-message">
        <div class="avatar ai-avatar">🤖</div>
        <div class="message-content">
          <div class="message-bubble ai loading">
            <span class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入框区域 -->
    <div class="chat-input-area">
      <div class="input-wrapper">
        <input
          type="text"
          v-model="inputMessage"
          @keyup.enter="sendMessage"
          placeholder="请输入您的问题..."
          :disabled="isLoading || !backendAvailable"
          class="chat-input"
        />
        <button
          @click="sendMessage"
          :disabled="!backendAvailable"
          :class="['send-button', { 'cancel-button': isLoading }]"
        >
          <span v-if="isLoading">⏸ 暂停</span>
          <span v-else-if="!backendAvailable">连接中...</span>
          <span v-else>发送</span>
        </button>
      </div>
      <div v-if="!backendAvailable" class="backend-status">
        ⚠️ 后端未连接，请确保 Spring Boot 服务已启动（端口 8123）
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'

// 响应式数据
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
const backendAvailable = ref(true)
const currentEventSource = ref(null) // 保存当前 SSE 连接

// 当前时间
const currentTime = computed(() => {
  const now = new Date()
  return now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
})

// 格式化时间
const formatTime = (date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 检查后端是否可用
const checkBackend = async () => {
  try {
    const response = await fetch('http://localhost:8123/api/ai/manus/chat?message=test', {
      method: 'GET'
    })
    backendAvailable.value = true
  } catch (error) {
    backendAvailable.value = false
    console.warn('后端服务未响应，请检查是否启动')
  }
}

// 暂停/取消当前请求
const cancelRequest = () => {
  if (currentEventSource.value) {
    currentEventSource.value.close()
    currentEventSource.value = null
    isLoading.value = false
    
    // 添加取消提示
    const lastMessageIndex = messages.value.length - 1
    if (messages.value[lastMessageIndex] && 
        messages.value[lastMessageIndex].type === 'ai' && 
        !messages.value[lastMessageIndex].content) {
      // 如果 AI 消息为空，移除它
      messages.value.pop()
    } else if (messages.value[lastMessageIndex] && 
               messages.value[lastMessageIndex].type === 'ai') {
      // 如果有部分内容，标记为已取消
      messages.value[lastMessageIndex].content += '\n\n[已取消]'
    }
    
    scrollToBottom()
  }
}

// 发送消息
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  
  // 如果正在加载，点击则取消
  if (isLoading.value) {
    cancelRequest()
    return
  }
  
  if (!message || !backendAvailable.value) return

  // 添加用户消息到聊天记录（显示在右边）
  messages.value.push({
    type: 'user',
    content: message,
    time: formatTime(new Date())
  })

  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    // 使用 EventSource 建立 SSE 连接
    const encodedMessage = encodeURIComponent(message)
    const eventSource = new EventSource(
      `http://localhost:8123/api/ai/manus/chat?message=${encodedMessage}`
    )
    
    // 保存当前连接引用
    currentEventSource.value = eventSource

    // 创建 AI 消息占位（显示在左边）
    const aiMessageIndex = messages.value.length
    messages.value.push({
      type: 'ai',
      content: '',
      time: formatTime(new Date()),
      isStreaming: true
    })

    // 监听 SSE 消息
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.content) {
          // 追加 AI 回复内容
          messages.value[aiMessageIndex].content += data.content
          scrollToBottom()
        }
      } catch (e) {
        // 如果不是 JSON，直接作为文本处理
        messages.value[aiMessageIndex].content += event.data
        scrollToBottom()
      }
    }

    // 监听错误
    eventSource.onerror = () => {
      eventSource.close()
      currentEventSource.value = null
      messages.value[aiMessageIndex].isStreaming = false
      isLoading.value = false
      
      // 如果内容为空，显示错误提示
      if (!messages.value[aiMessageIndex].content) {
        messages.value[aiMessageIndex].content = '抱歉，连接中断，请稍后重试。'
      }
      
      scrollToBottom()
    }

    // 监听结束事件
    eventSource.addEventListener('end', () => {
      eventSource.close()
      currentEventSource.value = null
      messages.value[aiMessageIndex].isStreaming = false
      isLoading.value = false
      scrollToBottom()
    })

  } catch (error) {
    console.error('发送消息失败:', error)
    currentEventSource.value = null
    messages.value.push({
      type: 'ai',
      content: '抱歉，发送失败，请稍后重试。',
      time: formatTime(new Date())
    })
    isLoading.value = false
  }

  scrollToBottom()
}

// 组件挂载时检查后端
onMounted(() => {
  checkBackend()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
}

/* 聊天头部 */
.chat-header {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.chat-header h1 {
  color: #667eea;
  font-size: 24px;
  margin-bottom: 5px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

/* 聊天记录区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: rgba(255, 255, 255, 0.5);
}

/* 消息样式 */
.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.user-message {
  justify-content: flex-end;
}

.ai-message {
  justify-content: flex-start;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-right: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  margin-left: 12px;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
  position: relative;
  word-wrap: break-word;
  min-width: 50px;
}

/* 用户消息气泡（右侧） */
.message-bubble.user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

/* AI 消息气泡（左侧） */
.message-bubble.ai {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.message-time {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
  padding: 0 5px;
}

.user-message .message-time {
  text-align: right;
}

/* 输入框区域 */
.chat-input-area {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.input-wrapper {
  display: flex;
  gap: 10px;
  max-width: 1200px;
  margin: 0 auto;
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 25px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}

.chat-input:focus {
  border-color: #667eea;
}

.chat-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.send-button {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, opacity 0.2s, background 0.3s;
  min-width: 80px;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 取消按钮样式 */
.send-button.cancel-button {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(255, 107, 107, 0.7);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(255, 107, 107, 0);
  }
}

.backend-status {
  text-align: center;
  color: #ff9800;
  font-size: 12px;
  margin-top: 8px;
}

/* 打字动画 */
.typing-indicator {
  display: inline-flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.5);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.7);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-content {
    max-width: 80%;
  }
  
  .chat-header h1 {
    font-size: 20px;
  }
  
  .input-wrapper {
    flex-direction: column;
  }
  
  .send-button {
    padding: 10px 20px;
  }
}
</style>
