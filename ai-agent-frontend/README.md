# AI Agent Frontend

Vue 3 前端工作台，包含 AI 恋爱大师与 AI 超级智能体两个实时聊天应用。

## 启动

```bash
npm install
npm run dev
```

开发服务默认运行在 `http://127.0.0.1:5173`。

后端接口默认使用 `http://localhost:8123/api`。如需修改，复制 `.env.example` 为 `.env.local` 并设置：

```text
VITE_API_BASE_URL=http://localhost:8123/api
```

## 检查

```bash
npm run typecheck
npm test
npm run build
```

聊天接口通过 Axios XHR 的下载进度事件增量解析 SSE，并使用 `AbortController` 取消仍在进行的回复。
