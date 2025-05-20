#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import paho.mqtt.client as mqtt
import json
import time
import random
import math
import threading
from datetime import datetime

# MQTT設置
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
TOPIC_HEALTH = "GW17F5_Health"
MQTT_CLIENT_ID = f"temperature_simulator_{random.randint(1000, 9999)}"

# 用戶設置 - 確保與位置模擬器使用完全相同的ID和名稱
USERS = [
    {"id": "E001", "name": "張三", "gateway_id": 137205},
    {"id": "E002", "name": "李四", "gateway_id": 137205},
    {"id": "E003", "name": "王五", "gateway_id": 137205},
    {"id": "E004", "name": "趙六", "gateway_id": 137205},
    {"id": "E005", "name": "錢七", "gateway_id": 137205}
]

# 溫度範圍設置
MIN_TEMP = 35.5  # 最低體溫 (°C)
MAX_TEMP = 38.5  # 最高體溫 (°C)
NORMAL_TEMP_MIN = 36.3  # 正常體溫下限
NORMAL_TEMP_MAX = 37.2  # 正常體溫上限

# 發送頻率設置
LOCATION_INTERVAL = 1.0  # 位置數據發送間隔（秒）
TEMP_INTERVAL = LOCATION_INTERVAL * 10  # 體溫數據發送間隔（秒），是位置數據的10倍

# 全局變量
running = True
client = None
temperature_history = {}  # 用於存儲每個用戶的體溫歷史記錄
max_history_records = 100  # 每個用戶最多保存的歷史記錄數

def setup_mqtt():
    """設置MQTT客戶端"""
    global client
    client = mqtt.Client(client_id=MQTT_CLIENT_ID)
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()
    print(f"已連接到MQTT代理 {MQTT_BROKER}:{MQTT_PORT}")

def generate_temperature(user_id):
    """
    為指定用戶生成體溫數據
    - 大多數情況生成正常範圍內的體溫
    - 有10%機率生成偏高的體溫
    - 有5%機率生成偏低的體溫
    - 對於特定用戶，可以有特殊的體溫模式
    """
    # 時間因子用於產生週期性溫度變化
    time_factor = time.time() / 3600.0  # 小時為單位
    day_cycle = math.sin(time_factor * math.pi / 12)  # 24小時一個週期
    
    # 確定溫度基準值和波動範圍
    if user_id == "E001":  # 張三 - 有輕微發燒趨勢
        base_temp = 37.0 + day_cycle * 0.3
        variation = 0.2
        # 有20%機率產生發熱
        if random.random() < 0.2:
            return round(37.8 + random.random() * 0.7, 1)
    elif user_id == "E002":  # 李四 - 體溫較穩定
        base_temp = 36.6 + day_cycle * 0.2
        variation = 0.1
    elif user_id == "E003":  # 王五 - 偶爾低溫
        base_temp = 36.4 + day_cycle * 0.25
        variation = 0.2
        # 有15%機率產生低溫
        if random.random() < 0.15:
            return round(35.7 + random.random() * 0.4, 1)
    else:  # 其他用戶 - 標準模式
        base_temp = 36.5 + day_cycle * 0.3
        variation = 0.2
        
    # 隨機決定是否產生異常溫度
    r = random.random()
    if r < 0.05:  # 5% 機率產生低溫
        return round(random.uniform(MIN_TEMP, NORMAL_TEMP_MIN - 0.1), 1)
    elif r < 0.15:  # 10% 機率產生高溫
        return round(random.uniform(NORMAL_TEMP_MAX + 0.1, MAX_TEMP), 1)
    else:  # 85% 機率產生正常溫度
        return round(base_temp + random.uniform(-variation, variation), 1)

