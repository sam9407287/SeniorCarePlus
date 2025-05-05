#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import paho.mqtt.client as mqtt
import json
import time
import sys
import argparse
from datetime import datetime

# 默認MQTT連接參數
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
MQTT_KEEPALIVE = 60
MQTT_CLIENT_ID = "mqtt-receiver-python"

# 接收到的消息計數
message_count = 0

# 存儲最近接收的消息
recent_messages = []
MAX_RECENT_MESSAGES = 10

# 當連接到MQTT代理成功時的回調函數
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print(f"已成功連接到MQTT伺服器: {MQTT_BROKER}:{MQTT_PORT}")
        
        # 使用通配符訂閱多個主題
        topics = [
            "GW+_Loca",         # 位置數據
            "GW+_Message",      # 消息數據
            "GW+_Health",       # 健康數據
            "GW+_Ack",          # 確認消息
            "UWB_Gateway",      # Gateway主題
            "#"                 # 所有主題（作為備選，可以註釋掉）
        ]
        
        for topic in topics:
            client.subscribe(topic)
            print(f"已訂閱主題: {topic}")
    else:
        print(f"連接失敗，返回碼: {rc}")
        # 連接失敗的返回碼意義：
        # 0: 連接成功
        # 1: 協議版本錯誤
        # 2: 無效的客戶端標識符
        # 3: 伺服器不可用
        # 4: 錯誤的用戶名或密碼
        # 5: 未授權
        # 6-255: 目前未使用

# 當接收到消息時的回調函數
def on_message(client, userdata, msg):
    global message_count, recent_messages
    
    # 獲取當前時間戳
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]
    
    # 增加消息計數
    message_count += 1
    
    # 嘗試解析JSON
    try:
        payload = msg.payload.decode("utf-8")
        json_data = json.loads(payload)
        parsed = True
    except:
        json_data = None
        parsed = False
    
    # 記錄消息
    message_info = {
        "timestamp": timestamp,
        "topic": msg.topic,
        "payload": payload if not parsed else json.dumps(json_data, indent=2, ensure_ascii=False),
        "parsed": parsed
    }
    
    # 添加到最近消息列表
    recent_messages.append(message_info)
    if len(recent_messages) > MAX_RECENT_MESSAGES:
        recent_messages.pop(0)
    
    # 輸出消息摘要
    print(f"\n[{timestamp}] 收到消息 #{message_count}:")
    print(f"主題: {msg.topic}")
    
    # 根據是否成功解析JSON顯示不同的信息
    if parsed:
        # 提取關鍵信息用於簡明顯示
        content = json_data.get("content", "未知內容")
        node_type = json_data.get("node", "未知節點")
        node_id = json_data.get("id", "未知ID")
        
        print(f"內容類型: {content}")
        print(f"節點類型: {node_type}")
        print(f"節點ID: {node_id}")
        
        # 如果是位置數據，顯示位置信息
        if content == "location" and "position" in json_data:
            pos = json_data["position"]
            print(f"位置: X={pos.get('x')}, Y={pos.get('y')}, Z={pos.get('z')}, 品質={pos.get('quality')}")
        
        # 如果是健康數據，顯示關鍵健康指標
        elif content == "300B":
            print(f"心率: {json_data.get('hr', '未知')} bpm")
            print(f"血氧: {json_data.get('SpO2', '未知')}%")
            print(f"血壓: {json_data.get('bp syst', '未知')}/{json_data.get('bp diast', '未知')} mmHg")
            print(f"體溫: {json_data.get('skin temp', '未知')}°C")
        
        # 如果是尿布數據，顯示關鍵指標
        elif "diaper" in content:
            print(f"溫度: {json_data.get('temp', '未知')}°C")
            print(f"濕度: {json_data.get('humi', '未知')}%")
            print(f"按鈕狀態: {json_data.get('button', '未知')}")
        
        print(f"完整數據: \n{json.dumps(json_data, indent=2, ensure_ascii=False)}")
    else:
        # 非JSON數據，直接顯示原始負載
        print(f"原始數據: {payload}")
    
    print("-" * 80)

# 顯示使用幫助
def print_help():
    print("""
MQTT接收器 (Python版本)
使用方法:
    python mqtt_receiver_python.py [選項]

選項:
    -h, --help              顯示此幫助信息
    -b, --broker ADDRESS    設置MQTT伺服器地址 (默認: localhost)
    -p, --port PORT         設置MQTT伺服器端口 (默認: 1883)
    -t, --topic TOPIC       設置要訂閱的主題 (可多次使用, 默認: 多個關鍵主題)
    
按 Ctrl+C 退出程序
    """)

# 主函數
def main():
    global MQTT_BROKER, MQTT_PORT, MQTT_CLIENT_ID
    
    # 解析命令行參數
    parser = argparse.ArgumentParser(description="MQTT接收器 (Python版本)")
    parser.add_argument("-b", "--broker", help="MQTT伺服器地址", default=MQTT_BROKER)
    parser.add_argument("-p", "--port", type=int, help="MQTT伺服器端口", default=MQTT_PORT)
    parser.add_argument("-t", "--topic", action="append", help="要訂閱的主題 (可多次使用)")
    parser.add_argument("--client-id", help="客戶端ID", default=MQTT_CLIENT_ID)
    
    args = parser.parse_args()
    
    # 更新連接參數
    MQTT_BROKER = args.broker
    MQTT_PORT = args.port
    MQTT_CLIENT_ID = args.client_id
    
    # 創建客戶端實例
    client = mqtt.Client(client_id=MQTT_CLIENT_ID)
    
    # 設置回調函數
    client.on_connect = on_connect
    client.on_message = on_message
    
    try:
        # 連接到MQTT伺服器
        print(f"正在連接到MQTT伺服器 {MQTT_BROKER}:{MQTT_PORT}...")
        client.connect(MQTT_BROKER, MQTT_PORT, MQTT_KEEPALIVE)
        
        # 如果指定了主題，直接訂閱
        if args.topic:
            for topic in args.topic:
                client.subscribe(topic)
                print(f"已訂閱主題: {topic}")
        
        # 開始網絡循環
        print("接收器已啟動，等待消息...")
        client.loop_start()
        
        # 保持程序運行，直到用戶中斷
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            print("\n用戶中斷，停止接收器...")
        finally:
            client.loop_stop()
            client.disconnect()
            
            # 顯示接收摘要
            print(f"\n接收摘要:")
            print(f"共接收到 {message_count} 條消息")
            if recent_messages:
                print(f"最近 {len(recent_messages)} 條消息:")
                for i, msg in enumerate(recent_messages):
                    print(f"{i+1}. [{msg['timestamp']}] 主題: {msg['topic']}")
            
            print("接收器已停止")
    
    except Exception as e:
        print(f"發生錯誤: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    sys.exit(main())