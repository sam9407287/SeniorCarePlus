#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import paho.mqtt.client as mqtt
import ssl
import time
import uuid
import json
import argparse
import threading
from datetime import datetime

# 預設連接參數
DEFAULT_BROKER = "067ec32ef1344d3bb20c4e53abdde99a.s1.eu.hivemq.cloud"
DEFAULT_PORT = 8884
DEFAULT_USERNAME = "testweb1"
DEFAULT_PASSWORD = "Aa000000"
DEFAULT_TOPICS = ["GW17F5_Loca", "GW17F5_Health", "UWB/GW16B8_Loca", "#"]  # 添加萬用字符以獲取所有消息
DEFAULT_CLIENT_ID_PREFIX = "PyMQTT_Test_"
DEFAULT_QOS = 1
DEFAULT_TEST_DURATION = 300  # 測試5分鐘
# 使用WebSocket時的路徑
DEFAULT_PATH = "/mqtt"

# 統計信息
stats = {
    "connected": False,
    "connect_time": None,
    "last_message_time": None,
    "messages_received": 0,
    "reconnects": 0,
    "connection_errors": [],
    "topics_with_messages": set()
}

# 最近收到的消息
recent_messages = []
MAX_RECENT_MESSAGES = 10

# 當連接成功時的回調
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print(f"\n[{datetime.now().strftime('%H:%M:%S.%f')[:-3]}] 已成功連接到MQTT服務器 {args.broker}:{args.port}")
        print(f"客戶端ID: {client._client_id.decode('utf-8')}")
        stats["connected"] = True
        stats["connect_time"] = datetime.now()
        
        # 訂閱主題
        for topic in args.topics:
            client.subscribe(topic, qos=args.qos)
            print(f"已訂閱主題: {topic} (QoS {args.qos})")
    else:
        connection_messages = {
            0: "連接成功",
            1: "連接被拒絕 - 協議版本不正確",
            2: "連接被拒絕 - 無效客戶端ID",
            3: "連接被拒絕 - 服務器不可用",
            4: "連接被拒絕 - 用戶名或密碼不正確",
            5: "連接被拒絕 - 未授權"
        }
        error_msg = connection_messages.get(rc, f"未知錯誤碼: {rc}")
        print(f"\n[{datetime.now().strftime('%H:%M:%S.%f')[:-3]}] 連接失敗: {error_msg}")
        stats["connection_errors"].append((datetime.now(), error_msg))

# 當斷開連接時的回調
def on_disconnect(client, userdata, rc):
    stats["connected"] = False
    if rc == 0:
        reason = "正常斷開"
    else:
        reason = f"非預期斷開 (錯誤碼: {rc})"
    
    connect_duration = None
    if stats["connect_time"]:
        connect_duration = (datetime.now() - stats["connect_time"]).total_seconds()
        
    print(f"\n[{datetime.now().strftime('%H:%M:%S.%f')[:-3]}] MQTT連接已斷開: {reason}")
    if connect_duration:
        print(f"連接持續時間: {connect_duration:.1f} 秒")
    
    # 如果不是用戶主動斷開，嘗試重新連接
    if rc != 0 and not stop_event.is_set():
        stats["reconnects"] += 1
        print(f"嘗試重新連接... (第{stats['reconnects']}次)")
        try:
            client.reconnect()
        except Exception as e:
            print(f"重新連接失敗: {str(e)}")
            stats["connection_errors"].append((datetime.now(), str(e)))

