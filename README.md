# 一个可以在root环境下实现静默安装APP并启动的工具APP
## 安装后启动的实现是监听 android.intent.action.PACKAGE_REPLACED 广播
## 包含了一个工具类ZCAppUpgradeTool，方便在其他APP调起


```java
     /**
     * 传入url，启动更新app，自动开始下载安装
     *
     * @param context     上下文
     * @param downloadUrl 下载url
     */
    public static void startUpdate(Context context, String downloadUrl) {
        try {
            ComponentName componentName = new ComponentName(ZC_PACKAGE_NAME, ZC_MAIN_ACTIVITY);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_PACKAGE_NAME, context.getPackageName());
            intent.putExtra(EXTRA_DOWNLOAD_URL, downloadUrl);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "启动更新APP失败,请重试", Toast.LENGTH_LONG).show();
        }
    }
```
