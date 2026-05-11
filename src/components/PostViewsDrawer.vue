<script setup lang="ts">
import { computed } from 'vue';
import { X } from 'lucide-vue-next';
import type { PostViewRecord } from '../types';

const props = defineProps<{
  open: boolean;
  loading: boolean;
  records: PostViewRecord[];
}>();

const emit = defineEmits<{
  close: [];
}>();

const title = computed(() => `浏览记录 · ${props.records.length}`);

const formatTime = (value: string) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};
</script>

<template>
  <Teleport to="body">
    <template v-if="open">
      <div
        class="fixed inset-0 z-40 bg-black/30"
        @click="emit('close')"
      />
      <aside
        class="fixed right-0 top-0 z-50 h-screen w-full max-w-md border-l border-border bg-bg-primary shadow-2xl"
      >
        <div class="flex h-full flex-col">
          <div class="flex items-center justify-between border-b border-border px-5 py-4">
            <div>
              <h3 class="text-lg font-bold">{{ title }}</h3>
              <p class="mt-1 text-sm text-text-secondary">仅作者和管理员可查看是谁在什么时候看过这条帖子。</p>
            </div>
            <button type="button" class="rounded-full p-2 hover:bg-bg-secondary" @click="emit('close')">
              <X :size="18" />
            </button>
          </div>

          <div class="flex-1 overflow-y-auto px-4 py-4">
            <div v-if="loading" class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-brand border-t-transparent" />
            </div>
            <div v-else-if="records.length === 0" class="rounded-3xl border border-dashed border-border p-6 text-center text-sm text-text-secondary">
              还没有可展示的浏览记录。
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="record in records"
                :key="record.id"
                class="flex items-center gap-3 rounded-3xl border border-border px-4 py-3"
              >
                <img :src="record.viewer.avatar" :alt="record.viewer.nickname" class="h-11 w-11 rounded-full object-cover" />
                <div class="min-w-0 flex-1">
                  <p class="truncate font-bold">{{ record.viewer.nickname }}</p>
                  <p class="truncate text-sm text-text-secondary">@{{ record.viewer.username }}</p>
                </div>
                <span class="shrink-0 text-xs text-text-secondary">{{ formatTime(record.viewedAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </template>
  </Teleport>
</template>
