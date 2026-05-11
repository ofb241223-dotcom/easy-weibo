<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue';
import { ChevronLeft, ChevronRight, X } from 'lucide-vue-next';

const props = defineProps<{
  open: boolean;
  images: string[];
  index: number;
}>();

const emit = defineEmits<{
  close: [];
  'update:index': [value: number];
}>();

const currentImage = computed(() => props.images[props.index] || '');
const hasPrev = computed(() => props.images.length > 1 && props.index > 0);
const hasNext = computed(() => props.images.length > 1 && props.index < props.images.length - 1);

const close = () => emit('close');
const prev = () => {
  if (hasPrev.value) {
    emit('update:index', props.index - 1);
  }
};
const next = () => {
  if (hasNext.value) {
    emit('update:index', props.index + 1);
  }
};

const handleKeydown = (event: KeyboardEvent) => {
  if (!props.open) {
    return;
  }
  if (event.key === 'Escape') {
    close();
  } else if (event.key === 'ArrowLeft') {
    prev();
  } else if (event.key === 'ArrowRight') {
    next();
  }
};

watch(
  () => props.open,
  (open) => {
    if (open) {
      window.addEventListener('keydown', handleKeydown);
    } else {
      window.removeEventListener('keydown', handleKeydown);
    }
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown);
});
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open && currentImage"
      class="fixed inset-0 z-[120] flex items-center justify-center bg-black/75 px-4 py-6"
      @click="close"
    >
      <button
        type="button"
        class="absolute right-5 top-5 inline-flex h-11 w-11 items-center justify-center rounded-full border border-white/20 bg-black/30 text-white transition hover:bg-black/50"
        aria-label="关闭图片预览"
        @click.stop="close"
      >
        <X :size="20" />
      </button>

      <button
        v-if="hasPrev"
        type="button"
        class="absolute left-5 inline-flex h-12 w-12 items-center justify-center rounded-full border border-white/20 bg-black/30 text-white transition hover:bg-black/50"
        aria-label="上一张"
        @click.stop="prev"
      >
        <ChevronLeft :size="22" />
      </button>

      <div class="max-h-full max-w-6xl" @click.stop>
        <img :src="currentImage" alt="预览图片" class="max-h-[88vh] max-w-full rounded-3xl object-contain shadow-2xl" />
      </div>

      <button
        v-if="hasNext"
        type="button"
        class="absolute right-5 inline-flex h-12 w-12 items-center justify-center rounded-full border border-white/20 bg-black/30 text-white transition hover:bg-black/50"
        aria-label="下一张"
        @click.stop="next"
      >
        <ChevronRight :size="22" />
      </button>
    </div>
  </Teleport>
</template>
