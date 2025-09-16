// 存储多个WebSocket连接 key为ID value为WebSocket对象
const wsConnections = {};
// 存储重连定时器
const reconnectTimers = {};
// 存储重连次数
const reconnectCounts = {};
// 最大重连次数
const MAX_RECONNECT_COUNT = 5;
// 存储连接配置信息
const connectionConfigs = {};

/**
 * 连接WebSocket
 * @param id
 * @param wsUrl
 * @param onMessage
 * @param onOpen
 * @param onClose
 */
export function connectWebSocket(id, wsUrl, onMessage, onOpen, onClose) {
    // 已有连接先断开
    if (wsConnections[id] && wsConnections[id].readyState === WebSocket.OPEN) {
        wsConnections[id].close();
    }
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    let socket;
    try {
        socket = new WebSocket(wsUrl);
    } catch (e) {
        // alert(e)
        socket = new WebSocket(`${wsProtocol}//${window.location.host}${wsUrl}`);
    }
    // 保存配置
    connectionConfigs[id] = {
        wsUrl,
        onMessage,
        onOpen,
        onClose
    }
    // 初始化重连计数
    reconnectCounts[id] = 0;
    // 连接成功回调
    socket.onopen = function (event) {
        console.log("WebSocket连接已建立");
        reconnectCounts[id] = 0;
        if (onOpen) onOpen(event);
    }
    // 接收消息回调
    socket.onmessage = function (event) {
        // console.log("收到消息: ", event.data);
        const message = event.data;
        if (onMessage) onMessage(message);
    }
    // 连接关闭回调
    socket.onclose = function (event) {
        // console.log("WebSocket连接已关闭");
        if (onClose) onClose(event);
        delete wsConnections[id];  // 从连接池移除
        // 尝试重连
        reconnect(id);
    }
    // 连接错误回调
    socket.onerror = function (error) {
        // alert(error);
        console.error(`WebSocker[${id}]连接错误: `, error);
    }

    wsConnections[id] = socket;
}

/**
 * 重连WebSocket
 * @param id
 */
function reconnect(id) {
    const config = connectionConfigs[id];
    if (!config) {
        console.error(`未找到WebSocket连接[${id}]的配置信息`);
        return;
    }

    if (reconnectCounts[id] >= MAX_RECONNECT_COUNT) {
        console.error(`WebSocket连接[${id}]重连失败，已达最大重连次数`);
        return;
    }

    reconnectCounts[id]++;
    console.log(`WebSocket连接[${id}]重连中... 第${reconnectCounts[id]}次`);

    // 清除之前的定时器
    if (reconnectTimers[id]) {
        clearTimeout(reconnectTimers[id]);
    }

    // 设置新的重连定时器
    reconnectTimers[id] = setTimeout(() => {
        connectWebSocket(id, config.wsUrl, config.onMessage, config.onOpen, config.onClose);
    }, 3000);
}

/**
 * 关闭WebSocket连接
 */
export function closeWebSocket(id) {
    if (id) {
        // 关闭特定连接
        if (wsConnections[id]) {
            wsConnections[id].close();
            delete wsConnections[id];
        }
        // 清除重连定时器
        if (reconnectTimers[id]) {
            clearTimeout(reconnectTimers[id]);
            delete reconnectTimers[id];
        }
        // 移除连接配置
        delete connectionConfigs[id];
        delete reconnectCounts[id];
    } else {
        // 关闭所有连接
        Object.keys(wsConnections).forEach(connId => {
            closeWebSocket(connId);
        });
    }
}

/**
 * 向特定连接发送消息
 * @param id 连接唯一标识符
 * @param message 消息内容
 */
export function sendMessage(id, message) {
    const socket = wsConnections[id];
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(message);
    } else {
        console.error(`WebSocket连接[${id}]未连接或已关闭，无法发送消息`);
    }
}

/**
 * 获取特定连接的状态
 * @param id 连接唯一标识符
 * @returns {number|null} WebSocket状态码，或null表示连接不存在
 */
export function getConnectionState(id) {
    const socket = wsConnections[id];
    return socket ? socket.readyState : null;
}