import type { ChatAppConfig } from '../types/chat'

export const loveAppConfig: ChatAppConfig = {
  id: 'love',
  route: '/love',
  title: 'AI 恋爱大师',
  shortTitle: '恋爱大师',
  eyebrow: '关系对话',
  description: '理清关系里的情绪、边界与下一步。',
  endpoint: '/ai/love_app/chat/sse',
  placeholder: '说说最近让你困扰的关系问题…',
  welcome: '你好，我在这里。你可以从最近反复想到的一件事说起。',
  suggestions: [
    '我们最近总为同一件事争吵，怎么沟通？',
    '我不确定这段关系是否适合继续',
    '怎样自然地认识更合适的人？',
  ],
  previewUser: '我们总在小事上争吵。',
  previewAssistant: '先别急着判断对错，我们看看争吵背后各自在保护什么。',
  supportsChatId: true,
  eventSeparator: '',
}

export const manusAppConfig: ChatAppConfig = {
  id: 'manus',
  route: '/manus',
  title: 'AI 超级智能体',
  shortTitle: '超级智能体',
  eyebrow: '任务执行',
  description: '拆解目标、调用工具并持续返回执行进度。',
  endpoint: '/ai/manus/chat',
  placeholder: '描述一个需要规划或执行的任务…',
  welcome: '告诉我目标和已有条件，我会从第一步开始处理。',
  suggestions: [
    '整理一份本周 AI 行业动态摘要',
    '分析这个项目并给出改进优先级',
    '帮我规划一份三天的学习安排',
  ],
  previewUser: '梳理项目的下一步工作。',
  previewAssistant: 'Step 1：读取现状并确定阻塞项。\nStep 2：按影响排序执行路径。',
  supportsChatId: false,
  eventSeparator: '\n\n',
}

export const chatApps = [loveAppConfig, manusAppConfig]
