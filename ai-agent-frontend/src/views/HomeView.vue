<script setup lang="ts">
import {
  ArrowUpRight,
  Bot,
  HeartHandshake,
  Radio,
} from '@lucide/vue'
import { RouterLink } from 'vue-router'
import { chatApps } from '../config/apps'
</script>

<template>
  <div class="home-shell">
    <header class="home-header">
      <RouterLink class="wordmark" to="/" aria-label="AI Agent 工作台首页">
        <span class="wordmark-mark"><Radio :size="17" /></span>
        <span>AI Agent</span>
      </RouterLink>
      <div class="system-status">
        <i />
        本地服务台
      </div>
    </header>

    <main class="home-main">
      <section class="home-intro" aria-labelledby="home-title">
        <p class="eyebrow">选择对话通道</p>
        <h1 id="home-title">今天，需要哪一种帮助？</h1>
        <p>进入应用后直接开始对话。每条回复都会在生成时实时送达。</p>
      </section>

      <section class="channel-board" aria-label="可用应用">
        <RouterLink
          v-for="app in chatApps"
          :key="app.id"
          :to="app.route"
          class="app-channel"
          :class="`app-channel--${app.id}`"
        >
          <div class="channel-index" aria-hidden="true">
            <span>{{ app.id === 'love' ? 'RELATION' : 'ACTION' }}</span>
            <i />
          </div>

          <div class="channel-heading">
            <span class="channel-icon" aria-hidden="true">
              <HeartHandshake v-if="app.id === 'love'" :size="26" :stroke-width="1.7" />
              <Bot v-else :size="26" :stroke-width="1.7" />
            </span>
            <div>
              <p>{{ app.eyebrow }}</p>
              <h2>{{ app.shortTitle }}</h2>
            </div>
            <span class="launch-icon" title="打开应用">
              <ArrowUpRight :size="21" />
            </span>
          </div>

          <p class="channel-description">{{ app.description }}</p>

          <div class="dialogue-preview" aria-hidden="true">
            <div class="preview-line preview-line--user">
              <span>{{ app.previewUser }}</span>
              <i>你</i>
            </div>
            <div class="preview-line preview-line--assistant">
              <i>AI</i>
              <span>{{ app.previewAssistant }}</span>
            </div>
          </div>

          <div class="channel-foot">
            <span><i /> 可连接</span>
            <span>{{ app.supportsChatId ? '连续会话' : '独立任务' }}</span>
          </div>
        </RouterLink>
      </section>
    </main>

    <footer class="home-footer">
      <span>AI AGENT CONSOLE</span>
      <span>02 CHANNELS</span>
    </footer>
  </div>
</template>

<style scoped>
.home-shell {
  display: grid;
  min-height: 100dvh;
  grid-template-rows: auto 1fr auto;
  background: var(--canvas);
}

.home-header,
.home-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-color: var(--line);
  padding-inline: clamp(18px, 4vw, 58px);
}

.home-header {
  min-height: 68px;
  border-bottom: 1px solid var(--line);
}

.wordmark {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  color: var(--ink-strong);
  font-family: var(--font-display);
  font-weight: 700;
}

.wordmark-mark {
  display: grid;
  width: 31px;
  height: 31px;
  place-items: center;
  border-radius: 50%;
  background: var(--ink-strong);
  color: white;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--ink-muted);
  font-family: var(--font-mono);
  font-size: 0.68rem;
}

.system-status i,
.channel-foot i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #3f9a6d;
  box-shadow: 0 0 0 3px rgb(63 154 109 / 0.13);
}

.home-main {
  width: min(100%, 1280px);
  margin: 0 auto;
  padding: 56px clamp(18px, 5vw, 72px) 48px;
}

.home-intro {
  display: grid;
  max-width: 820px;
  grid-template-columns: minmax(0, 1fr) minmax(240px, 0.56fr);
  column-gap: 46px;
  align-items: end;
  margin-bottom: 46px;
}

.home-intro .eyebrow {
  grid-column: 1 / -1;
  margin: 0 0 12px;
  color: var(--ink-faint);
  font-family: var(--font-mono);
  font-size: 0.7rem;
}

.home-intro h1 {
  margin: 0;
  color: var(--ink-strong);
  font-family: var(--font-display);
  font-size: 2.65rem;
  font-weight: 650;
  line-height: 1.18;
}

