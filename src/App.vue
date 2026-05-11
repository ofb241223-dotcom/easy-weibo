<script setup lang="ts">
import { useToast } from './composables/useToast';
import { CheckCircle2, AlertCircle, X } from 'lucide-vue-next';
import AiChatPanel from './components/ai/AiChatPanel.vue';

const { toasts, dismissToast } = useToast();
</script>

<template>
  <router-view />
  <AiChatPanel />
  
  <!-- Toast Container -->
  <div class="fixed top-20 md:top-6 left-1/2 md:left-auto md:right-6 -translate-x-1/2 md:translate-x-0 z-50 flex flex-col gap-2 w-full max-w-xs px-4 md:px-0 pointer-events-none">
    <transition-group name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="[
          'flex items-center justify-between gap-3 p-4 rounded-xl shadow-lg border transition-all duration-300 pointer-events-auto',
          toast.type === 'success' ? 'bg-white border-black text-black' : 'bg-black border-black text-white'
        ]"
      >
        <div class="flex items-center gap-2">
          <CheckCircle2 v-if="toast.type === 'success'" :size="18" />
          <AlertCircle v-else :size="18" />
          <span class="text-sm font-medium">{{ toast.message }}</span>
        </div>
        <button @click="dismissToast(toast.id)">
          <X :size="16" />
        </button>
      </div>
    </transition-group>
  </div>
</template>

<style>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateY(-16px) scale(0.94);
}
.toast-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.94);
}
</style>
