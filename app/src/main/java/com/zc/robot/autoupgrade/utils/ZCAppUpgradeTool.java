package com.zc.robot.autoupgrade.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;
import com.zc.robot.autoupgrade.BuildConfig;

import java.io.*;

/**
 * 自动更新APP工具类，复制此类到其他项目，方便调用
 */
public class ZCAppUpgradeTool {
    public static final String ZC_PACKAGE_NAME = "com.zc.robot.autoupgrade";
    public static final String ZC_MAIN_ACTIVITY = "com.zc.robot.autoupgrade.MainActivity";
    public static final String ACTION_INSTALL_FINISHED = "com.zc.robot.autoupgrade.ACTION_INSTALL_FINISHED";
    public static final String ACTION_LAUNCH_APP = "com.zc.robot.autoupgrade.ACTION_LAUNCH_APP";
    public static final String ACTION_SEND_PACKAGE_NAME = "com.zc.robot.autoupgrade.ACTION_SEND_PACKAGE_NAME";
    public static final String ACTION_INSTALLING_PACKAGE = "com.zc.robot.autoupgrade.ACTION_INSTALLING_PACKAGE";
    public static final String ACTION_DOWNLOAD_FAIL = "com.zc.robot.autoupgrade.ACTION_DOWNLOAD_FAIL";
    public static final String ACTION_DOWNLOADING = "com.zc.robot.autoupgrade.ACTION_DOWNLOADING";
    public static final String EXTRA_DOWNLOAD_URL = "download_url";
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    public static final String EXTRA_SAVE_FILE_NAME = "save_file_name";
    public static final String EXTRA_SAVE_FILE_PATH = "save_file_path";

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

    /**
     * 安装自动更新app
     *
     * @param activity
     * @param rawId  raw资源ID
     */
    public static void installZCAU(final Activity activity, final int rawId) {
        if (!hasPackgeInstalled(activity, ZC_PACKAGE_NAME)) {

            File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Download");
            if (!dir.exists())
                dir.mkdir();
            final File file = new File(dir, "zcauto");
            if (file.exists()) {
                file.delete();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream is = activity.getResources().openRawResource(rawId);
                    try {
                        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.close();
                        is.close();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (RootUtils.checkRoot()) {
                                    RootUtils.installPkg(file.getAbsolutePath());
                                } else {
                                    normalInstallApk(activity, file);
                                }

                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static boolean hasPackgeInstalled(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return !TextUtils.isEmpty(packageInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

    }

    /**
     * 非root环境下安装app
     * @param context
     * @param apk
     */
    private static void normalInstallApk(Context context, File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //如果sdk版本是android N以上，需要配置fileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", apk);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }
}
