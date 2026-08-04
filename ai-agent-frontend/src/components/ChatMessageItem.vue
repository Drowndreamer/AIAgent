<script setup lang="ts">
import { Bot, RefreshCw, UserRound } from '@lucide/vue'
import type { ChatMessage } from '../types/chat'
import MarkdownMessage from './MarkdownMessage.vue'

defineProps<{
  message: ChatMessage
  assistantLabel: string
}>()

defineEmits<{
  retry: [messageId: string]
}>()
</script>

<template>
  <article
    class="message"
    :class="[`message--${message.role}`, { 'message--error': message.status === 'error' }]"
    :aria-label="message.role === 'assistant' ? assistantLabel : '你的消息'"
  >
    <div class="message-avatar" aria-hidden="true">
      <Bot v-if="message.role === 'assistant'" :size="17" :stroke-width="1.8" />
      <UserRound v-else :size="17" :stroke-width="1.8" />
    </div>

    <div class="message-body">
      <div class="message-meta">
        {{ message.role === 'assistant' ? assistantLabel : '你' }}
      </div>
      <div class="message-bubble">
        <MarkdownMessage v-if="message.role === 'assistant'" :content="message.content" />
        <p v-else class="user-copy">{{ message.content }}</p>
        <span
          v-if="message.status === 'streaming'"
          class="stream-caret"
          aria-label="正在生成"
        />
      </div>

      <button
        v-if="message.status === 'error' && message.retrySource"
        class="retry-button"
        type="button"
        @click="$emit('retry', message.id)"
      >
        <RefreshCw :size="14" />
        重新连接
      </button>
    </div>
  </article>
</template>

<style scoped>
.message {
  display: grid;
  grid-template-columns: 32px minmax(0, auto);
  align-items: start;
  gap: 10px;
  max-width: min(82%, 720px);
}

.message--user {
  align-self: flex-end;
  grid-template-columns: minmax(0, auto) 32px;
}

.message--user .message-avatar { order: 2; }
.message--user .message-body { order: 1; align-items: flex-end; }

.message-avatar {
  display: grid;
  width: 32px;
  height: 32px;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 50%;
  background: var(--surface);
  color: var(--app-accent);
}

.message--user .message-avatar {
  border-color: var(--app-accent);
  background: var(--app-accent);
  color: white;
}

.message-body {
  display: flex;
  min-width: 0;
  flex-direction: column;
  align-items: flex-start;
}

.message-meta {
  margin-bottom: 5px;
  color: var(--ink-faint);
  font-family: var(--font-mono);
  font-size: 0.7rem;
}

.message-bubble {
  position: relative;
  min-width: 52px;
  border: 1px solid var(--line);
  border-radius: 4px 8px 8px 8px;
  background: var(--surface);
  padding: 12px 15px;
  color: var(--ink);
  box-shadow: 0 8px 24px rgb(24 32 29 / 0.035);
}

.message--user .message-bubble {
  border-color: var(--app-accent);
  border-radius: 8px 4px 8px 8px;
  background: var(--app-accent);
  color: white;
  box-shadow: none;
}

.message--error .message-bubble {
  border-color: #c97870;
  background: #fff5f3;
}

.user-copy {
  margin: 0;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  line-height: 1.65;
}

.stream-caret {
  display: inline-block;
  width: 7px;
  height: 1em;
  margin-left: 4px;
  vertical-align: -0.12em;
  background: var(--app-accent);
  animation: caret 0.85s steps(2, start) infinite;
}

.retry-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  border: 0;
  background: transparent;
  padding: 4px 0;
  color: #a33f37;
  font: inherit;
  font-size: 0.78rem;
  cursor: pointer;
}

@keyframes caret { 50% { opacity: 0; } }

@media (max-width: 640px) {
  .message { max-width: 94%; }
  .message-bubble { padding: 11px 13px; }
}

@media (prefers-reduced-motion: reduce) {
  .stream-caret { animation: none; }
}
</style>
