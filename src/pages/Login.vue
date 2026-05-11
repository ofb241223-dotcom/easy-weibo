<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ref } from 'vue';
import { authService } from '../api/services';
import { useAuth } from '../composables/useAuth';
import { useToast } from '../composables/useToast';
import SparkLoginShell from '../components/login/SparkLoginShell.vue';

const router = useRouter();
const { login, register } = useAuth();
const { showToast } = useToast();

const loginRef = ref<{
  setError: (message: string) => void;
  setLoading: (value: boolean) => void;
  setSuccess: (message: string) => void;
  setMode: (mode: 'login' | 'register' | 'reset') => void;
} | null>(null);

const onSubmit = async ({ username, password }: { username: string; password: string }) => {
  if (username.length < 3) {
    loginRef.value?.setError('用户名至少 3 个字符');
    return;
  }

  if (password.length < 6) {
    loginRef.value?.setError('密码至少 6 个字符');
    return;
  }

  loginRef.value?.setError('');
  loginRef.value?.setLoading(true);

  try {
    await login(username, password);
    loginRef.value?.setSuccess('登录成功，正在跳转...');
    showToast('登录成功！欢迎回来', 'success');
    setTimeout(() => {
      router.push('/');
    }, 1000);
  } catch (error) {
    loginRef.value?.setError(error instanceof Error ? error.message : '用户名或密码错误，可试用 johndoe / janedoe + password123');
    showToast(error instanceof Error ? error.message : '登录失败，请检查用户名和密码', 'error');
  }

  loginRef.value?.setLoading(false);
};

const handleRegister = async ({
  username,
  nickname,
  password,
  confirmPassword,
}: {
  username: string;
  nickname: string;
  password: string;
  confirmPassword: string;
}) => {
  if (nickname.length < 2) {
    loginRef.value?.setError('昵称至少 2 个字符');
    return;
  }

  if (username.length < 3) {
    loginRef.value?.setError('用户名至少 3 个字符');
    return;
  }

  if (!/^[A-Za-z0-9_]+$/.test(username)) {
    loginRef.value?.setError('用户名只能使用字母、数字和下划线');
    return;
  }

  if (password.length < 6) {
    loginRef.value?.setError('密码至少 6 个字符');
    return;
  }

  if (password !== confirmPassword) {
    loginRef.value?.setError('两次输入的密码不一致');
    return;
  }

  loginRef.value?.setError('');
  loginRef.value?.setLoading(true);

  try {
    await register(username, nickname, password);
    loginRef.value?.setSuccess('注册成功，正在进入首页...');
    showToast('注册成功！欢迎加入', 'success');
    setTimeout(() => {
      router.push('/');
    }, 900);
  } catch (error) {
    loginRef.value?.setError(error instanceof Error ? error.message : '注册失败，请稍后重试');
    showToast(error instanceof Error ? error.message : '注册失败，请稍后重试', 'error');
  }

  loginRef.value?.setLoading(false);
};

const handleResetPassword = async ({
  username,
  nickname,
  password,
  confirmPassword,
}: {
  username: string;
  nickname: string;
  password: string;
  confirmPassword: string;
}) => {
  if (nickname.length < 2) {
    loginRef.value?.setError('昵称至少 2 个字符');
    return;
  }

  if (username.length < 3) {
    loginRef.value?.setError('用户名至少 3 个字符');
    return;
  }

  if (password.length < 6) {
    loginRef.value?.setError('新密码至少 6 个字符');
    return;
  }

  if (password !== confirmPassword) {
    loginRef.value?.setError('两次输入的密码不一致');
    return;
  }

  loginRef.value?.setError('');
  loginRef.value?.setLoading(true);

  try {
    await authService.resetPassword(username, nickname, password);
    loginRef.value?.setMode('login');
    loginRef.value?.setSuccess('密码已重置，请使用新密码登录');
    showToast('密码重置成功，请重新登录', 'success');
  } catch (error) {
    loginRef.value?.setError(error instanceof Error ? error.message : '重置密码失败，请稍后重试');
    showToast(error instanceof Error ? error.message : '重置密码失败，请稍后重试', 'error');
  }

  loginRef.value?.setLoading(false);
};
</script>

<template>
  <SparkLoginShell
    ref="loginRef"
    @submit="onSubmit"
    @register="handleRegister"
    @reset-password="handleResetPassword"
  />
</template>
