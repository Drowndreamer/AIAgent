<script setup lang="ts">
import ChatWorkspace from '../components/ChatWorkspace.vue'
import { manusAppConfig } from '../config/apps'
</script>

<template>
  <ChatWorkspace :config="manusAppConfig" />
</template>
