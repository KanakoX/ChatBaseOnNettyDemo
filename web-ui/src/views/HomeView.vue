<script setup>
import {onMounted, ref} from "vue";
import {connectWebSocket, sendMessage} from "@/utils/websocket";

const chatEditText = ref("");

const submit = () => {
  sendMessage(1, chatEditText.value);
  chatEditText.value = "";
}

onMounted(() => {
  connectWebSocket(
      1,
      "ws://localhost:8081/ws/chat?userId=Kanako",
      (message) => {
        const data = JSON.parse(message);
        console.log(data);
      }
  );
});
</script>

<template>
  <div class="body">
    <div>
      <input type="text" v-model="chatEditText">
      <button @click="submit">发送</button>
    </div>
  </div>
</template>

<style scoped>
.body {
  width: 100%;
  height: 100%;
  background: rgba(243, 244, 245);
}
</style>