# 當收到消息時的回調
def on_message(client, userdata, msg):
    stats["messages_received"] += 1
    stats["last_message_time"] = datetime.now()
    stats["topics_with_messages"].add(msg.topic)
    
    timestamp = datetime.now().strftime("%H:%M:%S.%f")[:-3]
    
    try:
        payload = msg.payload.decode("utf-8")
        try:
            json_payload = json.loads(payload)
            payload = json.dumps(json_payload, indent=2, ensure_ascii=False)
            is_json = True
        except:
            is_json = False
    except:
        payload = str(msg.payload)
        is_json = False
    
    message_info = {
        "timestamp": timestamp,
        "topic": msg.topic,
        "payload": payload,
        "is_json": is_json
    }
    
    # 添加到最近消息
    recent_messages.append(message_info)
    if len(recent_messages) > MAX_RECENT_MESSAGES:
        recent_messages.pop(0)
    
    # 輸出消息信息
    print(f"\n[{timestamp}] 收到消息 #{stats['messages_received']}:")
    print(f"主題: {msg.topic}")
    print(f"QoS: {msg.qos}")
    print(f"是JSON: {is_json}")
    
    # 限制輸出長度
    max_payload_display = 500
    if len(payload) > max_payload_display:
        print(f"內容 (部分): {payload[:max_payload_display]}...")
    else:
        print(f"內容: {payload}")

# 當發布消息時的回調
def on_publish(client, userdata, mid):
    print(f"[{datetime.now().strftime('%H:%M:%S.%f')[:-3]}] 消息已發布: mid={mid}")

# 當日誌消息產生時的回調
def on_log(client, userdata, level, buf):
    if args.verbose:
        print(f"[{datetime.now().strftime('%H:%M:%S.%f')[:-3]}] LOG: {buf}")

# 顯示統計信息的線程
def display_stats():
    while not stop_event.is_set():
        time.sleep(10)  # 每10秒顯示一次統計
        if stats["connected"]:
            connected_duration = (datetime.now() - stats["connect_time"]).total_seconds()
            print(f"\n--- 連接統計 ---")
            print(f"連接狀態: {'已連接' if stats['connected'] else '未連接'}")
            print(f"連接時間: {stats['connect_time'].strftime('%H:%M:%S')}")
            print(f"已連接: {connected_duration:.1f} 秒")
            print(f"接收消息數: {stats['messages_received']}")
            print(f"已收到消息的主題: {', '.join(stats['topics_with_messages']) if stats['topics_with_messages'] else '無'}")
            if stats["last_message_time"]:
                last_msg_time = (datetime.now() - stats["last_message_time"]).total_seconds()
                print(f"最後消息接收: {last_msg_time:.1f} 秒前")
            print(f"重新連接次數: {stats['reconnects']}")
            print("-----------------")

