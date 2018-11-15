package com.zc.robot.autoupgrade;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.WindowManager;
import com.zc.robot.autoupgrade.utils.DownloadTool;
import com.zc.robot.autoupgrade.utils.EncryptUtils;
import com.zc.robot.autoupgrade.utils.RootUtils;
import com.zc.robot.autoupgrade.utils.ZCAppUpgradeTool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DownloadService extends Service {
    final String save_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    String tag = "DownloadService";
    private ProgressDialog mProgressDialog;
    private DownloadTool downloadTool;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(tag, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(tag, "onStartCommand");
        String download_url = intent.getStringExtra(ZCAppUpgradeTool.EXTRA_DOWNLOAD_URL);
        String package_name = intent.getStringExtra(ZCAppUpgradeTool.EXTRA_PACKAGE_NAME);

        //将报名以广播的形式发送到AutoUpgradeReceiver，PS:广播可以跨进程通讯
        HashMap<String, String> map = new HashMap<>();
        map.put(ZCAppUpgradeTool.EXTRA_PACKAGE_NAME, package_name);
        sendBroadcast(ZCAppUpgradeTool.ACTION_SEND_PACKAGE_NAME, map);


        String save_path = save_dir + File.separator + EncryptUtils.encryptMD5ToString(package_name);
        Log.e(tag, download_url);
        Log.e(tag, save_path);
        downloadTool = DownloadTool.newInstance().setSavePosition(save_path).startDownload(download_url, new DownloadTool.DownloadCallBack() {
            @Override
            public void onStart() {
                Log.e(tag, "开始下载");

                sendBroadcast(ZCAppUpgradeTool.ACTION_DOWNLOADING, null);

                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(getApplicationContext());
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setTitle("版本更新");
                    mProgressDialog.setMessage("正在下载，请稍后...");
                    mProgressDialog.setProgressNumberFormat("已下载 %1dMB/%2dMB");
                    mProgressDialog.setButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mProgressDialog.dismiss();
                        }
                    });
                }

                mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                mProgressDialog.show();

            }

            @Override
            public void onSuccess(File file) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                Log.e(tag, "下载完成，开始安装");
                sendBroadcast(ZCAppUpgradeTool.ACTION_INSTALLING_PACKAGE, null);
                installApk(file);
            }

            @Override
            public void onFail() {
                mProgressDialog.dismiss();
                sendBroadcast(ZCAppUpgradeTool.ACTION_DOWNLOAD_FAIL, null);
            }

            @Override
            public void onProgress(int progress, int totalSize) {
                mProgressDialog.setMax(totalSize / 1024 / 1024);
                mProgressDialog.setProgress(progress / 1024 / 1024);
            }
        });

        return super.onStartCommand(intent, flags, startId);

    }

    private void sendBroadcast(String action, Map<String, String> extras) {
        //这里的scheme需要配置成package才能正确发送到广播接收者
        Intent intent = new Intent(action, Uri.parse("package:" + getPackageName()));

        if (null != extras) {
            for (String key : extras.keySet()) {
                intent.putExtra(key, extras.get(key));
            }
        }
        sendBroadcast(intent);
    }

    private void installApk(File apk) {
        if (apk != null & apk.exists()) {

            if (RootUtils.checkRoot()) {
                RootUtils.installPkg(apk.getAbsolutePath());
            } else {
                normalInstallApk(this, apk);
            }
        }

    }

    private void normalInstallApk(Context context, File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //判断是否是AndroidN以及更高的版本
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
