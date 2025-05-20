#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import paho.mqtt.client as mqtt
import json
import time
import random
import math
import threading
from datetime import datetime, timedelta

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
MIN_TEMP = 34.0  # 最低體溫 (°C)
MAX_TEMP = 44.0  # 最高體溫 (°C)
NORMAL_TEMP_MIN = 36.3  # 正常體溫下限
NORMAL_TEMP_MAX = 37.2  # 正常體溫上限
# 異常溫度範圍（低溫：34-36°C，高溫：37.5-44°C）

# 發送頻率設置
LOCATION_INTERVAL = 1.0  # 位置數據發送間隔（秒）
TEMP_INTERVAL = LOCATION_INTERVAL  # 體溫數據發送間隔（秒），設置為每秒發送一次

# 全局變量
running = True
client = None
temperature_history = {}  # 用於存儲每個用戶的體溫歷史記錄
max_history_records = 1000  # 增加記錄數以存儲三天的數據

# 時間設置
DAYS_OF_HISTORY = 2  # 過去兩天的數據
DATA_INTERVAL_MINUTES = 5  # 數據間隔改為5分鐘，增加數據密度
SIMULATION_INTERVAL_SECONDS = 0.5  # 模擬器發送頻率到0.5秒一次，加快發送

# 日期格式
DATE_FORMAT = "%Y-%m-%d %H:%M:%S.%f"  # 標準年-月-日格式

def setup_mqtt():
    """設置MQTT客戶端"""
    global client
    client = mqtt.Client(client_id=MQTT_CLIENT_ID)
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()
    print(f"已連接到MQTT代理 {MQTT_BROKER}:{MQTT_PORT}")

def generate_temperature(user_id, timestamp=None):
    """
    為指定用戶在指定時間生成體溫數據
    - 大多數情況生成正常範圍內的體溫
    - 有10%機率生成偏高的體溫
    - 有5%機率生成偏低的體溫
    - 對於特定用戶，可以有特殊的體溫模式
    """
    # 如果提供了時間戳，則使用該時間產生相應的週期性變化
    if timestamp is None:
        timestamp = datetime.now()
    
    # 時間因子用於產生週期性溫度變化 - 使用時間戳的小時
    hour_of_day = timestamp.hour + timestamp.minute / 60.0
    day_cycle = math.sin(hour_of_day * math.pi / 12)  # 24小時一個週期
    
    # 為隨機性增加一個基於日期的種子，使不同日期產生不同的隨機序列
    day_seed = timestamp.year * 10000 + timestamp.month * 100 + timestamp.day
    # 將所有值轉換為字符串再相加
    seed_str = str(day_seed) + user_id + str(hour_of_day)
    # 使用字符串的雙埝hash生成整數種子
    random.seed(hash(seed_str))
    
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
    if r < 0.07:  # 7% 機率產生低溫
        # 加大低溫範圍，分為兩個區域，增加多樣性
        if random.random() < 0.3:  # 30% 機率生成非常低的溫度
            return round(random.uniform(MIN_TEMP, MIN_TEMP + 1.0), 1)  # 34-35°C
        else:
            return round(random.uniform(MIN_TEMP + 1.0, NORMAL_TEMP_MIN - 0.1), 1)  # 35-36.2°C
    elif r < 0.20:  # 13% 機率產生高溫
        # 加大高溫範圍，分為三個區域，增加多樣性
        sub_range = random.random()
        if sub_range < 0.6:  # 60% 機率生成較輕微發熱
            return round(random.uniform(NORMAL_TEMP_MAX + 0.1, 38.5), 1)  # 37.3-38.5°C
        elif sub_range < 0.9:  # 30% 機率生成中度發熱
            return round(random.uniform(38.5, 40.0), 1)  # 38.5-40°C
        else:  # 10% 機率生成高熱
            return round(random.uniform(40.0, MAX_TEMP), 1)  # 40-44°C
    else:  # 80% 機率產生正常溫度
        return round(base_temp + random.uniform(-variation, variation), 1)

