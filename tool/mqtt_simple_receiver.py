#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import paho.mqtt.client as mqtt
import json
import time
import sys

# 設定MQTT連接參數
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
MQTT_CLIENT_ID = "mqtt-simple-receiver"

# 接收到的消息計數
message_count = 0

# 當連接到MQTT代理成功時的回調函數
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print(f"已成功連接到MQTT伺服器: {MQTT_BROKER}:{MQTT_PORT}")
        
        # 訂閱所有相關主題
        client.subscribe("#")  # 訂閱所有主題
        print("已訂閱所有主題 (#)")
    else:
        print(f"連接失敗，返回碼: {rc}")

# 當接收到消息時的回調函數
def on_message(client, userdata, msg):
    global message_count
    message_count += 1
    
    print(f"\n收到消息 #{message_count}:")
    print(f"主題: {msg.topic}")
    
    # 嘗試解析JSON
    try:
        payload = msg.payload.decode("utf-8")
        json_data = json.loads(payload)
        print(f"JSON數據: {json.dumps(json_data, indent=2, ensure_ascii=False)}")
        
        # 提取並顯示特定數據類型的關鍵信息
        if "content" in json_data:
            content_type = json_data["content"]
            
            if content_type == "location":
                position = json_data.get("position", {})
                print(f"位置數據: X={position.get('x')}, Y={position.get('y')}, Z={position.get('z')}")
            
            elif content_type == "300B":
                print(f"健康數據: 心率={json_data.get('hr')}bpm, 血氧={json_data.get('SpO2')}%, "
                      f"血壓={json_data.get('bp syst')}/{json_data.get('bp diast')}mmHg, "
                      f"體溫={json_data.get('skin temp')}°C")
            
            elif "diaper" in content_type:
                print(f"尿布數據: 溫度={json_data.get('temp')}°C, 濕度={json_data.get('humi')}%")
            
            else:
                print(f"消息類型: {content_type}")
    except:
        print(f"原始数据 (非JSON): {msg.payload}")
    
    print("-" * 50)

# 主函數
def main():
    # 創建客戶端實例
    client = mqtt.Client(client_id=MQTT_CLIENT_ID)
    
    # 設置回調函數
    client.on_connect = on_connect
    client.on_message = on_message
    
    try:
        # 連接到MQTT伺服器
        print(f"正在連接到MQTT伺服器 {MQTT_BROKER}:{MQTT_PORT}...")
        client.connect(MQTT_BROKER, MQTT_PORT, 60)
        
        # 開始網絡循環
        print("接收器已啟動，等待消息...(按Ctrl+C停止)")
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
            print(f"\n總共接收到 {message_count} 條消息")
            print("接收器已停止")
    
    except Exception as e:
        print(f"發生錯誤: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    sys.exit(main())