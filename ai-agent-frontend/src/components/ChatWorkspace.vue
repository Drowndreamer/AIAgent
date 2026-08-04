<script setup lang="ts">
import {
  ArrowDown,
  ArrowLeft,
  Bot,
  Check,
  Copy,
  HeartHandshake,
  Plus,
  Send,
  Square,
} from '@lucide/vue'
import { computed, nextTick, ref, useTemplateRef, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { useChat } from '../composables/useChat'
import type { ChatAppConfig } from '../types/chat'
import AutoGrowTextarea from './AutoGrowTextarea.vue'
import ChatMessageItem from './ChatMessageItem.vue'

const props = defineProps<{ config: ChatAppConfig }>()
const chat = useChat(props.config)
const messageScroller = useTemplateRef<HTMLElement>('messageScroller')
const textInput = useTemplateRef<InstanceType<typeof AutoGrowTextarea>>('textInput')
const stickToBottom = ref(true)
const copied = ref(false)

const sessionLabel = computed(() => chat.sessionId.value.slice(0, 8).toUpperCase())
const canSend = computed(() => Boolean(chat.input.value.trim()) && !chat.isStreaming.value)
const messageSignature = computed(() =>
  chat.messages.value.map((message) => `${message.id}:${message.content.length}:${message.status}`).join('|'),
)

async function scrollToBottom(force = false): Promise<void> {
  if (!force && !stickToBottom.value) return
  await nextTick()
  const element = messageScroller.value
  if (element) element.scrollTop = element.scrollHeight
}

function handleScroll(): void {
  const element = messageScroller.value
  if (!element) return
  stickToBottom.value = element.scrollHeight - element.scrollTop - element.clientHeight < 72
}

async function send(value?: string): Promise<void> {
  stickToBottom.value = true
  await chat.send(value)
  textInput.value?.focus()
}

async function retry(messageId: string): Promise<void> {
  stickToBottom.value = true
  await chat.retry(messageId)
}

function reset(): void {
  chat.reset()
  stickToBottom.value = true
  void scrollToBottom(true)
  textInput.value?.focus()
}

async function copySessionId(): Promise<void> {
  try {
    await navigator.clipboard.writeText(chat.sessionId.value)
    copied.value = true
    window.setTimeout(() => { copied.value = false }, 1400)
  } catch {
    copied.value = false
  }
}

watch(messageSignature, () => void scrollToBottom())
</script>

<template>
  <section class="workspace" :class="`workspace--${config.id}`">
    <header class="workspace-header">
      <RouterLink class="icon-button" to="/" aria-label="返回应用首页" title="返回应用首页">
        <ArrowLeft :size="20" />
      </RouterLink>

      <div class="app-identity">
        <span class="app-symbol" aria-hidden="true">
          <HeartHandshake v-if="config.id === 'love'" :size="20" :stroke-width="1.8" />
          <Bot v-else :size="20" :stroke-width="1.8" />
        </span>
        <div>
          <p>{{ config.eyebrow }}</p>
          <h1>{{ config.title }}</h1>
        </div>
      </div>

      <div class="header-actions">
        <button
          v-if="config.supportsChatId"
          class="session-id"
          type="button"
          :title="copied ? '已复制' : '复制完整会话编号'"
          @click="copySessionId"
        >
          <span>SESSION {{ sessionLabel }}</span>
          <Check v-if="copied" :size="14" />
          <Copy v-else :size="14" />
        </button>
        <span v-else class="task-mode">独立任务模式</span>

        <button
          class="new-chat-button"
          type="button"
          aria-label="新对话"
          title="新对话"
          @click="reset"
        >
          <Plus :size="17" />
          <span>新对话</span>
        </button>
      </div>
    </header>

    <main
      ref="messageScroller"
      class="message-scroller"
      role="log"
      aria-live="polite"
      :aria-busy="chat.isStreaming.value"
      @scroll.passive="handleScroll"
    >
      <div class="message-column">
        <div class="conversation-marker">
          <span>{{ config.supportsChatId ? '当前会话' : '当前任务列' }}</span>
          <i />
        </div>

        <ChatMessageItem
          v-for="message in chat.messages.value"
          :key="message.id"
          :message="message"
          :assistant-label="config.shortTitle"
          @retry="retry"
        />

        <div v-if="!chat.hasConversation.value" class="suggestions" aria-label="推荐问题">
          <button
            v-for="suggestion in config.suggestions"
            :key="suggestion"
            type="button"
            @click="send(suggestion)"
          >
            {{ suggestion }}
          </button>
        </div>
      </div>

      <button
        v-if="!stickToBottom"
        class="scroll-button icon-button"
        type="button"
        aria-label="回到最新消息"
        title="回到最新消息"
        @click="stickToBottom = true; scrollToBottom(true)"
      >
        <ArrowDown :size="18" />
      </button>
    </main>

    <footer class="composer-band">
      <div class="composer-shell">
        <AutoGrowTextarea
          ref="textInput"
          v-model="chat.input.value"
          :placeholder="config.placeholder"
          @submit="send()"
        />
        <button
          v-if="chat.isStreaming.value"
          class="send-button send-button--stop"
          type="button"
          aria-label="停止生成"
          title="停止生成"
          @click="chat.stop"
        >
          <Square :size="16" fill="currentColor" />
        </button>
        <button
          v-else
          class="send-button"
          type="button"
          :disabled="!canSend"
          aria-label="发送消息"
          title="发送消息"
          @click="send()"
        >
          <Send :size="18" />
        </button>
      </div>
      <div class="composer-status">
        <span :class="{ active: chat.isStreaming.value }">
          {{ chat.isStreaming.value ? '正在接收实时回复' : '连接就绪' }}
        </span>
        <span>{{ chat.input.value.length }} / 4000</span>
      </div>
    </footer>
  </section>
</template>

<style scoped>
.workspace {
  --app-accent: #14786f;
  --app-soft: #e4f1ee;
  display: grid;
  height: 100dvh;
  overflow: hidden;
  grid-template-rows: auto minmax(0, 1fr) auto;
  background: var(--canvas);
}

.workspace--love {
  --app-accent: #d95763;
  --app-soft: #fbeaec;
}

.workspace-header {
  z-index: 5;
  display: grid;
  min-height: 76px;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  border-bottom: 1px solid var(--line);
  background: rgb(255 255 255 / 0.88);
  padding: 10px clamp(16px, 3vw, 42px);
  backdrop-filter: blur(14px);
}

.app-identity {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 11px;
}

.app-symbol {
  display: grid;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  background: var(--app-soft);
  color: var(--app-accent);
}

.app-identity p {
  margin: 0 0 2px;
  color: var(--ink-faint);
  font-family: var(--font-mono);
  font-size: 0.65rem;
  text-transform: uppercase;
}

.app-identity h1 {
  margin: 0;
  overflow: hidden;
  color: var(--ink-strong);
  font-family: var(--font-display);
  font-size: 1.15rem;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 9px;
}

.session-id,
.task-mode {
  display: inline-flex;
  min-height: 30px;
  align-items: center;
  gap: 7px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: var(--canvas);
  padding: 5px 9px;
  color: var(--ink-muted);
  font-family: var(--font-mono);
  font-size: 0.68rem;
}

.session-id { cursor: pointer; }

.new-chat-button {
  display: inline-flex;
  min-height: 36px;
  align-items: center;
  gap: 7px;
  border: 1px solid var(--ink-strong);
  border-radius: 5px;
  background: var(--ink-strong);
  padding: 7px 12px;
  color: white;
  font: inherit;
  font-size: 0.82rem;
  cursor: pointer;
}

.icon-button {
  display: inline-grid;
  width: 38px;
  height: 38px;
  flex: 0 0 auto;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 50%;
  background: var(--surface);
  color: var(--ink);
  cursor: pointer;
}

.message-scroller {
  position: relative;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-color: var(--line-strong) transparent;
}

.message-column {
  display: flex;
  width: min(100%, 940px);
  min-height: 100%;
  flex-direction: column;
  gap: 24px;
  margin: 0 auto;
  padding: clamp(26px, 5vh, 54px) clamp(18px, 4vw, 46px) 38px;
}

.conversation-marker {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--ink-faint);
  font-family: var(--font-mono);
  font-size: 0.66rem;
  text-transform: uppercase;
}