# 模擬器的當前時間計數，從5月19日開始到現在
SIMULATION_START_TIME = datetime(2025, 5, 19, 0, 0, 0)  # 以5月19日開始
simulation_current_time = SIMULATION_START_TIME

# 如果當前時間已經超過目標日期，重設為下一天的開始
def check_and_reset_time():
    global simulation_current_time
    if simulation_current_time.day > 21:  # 如果超過21日，重設回19日
        simulation_current_time = datetime(2025, 5, 19, 0, 0, 0)
    
    # 確保每天都有充分的數據
    if simulation_current_time.hour >= 23 and simulation_current_time.minute >= 30:
        # 移動到下一天的開始
        next_day = simulation_current_time + timedelta(days=1)
        simulation_current_time = datetime(next_day.year, next_day.month, next_day.day, 0, 0, 0)

def send_temperature_data(user, timestamp=None, send_mqtt=True):
    """為單個用戶發送特定時間點的體溫數據"""
    global simulation_current_time
    user_id = user["id"]
    user_name = user["name"]
    gateway_id = user["gateway_id"]
    
    # 檢查並重設時間如果需要
    check_and_reset_time()
    
    # 使用提供的時間戳或模擬的當前時間
    if timestamp is None:
        timestamp = simulation_current_time
        # 在實時發送模式下，每次增加10分鐘
        simulation_current_time += timedelta(minutes=10)
    
    # 將datetime物件格式化為字符串
    current_time = timestamp.strftime(DATE_FORMAT)[:-4]  # 使用標準的年-月-日格式
    
    # 生成該時間點的體溫數據
    skin_temp = generate_temperature(user_id, timestamp)
    room_temp = round(random.uniform(22.0, 26.0), 1)  # 室溫
    
    # 保存到歷史記錄
    if user_id not in temperature_history:
        temperature_history[user_id] = []
    
    # 添加新記錄
    new_record = {
        "temperature": skin_temp,
        "timestamp": current_time,  # 字符串格式的時間戳
        "datetime": timestamp  # 原始的datetime對象
    }
    temperature_history[user_id].append(new_record)
    
    # 限制歷史記錄數量
    if len(temperature_history[user_id]) > max_history_records:
        temperature_history[user_id].pop(0)  # 移除最舊的記錄
    
    # 創建MQTT消息 - 只有當需要時才發送
    if send_mqtt:
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
    else:
        return {
            "id": user_id,
            "name": user_name,
            "temperature": skin_temp,
            "time": current_time
        }

def generate_historical_data():
    """生成從5月19日到5月21日的歷史數據，每5分鐘一筆"""
    print("\n正在生成從2025-05-19到2025-05-21的歷史溫度數據...")
    
    # 計算開始和結束時間
    # 使用5月19日到5月21日的完整時間範圍
    current_date = datetime.now()
    end_time = datetime(2025, 5, 21, 23, 59, 59)  # 固定結束日期為5月21日
    start_time = datetime(2025, 5, 19, 0, 0, 0)  # 固定開始日期為5月19日
    
    # 計算需要生成的時間點總數
    data_points_per_day = 24 * 60 // DATA_INTERVAL_MINUTES  # 每天的數據點數
    days_diff = (end_time - start_time).days + 1  # 計算實際天數
    total_points = data_points_per_day * days_diff
    
    print(f"將為每個用戶生成約{total_points}筆歷史數據（從{start_time.strftime('%Y-%m-%d')}到{end_time.strftime('%Y-%m-%d')}，每{DATA_INTERVAL_MINUTES}分鐘一筆）")
    
    # 生成每個時間點的數據
    current_time = start_time
    while current_time <= end_time:
        for user in USERS:
            # 生成數據但不發送MQTT消息
            send_temperature_data(user, current_time, send_mqtt=False)
        
        # 向前推進10分鐘
        current_time += timedelta(minutes=DATA_INTERVAL_MINUTES)
    
    print(f"歷史數據生成完成。總共為每個用戶生成了{len(temperature_history[USERS[0]['id']])}筆數據")