# 測試發布消息
def publish_test_message(client):
    if stats["connected"]:
        test_topic = "test/python_client"
        test_payload = json.dumps({
            "client_id": client._client_id.decode('utf-8'),
            "timestamp": datetime.now().isoformat(),
            "test_message": "This is a test message"
        })
        result = client.publish(test_topic, test_payload, qos=args.qos)
        print(f"\n[{datetime.now().strftime('%H:%M:%S.%f')[:-3]}] 發布測試消息到 {test_topic}")
        return result.rc == mqtt.MQTT_ERR_SUCCESS
    return False

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='MQTT HiveMQ雲連接測試工具')
    
    parser.add_argument('-b', '--broker', default=DEFAULT_BROKER,
                      help='MQTT代理主機地址')
    parser.add_argument('-p', '--port', type=int, default=DEFAULT_PORT,
                      help='MQTT代理端口')
    parser.add_argument('-u', '--username', default=DEFAULT_USERNAME,
                      help='MQTT用戶名')
    parser.add_argument('-P', '--password', default=DEFAULT_PASSWORD,
                      help='MQTT密碼')
    parser.add_argument('-t', '--topics', nargs='+', default=DEFAULT_TOPICS,
                      help='要訂閱的主題列表')
    parser.add_argument('-q', '--qos', type=int, default=DEFAULT_QOS,
                      help='QoS級別')
    parser.add_argument('-c', '--client-id', 
                      default=DEFAULT_CLIENT_ID_PREFIX + str(uuid.uuid4())[:8],
                      help='客戶端ID')
    parser.add_argument('-d', '--duration', type=int, default=DEFAULT_TEST_DURATION,
                      help='測試持續時間（秒）')
    parser.add_argument('-v', '--verbose', action='store_true',
                      help='顯示詳細日誌')
    parser.add_argument('--disable-tls', action='store_true',
                      help='禁用TLS/SSL連接')
    parser.add_argument('--websocket', action='store_true', default=True,
                      help='使用MQTT over WebSocket連接')
    parser.add_argument('--publish', action='store_true',
                      help='每30秒發布一次測試消息')
    
    args = parser.parse_args()

    # 初始化MQTT客戶端 (決定是否使用WebSocket)
    print(f"正在初始化MQTT客戶端...")
    if args.websocket:
        print("使用MQTT over WebSocket協議")
        client = mqtt.Client(client_id=args.client_id, clean_session=True, transport="websockets")
    else:
        print("使用標準MQTT協議")
        client = mqtt.Client(client_id=args.client_id, clean_session=True)
    
    # 設置回調函數
    client.on_connect = on_connect
    client.on_disconnect = on_disconnect
    client.on_message = on_message
    client.on_publish = on_publish
    if args.verbose:
        client.on_log = on_log
    
    # 設置用戶名和密碼
    if args.username:
        client.username_pw_set(args.username, args.password)
        print(f"已配置用戶名和密碼")
    
    # 配置TLS
    if not args.disable_tls:
        print(f"配置TLS連接...")
        client.tls_set(cert_reqs=ssl.CERT_REQUIRED, tls_version=ssl.PROTOCOL_TLSv1_2)
        client.tls_insecure_set(False)  # 生產環境應設為False
    
    # 連接停止事件
    stop_event = threading.Event()
    
    # 啟動統計信息線程
    stats_thread = threading.Thread(target=display_stats)
    stats_thread.daemon = True
    stats_thread.start()
    
    try:
        # 連接到代理
        if args.websocket:
            # WebSocket版本需要指定路徑
            ws_path = DEFAULT_PATH
            print(f"正在通過WebSocket連接到MQTT服務器 {args.broker}:{args.port}{ws_path}...")
            client.connect(args.broker, args.port, keepalive=60)
        else:
            print(f"正在連接到MQTT服務器 {args.broker}:{args.port}...")
            client.connect(args.broker, args.port, keepalive=60)
        
        # 啟動MQTT循環
        client.loop_start()
        
        # 發布測試消息的線程
        if args.publish:
            def publish_loop():
                while not stop_event.is_set():
                    if stats["connected"]:
                        publish_test_message(client)
                    time.sleep(30)  # 每30秒發布一次
            
            publish_thread = threading.Thread(target=publish_loop)
            publish_thread.daemon = True
            publish_thread.start()
        
        # 等待測試時間結束
        test_end_time = time.time() + args.duration
        try:
            print(f"開始測試，將持續 {args.duration} 秒...")
            while time.time() < test_end_time and not stop_event.is_set():
                time.sleep(1)
        except KeyboardInterrupt:
            print("\n接收到終止信號，正在停止測試...")
        
        # 顯示最終統計和測試結果
        print("\n======== 測試結果 ========")
        print(f"測試持續時間: {min(time.time() - (test_end_time - args.duration), args.duration):.1f} 秒")
        print(f"連接狀態: {'已連接' if stats['connected'] else '未連接'}")
        if stats["connect_time"]:
            connected_duration = (datetime.now() - stats["connect_time"]).total_seconds()
            print(f"最後連接持續: {connected_duration:.1f} 秒")
        print(f"接收消息數: {stats['messages_received']}")
        print(f"重新連接次數: {stats['reconnects']}")
        print(f"已收到消息的主題: {', '.join(stats['topics_with_messages']) if stats['topics_with_messages'] else '無'}")
        
        if stats["connection_errors"]:
            print("\n連接錯誤:")
            for time, error in stats["connection_errors"]:
                print(f"  {time.strftime('%H:%M:%S')}: {error}")
        
        if recent_messages:
            print("\n最近收到的消息:")
            for i, msg in enumerate(recent_messages[-5:], 1):  # 只顯示最後5條
                print(f"  {i}. {msg['timestamp']} 主題:{msg['topic']}")
        
        print("==========================")
        
        # 停止測試
        stop_event.set()
        
    except Exception as e:
        print(f"發生錯誤: {str(e)}")
    finally:
        # 斷開連接
        print("正在斷開MQTT連接...")
        client.disconnect()
        client.loop_stop()
        print("測試完成")
