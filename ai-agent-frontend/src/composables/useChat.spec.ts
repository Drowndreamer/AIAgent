import { defineComponent } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { loveAppConfig, manusAppConfig } from '../config/apps'
import type { StreamCallbacks } from '../types/chat'
import { useChat } from './useChat'

type StreamRequest = (
  endpoint: string,
  params: Record<string, string>,
  callbacks: StreamCallbacks,
  signal: AbortSignal,
) => Promise<void>

function mountChat(config = loveAppConfig, request: StreamRequest) {
  return mount(defineComponent({
    setup() {
      return useChat(config, request)
    },
    template: '<p data-test="last-message">{{ messages[messages.length - 1].status }}:{{ messages[messages.length - 1].content }}</p>',
  }))
}

describe('useChat', () => {
  it('keeps a stable love chat id and renews it on reset', async () => {
    const request = vi.fn<StreamRequest>(async (_endpoint, _params, callbacks) => {
      callbacks.onEvent({ data: '收到' })
    })
    const wrapper = mountChat(loveAppConfig, request)
    const firstId = wrapper.vm.sessionId

    await wrapper.vm.send('第一条')
    await wrapper.vm.send('第二条')

    expect(request).toHaveBeenCalledTimes(2)
    expect(request.mock.calls[0]?.[1]).toMatchObject({ message: '第一条', chatId: firstId })
    expect(request.mock.calls[1]?.[1]).toMatchObject({ message: '第二条', chatId: firstId })

    wrapper.vm.reset()
    expect(wrapper.vm.sessionId).not.toBe(firstId)
    wrapper.unmount()
  })

  it('sends Manus prompts as independent requests without chat history or chatId', async () => {
    const request = vi.fn<StreamRequest>(async (_endpoint, _params, callbacks) => {
      callbacks.onEvent({ data: 'Step1: 工具 searchWeb返回的结果：{"link":"https://example.com"}' })
      callbacks.onEvent({ data: 'Step2: 工具 scrapeWebPage返回的结果：很长的页面正文' })
      callbacks.onEvent({ data: 'Step3: 最终结果：任务需要的资料已经整理完成。' })
    })
    const wrapper = mountChat(manusAppConfig, request)

    await wrapper.vm.send('处理任务')

    expect(request.mock.calls[0]?.[1]).toEqual({ message: '处理任务' })
    expect(wrapper.vm.messages.at(-1)?.content).toContain('Step 1：调用 `searchWeb` 工具')
    expect(wrapper.vm.messages.at(-1)?.content).toContain('Step 2：调用 `scrapeWebPage` 工具')
    expect(wrapper.vm.messages.at(-1)?.content).toContain('### 最终结果')
    expect(wrapper.vm.messages.at(-1)?.content).not.toContain('https://example.com')
    expect(wrapper.get('[data-test="last-message"]').text()).toContain('complete:Step 1：调用 `searchWeb` 工具')
    wrapper.unmount()
  })

  it('prevents duplicate submission while a stream is active and can stop it', async () => {
    const request = vi.fn<StreamRequest>((_endpoint, _params, _callbacks, signal) => new Promise((_resolve, reject) => {
      signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true })
    }))
    const wrapper = mountChat(loveAppConfig, request)

    const pending = wrapper.vm.send('保持连接')
    await wrapper.vm.send('不应发送')
    expect(request).toHaveBeenCalledTimes(1)

    wrapper.vm.stop()
    await pending
    expect(wrapper.vm.messages.at(-1)?.content).toBe('已停止生成。')
    expect(wrapper.vm.messages.at(-1)?.status).toBe('complete')
    wrapper.unmount()
  })

  it('marks failures as retryable and retries without duplicating the user message', async () => {
    let attempt = 0
    const request = vi.fn<StreamRequest>(async (_endpoint, _params, callbacks) => {
      attempt += 1
      if (attempt === 1) throw new Error('offline')
      callbacks.onEvent({ data: '恢复成功' })
    })
    const wrapper = mountChat(loveAppConfig, request)

    await wrapper.vm.send('重试这条')
    const failed = wrapper.vm.messages.at(-1)
    expect(failed?.status).toBe('error')

    await wrapper.vm.retry(failed!.id)
    await flushPromises()
    expect(wrapper.vm.messages.filter((message) => message.role === 'user')).toHaveLength(1)
    expect(wrapper.vm.messages.at(-1)?.content).toBe('恢复成功')
    expect(wrapper.vm.messages.at(-1)?.status).toBe('complete')
    wrapper.unmount()
  })

  it('aborts the active request when the chat component unmounts', async () => {
    let requestSignal: AbortSignal | undefined
    const request = vi.fn<StreamRequest>((_endpoint, _params, _callbacks, signal) => {
      requestSignal = signal
      return new Promise((_resolve, reject) => {
        signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')), { once: true })
      })
    })
    const wrapper = mountChat(loveAppConfig, request)

    const pending = wrapper.vm.send('离开页面')
    await Promise.resolve()
    wrapper.unmount()
    await pending

    expect(requestSignal?.aborted).toBe(true)
  })
})