def send_temperature_data(user):
    """為單個用戶發送體溫數據"""
    user_id = user["id"]
    user_name = user["name"]
    gateway_id = user["gateway_id"]
    
    # 生成體溫數據
    skin_temp = generate_temperature(user_id)
    room_temp = round(random.uniform(22.0, 26.0), 1)  # 室溫
    
    # 保存到歷史記錄
    if user_id not in temperature_history:
        temperature_history[user_id] = []
    
    # 添加新記錄
    current_time = datetime.now().strftime("%Y-%j %H:%M:%S.%f")[:-4]  # 使用與位置模擬器相同的時間格式
    temperature_history[user_id].append({
        "temperature": skin_temp,
        "timestamp": current_time
    })
    
    # 限制歷史記錄數量
    if len(temperature_history[user_id]) > max_history_records:
        temperature_history[user_id].pop(0)  # 移除最舊的記錄
    
    # 創建MQTT消息 - 結構與位置數據類似，但content類型不同
    data = {
        "content": "temperature",  # 區分這是體溫數據
        "gateway id": gateway_id,
        "node": "TAG",
        "id": user_id,
        "name": user_name,
        "temperature": {
            "value": skin_temp,
            "unit": "celsius",
            "is_abnormal": skin_temp > 37.5 or skin_temp < 36.0,
            "room_temp": room_temp
        },
        "time": current_time,
        "serial no": random.randint(0, 65535)
    }
    
    message = json.dumps(data)
    client.publish(TOPIC_HEALTH, message, qos=1, retain=True)
    
    print(f"用戶: {user_name} (ID: {user_id})")
    print(f"體溫: {skin_temp}°C, 室溫: {room_temp}°C")
    print(f"時間: {current_time}")
    print("----------------------------")
    
    return data

def temperature_simulation_loop():
    """定期發送體溫數據的主循環"""
    global running
    iteration = 1
    
    try:
        while running:
            print(f"\n======== 體溫數據迭代 #{iteration} ========")
            
            # 為每個用戶發送體溫數據
            all_data = []
            for user in USERS:
                data = send_temperature_data(user)
                all_data.append(data)
                time.sleep(0.5)  # 短暫停頓，避免同時發送太多消息
            
            print(f"等待{TEMP_INTERVAL}秒後發送下一批體溫數據...")
            time.sleep(TEMP_INTERVAL)
            iteration += 1
            
    except KeyboardInterrupt:
        print("\n用戶中止了體溫模擬。")
    except Exception as e:
        print(f"\n體溫模擬中發生錯誤: {e}")
    finally:
        print("正在關閉MQTT連接...")
        client.loop_stop()
        client.disconnect()
        print("體溫模擬結束。")

def print_statistics():
    """每分鐘打印一次統計信息"""
    global running
    try:
        while running:
            time.sleep(60)  # 每分鐘執行一次
            if not running:
                break
                
            print("\n======== 體溫歷史統計 ========")
            for user_id, history in temperature_history.items():
                if history:
                    # 篩選出異常溫度
                    abnormal_temps = [h["temperature"] for h in history 
                                     if h["temperature"] > 37.5 or h["temperature"] < 36.0]
                    
                    user_name = next((u["name"] for u in USERS if u["id"] == user_id), "未知")
                    avg_temp = sum(h["temperature"] for h in history) / len(history)
                    
                    print(f"用戶: {user_name} (ID: {user_id})")
                    print(f"  歷史記錄數: {len(history)} 筆")
                    print(f"  平均體溫: {avg_temp:.1f}°C")
                    print(f"  異常體溫次數: {len(abnormal_temps)} 次")
                    if abnormal_temps:
                        print(f"  異常值: {', '.join(f'{t:.1f}°C' for t in abnormal_temps[:5])}{'...' if len(abnormal_temps) > 5 else ''}")
                    print("----------------------------")
    except Exception as e:
        print(f"統計信息線程發生錯誤: {e}")

if __name__ == "__main__":
    print("開始體溫模擬器 - 每個用戶的體溫將每10秒更新一次")
    print("按Ctrl+C停止")
    print("---------------------------------")
    
    # 設置MQTT
    setup_mqtt()
    
    # 啟動統計信息線程
    stats_thread = threading.Thread(target=print_statistics)
    stats_thread.daemon = True
    stats_thread.start()
    
    # 開始模擬
    temperature_simulation_loop()
