#!/usr/bin/env python3
import json
import random
import time
from datetime import datetime

# 病患資料
patients = [
    {"id": "E001", "name": "張三"},
    {"id": "E002", "name": "李四"},
    {"id": "E003", "name": "王五"},
    {"id": "E004", "name": "趙六"},
    {"id": "E005", "name": "錢七"}
]

# 模擬生成溫度讀數
def generate_temperature_reading(patient_id, min_temp=30.0, max_temp=42.0):
    # 為每個病患生成特殊的溫度分佈
    # E001 張三 - 偏高溫
    # E003 王五 - 偏低溫
    # E005 錢七 - 偏極端
    # 其他病患正常溫度為主
    
    choice = random.random()
    
    if patient_id == "E001":  # 張三偏高溫
        if choice < 0.6:
            return round(random.uniform(37.3, 38.5), 1)  # 60% 中高溫
        elif choice < 0.9:
            return round(random.uniform(36.1, 37.2), 1)  # 30% 正常溫度
        else:
            return round(random.uniform(38.6, 39.5), 1)  # 10% 高溫
    
    elif patient_id == "E003":  # 王五偏低溫
        if choice < 0.6:
            return round(random.uniform(35.2, 35.9), 1)  # 60% 低溫
        elif choice < 0.9:
            return round(random.uniform(36.0, 36.5), 1)  # 30% 正常偏低
        else:
            # 為測試數據多樣性，有時生成異常高值
            return round(random.uniform(38.5, 39.0), 1)  # 10% 異常高溫
    
    elif patient_id == "E005":  # 錢七偏極端
        if choice < 0.4:
            return round(random.uniform(36.5, 37.2), 1)  # 40% 正常溫度
        elif choice < 0.7:
            return round(random.uniform(37.5, 38.5), 1)  # 30% 中高溫
        elif choice < 0.85:
            return round(random.uniform(30.0, 35.0), 1)  # 15% 特低溫
        else:
            return round(random.uniform(39.0, 42.0), 1)  # 15% 特高溫
    
    else:  # 其他病患大多數時間正常
        if choice < 0.85:  # 85% 正常溫度
            return round(random.uniform(36.3, 37.2), 1)
        elif choice < 0.95:  # 10% 稍高溫
            return round(random.uniform(37.3, 38.0), 1)
        else:  # 5% 異常溫度
            if random.random() < 0.5:
                return round(random.uniform(35.5, 36.0), 1)  # 低溫
            else:
                return round(random.uniform(38.1, 38.9), 1)  # 高溫

# 判斷溫度是否異常
def is_temperature_abnormal(temp):
    # 正常體溫範圍考慮傳統體溫計和耳溫槽的差異
    # 邏先使用更寬的範圍以產生更多異常數據進行測試
    return temp < 35.5 or temp > 37.8

# 生成MQTT消息
def generate_temperature_message(patient):
    # 用病患ID生成特定溫度分佈
    temperature = generate_temperature_reading(patient["id"])
    room_temp = round(random.uniform(20.0, 28.0), 1)
    
    current_time = datetime.now()
    time_str = f"{current_time.year}-{current_time.timetuple().tm_yday} {current_time.strftime('%H:%M:%S.%f')[:-3]}"
    
    message = {
        "content": "temperature",
        "gateway id": 137205,
        "node": "TAG",
        "id": patient["id"],
        "name": patient["name"],
        "temperature": {
            "value": temperature,
            "unit": "celsius",
            "is_abnormal": is_temperature_abnormal(temperature),
            "room_temp": room_temp
        },
        "time": time_str,
        "serial no": random.randint(1000, 99999)
    }
    
    return json.dumps(message)

# 主函數
def main():
    print("溫度模擬器啟動中... 按 Ctrl+C 停止")
    print("模擬器會盡量商消息一次所有病患的數據")
    print("以下是模擬的MQTT消息格式，可以直接複製到測試工具中：")
    print("==========================================================")
    
    # 打印病患信息
    print("\n模擬用病患資料:")
    for patient in patients:
        print(f"  - {patient['name']} (ID: {patient['id']})")
    print("\n")
    
    cycle_count = 0
    try:
        while True:
            cycle_count += 1
            print(f"\n第 {cycle_count} 輪數據生成:")
            print("==========================================================")
            
            # 確保生成所有病患的數據
            for patient in patients:
                message = generate_temperature_message(patient)
                print(f"主題: GW17F5_Health")
                print(f"內容: {message}")
                # 解析JSON數據並顯示行現溫度和狀態
                data = json.loads(message)
                temp = data["temperature"]["value"]
                abnormal = data["temperature"]["is_abnormal"]
                status = "異常" if abnormal else "正常"
                print(f"  -> 病患: {data['name']}, 體溫: {temp}°C ({status})")
                print("----------------------------------------------------------")
            
            # 等待一段時間再生成下一組數據
            print(f"\n等待 5 秒後生成下一組數據...")
            time.sleep(5)
            
    except KeyboardInterrupt:
        print("\n溫度模擬器已停止")

if __name__ == "__main__":
    main()
