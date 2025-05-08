#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import paho.mqtt.client as mqtt
import json
import time
import random
import threading
import math
from datetime import datetime

# MQTT設置
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
TOPIC_LOCATION = "GW17F5_Loca"
MQTT_CLIENT_ID = f"location_simulator_{random.randint(1000, 9999)}"

# 用戶設置
USERS = [
    {"id": "E001", "name": "張三", "position": {"x": 0.5, "y": 0.5, "quality": 95}, "gateway_id": 137205},
    {"id": "E002", "name": "李四", "position": {"x": 1.0, "y": 1.0, "quality": 90}, "gateway_id": 137205},
    {"id": "E003", "name": "王五", "position": {"x": 1.5, "y": 0.5, "quality": 85}, "gateway_id": 137205},
    {"id": "E004", "name": "趙六", "position": {"x": 0.5, "y": 1.5, "quality": 88}, "gateway_id": 137205},
    {"id": "E005", "name": "錢七", "position": {"x": 1.2, "y": 1.8, "quality": 92}, "gateway_id": 137205}
]

# 移動範圍設置
MIN_X = 0.1
MAX_X = 2.5
MIN_Y = 0.1
MAX_Y = 2.5
MOVE_STEP = 0.02  # 每次移動的最大距離

# 全局變量
running = True
client = None

def setup_mqtt():
    """設置MQTT客戶端"""
    global client
    client = mqtt.Client(client_id=MQTT_CLIENT_ID)
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()
    print(f"已連接到MQTT代理 {MQTT_BROKER}:{MQTT_PORT}")

def move_users():
    """移動所有用戶位置，每個用戶有特定的移動模式"""
    # 獲取基於時間的周期性因子，用於產生圓形和波浪運動
    time_factor = time.time() % (2 * 3.14159)  # 時間循環在0到2π之間
    sin_factor = math.sin(time_factor)
    cos_factor = math.cos(time_factor)
    
    for user in USERS:
        user_id = user["id"]
        
        if user_id == "E001":  # 張三 - 只上下移動
            # 保持X軸幾乎不變，Y軸做正弦波動
            move_x = random.uniform(-0.005, 0.005)  # 極小隨機偏移
            move_y = 0.05 * sin_factor  # 有規律的上下移動
        
        elif user_id == "E002":  # 李四 - 只左右移動
            # 保持Y軸幾乎不變，X軸做正弦波動
            move_x = 0.05 * cos_factor  # 有規律的左右移動
            move_y = random.uniform(-0.005, 0.005)  # 極小隨機偏移
        
        elif user_id == "E003":  # 王五 - 斜向移動
            # X和Y軸同時變化，形成斜向運動
            move_x = 0.03 * cos_factor
            move_y = 0.03 * sin_factor
        
        elif user_id == "E004":  # 趙六 - 幾乎不動
            # 極小的隨機移動
            move_x = random.uniform(-0.002, 0.002)
            move_y = random.uniform(-0.002, 0.002)
        
        elif user_id == "E005":  # 錢七 - 圓形移動
            # 使用正弦和餘弦函數產生圓形軌跡
            move_x = 0.04 * cos_factor
            move_y = 0.04 * sin_factor
        
        else:  # 其他用戶 - 隨機移動
            move_x = random.uniform(-MOVE_STEP, MOVE_STEP)
            move_y = random.uniform(-MOVE_STEP, MOVE_STEP)
        
        # 計算新位置
        new_x = user["position"]["x"] + move_x
        new_y = user["position"]["y"] + move_y
        
        # 確保在範圍內
        new_x = max(MIN_X, min(MAX_X, new_x))
        new_y = max(MIN_Y, min(MAX_Y, new_y))
        
        # 更新位置
        user["position"]["x"] = new_x
        user["position"]["y"] = new_y
        
        # 隨機改變信號質量
        user["position"]["quality"] = random.randint(75, 98)

def send_user_location(user):
    """為單個用戶發送位置數據"""
    data = {
        "content": "location",
        "gateway id": user["gateway_id"],
        "node": "TAG",
        "id": user["id"],
        "name": user["name"],
        "position": {
            "x": round(user["position"]["x"], 6),
            "y": round(user["position"]["y"], 6),
            "z": round(random.uniform(0, 1.0), 6),
            "quality": user["position"]["quality"]
        },
        "time": datetime.now().strftime("%Y-%j %H:%M:%S.%f")[:-4],
        "serial no": random.randint(0, 65535)
    }
    
    message = json.dumps(data)
    topic = TOPIC_LOCATION
    client.publish(topic, message, qos=1, retain=True)
    return data

def send_all_locations():
    """發送所有用戶的位置數據"""
    all_data = []
    for user in USERS:
        data = send_user_location(user)
        all_data.append(data)
        # 每個用戶之間短暫停頓
        time.sleep(0.1)
    return all_data

def simulation_loop():
    """持續移動和發送用戶位置的主循環"""
    global running
    iteration = 1
    
    try:
        while running:
            print(f"\n======== 迭代 #{iteration} ========")
            # 移動用戶
            move_users()
            
            # 發送所有位置
            all_data = send_all_locations()
            
            # 輸出簡潔的當前位置信息
            for data in all_data:
                print(f"用戶: {data['name']} (ID: {data['id']})")
                print(f"位置: X={data['position']['x']}, Y={data['position']['y']}, 質量={data['position']['quality']}")
                print("----------------------------")
            
            # 等待一段時間再移動
            print(f"等待1秒後進行下一次更新...")
            time.sleep(1)
            iteration += 1
            
    except KeyboardInterrupt:
        print("\n用戶中止了模擬。")
    except Exception as e:
        print(f"\n模擬中發生錯誤: {e}")
    finally:
        print("正在關閉MQTT連接...")
        client.loop_stop()
        client.disconnect()
        print("模擬結束。")

if __name__ == "__main__":
    print("開始位置模擬器 - 同時模擬5個用戶緩慢移動")
    print("按Ctrl+C停止")
    print("---------------------------------")
    
    # 設置MQTT
    setup_mqtt()
    
    # 開始模擬
    simulation_loop()
