import { ref } from 'vue';

interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error';
}

const toasts = ref<Toast[]>([]);

export function useToast() {
  const dismissToast = (id: string) => {
    toasts.value = toasts.value.filter((toast) => toast.id !== id);
  };

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    const id = Math.random().toString(36).substr(2, 9);
    toasts.value.push({ id, message, type });
    setTimeout(() => {
      dismissToast(id);
    }, 3000);
  };

  return { toasts, showToast, dismissToast };
}