.conversation-marker i {
  height: 1px;
  flex: 1;
  background: var(--line);
}

.suggestions {
  display: flex;
  max-width: 720px;
  flex-wrap: wrap;
  gap: 8px;
  margin-left: 42px;
}

.suggestions button {
  border: 1px solid var(--line-strong);
  border-radius: 6px;
  background: transparent;
  padding: 8px 11px;
  color: var(--ink-muted);
  font: inherit;
  font-size: 0.78rem;
  text-align: left;
  cursor: pointer;
  transition: border-color 160ms ease, background 160ms ease, color 160ms ease;
}

.suggestions button:hover {
  border-color: var(--app-accent);
  background: var(--app-soft);
  color: var(--ink-strong);
}

.scroll-button {
  position: sticky;
  bottom: 18px;
  left: calc(50% - 19px);
  box-shadow: 0 8px 22px rgb(24 32 29 / 0.12);
}

.composer-band {
  z-index: 4;
  border-top: 1px solid var(--line);
  background: rgb(242 245 243 / 0.92);
  padding: 14px max(18px, env(safe-area-inset-right)) max(12px, env(safe-area-inset-bottom));
  backdrop-filter: blur(14px);
}

.composer-shell,
.composer-status {
  width: min(100%, 850px);
  margin: 0 auto;
}

.composer-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 42px;
  align-items: end;
  gap: 10px;
  border: 1px solid var(--line-strong);
  border-radius: 8px;
  background: var(--surface);
  padding: 12px 12px 12px 16px;
  box-shadow: 0 12px 34px rgb(24 32 29 / 0.06);
}

.composer-shell:focus-within {
  border-color: var(--app-accent);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--app-accent) 13%, transparent);
}

.send-button {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border: 0;
  border-radius: 6px;
  background: var(--app-accent);
  color: white;
  cursor: pointer;
}

.send-button:disabled {
  background: var(--line);
  color: var(--ink-faint);
  cursor: not-allowed;
}

.send-button--stop { background: var(--ink-strong); }

.composer-status {
  display: flex;
  justify-content: space-between;
  padding-top: 7px;
  color: var(--ink-faint);
  font-family: var(--font-mono);
  font-size: 0.65rem;
}

.composer-status .active { color: var(--app-accent); }

@media (max-width: 720px) {
  .workspace-header {
    min-height: 66px;
    grid-template-columns: 38px minmax(0, 1fr) auto;
    gap: 9px;
    padding: 8px 12px;
  }

  .app-symbol,
  .session-id,
  .task-mode,
  .new-chat-button span { display: none; }
  .new-chat-button { width: 38px; height: 38px; justify-content: center; padding: 0; }
  .app-identity h1 { font-size: 1rem; }
  .message-column { padding-top: 24px; }
  .suggestions { margin-left: 0; }
  .suggestions button { width: 100%; }
  .composer-band { padding-inline: 10px; }
}
</style>
