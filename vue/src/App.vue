<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const isMenuOpen = ref(false);

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;
};

const closeMenu = () => {
  isMenuOpen.value = false;
};
</script>

<template>
  <div id="app">
    <header class="app-header">
      <div class="header-content">
        <div class="logo-section">
          <router-link to="/" class="logo">
            <span class="logo-icon">🎮</span>
            <h1>CSGO 统计分析</h1>
          </router-link>
          <button class="menu-toggle" @click="toggleMenu">
            <span></span><span></span><span></span>
          </button>
        </div>
        <nav :class="['main-nav', { 'menu-active': isMenuOpen }]">
          <router-link to="/" class="nav-link" @click="closeMenu">
            <span class="nav-icon">🏠</span>
            <span class="nav-text">首页</span>
          </router-link>
          <router-link to="/statistics" class="nav-link" @click="closeMenu">
            <span class="nav-icon">📊</span>
            <span class="nav-text">市场统计</span>
          </router-link>
        </nav>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
    <footer class="app-footer">
      <div>© {{ new Date().getFullYear() }} CSGO 统计数据分析平台</div>
    </footer>
  </div>
</template>

<style>
:root {
  --primary-color: #1b2838;
  --secondary-color: #66c0f4;
  --accent-color: #171a21;
  --text-light: #ffffff;
  --text-dark: #1b2838;
  --background-light: #f5f7fa;
  --card-bg: #ffffff;
  --success-color: #66bb6a;
  --warning-color: #ffa726;
  --error-color: #ef5350;
  --border-radius: 8px;
  --box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  --transition: all 0.3s ease;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: var(--text-dark);
  background: var(--background-light);
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: var(--primary-color);
  color: var(--text-light);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
}

.logo-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  text-decoration: none;
  color: var(--text-light);
}

.logo-icon {
  font-size: 1.8rem;
}

.logo h1 {
  font-size: 1.3rem;
  font-weight: 600;
}

.menu-toggle {
  display: none;
  background: transparent;
  border: none;
  cursor: pointer;
  flex-direction: column;
  gap: 5px;
  padding: 5px;
}

.menu-toggle span {
  display: block;
  width: 25px;
  height: 3px;
  background: var(--text-light);
  border-radius: 3px;
  transition: var(--transition);
}

.main-nav {
  display: flex;
  gap: 0.5rem;
}

.nav-link {
  color: var(--text-light);
  text-decoration: none;
  padding: 0.6rem 1rem;
  border-radius: var(--border-radius);
  transition: var(--transition);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.router-link-active {
  background: var(--secondary-color);
  color: var(--text-dark);
}

.app-main {
  flex: 1;
}

.app-footer {
  background: var(--accent-color);
  color: var(--text-light);
  text-align: center;
  padding: 1rem;
  margin-top: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .menu-toggle {
    display: flex;
  }

  .logo h1 {
    font-size: 1.1rem;
  }

  .main-nav {
    display: none;
    flex-direction: column;
    width: 100%;
    padding: 0.5rem 0;
  }

  .menu-active {
    display: flex;
  }

  .nav-link {
    padding: 0.8rem;
  }
}
</style>