.home-intro > p:last-child {
  margin: 0 0 5px;
  color: var(--ink-muted);
  line-height: 1.7;
}

.channel-board {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-top: 1px solid var(--line-strong);
  border-bottom: 1px solid var(--line-strong);
}

.app-channel {
  --channel: #d95763;
  --channel-soft: #fbeaec;
  position: relative;
  display: flex;
  min-width: 0;
  min-height: 430px;
  flex-direction: column;
  padding: 22px 32px 24px;
  color: var(--ink);
  transition: background-color 180ms ease;
}

.app-channel + .app-channel { border-left: 1px solid var(--line-strong); }
.app-channel--manus { --channel: #14786f; --channel-soft: #e4f1ee; }
.app-channel:hover { background: color-mix(in srgb, var(--channel-soft) 48%, var(--canvas)); }

.channel-index {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--ink-faint);
  font-family: var(--font-mono);
  font-size: 0.62rem;
}

.channel-index i {
  height: 1px;
  flex: 1;
  background: var(--line);
}

.channel-heading {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) 34px;
  align-items: center;
  gap: 12px;
  margin-top: 28px;
}

.channel-icon {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 50%;
  background: var(--channel-soft);
  color: var(--channel);
}

.channel-heading p {
  margin: 0 0 2px;
  color: var(--ink-faint);
  font-size: 0.72rem;
}

.channel-heading h2 {
  margin: 0;
  color: var(--ink-strong);
  font-family: var(--font-display);
  font-size: 1.55rem;
  font-weight: 650;
}

.launch-icon {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border: 1px solid var(--line-strong);
  border-radius: 50%;
  transition: border-color 180ms ease, background 180ms ease, color 180ms ease, transform 180ms ease;
}

.app-channel:hover .launch-icon {
  border-color: var(--channel);
  background: var(--channel);
  color: white;
  transform: translate(2px, -2px);
}

.channel-description {
  margin: 18px 0 26px 60px;
  color: var(--ink-muted);
  font-size: 0.9rem;
}

.dialogue-preview {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: center;
  gap: 13px;
  border-top: 1px dashed var(--line-strong);
  border-bottom: 1px dashed var(--line-strong);
  padding: 25px 0;
}

.preview-line {
  display: flex;
  max-width: 88%;
  align-items: flex-start;
  gap: 8px;
  font-size: 0.83rem;
  line-height: 1.55;
}

.preview-line i {
  display: grid;
  width: 25px;
  height: 25px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  background: var(--surface);
  color: var(--ink-faint);
  font-family: var(--font-mono);
  font-size: 0.57rem;
  font-style: normal;
}

.preview-line span {
  border: 1px solid var(--line);
  border-radius: 4px 7px 7px 7px;
  background: var(--surface);
  padding: 8px 11px;
}

.preview-line--user {
  align-self: flex-end;
  justify-content: flex-end;
}

.preview-line--user span {
  border-color: var(--channel);
  border-radius: 7px 4px 7px 7px;
  background: var(--channel);
  color: white;
}

.channel-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 20px;
  color: var(--ink-faint);
  font-family: var(--font-mono);
  font-size: 0.64rem;
}

.channel-foot span:first-child {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.home-footer {
  min-height: 48px;
  border-top: 1px solid var(--line);
  color: var(--ink-faint);
  font-family: var(--font-mono);
  font-size: 0.62rem;
}

@media (max-width: 800px) {
  .home-main { padding-top: 38px; }
  .home-intro { display: block; margin-bottom: 32px; }
  .home-intro h1 { font-size: 2.1rem; }
  .home-intro > p:last-child { margin-top: 14px; max-width: 430px; }
  .channel-board { grid-template-columns: 1fr; }
  .app-channel { min-height: 390px; padding-inline: 22px; }
  .app-channel + .app-channel { border-top: 1px solid var(--line-strong); border-left: 0; }
}

@media (max-width: 460px) {
  .home-header { min-height: 60px; }
  .system-status { font-size: 0; }
  .home-main { padding: 30px 14px 34px; }
  .home-intro h1 { font-size: 1.9rem; }
  .app-channel { min-height: 366px; padding: 18px 16px 20px; }
  .channel-heading { margin-top: 20px; }
  .channel-description { margin-left: 0; }
  .home-footer { padding-inline: 14px; }
}

@media (prefers-reduced-motion: reduce) {
  .app-channel,
  .launch-icon { transition: none; }
}
</style>
