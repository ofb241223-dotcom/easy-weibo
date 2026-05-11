<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import AnimatedCharacters, { type CharacterConfig } from './AnimatedCharacters.vue';

type LoginPayload = {
  username: string;
  password: string;
  remember: boolean;
};

type RegisterPayload = {
  username: string;
  nickname: string;
  password: string;
  confirmPassword: string;
};

type ResetPayload = {
  username: string;
  nickname: string;
  password: string;
  confirmPassword: string;
};

const emit = defineEmits<{
  submit: [payload: LoginPayload];
  register: [payload: RegisterPayload];
  resetPassword: [payload: ResetPayload];
}>();

const mode = ref<'login' | 'register' | 'reset'>('login');
const nickname = ref('');
const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const remember = ref(false);
const showPassword = ref(false);
const focusedField = ref<'none' | 'nickname' | 'username' | 'password' | 'confirm'>('none');
const isTyping = ref(false);
const isLoginError = ref(false);
const errorTimer = ref<number | null>(null);
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

const characterConfig: CharacterConfig = {
  scale: 0.92,
};

const isRegisterMode = computed(() => mode.value === 'register');
const isResetMode = computed(() => mode.value === 'reset');
const heading = computed(() => {
  if (isRegisterMode.value) {
    return '创建 HNUST Easy WeiBo 账号';
  }
  if (isResetMode.value) {
    return '重置 HNUST Easy WeiBo 密码';
  }
  return '登录到 HNUST Easy WeiBo';
});
const submitLabel = computed(() => {
  if (loading.value) {
    if (isRegisterMode.value) {
      return '注册中...';
    }
    if (isResetMode.value) {
      return '重置中...';
    }
    return '登录中...';
  }
  if (isRegisterMode.value) {
    return '注册并进入';
  }
  if (isResetMode.value) {
    return '重置密码';
  }
  return '登录';
});
const switchPrompt = computed(() => (
  isRegisterMode.value ? '已经有账号？' : isResetMode.value ? '想起密码了？' : '还没有账号？'
));
const switchLabel = computed(() => (
  isRegisterMode.value ? '立即登录' : isResetMode.value ? '返回登录' : '立即注册'
));
const switchTarget = computed<'login' | 'register'>(() => (
  isRegisterMode.value || isResetMode.value ? 'login' : 'register'
));

function clearBanners() {
  errorMsg.value = '';
  successMsg.value = '';
}

function resetErrorState() {
  if (!isLoginError.value) {
    return;
  }
  isLoginError.value = false;
  if (errorTimer.value) {
    window.clearTimeout(errorTimer.value);
    errorTimer.value = null;
  }
}

function triggerErrorState() {
  isLoginError.value = true;
  if (errorTimer.value) {
    window.clearTimeout(errorTimer.value);
  }
  errorTimer.value = window.setTimeout(() => {
    isLoginError.value = false;
  }, 2200);
}

function switchMode(nextMode: 'login' | 'register' | 'reset') {
  mode.value = nextMode;
  clearBanners();
  resetErrorState();
  loading.value = false;
  password.value = '';
  confirmPassword.value = '';
  nickname.value = '';
  username.value = '';
}

function onSubmit() {
  clearBanners();
  if (isRegisterMode.value) {
    emit('register', {
      username: username.value.trim(),
      nickname: nickname.value.trim(),
      password: password.value,
      confirmPassword: confirmPassword.value,
    });
    return;
  }

  if (isResetMode.value) {
    emit('resetPassword', {
      username: username.value.trim(),
      nickname: nickname.value.trim(),
      password: password.value,
      confirmPassword: confirmPassword.value,
    });
    return;
  }

  emit('submit', {
    username: username.value.trim(),
    password: password.value,
    remember: remember.value,
  });
}

watch([username, nickname, password, confirmPassword], () => {
  if (errorMsg.value) {
    errorMsg.value = '';
  }
  resetErrorState();
});

defineExpose({
  setError(message: string) {
    errorMsg.value = message;
    successMsg.value = '';
    if (message) {
      triggerErrorState();
    }
  },
  setLoading(value: boolean) {
    loading.value = value;
  },
  setSuccess(message: string) {
    successMsg.value = message;
    errorMsg.value = '';
    resetErrorState();
  },
  setMode(nextMode: 'login' | 'register' | 'reset') {
    switchMode(nextMode);
  },
});
</script>

