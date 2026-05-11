<script setup lang="ts">
import { useRoute } from 'vue-router';
import Sidebar from './Sidebar.vue';
import RightPanel from './RightPanel.vue';
import Navbar from './Navbar.vue';

const route = useRoute();
</script>

<template>
  <div class="min-h-screen bg-bg-primary text-text-primary">
    <Navbar />
    <div class="max-w-[1520px] mx-auto flex justify-center">
      <!-- Left Sidebar -->
      <div class="hidden md:block w-20 lg:w-64 sticky top-0 h-screen border-r border-border">
        <Sidebar />
      </div>

      <!-- Main Content -->
      <main class="flex-1 max-w-[920px] min-h-screen border-r border-border pb-20 md:pb-0">
        <router-view :key="route.fullPath" />
      </main>

      <!-- Right Panel -->
      <div class="hidden lg:block w-[360px] sticky top-0 h-screen p-4">
        <RightPanel />
      </div>
    </div>
  </div>
</template>
