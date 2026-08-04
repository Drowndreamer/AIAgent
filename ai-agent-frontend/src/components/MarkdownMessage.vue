<script setup lang="ts">
import { computed } from 'vue'
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'

const props = defineProps<{ content: string }>()

const markdown = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
  typographer: false,
})

const defaultLinkOpen = markdown.renderer.rules.link_open
  ?? ((tokens, index, options, _env, self) => self.renderToken(tokens, index, options))

markdown.renderer.rules.link_open = (tokens, index, options, env, self) => {
  tokens[index]?.attrSet('target', '_blank')
  tokens[index]?.attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, index, options, env, self)
}

const rendered = computed(() => DOMPurify.sanitize(markdown.render(props.content)))
</script>

<template>
  <div class="markdown" v-html="rendered" />
</template>

<style scoped>
.markdown {
  overflow-wrap: anywhere;
  line-height: 1.72;
}

.markdown :deep(*) {
  max-width: 100%;
}

.markdown :deep(p),
.markdown :deep(ul),
.markdown :deep(ol),
.markdown :deep(pre),
.markdown :deep(blockquote) {
  margin: 0 0 0.8em;
}

.markdown :deep(:last-child) {
  margin-bottom: 0;
}

.markdown :deep(ul),
.markdown :deep(ol) {
  padding-left: 1.35rem;
}

.markdown :deep(li + li) {
  margin-top: 0.3rem;
}

.markdown :deep(h1),
.markdown :deep(h2),
.markdown :deep(h3) {
  margin: 1.05em 0 0.5em;
  color: var(--ink-strong);
  font-family: var(--font-display);
  line-height: 1.25;
}

.markdown :deep(h1) { font-size: 1.2rem; }
.markdown :deep(h2) { font-size: 1.08rem; }
.markdown :deep(h3) { font-size: 1rem; }

.markdown :deep(a) {
  color: var(--app-accent);
  text-decoration-thickness: 1px;
  text-underline-offset: 3px;
}

.markdown :deep(code) {
  border: 1px solid var(--line);
  border-radius: 4px;
  background: var(--code-bg);
  padding: 0.12em 0.35em;
  font-family: var(--font-mono);
  font-size: 0.88em;
}

.markdown :deep(pre) {
  overflow-x: auto;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: var(--code-bg);
  padding: 0.9rem 1rem;
}

.markdown :deep(pre code) {
  border: 0;
  background: transparent;
  padding: 0;
}

.markdown :deep(blockquote) {
  border-left: 3px solid var(--app-accent);
  color: var(--ink-muted);
  padding-left: 0.9rem;
}
</style>
