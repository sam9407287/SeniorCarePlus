#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import paho.mqtt.client as mqtt
import json
import time
import random
import sys
import os
from datetime import datetime

# 設定MQTT連接參數
MQTT_BROKER = "localhost"  # 默認是本地broker，可以修改為實際伺服器地址
MQTT_PORT = 1883
MQTT_CLIENT_ID = f"senior-care-simulator-{random.randint(0, 1000)}"
MQTT_KEEPALIVE = 60

# 默認主題前綴 (可修改為實際的Gateway ID)
TOPIC_PREFIX = "GW17F5"

# 載入JSON數據
def load_json_data(json_file_path):
    try:
        with open(json_file_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"錯誤: 無法讀取JSON文件: {e}")
        return None

# 從字符串中提取JSON對象
def extract_json_from_string(json_str):
    try:
        # 尋找JSON開始和結束的位置
        start = json_str.find('{')
        end = json_str.rfind('}') + 1
        if start >= 0 and end > start:
            json_content = json_str[start:end]
            return json.loads(json_content)
        else:
            return None
    except json.JSONDecodeError:
        return None

# 連接MQTT伺服器
def connect_mqtt():
    client = mqtt.Client(client_id=MQTT_CLIENT_ID)
    try:
        client.connect(MQTT_BROKER, MQTT_PORT, MQTT_KEEPALIVE)
        print(f"已連接到MQTT伺服器: {MQTT_BROKER}:{MQTT_PORT}")
        return client
    except Exception as e:
        print(f"連接MQTT伺服器失敗: {e}")
        return None

# 發送MQTT消息
def publish_message(client, topic, message, qos=0):
    try:
        result = client.publish(topic, message, qos=qos)
        status = result[0]
        if status == 0:
            print(f"消息發送成功 - 主題: {topic}")
            return True
        else:
            print(f"無法發送消息到主題: {topic}, 錯誤碼: {status}")
            return False
    except Exception as e:
        print(f"發送消息時出錯: {e}")
        return False

# 從JSON數據中提取消息
def extract_messages_by_type(data, message_type):
    messages = []
    
    # 遍歷所有sheet
    for sheet_name, sheet_data in data.items():
        # 尋找與消息類型匹配的sheet
        if message_type.lower() in sheet_name.lower():
            for row in sheet_data:
                if "Unnamed: 1" in row and row["Unnamed: 1"]:
                    json_obj = extract_json_from_string(row["Unnamed: 1"])
                    if json_obj:
                        # 提取主題信息
                        topic = None
                        if "Unnamed: 1" in row and isinstance(row["Unnamed: 1"], str) and "Topic:" in row["Unnamed: 1"]:
                            topic = row["Unnamed: 1"].split("Topic: ")[1].strip()
                        
                        # 查找更上面的行是否有主題信息
                        if topic is None and "Unnamed: 0" in row and row["Unnamed: 0"] is not None:
                            # 向上查找前幾行
                            idx = sheet_data.index(row)
                            for i in range(max(0, idx-5), idx):
                                if "Unnamed: 1" in sheet_data[i] and isinstance(sheet_data[i]["Unnamed: 1"], str) and "Topic:" in sheet_data[i]["Unnamed: 1"]:
                                    topic = sheet_data[i]["Unnamed: 1"].split("Topic: ")[1].strip()
                                    break
                        
                        messages.append({
                            "json": json_obj,
                            "topic": topic,
                            "sheet": sheet_name
                        })
    
    return messages

