<script setup>
import {nextTick, onMounted, onUnmounted, ref, watch} from "vue";
import {closeWebSocket, connectWebSocket, sendMessage} from "@/utils/websocket";
import {dateFormat} from "@/utils/MyUtils";
import axios from "axios";

const username = ref("未登录");

// 输入框内容
const chatEditText = ref("");

// 聊天区域内容（聊天信息+状态信息）
const allMessages = ref([]);

// 在线人数
const onlineCount = ref(0);

// 绑定聊天滚动区域
const bindChatScrollZone = ref();

// 登录注册参数
const params = ref({
  username: "",
  password: ""
});

// cover组件是否可见
const isCoverVisible = ref(true);

// 发送
const submit = () => {
  sendMessage(1, chatEditText.value);
  chatEditText.value = "";
}

onMounted(() => {
  // console.log(getUser());
  // 用户有登录记录
  if (getUser()) {
    isCoverVisible.value = false;
    connectws(getUser().id);
    nextTick(() => {
      username.value = getUser().username;
    });
  }
});

// 连接WebSocket
const connectws = (userId) => {
  connectWebSocket(
      1,
      `${WS_BASE_URL}/chat?userId=${userId}`,
      (message) => {
        const dataObject = JSON.parse(message);
        if (!dataObject.timestamp) dataObject.timestamp = new Date();
        allMessages.value.push(dataObject);
        allMessages.value.sort((a, b) => {
          return new Date(a.timestamp) - new Date(b.timestamp);
        });
        // console.log(allMessages.value)
        if (dataObject.currentUsers) onlineCount.value = dataObject.currentUsers.length;
      }
  );
}

// 判断是否为状态消息
const isStatusMessage = (item) => {
  return item.type === "STATUS";
}

// 判断是否为正在聊天消息
const isChattingMessage = (item) => {
  return item.type === "CHAT";
}

// 判断是否为自己的消息
const isMyselfMessage = (item) => {
  // console.log(item.senderInfo.username, username)
  return item.senderInfo.username === username.value;
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
  // console.log(item)
  const username = item.userInfo.username;
  const status = item.data;
  if (status === "join") return `${username} 进入聊天室`;
  return `${username} 离开聊天室`;
}

// 监听allMessages变化，自动保存
watch(allMessages, () => {
  scrollToBottom();
}, { deep: true });

// 登录按钮
const loginBtn = async () => {
  const response = await axios.post(`${HTTP_BASE_URL}/user/login`, params.value);
  if (response.data.code === 200) {
    // console.log(response.data.data);
    localStorage.setItem("user", JSON.stringify(response.data.data));
    username.value = response.data.data.username
    isCoverVisible.value = false;
    connectws(response.data.data.id);
    params.value = {username: "", password: ""};
  } else {
    alert(response.data.message);
  }
}

// 注册按钮
const registerBtn = async () => {
  const response = await axios.post(`${HTTP_BASE_URL}/user/register`, params.value);
  if (response.data.code === 200) {
    alert("成功");
  } else {
    alert(response.data.message);
  }
}

// 退出登录
const logout = () => {
  localStorage.removeItem("user");
  isCoverVisible.value = true;
  closeWebSocket(1);
  username.value = "未登录";
}

onUnmounted(() => {
  closeWebSocket(1);
});

</script>

<template>
  <div class="body">

    <div class="cover" v-if="isCoverVisible">
      <div class="login-card">
        <div class="input-group">
          <label for="username">用户名: </label>
          <input type="text" id="username" v-model="params.username">
        </div>
        <div class="input-group">
          <label for="password">密码: </label>
          <input type="password" id="password" v-model="params.password">
        </div>
        <div class="button-container">
          <button @click="loginBtn">登录</button>
          <button @click="registerBtn" style="margin-left: 50px">注册</button>
        </div>
      </div>
    </div>

    <div class="chat-container">
<!--      header -->
      <div class="chat-header">
        <span>当前在线人数 ({{ onlineCount }})</span>
        <span class="user" @click="logout">{{ username }}</span>
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

            <li v-else-if="isChattingMessage(item)" class="chat-item" :class="{'isMyself': isMyselfMessage(item)}">
              <div class="info">
                <div class="content">{{ item.data }}</div>
                <div class="extra">
                  <span>{{ item.senderInfo.username }}</span>
                  <span>{{ dateFormat(item.timestamp) }}</span>
                </div>
              </div>
            </li>

            <li v-else v-for="historyItem in item.data" class="chat-item" :class="{'isMyself': isMyselfMessage(historyItem)}">
              <div class="info">
                <div class="content">{{ historyItem.content }}</div>
                <div class="extra">
                  <span>{{ historyItem.senderInfo.username }}</span>
                  <span>{{ dateFormat(historyItem.createdAt) }}</span>
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

.cover {
  width: 100%;
  height: 100%;
  position: absolute;
  background: rgba(243, 244, 245, .5);
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 20px 0 rgba(0, 0, 0, .3);
}

.login-card .input-group {
  position: relative;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  label {
    width: 80px;
    text-align: right;
    padding-right: 15px;
    color: #555;
    font-size: 16px;
  }
  input {
    flex: 1;
    padding: 12px 15px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 16px;
    outline: none;
  }
  input:focus {
    border-color: #00ffff;
    box-shadow: 2px 2px 20px 0 rgb(0, 255, 255, .2);
  }
}

.button-container {
  width: 100%;
  display: flex;
  justify-content: space-around;
  button {
    background-color: #4a86e8;
    color: white;
    border: none;
    border-radius: 6px;
    padding: 10px 20px;
    font-size: 16px;
    cursor: pointer;
    transition: background-color .3s;
  }
  button:hover {
    background-color: #3a76d8;
  }
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

.chat-header .user {
  position: absolute;
  right: 10px;
  font-size: 16px;
  cursor: pointer;
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