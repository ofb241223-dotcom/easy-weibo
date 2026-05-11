import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { initializeAuth } from './composables/useAuth';
import './index.css';

const bootstrap = async () => {
  await initializeAuth();

  const app = createApp(App);
  app.use(router);
  await router.isReady();

  const title = typeof router.currentRoute.value.meta.title === 'string'
    ? router.currentRoute.value.meta.title
    : 'Easy WeiBo';
  document.title = title.includes('Easy WeiBo') ? title : `${title} / Easy WeiBo`;

  app.mount('#app');
};

bootstrap();