# 更新時間戳和序列號
def update_dynamic_fields(message):
    if isinstance(message, dict):
        # 更新序列號
        if "serial no" in message:
            message["serial no"] = random.randint(0, 65535)
        
        # 更新時間戳
        if "time" in message:
            current_time = datetime.now()
            year_day = current_time.strftime("%Y-%j")
            hour_min_sec = current_time.strftime("%H:%M:%S.%f")[:-4]
            message["time"] = f"{year_day} {hour_min_sec}"
        
        # 如果有位置數據，稍微隨機化它
        if "position" in message and isinstance(message["position"], dict):
            for key in ["x", "y", "z"]:
                if key in message["position"]:
                    # 在原值基礎上加減最多0.5
                    delta = random.uniform(-0.5, 0.5)
                    message["position"][key] = round(message["position"][key] + delta, 6)
            
            # 更新品質值
            if "quality" in message["position"]:
                message["position"]["quality"] = random.randint(60, 100)
        
        # 如果是健康數據，隨機化一些值
        if "content" in message and message["content"] == "300B":
            if "hr" in message:  # 心率
                message["hr"] = random.randint(65, 100)
            if "SpO2" in message:  # 血氧
                message["SpO2"] = random.randint(93, 100)
            if "bp syst" in message:  # 收縮壓
                message["bp syst"] = random.randint(110, 140)
            if "bp diast" in message:  # 舒張壓
                message["bp diast"] = random.randint(70, 90)
            if "skin temp" in message:  # 皮膚溫度
                message["skin temp"] = round(random.uniform(33.0, 35.0), 1)
            if "room temp" in message:  # 室溫
                message["room temp"] = round(random.uniform(22.0, 26.0), 1)
            if "battery level" in message:  # 電池電量
                message["battery level"] = random.randint(50, 100)
        
        # 如果是尿布數據，隨機化一些值
        if "content" in message and "diaper" in message["content"]:
            if "temp" in message:  # 溫度
                message["temp"] = round(random.uniform(33.0, 35.0), 1)
            if "humi" in message:  # 濕度
                message["humi"] = round(random.uniform(40.0, 70.0), 1)
            if "battery level" in message:  # 電池電量
                message["battery level"] = random.randint(50, 100)
    
    return message

# 顯示主菜單
def show_menu(message_types):
    print("\n===== MQTT消息發送器 =====")
    print("可用的消息類型:")
    
    for i, msg_type in enumerate(message_types, 1):
        print(f"{i}. {msg_type}")
    
    print(f"{len(message_types) + 1}. 設置")
    print("0. 退出程序")
    
    while True:
        try:
            choice = int(input("\n請選擇要發送的消息類型 (0-{0}): ".format(len(message_types) + 1)))
            if 0 <= choice <= len(message_types) + 1:
                return choice
            else:
                print(f"請輸入0到{len(message_types) + 1}之間的數字")
        except ValueError:
            print("請輸入數字")

# 顯示MQTT連接設置菜單
def show_settings_menu():
    global MQTT_BROKER, MQTT_PORT, TOPIC_PREFIX
    
    print("\n===== MQTT連接設置 =====")
    print(f"1. MQTT伺服器地址: {MQTT_BROKER}")
    print(f"2. MQTT伺服器端口: {MQTT_PORT}")
    print(f"3. 主題前綴: {TOPIC_PREFIX}")
    print("4. 返回主菜單")
    
    while True:
        try:
            choice = int(input("\n請選擇要修改的設置 (1-4): "))
            if choice == 1:
                MQTT_BROKER = input("輸入新的MQTT伺服器地址: ")
                return True
            elif choice == 2:
                MQTT_PORT = int(input("輸入新的MQTT伺服器端口: "))
                return True
            elif choice == 3:
                TOPIC_PREFIX = input("輸入新的主題前綴: ")
                return True
            elif choice == 4:
                return False
            else:
                print("請輸入1到4之間的數字")
        except ValueError:
            print("請輸入數字")

