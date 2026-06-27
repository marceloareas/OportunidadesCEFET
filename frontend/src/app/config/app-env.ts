export interface AppEnv {
  apiBaseUrl: string;
}

declare global {
  interface Window {
    __APP_ENV__?: Partial<AppEnv>;
  }
}

const defaultEnv: AppEnv = {
  apiBaseUrl: '/api',
};

function resolveAppEnv(): AppEnv {
  const runtimeEnv = typeof window !== 'undefined' ? window.__APP_ENV__ : undefined;
  return {
    ...defaultEnv,
    ...(runtimeEnv || {}),
  };
}

export const APP_ENV = resolveAppEnv();
export const API_BASE_URL = APP_ENV.apiBaseUrl.replace(/\/$/, '');