<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="left-panel">
        <div class="left-brand">
          <img src="/favicon.svg" alt="HNUST logo" class="left-brand__logo" />
          <div>
            <p class="left-brand__title">HNUST Easy WeiBo</p>
            <p class="left-brand__meta">校园社交分享平台</p>
          </div>
        </div>

        <div class="characters-stage">
          <AnimatedCharacters
            :config="characterConfig"
            :focused-field="focusedField === 'password' || focusedField === 'confirm' ? 'password' : focusedField === 'none' ? 'none' : 'email'"
            :is-password-visible="showPassword"
            :password-length="Math.max(password.length, confirmPassword.length)"
            :is-login-error="isLoginError"
            :is-typing="isTyping"
          />
        </div>

        <div class="left-footer-spacer" aria-hidden="true"></div>
      </section>

      <section class="right-panel">
        <div class="form-panel">
          <div class="mobile-brand">
            <img src="/favicon.svg" alt="HNUST logo" class="mobile-brand__logo" />
            <span>HNUST Easy WeiBo</span>
          </div>

          <form class="form-center" @submit.prevent="onSubmit">
            <div v-if="errorMsg" class="form-banner form-banner--error" role="alert">
              {{ errorMsg }}
            </div>
            <div v-else-if="successMsg" class="form-banner form-banner--success" role="status">
              {{ successMsg }}
            </div>

            <h1>{{ heading }}</h1>

            <template v-if="isRegisterMode || isResetMode">
              <label class="field-label" for="register-nickname">昵称</label>
              <div class="input-wrap" :class="{ focused: focusedField === 'nickname' }">
                <input
                  id="register-nickname"
                  v-model.trim="nickname"
                  type="text"
                  maxlength="32"
                  placeholder="请输入昵称"
                  autocomplete="nickname"
                  @focus="focusedField = 'nickname'; isTyping = true"
                  @blur="focusedField = 'none'; isTyping = false"
                />
              </div>
            </template>

            <label class="field-label" for="login-username">用户名</label>
            <div class="input-wrap" :class="{ focused: focusedField === 'username' }">
              <input
                id="login-username"
                v-model.trim="username"
                type="text"
                maxlength="24"
                :placeholder="isRegisterMode ? '3-24 个字符，可使用字母数字下划线' : isResetMode ? '请输入需要重置的用户名' : '请输入用户名'"
                autocomplete="username"
                @focus="focusedField = 'username'; isTyping = true"
                @blur="focusedField = 'none'; isTyping = false"
              />
            </div>

            <label class="field-label" for="login-password">密码</label>
            <div class="input-wrap" :class="{ focused: focusedField === 'password' }">
              <input
                id="login-password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                maxlength="64"
                :placeholder="isRegisterMode || isResetMode ? '至少 6 位密码' : '请输入密码'"
                :autocomplete="isRegisterMode || isResetMode ? 'new-password' : 'current-password'"
                @keydown.enter.prevent="onSubmit"
                @focus="focusedField = 'password'; isTyping = true"
                @blur="focusedField = 'none'; isTyping = false"
              />
              <button class="eye-btn" type="button" tabindex="-1" @click="showPassword = !showPassword">
                <svg
                  v-if="!showPassword"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <svg
                  v-else
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                  <line x1="1" y1="1" x2="23" y2="23" />
                </svg>
              </button>
            </div>

            <template v-if="isRegisterMode || isResetMode">
              <label class="field-label" for="register-confirm-password">确认密码</label>
              <div class="input-wrap" :class="{ focused: focusedField === 'confirm' }">
                <input
                  id="register-confirm-password"
                  v-model="confirmPassword"
                  :type="showPassword ? 'text' : 'password'"
                  maxlength="64"
                  placeholder="再次输入密码"
                  autocomplete="new-password"
                  @keydown.enter.prevent="onSubmit"
                  @focus="focusedField = 'confirm'; isTyping = true"
                  @blur="focusedField = 'none'; isTyping = false"
                />
              </div>
            </template>

            <div v-if="!isRegisterMode && !isResetMode" class="action-row">
              <label class="checkbox-wrap">
                <input v-model="remember" type="checkbox" />
                <span class="checkmark" :class="{ checked: remember }">
                  <svg v-if="remember" width="10" height="10" viewBox="0 0 10 10">
                    <path
                      d="M2 5l2.5 2.5L8 3"
                      stroke="#fff"
                      stroke-width="1.5"
                      fill="none"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </span>
                <span>记住这台设备</span>
              </label>
              <button class="forgot-link" type="button" @click="switchMode('reset')">忘记密码？</button>
            </div>

            <p v-else class="register-hint">
              {{ isRegisterMode ? '注册成功后会自动登录并进入首页。' : '重置成功后可直接返回登录页使用新密码登录。' }}
            </p>

            <button class="login-btn" type="submit" :disabled="loading">
              {{ submitLabel }}
            </button>

            <p class="signup-row">
              {{ switchPrompt }}
              <button class="signup-link" type="button" @click="switchMode(switchTarget)">
                {{ switchLabel }}
              </button>
            </p>
          </form>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f3f4f6;
}

