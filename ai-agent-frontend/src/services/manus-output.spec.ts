import { describe, expect, it } from 'vitest'
import { ManusOutputPresenter } from './manus-output'

describe('ManusOutputPresenter', () => {
  it('replaces raw tool payloads with one concise step', () => {
    const presenter = new ManusOutputPresenter()
    const output = presenter.push(
      'Step1: 工具 searchWeb返回的结果：{"position":1,"link":"https://example.com","title":"很长的搜索结果"}',
    )

    expect(output).toBe('Step 1：调用 `searchWeb` 工具')
    expect(output).not.toContain('position')
    expect(output).not.toContain('https://')
  })

  it('deduplicates repeated tool calls inside the same backend step', () => {
    const presenter = new ManusOutputPresenter()
    const output = presenter.push(
      'Step1: 工具 searchWeb返回的结果：结果一\n工具 searchWeb返回的结果：结果二',
    )

    expect(output).toBe('Step 1：调用 `searchWeb` 工具')
  })

  it('shows tool names but never exposes success or failure details', () => {
    const presenter = new ManusOutputPresenter()
    presenter.push('Step1: 工具 searchWeb返回的结果：Error searching Baidu')
    const output = presenter.push('Step2: 工具 scrapeWebPage返回的结果：抓取失败，页面正文很多')

    expect(output).toBe([
      'Step 1：调用 `searchWeb` 工具',
      'Step 2：调用 `scrapeWebPage` 工具',
    ].join('\n'))
    expect(output).not.toContain('Error')
    expect(output).not.toContain('失败')
  })

  it('hides terminate and internal status events', () => {
    const presenter = new ManusOutputPresenter()
    presenter.push('Step1: 工具 searchWeb返回的结果：[]')
    presenter.push('Step2: 工具 doTerminate返回的结果：任务结束')
    presenter.push('Step3: 思考完成，无需行动')

    expect(presenter.finish()).toBe([
      'Step 1：调用 `searchWeb` 工具',
      '',
      '---',
      '',
      '任务处理结束，暂无可展示的最终结果。',
    ].join('\n'))
  })

  it('places a standalone final answer after the concise steps', () => {
    const presenter = new ManusOutputPresenter()
    presenter.push('Step1: 工具 searchWeb返回的结果：大量原始数据')
    presenter.push('Step2: 最终结果：推荐地点为人才公园和深圳湾公园。')

    expect(presenter.finish()).toBe([
      'Step 1：调用 `searchWeb` 工具',
      '',
      '---',
      '',
      '### 最终结果',
      '',
      '推荐地点为人才公园和深圳湾公园。',
    ].join('\n'))
  })

  it('returns an empty string when the stream produced no events', () => {
    expect(new ManusOutputPresenter().finish()).toBe('')
  })
})
