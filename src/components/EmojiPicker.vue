<script setup lang="ts">
import { ref, onBeforeUnmount, onMounted } from 'vue';
import { Smile } from 'lucide-vue-next';

const emit = defineEmits<{
  select: [emoji: string];
}>();

const isOpen = ref(false);

const emojis = [
  '😀', '😄', '🥳', '😍', '🤔', '😎', '🥲', '😭',
  '🔥', '✨', '👍', '👏', '🙏', '💡', '📚', '🎓',
  '☕', '🍜', '🌧️', '🌞', '🎉', '💻', '📍', '❤️',
];

const close = () => {
  isOpen.value = false;
};

const handleWindowClick = () => {
  close();
};

onMounted(() => {
  window.addEventListener('click', handleWindowClick);
});

onBeforeUnmount(() => {
  window.removeEventListener('click', handleWindowClick);
});
</script>

<template>
  <div class="relative">
    <button
      type="button"
      class="p-2 rounded-full transition-colors hover:bg-brand/10"
      @click.stop="isOpen = !isOpen"
    >
      <Smile :size="22" />
    </button>

    <div
      v-if="isOpen"
      class="absolute bottom-12 left-0 z-20 w-[280px] rounded-3xl border border-border bg-bg-primary p-3 shadow-xl"
      @click.stop
    >
      <div class="grid grid-cols-6 gap-2">
        <button
          v-for="emoji in emojis"
          :key="emoji"
          type="button"
          class="flex h-10 w-10 items-center justify-center rounded-2xl text-xl transition-colors hover:bg-bg-secondary"
          @click="emit('select', emoji); close()"
        >
          {{ emoji }}
        </button>
      </div>
    </div>
  </div>
</template>