# 主函數
def main():
    # 查找JSON文件
    json_file = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "UWB_JSON_20250225.json")
    
    if not os.path.exists(json_file):
        print(f"錯誤: 找不到{json_file}文件。")
        alt_path = input("請輸入JSON文件的完整路徑: ")
        if os.path.exists(alt_path):
            json_file = alt_path
        else:
            print("文件不存在，請先運行excel_to_json.py轉換Excel文件。")
            sys.exit(1)
    
    # 載入JSON數據
    data = load_json_data(json_file)
    if not data:
        sys.exit(1)
    
    # 獲取可用的消息類型
    message_types = list(data.keys())
    
    # 連接MQTT伺服器
    client = None
    
    while True:
        # 顯示菜單並獲取用戶選擇
        choice = show_menu(message_types)
        
        if choice == 0:
            print("程序退出")
            if client:
                client.disconnect()
            sys.exit(0)
        
        # 特殊選項：設置
        if choice == len(message_types) + 1:
            settings_changed = show_settings_menu()
            if settings_changed and client:
                client.disconnect()
                client = None
            continue
        
        # 確保連接已建立
        if not client:
            client = connect_mqtt()
            if not client:
                print("無法連接到MQTT伺服器，請檢查設置")
                continue
        
        # 獲取選定的消息類型
        selected_type = message_types[choice - 1]
        
        # 提取該類型的消息
        messages = extract_messages_by_type(data, selected_type)
        
        if not messages:
            print(f"在'{selected_type}'中找不到有效的消息")
            continue
        
        # 顯示找到的消息並讓用戶選擇
        print(f"\n在'{selected_type}'中找到 {len(messages)} 條消息:")
        for i, msg in enumerate(messages, 1):
            topic = msg["topic"] if msg["topic"] else "未知主題"
            content_preview = str(msg["json"])[:50] + "..." if len(str(msg["json"])) > 50 else str(msg["json"])
            print(f"{i}. 主題: {topic}, 內容: {content_preview}")
        
        # 添加循環發送選項
        print(f"{len(messages) + 1}. 循環發送所有消息")
        
        msg_choice = int(input(f"\n請選擇要發送的消息 (1-{len(messages) + 1}): "))
        
        if 1 <= msg_choice <= len(messages):
            # 發送單條消息
            selected_msg = messages[msg_choice - 1]
            topic = selected_msg["topic"]
            
            # 如果主題包含通配符，替換為實際主題
            if topic and "xxxx" in topic:
                topic = topic.replace("xxxx", TOPIC_PREFIX)
            
            # 更新動態字段
            json_msg = update_dynamic_fields(selected_msg["json"])
            
            # 發送消息
            publish_message(client, topic, json.dumps(json_msg))
            
            # 打印發送的完整消息
            print(f"\n發送的消息內容: \n{json.dumps(json_msg, indent=2)}")
        
        elif msg_choice == len(messages) + 1:
            # 循環發送所有消息
            loop_count = int(input("請輸入發送次數 (0表示無限循環): "))
            interval = float(input("請輸入發送間隔 (秒): "))
            
            count = 0
            try:
                while loop_count == 0 or count < loop_count:
                    for msg in messages:
                        topic = msg["topic"]
                        
                        # 如果主題包含通配符，替換為實際主題
                        if topic and "xxxx" in topic:
                            topic = topic.replace("xxxx", TOPIC_PREFIX)
                        
                        # 更新動態字段
                        json_msg = update_dynamic_fields(msg["json"])
                        
                        # 發送消息
                        publish_message(client, topic, json.dumps(json_msg))
                        
                        # 間隔發送
                        time.sleep(interval)
                    
                    count += 1
                    print(f"完成第 {count} 輪發送")
                    
                    if loop_count > 0 and count >= loop_count:
                        break
            except KeyboardInterrupt:
                print("\n已停止循環發送")
        
        else:
            print("無效的選擇")
        
        # 選擇是否繼續
        cont = input("\n是否繼續發送其他消息? (y/n): ")
        if cont.lower() != 'y':
            print("程序退出")
            if client:
                client.disconnect()
            break

# 程序入口
if __name__ == "__main__":
    # 添加設置選項
    try:
        main()
    except KeyboardInterrupt:
        print("\n程序被用戶中斷")
        sys.exit(0)