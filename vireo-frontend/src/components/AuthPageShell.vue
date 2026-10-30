<script setup lang="ts">
import vireoLogo from '@/assets/vireo-logo.png'

defineProps<{
  title: string
  description: string
}>()
</script>

<template>
  <main class="auth-page-shell">
    <section class="auth-page-shell__card" aria-labelledby="auth-page-title">
      <div class="auth-page-shell__brand">
        <img class="auth-page-shell__logo" :src="vireoLogo" alt="Vireo" />
        <div>
          <span class="auth-page-shell__name">Vireo</span>
          <p class="auth-page-shell__intro">一个专注于内容创作与项目协作的数字工作空间。</p>
        </div>
      </div>

      <div class="auth-page-shell__heading">
        <h1 id="auth-page-title">{{ title }}</h1>
        <p>{{ description }}</p>
      </div>

      <slot />
    </section>
  </main>
</template>

<style scoped>
.auth-page-shell {
  --auth-primary: #1677ff;
  --auth-primary-hover: #4096ff;

  display: grid;
  min-height: calc(100vh - 64px);
  padding: 48px 20px;
  background-color: #f3f6f7;
  background-image:
    linear-gradient(#e6ecef 1px, transparent 1px),
    linear-gradient(90deg, #e6ecef 1px, transparent 1px);
  background-size: 32px 32px;
}

.auth-page-shell__card {
  width: min(100%, 440px);
  margin: auto;
  padding: 34px 40px 40px;
  border: 1px solid #d9e2e7;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 20px 50px rgb(22 119 255 / 10%);
}

.auth-page-shell__brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e6ecef;
}

.auth-page-shell__logo {
  display: block;
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.auth-page-shell__name {
  display: block;
  color: #18262f;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}

.auth-page-shell__intro {
  margin: 4px 0 0;
  color: #66747d;
  font-size: 13px;
  line-height: 1.5;
}

.auth-page-shell__heading {
  margin: 26px 0 22px;
}

.auth-page-shell__heading h1 {
  margin: 0;
  color: #18262f;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
}

.auth-page-shell__heading p {
  margin: 8px 0 0;
  color: #66747d;
  font-size: 14px;
  line-height: 1.6;
}

.auth-page-shell :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.auth-page-shell :deep(.ant-form-item-label > label) {
  color: #34454f;
  font-weight: 600;
}

.auth-page-shell :deep(.ant-input),
.auth-page-shell :deep(.ant-input-affix-wrapper) {
  min-height: 42px;
  border-color: #cbd7dd;
  border-radius: 6px;
}

.auth-page-shell :deep(.ant-input-affix-wrapper .ant-input) {
  min-height: auto;
}

.auth-page-shell :deep(.ant-input:hover),
.auth-page-shell :deep(.ant-input-affix-wrapper:hover) {
  border-color: var(--auth-primary-hover);
}

.auth-page-shell :deep(.ant-btn-primary) {
  height: 42px;
  border-color: var(--auth-primary);
  border-radius: 6px;
  background: var(--auth-primary);
  box-shadow: none;
  font-weight: 600;
}

.auth-page-shell :deep(.ant-btn-primary:not(:disabled):hover) {
  border-color: var(--auth-primary-hover);
  background: var(--auth-primary-hover);
}

.auth-page-shell :deep(.auth-switch) {
  margin: -2px 0 18px;
  color: #66747d;
  font-size: 13px;
  text-align: right;
}

.auth-page-shell :deep(.auth-switch a) {
  margin-left: 4px;
  color: var(--auth-primary);
  font-weight: 600;
}

.auth-page-shell :deep(.auth-switch a:hover) {
  color: var(--auth-primary-hover);
}

.auth-page-shell :deep(.auth-form .ant-form-item:last-child) {
  margin-bottom: 0;
}

@media (max-width: 600px) {
  .auth-page-shell {
    padding: 28px 16px;
  }

  .auth-page-shell__card {
    padding: 28px 22px 30px;
  }

  .auth-page-shell__brand {
    align-items: flex-start;
  }

  .auth-page-shell__heading h1 {
    font-size: 25px;
  }
}
</style>
