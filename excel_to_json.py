#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd
import json
import os
import sys

def excel_to_json(excel_file_path, output_file=None):
    """
    將Excel文件轉換為JSON格式
    
    參數:
        excel_file_path (str): Excel文件的路徑
        output_file (str, optional): 輸出JSON文件的路徑，如果為None則使用與Excel同名的文件
    
    返回:
        str: 生成的JSON文件路徑
    """
    try:
        # 檢查文件是否存在
        if not os.path.exists(excel_file_path):
            print(f"錯誤: 找不到文件 {excel_file_path}")
            return None
        
        # 獲取所有sheet的名稱
        xlsx = pd.ExcelFile(excel_file_path)
        sheet_names = xlsx.sheet_names
        
        all_data = {}
        
        # 讀取每個sheet的數據
        for sheet_name in sheet_names:
            # 讀取Excel文件
            df = pd.read_excel(excel_file_path, sheet_name=sheet_name)
            
            # 將NaN值替換為None，這樣在JSON中顯示為null
            df = df.where(pd.notnull(df), None)
            
            # 將DataFrame轉換為字典列表
            data_list = df.to_dict('records')
            
            # 添加到總數據中
            all_data[sheet_name] = data_list
        
        # 如果沒有指定輸出文件路徑，使用與Excel同名的路徑，但擴展名為.json
        if output_file is None:
            base_name = os.path.splitext(excel_file_path)[0]
            output_file = f"{base_name}.json"
        
        # 將數據寫入JSON文件
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(all_data, f, ensure_ascii=False, indent=4)
        
        print(f"轉換成功！已將數據保存到 {output_file}")
        return output_file
    
    except Exception as e:
        print(f"轉換過程中發生錯誤: {str(e)}")
        return None

def main():
    # 檢查命令行參數
    if len(sys.argv) < 2:
        excel_file = input("請輸入Excel文件路徑: ")
    else:
        excel_file = sys.argv[1]
    
    # 尋找當前目錄中的Excel文件
    if excel_file == "auto":
        excel_files = [f for f in os.listdir('.') if f.endswith('.xlsx') or f.endswith('.xls')]
        if not excel_files:
            print("當前目錄下沒有找到Excel文件")
            return
        elif len(excel_files) == 1:
            excel_file = excel_files[0]
            print(f"找到Excel文件: {excel_file}")
        else:
            print("找到多個Excel文件:")
            for i, file in enumerate(excel_files):
                print(f"{i+1}. {file}")
            selection = int(input("請選擇要轉換的文件編號: ")) - 1
            excel_file = excel_files[selection]
    
    # 轉換文件
    json_file = excel_to_json(excel_file)
    
    if json_file:
        # 嘗試讀取生成的JSON文件以顯示其內容
        try:
            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            # 計算總行數
            total_rows = sum(len(sheet_data) for sheet_data in data.values())
            
            print(f"\nJSON數據概要:")
            print(f"sheets數量: {len(data)}")
            print(f"總行數: {total_rows}")
            
            # 顯示每個sheet的前三行數據（如果有）
            for sheet_name, rows in data.items():
                print(f"\nSheet '{sheet_name}' ({len(rows)}行):")
                
                # 獲取列名（假設所有行的列都相同）
                if rows:
                    columns = list(rows[0].keys())
                    print(f"  列: {', '.join(columns)}")
                    
                    # 顯示前三行數據
                    for i, row in enumerate(rows[:3]):
                        print(f"  行{i+1}: {row}")
                    
                    if len(rows) > 3:
                        print(f"  ... 還有{len(rows)-3}行")
        except Exception as e:
            print(f"讀取JSON數據時發生錯誤: {str(e)}")

if __name__ == "__main__":
    main()
