<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ArrowLeft } from 'lucide-vue-next';

const router = useRouter();

const faqs = [
  {
    question: '如何发布帖子？',
    answer: '登录后可以直接在首页编辑区发布，也可以进入独立发布页使用更大的编辑区域。',
  },
  {
    question: '如何找回密码？',
    answer: '登录页提供了演示版密码重置流程，只需要输入用户名、昵称和新密码即可重置。',
  },
  {
    question: '哪些互动已经接通后端？',
    answer: '登录、注册、发帖、评论、点赞、收藏、转发、关注、通知和资料编辑都已经是后端持久化数据。',
  },
  {
    question: '为什么位置和定时发布没有显示？',
    answer: '这两个入口目前没有做半成品实现，先从界面里移除了，避免误导用户点击无效功能。',
  },
];
</script>

<template>
  <div class="min-h-screen">
    <div class="sticky top-0 bg-bg-primary/80 backdrop-blur-md z-10 px-4 py-2 border-b border-border flex items-center gap-6">
      <button @click="router.back()" class="p-2 hover:bg-bg-secondary rounded-full">
        <ArrowLeft :size="20" />
      </button>
      <h1 class="text-xl font-bold">帮助中心</h1>
    </div>

    <div class="mx-auto max-w-3xl px-4 py-6">
      <div class="rounded-3xl border border-border bg-bg-primary p-6 shadow-sm">
        <h2 class="text-lg font-bold">常见问题</h2>
        <p class="mt-2 text-sm text-text-secondary">这里放的是当前演示版最常见的使用说明，方便快速自测和展示。</p>

        <div class="mt-6 divide-y divide-border">
          <div v-for="item in faqs" :key="item.question" class="py-5">
            <h3 class="text-base font-bold">{{ item.question }}</h3>
            <p class="mt-2 text-sm leading-7 text-text-secondary">{{ item.answer }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
