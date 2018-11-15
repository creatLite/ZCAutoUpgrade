package com.zc.robot.autoupgrade.utils;

import android.content.Context;
import android.util.Log;

import java.io.*;

public class RootUtils {
    public static final String[] SU_BINARY_DIRS = {
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sbin"
    };

    /**
     * 检查设备是否root
     *
     * @return
     */
    public static boolean checkRoot() {
        boolean isRoot = false;
        try {
            for (String dir : SU_BINARY_DIRS) {
                File su = new File(dir, "su");
                if (su.exists()) {
                    isRoot = true;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return isRoot;
    }

    private static void closeIO(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 运行root命令
     *
     * @return
     */
    public static boolean runRootCmd(String cmd) {
        boolean grandted;
        DataOutputStream outputStream = null;
        BufferedReader reader = null;
        try {
            Process process = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes(cmd + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            grandted = true;

            String msg = reader.readLine();
            if (msg != null) {
                Log.v(RootUtils.class.getSimpleName(), msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            grandted = false;

            closeIO(outputStream);
            closeIO(reader);
        }
        return grandted;
    }

    public static boolean installPkg(String apkPath) {
        return runRootCmd(Commands.INSTALL_PKG + apkPath);
    }

    /**
     * 为app申请root权限
     *
     * @param context
     * @return
     */
    public static boolean grantRoot(Context context) {
        return runRootCmd(Commands.GRAND_ROOT + context.getPackageCodePath());
    }

    public static interface Commands {
        String INSTALL_PKG = "pm install -r ";
        String GRAND_ROOT = "chmod 777 ";
    }
}
