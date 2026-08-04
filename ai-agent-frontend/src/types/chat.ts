export type AppId = 'love' | 'manus'
export type MessageRole = 'assistant' | 'user'
export type MessageStatus = 'idle' | 'streaming' | 'complete' | 'error'

export interface ChatMessage {
  id: string
  role: MessageRole
  content: string
  status: MessageStatus
  retrySource?: string
}

export interface ChatAppConfig {
  id: AppId
  route: string
  title: string
  shortTitle: string
  eyebrow: string
  description: string
  endpoint: string
  placeholder: string
  welcome: string
  suggestions: string[]
  previewUser: string
  previewAssistant: string
  supportsChatId: boolean
  eventSeparator: string
}

export interface StreamEvent {
  data: string
  event?: string
  id?: string
}

export interface StreamCallbacks {
  onEvent: (event: StreamEvent) => void
}
