<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  document.title = '用户协议与规则'
  const favicon = document.getElementById('favicon') as HTMLLinkElement
  if (favicon) {
    favicon.href = 'data:,'
  }
})

// Disagree - return to member login page
const handleDisagree = () => {
  authStore.logout()
  router.push('/member/login')
}

// Agree - enter game home page
const handleAgree = () => {
  // Directly enter game home page
  router.push('/game')
}
</script>

<template>
  <div class="home-container">
    <!-- Top section: 1.png (980x634) - title and content box background -->
    <div class="home-wrapper">
      <!-- Content text area - placed inside content box -->
      <div class="home-content">
        <p>1，使用本公司网站的客户，请留意您所在的国家或居住地的相关法律规定，如有疑问应就相关问题，寻求当地法律意见。</p>
        
        <p>2、若发生遭黑客入侵破坏行为或不可抗拒之灾害导致网站故障或数据损坏、数据丢失等情况，我们将以本公司之后备数据为最后处理依据；为确保各方利益，请各会员投注后打印数据。本公司不会接受没有打印数据的投诉。</p>
        
        <p>3、为避免纠纷，各会员在投注之后，请务必进入下注状况检查及打印数据。若发现任何异常，请立即与代理商联系查证，一切投注将以本公司数据库的数据为准，不得异议。如出现特殊网络情况或线路不稳定导致不能下注或下注失败。本公司概不负责。</p>
        
        <p>4、单期同玩法最高派彩上限为<b class="color-red">二百万</b>。</p>
        
        <p>5、开奖结果以官方公布的结果为准。</p>
        
        <p>6、我们将竭力提供准确而可靠的开奖统计等数据，但并不保证数据绝对无误，统计数据只供参考，并非是对客户行为的指引，本公司也不接受关于统计数据产生错误而引起的相关投诉。</p>
        
        <p>7,本公司拥有一切判决及注销任何涉嫌以非正常方式下注之权利，在进行更深入调查期间将停止发放与其有关之任何彩金。客户有责任确保自己的帐户及密码保密，如果客户怀疑自己的资料被盗用，应立即通知本公司，并须更改其个人详细资料。所有被盗用帐号之损失将由客户自行负责。在某种特殊情况下，客人之信用额可能会透支。</p>
        
        <p class="mt10 text-right">管理层 敬启</p>
        
        <p class="text-center mt10">我了解以及同意下注列明的协定和规则。</p>
      </div>
    </div>

    <!-- Bottom section: 2.png (980x161) - buttons on image -->
    <div class="home-footer">
      <!-- Transparent click areas overlaid on image buttons -->
      <span class="btn-area btn-disagree-area" @click="handleDisagree"></span>
      <span class="btn-area btn-agree-area" @click="handleAgree"></span>
    </div>
  </div>
</template>

<style scoped>
/* Main container */
.home-container {
  min-height: 100vh;
  width: 100%;
  background-color: #1a0a00;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* Top section - background image 1 (980x634) */
.home-wrapper {
  width: 980px;
  height: 634px;
  background: url('@/assets/用户协议规则/1.png') center center no-repeat;
  background-size: 980px 634px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  position: relative;
}

/* Bottom section - background image 2 (980x161) - buttons on image */
.home-footer {
  width: 980px;
  height: 161px;
  background: url('@/assets/用户协议规则/2.png') center center no-repeat;
  background-size: 980px 161px;
  position: relative;
}

/* Transparent click areas */
.btn-area {
  position: absolute;
  width: 120px;
  height: 40px;
  cursor: pointer;
  top: 16px;
}

/* Disagree button click area - left side */
.btn-disagree-area {
  left: 355px;
}

/* Agree button click area - right side */
.btn-agree-area {
  left: 505px;
}

/* Content text area 540x416 */
.home-content {
  width: 540px;
  height: 416px;
  color: #FFFFFF;
  font-size: 12px;
  line-height: 1.6;
  overflow: hidden;
  margin-top: 140px;
}

.home-content p {
  margin: 0 0 8px 0;
  text-align: justify;
  text-shadow: none;
}

/* Blue highlighted text */
.color-blue {
  color: #00BFFF;
  font-weight: normal;
  text-decoration: underline;
}

/* Red highlighted text */
.color-red {
  color: #ff0000;
  font-weight: normal;
  text-decoration: underline;
}

/* Right align */
.text-right {
  text-align: right !important;
}

/* Center */
.text-center {
  text-align: center !important;
}

/* Top margin */
.mt10 {
  margin-top: 10px !important;
}

/* Mobile responsive */
@media screen and (max-width: 1024px) {
  .home-container {
    overflow-x: auto;
    align-items: flex-start;
    justify-content: flex-start;
    padding: 10px;
  }
  
  .home-wrapper,
  .home-footer {
    min-width: 980px;
  }
}
</style>
