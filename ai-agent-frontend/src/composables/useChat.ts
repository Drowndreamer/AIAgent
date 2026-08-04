import axios from 'axios'
import { computed, onBeforeUnmount, ref } from 'vue'
import { ManusOutputPresenter } from '../services/manus-output'
import { streamSse } from '../services/sse'
import type { ChatAppConfig, ChatMessage } from '../types/chat'

function createId(): string {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function welcomeMessage(config: ChatAppConfig): ChatMessage {
  return {
    id: createId(),
    role: 'assistant',
    content: config.welcome,
    status: 'complete',
  }
}

export function useChat(config: ChatAppConfig, requestStream = streamSse) {
  const input = ref('')
  const messages = ref<ChatMessage[]>([welcomeMessage(config)])
  const sessionId = ref(createId())
  const activeController = ref<AbortController | null>(null)
  const hasConversation = computed(() => messages.value.some((message) => message.role === 'user'))
  const isStreaming = computed(() => activeController.value !== null)

  function stop(): void {
    activeController.value?.abort()
    activeController.value = null
  }

  function reset(): void {
    stop()
    input.value = ''
    sessionId.value = createId()
    messages.value = [welcomeMessage(config)]
  }

  async function run(prompt: string, assistant: ChatMessage): Promise<void> {
    const controller = new AbortController()
    activeController.value = controller
    assistant.content = ''
    assistant.status = 'streaming'
    assistant.retrySource = prompt

    const params: Record<string, string> = { message: prompt }
    if (config.supportsChatId) params.chatId = sessionId.value
    const manusOutput = config.id === 'manus' ? new ManusOutputPresenter() : null

    try {
      await requestStream(
        config.endpoint,
        params,
        {
          onEvent: ({ data }) => {
            if (!data) return
            if (manusOutput) {
              assistant.content = manusOutput.push(data)
              return
            }
            if (assistant.content && config.eventSeparator) {
              assistant.content += config.eventSeparator
            }
            assistant.content += data
          },
        },
        controller.signal,
      )

      if (manusOutput) assistant.content = manusOutput.finish()

      if (!assistant.content.trim()) {
        assistant.content = '没有收到有效回复，请稍后重试。'
        assistant.status = 'error'
      } else {
        assistant.status = 'complete'
      }
    } catch (error) {
      if (axios.isCancel(error) || controller.signal.aborted) {
        assistant.status = 'complete'
        if (!assistant.content) assistant.content = '已停止生成。'
      } else {
        assistant.status = 'error'
        if (!assistant.content) assistant.content = '连接失败，请确认后端服务已启动后重试。'
      }
    } finally {
      if (activeController.value === controller) activeController.value = null
    }
  }

  async function send(value = input.value): Promise<void> {
    const prompt = value.trim()
    if (!prompt || isStreaming.value) return

    input.value = ''
    messages.value.push({
      id: createId(),
      role: 'user',
      content: prompt,
      status: 'complete',
    })
    const assistantSeed: ChatMessage = {
      id: createId(),
      role: 'assistant',
      content: '',
      status: 'streaming',
      retrySource: prompt,
    }
    messages.value.push(assistantSeed)
    const assistant = messages.value[messages.value.length - 1]
    if (!assistant) return
    await run(prompt, assistant)
  }

  async function retry(messageId: string): Promise<void> {
    if (isStreaming.value) return
    const assistant = messages.value.find((message) => message.id === messageId)
    if (!assistant?.retrySource) return
    await run(assistant.retrySource, assistant)
  }

  onBeforeUnmount(stop)

  return {
    input,
    messages,
    sessionId,
    hasConversation,
    isStreaming,
    send,
    retry,
    stop,
    reset,
  }
}
