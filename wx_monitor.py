import time
import re
from wxauto import WeChat
from collections import deque

def get_money(content):
    """从字符串中提取金额"""
    # 改进正则表达式以匹配更通用的收款通知
    pattern = re.compile(r"(?:收款|向你付款|向您付款)\s*([0-9]+\.?[0-9]*)")
    match = pattern.search(content)
    if match:
        return match.group(1)
    return None

def main():
    """主监控函数"""
    print("正在获取微信实例...")
    # 明确指定微信路径，提高成功率
    wx = WeChat(exe_path=r"C:\Program Files (x86)\Tencent\WeChat\WeChat.exe")
    print("获取成功，准备开始监控...")

    # 监控对象设置为“微信支付”
    who = "微信支付"
    
    print(f"正在监控 -> {who} <- 的消息")
    print("请保持微信窗口在前台，不要最小化。")
    print("-----------")

    # 使用一个固定大小的队列来存储已处理的消息，避免重复
    processed_messages = deque(maxlen=20)

    while True:
        try:
            # 每次循环都强制切换窗口，确保获取最新信息
            wx.ChatWith(who)
            time.sleep(1) # 等待窗口切换完成
            
            # 获取当前窗口的所有消息
            messages = wx.GetAllMessage()
            
            if not messages:
                time.sleep(2)
                continue

            for message in messages:
                content = ""
                # 检查消息格式是否是我们预期的元组格式
                if isinstance(message, tuple) and len(message) == 2:
                    sender, content = message
                else:
                    # 如果不是，就当作系统消息，直接转为字符串
                    sender = "System"
                    content = str(message)

                if not content:
                    continue

                # 如果这条消息我们没处理过
                if content not in processed_messages:
                    print(f"[调试] 发现新消息: {content}")
                    processed_messages.append(content) # 记录为已处理

                    # 现在检查这条新消息是否是收款通知
                    if "收款" in content or "向你付款" in content or "向您付款" in content:
                        money = get_money(content)
                        if money:
                            print("=================================")
                            print(f"【【【发现收款通知】】】")
                            print(f"原始消息: {content}")
                            print(f"收款金额: {money}元")
                            print("=================================")
            
            # 每隔2秒检查一次
            time.sleep(2)

        except Exception as e:
            print(f"发生错误: {e}")
            print("将在5秒后重试...")
            time.sleep(5)

if __name__ == "__main__":
    main()
