const API_ORIGIN = (import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080').replace(/\/$/, '');
const API_BASE_URL = `${API_ORIGIN}/api`;
const TOKEN_STORAGE_KEY = 'easyweibo_token';

type AuthMode = false | 'optional' | 'required';

let authToken: string | null = typeof window !== 'undefined'
  ? window.sessionStorage.getItem(TOKEN_STORAGE_KEY)
  : null;

const buildHeaders = (
  initHeaders: HeadersInit | undefined,
  authMode: AuthMode,
  isFormData: boolean,
): Headers => {
  const headers = new Headers(initHeaders);

  if (!isFormData && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (authMode) {
    if (!authToken && authMode === 'required') {
      throw new Error('请先登录');
    }

    if (authToken) {
      headers.set('Authorization', `Bearer ${authToken}`);
    }
  }

  return headers;
};

const parseResponse = async <T>(response: Response): Promise<T> => {
  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = payload?.message || payload?.error || '请求失败，请稍后重试';
    const error = new Error(message) as Error & { status?: number };
    error.status = response.status;
    throw error;
  }

  return payload as T;
};

export const apiRequest = async <T>(
  path: string,
  init: RequestInit = {},
  authMode: AuthMode = false,
): Promise<T> => {
  const isFormData = init.body instanceof FormData;
  const headers = buildHeaders(init.headers, authMode, isFormData);

  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers,
    });
  } catch {
    throw new Error(`无法连接后端，请确认 Spring Boot 正在 ${API_ORIGIN} 运行`);
  }

  return parseResponse<T>(response);
};

export const normalizeAssetUrl = (value: string) => {
  if (!value) {
    return value;
  }

  if (/^https?:\/\//i.test(value)) {
    return value;
  }

  return `${API_ORIGIN}${value.startsWith('/') ? value : `/${value}`}`;
};

export const getAuthToken = () => authToken;

export const setAuthToken = (token: string | null) => {
  authToken = token;

  if (typeof window === 'undefined') {
    return;
  }

  if (token) {
    window.sessionStorage.setItem(TOKEN_STORAGE_KEY, token);
  } else {
    window.sessionStorage.removeItem(TOKEN_STORAGE_KEY);
  }
};

export { API_ORIGIN };
