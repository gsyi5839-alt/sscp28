<template>
  <div class="system-closed-overlay">
    <img src="@/assets/通用/bg.png" alt="系统封盘" class="system-closed-bg" />
  </div>
</template>

<style scoped>
.system-closed-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
}

.system-closed-bg {
  width: 500px;
  max-width: 90%;
  opacity: 0.9;
}
</style>
