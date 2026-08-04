import { describe, expect, it } from 'vitest'
import { SseParser } from './sse'

describe('SseParser', () => {
  it('parses multiple LF-delimited events', () => {
    const parser = new SseParser()
    expect(parser.feed('data: one\n\ndata: two\n\n')).toEqual([
      { data: 'one', event: undefined, id: undefined },
      { data: 'two', event: undefined, id: undefined },
    ])
  })

  it('preserves an event split across arbitrary chunks', () => {
    const parser = new SseParser()
    expect(parser.feed('data: hel')).toEqual([])
    expect(parser.feed('lo\n')).toEqual([])
    expect(parser.feed('\ndata: next\n\n')).toEqual([
      { data: 'hello', event: undefined, id: undefined },
      { data: 'next', event: undefined, id: undefined },
    ])
  })

  it('handles CRLF, metadata, comments, and multiline data', () => {
    const parser = new SseParser()
    expect(parser.feed(': ping\r\nid: 7\r\nevent: step\r\ndata: line 1\r\ndata: line 2\r\n\r\n')).toEqual([
      { data: 'line 1\nline 2', event: 'step', id: '7' },
    ])
  })

  it('flushes a final event without a blank terminator', () => {
    const parser = new SseParser()
    parser.feed('data: tail')
    expect(parser.finish()).toEqual([
      { data: 'tail', event: undefined, id: undefined },
    ])
    expect(parser.finish()).toEqual([])
  })

  it('ignores blocks with no data field', () => {
    const parser = new SseParser()
    expect(parser.feed('event: ping\n\n: heartbeat\n\n')).toEqual([])
  })
})
