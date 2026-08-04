<script setup lang="ts">
import ChatWorkspace from '../components/ChatWorkspace.vue'
import { loveAppConfig } from '../config/apps'
</script>

<template>
  <ChatWorkspace :config="loveAppConfig" />
</template>
