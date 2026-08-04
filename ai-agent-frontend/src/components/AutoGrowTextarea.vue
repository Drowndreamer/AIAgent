<script setup lang="ts">
import { nextTick, onMounted, useTemplateRef, watch } from 'vue'

const props = defineProps<{
  modelValue: string
  placeholder: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  submit: []
}>()

const textarea = useTemplateRef<HTMLTextAreaElement>('textarea')

function resize(): void {
  const element = textarea.value
  if (!element) return
  element.style.height = '0px'
  element.style.height = `${Math.min(element.scrollHeight, 144)}px`
}

function handleInput(event: Event): void {
  emit('update:modelValue', (event.target as HTMLTextAreaElement).value)
  resize()
}

function handleKeydown(event: KeyboardEvent): void {
  if (event.key !== 'Enter' || event.shiftKey || event.isComposing) return
  event.preventDefault()
  emit('submit')
}

function focus(): void {
  textarea.value?.focus()
}

watch(
  () => props.modelValue,
  async () => {
    await nextTick()
    resize()
  },
)

onMounted(resize)
defineExpose({ focus })
</script>

<template>
  <textarea
    ref="textarea"
    class="composer-textarea"
    rows="1"
    :value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    aria-label="消息内容"
    @input="handleInput"
    @keydown="handleKeydown"
  />
</template>

<style scoped>
.composer-textarea {
  display: block;
  width: 100%;
  min-height: 28px;
  max-height: 144px;
  resize: none;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--ink-strong);
  font: inherit;
  line-height: 1.55;
}

.composer-textarea::placeholder {
  color: var(--ink-faint);
}

.composer-textarea:disabled {
  cursor: not-allowed;
}
</style>