.login-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.left-panel {
  position: relative;
  background: #ededed;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 48px;
  overflow: hidden;
}

.characters-stage {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  height: 500px;
  position: relative;
  z-index: 20;
}

.left-footer-spacer {
  width: 100%;
  height: 24px;
  flex-shrink: 0;
}

.left-brand {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #171717;
  position: relative;
  z-index: 20;
  align-self: stretch;
}

.left-brand__logo {
  width: 54px;
  height: 54px;
  object-fit: contain;
}

.left-brand__title {
  font-size: 1.15rem;
  font-weight: 700;
}

.left-brand__meta {
  margin-top: 2px;
  color: #71717a;
  font-size: 0.88rem;
}

.right-panel {
  background: white;
  display: flex;
  align-items: stretch;
  justify-content: center;
}

.form-panel {
  width: min(540px, 100%);
  min-height: 100vh;
  padding: 64px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  opacity: 0;
  transform: translateY(24px) scale(0.985);
  animation: form-reveal 0.7s cubic-bezier(0.22, 1, 0.36, 1) 0.12s forwards;
}

.form-center {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.mobile-brand {
  display: none;
}

.mobile-brand__logo {
  width: 42px;
  height: 42px;
  object-fit: contain;
}

.form-banner {
  width: 100%;
  padding: 10px 14px;
  margin-bottom: 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.45;
  text-align: center;
}

.form-banner--error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #b91c1c;
}

.form-banner--success {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #166534;
}

h1 {
  font-size: clamp(1.8rem, 1.45rem + 1vw, 2.1rem);
  font-weight: 700;
  color: #171717;
  letter-spacing: -0.03em;
  line-height: 1.15;
  white-space: nowrap;
}

.field-label {
  display: block;
  font-size: 1rem;
  font-weight: 700;
  color: #171717;
  margin-top: 26px;
  margin-bottom: 10px;
}

.input-wrap {
  width: 100%;
  height: 62px;
  display: flex;
  align-items: center;
  border: 1px solid #d4d4d8;
  border-radius: 999px;
  background: #fff;
  padding: 0 22px;
  margin-bottom: 22px;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
}

.input-wrap.focused {
  border-color: #52525b;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
}

.input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 1.1rem;
  color: #171717;
  height: 100%;
}

.input-wrap input::placeholder {
  color: #a1a1aa;
}

.eye-btn {
  background: none;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #71717a;
  width: 32px;
  height: 32px;
}

.action-row {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 26px;
  font-size: 0.98rem;
}

.checkbox-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #52525b;
  font-weight: 500;
  user-select: none;
}

.checkbox-wrap input {
  display: none;
}

.checkmark {
  width: 16px;
  height: 16px;
  border: 1.5px solid #171717;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease;
  flex-shrink: 0;
}

.checkmark.checked {
  background: #171717;
}

.forgot-link,
.signup-link {
  border: none;
  background: none;
  color: #171717;
  font-weight: 700;
  cursor: pointer;
}

.register-hint {
  margin-bottom: 26px;
  font-size: 0.95rem;
  color: #737373;
}

.login-btn {
  width: 100%;
  height: 72px;
  border: none;
  border-radius: 999px;
  background: #171717;
  color: white;
  font-size: 1.22rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s ease, opacity 0.2s ease;
}

.login-btn:hover:not(:disabled) {
  background: #000;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.signup-row {
  margin-top: 24px;
  font-size: 1rem;
  color: #737373;
  text-align: center;
}

@keyframes form-reveal {
  from {
    opacity: 0;
    transform: translateY(24px) scale(0.985);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 1080px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .left-panel {
    min-height: 360px;
    padding: 40px 32px 24px;
  }

  .characters-stage {
    height: 360px;
  }

  .left-footer-spacer {
    height: 16px;
  }

  .form-panel {
    min-height: auto;
    width: 100%;
    max-width: 560px;
    padding: 40px 28px 52px;
  }
}

@media (max-width: 720px) {
  .left-panel {
    display: none;
  }

  .form-panel {
    padding: 32px 20px 42px;
  }

  .mobile-brand {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 24px;
    font-weight: 700;
    color: #171717;
  }

  h1 {
    white-space: normal;
  }

  .input-wrap {
    height: 58px;
    padding: 0 18px;
    margin-bottom: 18px;
  }

  .input-wrap input {
    font-size: 1rem;
  }

  .action-row,
  .register-hint {
    font-size: 0.92rem;
  }

  .login-btn {
    height: 64px;
    font-size: 1.12rem;
  }
}
</style>
