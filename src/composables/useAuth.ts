import { computed, ref } from 'vue';
import type { User } from '../types';
import { authService, authSession } from '../api/services';
import { refreshUnreadCount, useNotifications } from './useNotifications';

const user = ref<User | null>(null);
const initialized = ref(false);
const isAuthenticated = computed(() => !!user.value);

export async function initializeAuth() {
  if (initialized.value) {
    return;
  }

  if (!authSession.hasToken()) {
    initialized.value = true;
    return;
  }

  try {
    user.value = await authService.getCurrentUser();
    await refreshUnreadCount();
  } catch {
    authService.logout();
    user.value = null;
  } finally {
    initialized.value = true;
  }
}

export function useAuth() {
  const { clearUnreadCount } = useNotifications();

  const login = async (username: string, password: string) => {
    const result = await authService.login(username, password);
    user.value = result.user;
    await refreshUnreadCount();
    return result.user;
  };

  const register = async (username: string, nickname: string, password: string) => {
    const result = await authService.register(username, nickname, password);
    user.value = result.user;
    await refreshUnreadCount();
    return result.user;
  };

  const setCurrentUser = (nextUser: User | null) => {
    user.value = nextUser;
  };

  const logout = () => {
    authService.logout();
    user.value = null;
    clearUnreadCount();
  };

  return {
    user,
    isAuthenticated,
    initialized,
    login,
    register,
    setCurrentUser,
    logout,
  };
}