def generate_balanced_data():
    """為三天生成均衡的數據，確保以下日期都有數據：前天(5月18日)、昨天(5月19日)、今天(5月20日)"""
    print("\n正在為三天生成均衡分布的溫度數據...")
    
    # 三天的時間點
    days = [
        datetime(2025, 5, 18, 0, 0, 0),  # 前天
        datetime(2025, 5, 19, 0, 0, 0),  # 昨天
        datetime(2025, 5, 20, 0, 0, 0)   # 今天
    ]
    
    # 為每一天生成多個時間點
    time_points_per_day = 24 * 60 // DATA_INTERVAL_MINUTES  # 每天的數據點數
    
    # 清空歷史數據
    for user in USERS:
        temperature_history[user["id"]] = []
    
    print(f"將為每個用戶生成約{time_points_per_day * 3}筆數據（每天約{time_points_per_day}筆）")
    
    # 為每一天生成數據
    for day in days:
        # 為一整天生成數據
        for hour in range(24):
            for minute in range(0, 60, DATA_INTERVAL_MINUTES):
                current_time = day.replace(hour=hour, minute=minute)
                
                # 為每個用戶生成這個時間點的數據
                for user in USERS:
                    send_temperature_data(user, current_time, send_mqtt=False)
    
    total_data_points = sum(len(history) for history in temperature_history.values())
    print(f"數據生成完成，總共生成了{total_data_points}筆數據 ({total_data_points//len(USERS)} 筆/用戶)")

def temperature_simulation_loop():
    """定期從歷史數據中發送體溫數據的主循環"""
    global running
    iteration = 1
    
    # 改用均衡的數據生成方式
    generate_balanced_data()
    
    # 為每個用戶建立一個指向其歷史數據的索引
    user_indices = {user["id"]: 0 for user in USERS}
    
    try:
        while running:
            print(f"\n======== 體溫數據迭代 #{iteration} ========")
            
            # 為每個用戶發送一筆歷史數據
            all_data = []
            for user in USERS:
                user_id = user["id"]
                user_name = user["name"]
                gateway_id = user["gateway_id"]
                
                # 獲取用戶的歷史數據
                if user_id in temperature_history and len(temperature_history[user_id]) > 0:
                    # 獲取當前索引
                    index = user_indices[user_id]
                    if index >= len(temperature_history[user_id]):
                        # 如果已經發送完所有數據，重置索引
                        index = 0
                        user_indices[user_id] = 0
                    
                    # 獲取歷史記錄
                    record = temperature_history[user_id][index]
                    skin_temp = record["temperature"]
                    record_time = record["timestamp"]
                    
                    # 生成MQTT消息
                    room_temp = round(random.uniform(22.0, 26.0), 1)  # 隨機室溫
                    data = {
                        "content": "temperature",
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
                        "time": record_time,
                        "serial no": random.randint(0, 65535)
                    }
                    
                    message = json.dumps(data)
                    client.publish(TOPIC_HEALTH, message, qos=1, retain=True)
                    
                    print(f"用戶: {user_name} (ID: {user_id})")
                    print(f"體溫: {skin_temp}°C, 室溫: {room_temp}°C")
                    print(f"時間: {record_time}")
                    print("----------------------------")
                    
                    # 更新索引
                    user_indices[user_id] = index + 1
                    all_data.append(data)
                
                time.sleep(0.5)  # 短暫停頓，避免同時發送太多消息
            
            print(f"等待{SIMULATION_INTERVAL_SECONDS}秒後發送下一批體溫數據...")
            time.sleep(SIMULATION_INTERVAL_SECONDS)
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
    print(f"開始體溫模擬器 - 從{SIMULATION_START_TIME.strftime('%Y-%m-%d')}開始，生成過去三天的數據（每10分鐘一筆），每秒發送一次")
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
