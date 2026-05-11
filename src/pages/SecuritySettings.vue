<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft } from 'lucide-vue-next';
import { authService } from '../api/services';
import { useToast } from '../composables/useToast';

const router = useRouter();
const { showToast } = useToast();

const currentPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const saving = ref(false);

const handleChangePassword = async () => {
  if (currentPassword.value.length < 6) {
    showToast('当前密码至少 6 个字符', 'error');
    return;
  }
  if (newPassword.value.length < 6) {
    showToast('新密码至少 6 个字符', 'error');
    return;
  }
  if (newPassword.value !== confirmPassword.value) {
    showToast('两次输入的新密码不一致', 'error');
    return;
  }

  saving.value = true;
  try {
    await authService.changePassword(currentPassword.value, newPassword.value);
    currentPassword.value = '';
    newPassword.value = '';
    confirmPassword.value = '';
    showToast('密码修改成功', 'success');
  } catch (error) {
    showToast(error instanceof Error ? error.message : '密码修改失败，请稍后重试', 'error');
  } finally {
    saving.value = false;
  }
};
</script>

<template>
  <div class="min-h-screen">
    <div class="sticky top-0 bg-bg-primary/80 backdrop-blur-md z-10 px-4 py-2 border-b border-border flex items-center gap-6">
      <button @click="router.back()" class="p-2 hover:bg-bg-secondary rounded-full">
        <ArrowLeft :size="20" />
      </button>
      <h1 class="text-xl font-bold">安全与隐私</h1>
    </div>

    <div class="mx-auto max-w-2xl px-4 py-6">
      <div class="rounded-3xl border border-border bg-bg-primary p-6 shadow-sm">
        <h2 class="text-lg font-bold">修改密码</h2>
        <p class="mt-2 text-sm text-text-secondary">修改后请使用新密码重新登录。演示账号的默认密码仍然是 `password123`。</p>

        <div class="mt-6 space-y-4">
          <label class="block">
            <span class="mb-2 block text-sm font-bold">当前密码</span>
            <input
              v-model="currentPassword"
              type="password"
              autocomplete="current-password"
              class="w-full rounded-2xl border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary"
            />
          </label>

          <label class="block">
            <span class="mb-2 block text-sm font-bold">新密码</span>
            <input
              v-model="newPassword"
              type="password"
              autocomplete="new-password"
              class="w-full rounded-2xl border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary"
            />
          </label>

          <label class="block">
            <span class="mb-2 block text-sm font-bold">确认新密码</span>
            <input
              v-model="confirmPassword"
              type="password"
              autocomplete="new-password"
              class="w-full rounded-2xl border border-border bg-bg-primary px-4 py-3 outline-none transition focus:border-text-primary"
            />
          </label>
        </div>

        <div class="mt-6 flex justify-end">
          <button
            type="button"
            class="rounded-full bg-text-primary px-5 py-3 font-bold text-bg-primary hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="saving"
            @click="handleChangePassword"
          >
            {{ saving ? '保存中...' : '保存新密码' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
