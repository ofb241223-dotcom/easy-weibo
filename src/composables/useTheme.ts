import { ref, watch } from 'vue';

const theme = ref<'light' | 'dark'>('light');

watch(
  theme,
  (value) => {
    if (typeof window === 'undefined') return;

    const root = window.document.documentElement;
    root.classList.toggle('dark', value === 'dark');
  },
  { immediate: true },
);

export function useTheme() {
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light';
  };
  return { theme, toggleTheme };
}
