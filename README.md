# 一个可以在root环境下实现静默安装APP并启动的工具APP
## 安装后启动的实现是监听 android.intent.action.PACKAGE_REPLACED 广播
## 包含了一个工具类ZCAppUpgradeTool，方便在其他APP调起