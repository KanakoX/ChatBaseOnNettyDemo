<script setup>
import {nextTick, onMounted, ref, watch} from "vue";
import {connectWebSocket, sendMessage} from "@/utils/websocket";
import {dateFormat} from "../utils/MyUtils";

const userId = "Kanako";

// 输入框内容
const chatEditText = ref("");

// 聊天区域内容（聊天信息+状态信息）
const allMessages = ref([]);

// 在线人数
const onlineCount = ref(0);

// 绑定聊天滚动区域
const bindChatScrollZone = ref();

// 发送
const submit = () => {
  sendMessage(1, chatEditText.value);
  chatEditText.value = "";
}

onMounted(() => {
  connectWebSocket(
      1,
      `ws://localhost:8081/ws/chat?userId=${userId}`,
      (message) => {
        const dataObject = JSON.parse(message);
        if (!dataObject.timestamp) dataObject.timestamp = new Date();
        allMessages.value.push(dataObject);
        allMessages.value.sort((a, b) => {
          return new Date(a.timestamp) - new Date(b.timestamp);
        });
        console.log(allMessages.value)
        if (dataObject.currentUsers) onlineCount.value = dataObject.currentUsers.length;
      }
  );
});

// 判断是否为状态消息
const isStatusMessage = (item) => {
  return item.type === "STATUS";
}

// 判断是否为自己的消息
const isMyselfMessage = (item) => {
  console.log(item.sender === userId)
  return item.sender === userId;
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick();
  setTimeout(() => {
    bindChatScrollZone.value.scrollTo({
      top: bindChatScrollZone.value.scrollHeight,
      behavior: "smooth"
    });
  }, 0);
}

// 处理状态信息
const handleStatusMessage = (item) => {
  const userId = item.userId;
  const status = item.data;
  if (status === "join") return `${userId} 进入聊天室`;
  return `${userId} 离开聊天室`;
}

// 监听allMessages变化，自动保存
watch(allMessages, () => {
  scrollToBottom();
}, { deep: true });

</script>

<template>
  <div class="body">
    <div class="chat-container">
<!--      header -->
      <div class="chat-header">
        <span>当前在线人数 ({{ onlineCount }})</span>
      </div>

<!--      scrollable -->
      <div class="chat-content" ref="bindChatScrollZone">
        <ul class="chat-list">
          <template v-for="(item, index) in allMessages" :key="index">
            <li class="status-item"
                v-if="isStatusMessage(item)"
            >
              {{ handleStatusMessage(item) }}
            </li>

            <li v-else class="chat-item" :class="{'isMyself': isMyselfMessage(item)}">
              <div class="info">
                <div class="content">{{ item.data }}</div>
                <div class="extra">
                  <span>{{ item.sender }}</span>
                  <span>{{ dateFormat(item.timestamp) }}</span>
                </div>
              </div>
            </li>
          </template>
        </ul>
      </div>

<!--      input submit area-->
      <div class="chat-input-box">
        <input type="text" v-model="chatEditText">
        <button @click="submit">发送</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.body {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  margin: 0;
  padding: 0 10px;
}

.chat-container {
  position: relative;
  width: 100%;
  max-width: 600px;
  height: 100vh;
  max-height: 100vh;
  display: flex;
  flex-direction: column;
  margin: 0 auto;
}

.chat-header {
  flex-shrink: 0;
  width: 100%;
  height: 60px;
  background: linear-gradient(-45deg, #B3E5FC, #4FC3F7);
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-header span {
  font-size: 24px;
  transform: translateY(-10%);
}

.chat-content {
  flex: 1;
  border-radius: 12px;
  background: #fff;
  margin: 10px 0;
  padding: 10px 12px;
  overflow-y: auto;
  scrollbar-width: thin;
}

.chat-list {
  list-style: none;
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: center;
}

.chat-item {
  flex: initial;
  max-width: 70%;
  padding: 12px 16px;
  position: relative;
  line-height: 1.5;
  box-shadow: 0 2px 10px rgba(0, 0, 0, .05);

  align-self: flex-start;
  background: white;
  border-radius: 4px 18px 18px 18px;
}

.chat-item.isMyself {
  border-radius: 18px 4px 18px 18px;
  background: linear-gradient(-45deg, #4FC3F7, #4ff7f7);
  align-self: flex-end;
  color: #ffffff;
}

.chat-item .info {
  display: flex;
  flex-direction: column;
}

.chat-item .info .content {
  font-size: 24px;
  font-weight: bold;
  word-break: break-word;
  break-after: column;
}

.chat-item .info .extra {
  display: flex;
  justify-content: space-between;
  gap: 10px;

  span {
    font-size: 12px;
    font-weight: lighter;
  }
}

.chat-input-box {
  width: 100%;
  height: 60px;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
  padding: 10px;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 20px;
}

.chat-input-box input {
  flex: 1;
  border: 1px solid #bcbcbc;
  border-radius: 24px;
  padding: 12px 18px;
  outline: none;
  transition: border-color .5s;
  font-size: 16px;
  height: 40px;
}

.chat-input-box button {
  background: #4FC3F7;
  border: none;
  border-radius: 12px;
  color: #fff;
  padding: 8px 12px;
  font-size: 12px;
  height: 40px;
  cursor: pointer;
}

</style>