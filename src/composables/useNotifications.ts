import { computed, ref } from 'vue';
import { authSession, notificationService } from '../api/services';

const unreadCount = ref(0);
const loadingUnread = ref(false);

export async function refreshUnreadCount() {
  if (!authSession.hasToken()) {
    unreadCount.value = 0;
    return;
  }

  loadingUnread.value = true;
  try {
    unreadCount.value = await notificationService.getUnreadCount();
  } catch {
    unreadCount.value = 0;
  } finally {
    loadingUnread.value = false;
  }
}

export function useNotifications() {
  const hasUnread = computed(() => unreadCount.value > 0);

  const clearUnreadCount = () => {
    unreadCount.value = 0;
  };

  const decrementUnreadCount = (amount = 1) => {
    unreadCount.value = Math.max(unreadCount.value - amount, 0);
  };

  return {
    unreadCount,
    hasUnread,
    loadingUnread,
    refreshUnreadCount,
    clearUnreadCount,
    decrementUnreadCount,
  };
}
