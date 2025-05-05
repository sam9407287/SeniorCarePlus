#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import paho.mqtt.publish as publish
import json
import time
import random
from datetime import datetime

# 設定MQTT連接參數
MQTT_BROKER = "localhost"  # 默認是本地broker
MQTT_PORT = 1883

# 主題
TOPIC_LOCATION = "GW17F5_Loca"
TOPIC_HEALTH = "GW17F5_Health"
TOPIC_MESSAGE = "GW17F5_Message"

# 示例位置數據
def generate_location_data():
    # 生成一個隨機的位置數據
    x = random.uniform(0, 3)
    y = random.uniform(0, 3)
    z = random.uniform(0, 1.5)
    quality = random.randint(60, 100)
    
    data = {
        "content": "location",
        "gateway id": 137205,
        "node": "TAG",
        "id": 23349,
        "position": {
            "x": round(x, 6),
            "y": round(y, 6),
            "z": round(z, 6),
            "quality": quality
        },
        "time": datetime.now().strftime("%Y-%j %H:%M:%S.%f")[:-4],
        "serial no": random.randint(0, 65535)
    }
    
    return data

# 示例健康數據
def generate_health_data():
    # 生成一個隨機的健康數據
    data = {
        "content": "300B",
        "gateway id": 137205,
        "MAC": "E0:0E:08:36:93:F8",
        "SOS": 0,
        "hr": random.randint(65, 100),
        "SpO2": random.randint(93, 100),
        "bp syst": random.randint(110, 140),
        "bp diast": random.randint(70, 90),
        "skin temp": round(random.uniform(33.0, 35.0), 1),
        "room temp": round(random.uniform(22.0, 26.0), 1),
        "steps": random.randint(1000, 5000),
        "sleep time": "22:46",
        "wake time": "7:13",
        "light sleep (min)": 297,
        "deep sleep (min)": 38,
        "move": 26,
        "wear": 1,
        "battery level": random.randint(50, 100),
        "serial no": random.randint(0, 65535)
    }
    
    return data

# 示例尿布數據
def generate_diaper_data():
    # 生成一個隨機的尿布數據
    data = {
        "content": "diaper DV1",
        "gateway id": 137205,
        "MAC": "E0:0E:08:36:93:F8",
        "name": "DV1_3693F8",
        "fw ver": 1.01,
        "temp": round(random.uniform(33.0, 35.0), 1),
        "humi": round(random.uniform(40.0, 70.0), 1),
        "button": 0,
        "mssg idx": 143,
        "ack": 0,
        "battery level": random.randint(50, 100),
        "serial no": random.randint(0, 65535)
    }
    
    return data

# 示例消息數據
def generate_message_data():
    # 生成一個簡單的消息數據
    data = {
        "content": "heartbeat",
        "gateway id": 137205,
        "node": "GW",
        "name": "GW17F5",
        "fw ver": "1.0",
        "fw serial": 100,
        "UWB HW Com OK": "yes",
        "UWB Joined": "yes",
        "UWB Network ID": 4660,
        "connected AP": "Wifi_Office",
        "anchor cfg stack": 15
    }
    
    return data

# 發送測試數據
def send_test_data():
    print("開始發送測試MQTT消息...")
    
    try:
        # 1. 發送位置數據
        location_data = generate_location_data()
        location_json = json.dumps(location_data)
        publish.single(TOPIC_LOCATION, location_json, hostname=MQTT_BROKER, port=MQTT_PORT)
        print(f"已發送位置數據到 {TOPIC_LOCATION}:")
        print(json.dumps(location_data, indent=2, ensure_ascii=False))
        time.sleep(1)
        
        # 2. 發送健康數據
        health_data = generate_health_data()
        health_json = json.dumps(health_data)
        publish.single(TOPIC_HEALTH, health_json, hostname=MQTT_BROKER, port=MQTT_PORT)
        print(f"已發送健康數據到 {TOPIC_HEALTH}:")
        print(json.dumps(health_data, indent=2, ensure_ascii=False))
        time.sleep(1)
        
        # 3. 發送尿布數據
        diaper_data = generate_diaper_data()
        diaper_json = json.dumps(diaper_data)
        publish.single(TOPIC_HEALTH, diaper_json, hostname=MQTT_BROKER, port=MQTT_PORT)
        print(f"已發送尿布數據到 {TOPIC_HEALTH}:")
        print(json.dumps(diaper_data, indent=2, ensure_ascii=False))
        time.sleep(1)
        
        # 4. 發送消息數據
        message_data = generate_message_data()
        message_json = json.dumps(message_data)
        publish.single(TOPIC_MESSAGE, message_json, hostname=MQTT_BROKER, port=MQTT_PORT)
        print(f"已發送消息數據到 {TOPIC_MESSAGE}:")
        print(json.dumps(message_data, indent=2, ensure_ascii=False))
        
        print("\n所有測試消息已發送完成。")
        return True
    except Exception as e:
        print(f"發送消息時出錯: {e}")
        return False

if __name__ == "__main__":
    send_test_data()