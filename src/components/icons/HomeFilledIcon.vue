<script setup lang="ts">
withDefaults(
  defineProps<{
    size?: number | string;
  }>(),
  {
    size: 24,
  },
);
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="currentColor"
    aria-hidden="true"
  >
    <g transform="translate(12 12) scale(1.2) translate(-12 -12)">
      <path d="M10 20v-5.25c0-.41.34-.75.75-.75h2.5c.41 0 .75.34.75.75V20H19v-8h3L12 4 2 12h3v8h5Z" />
    </g>
  </svg>
</template>
