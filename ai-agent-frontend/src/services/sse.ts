import type { AxiosProgressEvent } from 'axios'
import type { StreamCallbacks, StreamEvent } from '../types/chat'
import { apiClient } from './http'

export class SseParser {
  private buffer = ''

  feed(chunk: string): StreamEvent[] {
    this.buffer += chunk
    const events: StreamEvent[] = []
    let boundary = this.findBoundary()

    while (boundary) {
      const block = this.buffer.slice(0, boundary.index)
      this.buffer = this.buffer.slice(boundary.index + boundary.length)
      const event = this.parseBlock(block)
      if (event) events.push(event)
      boundary = this.findBoundary()
    }

    return events
  }

  finish(): StreamEvent[] {
    if (!this.buffer.trim()) {
      this.buffer = ''
      return []
    }

    const event = this.parseBlock(this.buffer)
    this.buffer = ''
    return event ? [event] : []
  }

  private findBoundary(): { index: number; length: number } | null {
    const match = /\r\n\r\n|\n\n|\r\r/.exec(this.buffer)
    return match ? { index: match.index, length: match[0].length } : null
  }

  private parseBlock(block: string): StreamEvent | null {
    const data: string[] = []
    let event: string | undefined
    let id: string | undefined

    for (const line of block.split(/\r\n|\r|\n/)) {
      if (!line || line.startsWith(':')) continue
      const colon = line.indexOf(':')
      const field = colon === -1 ? line : line.slice(0, colon)
      let value = colon === -1 ? '' : line.slice(colon + 1)
      if (value.startsWith(' ')) value = value.slice(1)

      if (field === 'data') data.push(value)
      if (field === 'event') event = value
      if (field === 'id') id = value
    }

    if (!data.length) return null
    return { data: data.join('\n'), event, id }
  }
}

function readResponseText(progress: AxiosProgressEvent): string {
  const nativeEvent = progress.event as ProgressEvent<XMLHttpRequest> | undefined
  const request = (nativeEvent?.currentTarget ?? nativeEvent?.target) as XMLHttpRequest | null
  return request?.responseText ?? ''
}

export async function streamSse(
  endpoint: string,
  params: Record<string, string>,
  callbacks: StreamCallbacks,
  signal: AbortSignal,
): Promise<void> {
  const parser = new SseParser()
  let offset = 0

  const emit = (text: string) => {
    if (!text || text.length <= offset) return
    const chunk = text.slice(offset)
    offset = text.length
    parser.feed(chunk).forEach(callbacks.onEvent)
  }

  const response = await apiClient.get<string>(endpoint, {
    adapter: 'xhr',
    params,
    responseType: 'text',
    signal,
    onDownloadProgress: (progress) => emit(readResponseText(progress)),
  })

  emit(response.data)
  parser.finish().forEach(callbacks.onEvent)
}
