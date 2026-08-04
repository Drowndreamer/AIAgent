const STEP_PREFIX = /^\s*Step\s*(\d+)\s*[:：]\s*/i
const TOOL_RESULT_PATTERN = /工具\s+([A-Za-z_][A-Za-z0-9_.-]*)\s*返回的结果\s*[:：]/g
const INTERNAL_TOOLS = new Set(['doTerminate'])
const INTERNAL_MESSAGE_PATTERN = /^(?:工具\s|思考完成，无需行动|达到最大步骤|任务结束|Terminated\b|执行错误|错误|.*失败)/i

interface ManusStep {
  sourceId: string
  tools: string[]
}

export class ManusOutputPresenter {
  private readonly steps = new Map<string, ManusStep>()
  private finalResult = ''
  private receivedEvent = false

  push(rawEvent: string): string {
    const event = rawEvent.trim()
    if (!event) return this.render(false)
    this.receivedEvent = true

    const stepMatch = STEP_PREFIX.exec(event)
    const sourceId = stepMatch?.[1] ?? `event-${this.steps.size + 1}`
    const tools = Array.from(event.matchAll(TOOL_RESULT_PATTERN), (match) => match[1])
      .filter((tool): tool is string => Boolean(tool) && !INTERNAL_TOOLS.has(tool))
      .filter((tool, index, all) => all.indexOf(tool) === index)

    if (tools.length) {
      this.steps.set(sourceId, { sourceId, tools })
      return this.render(false)
    }

    const payload = stepMatch ? event.slice(stepMatch[0].length).trim() : event
    const result = payload.replace(/^(?:最终结果|Final result)\s*[:：]\s*/i, '').trim()
    if (result && !INTERNAL_MESSAGE_PATTERN.test(result)) {
      this.finalResult = result
    }

    return this.render(false)
  }

  finish(): string {
    return this.render(true)
  }

  private render(completed: boolean): string {
    const stepLines = Array.from(this.steps.values()).map((step, index) => {
      const toolNames = step.tools.map((tool) => `\`${tool}\``).join('、')
      return `Step ${index + 1}：调用 ${toolNames} 工具`
    })

    const sections: string[] = []
    if (stepLines.length) sections.push(stepLines.join('\n'))

    if (this.finalResult) {
      sections.push(`### 最终结果\n\n${this.finalResult}`)
    } else if (completed && this.receivedEvent) {
      sections.push('任务处理结束，暂无可展示的最终结果。')
    }

    return sections.join('\n\n---\n\n')
  }
}
