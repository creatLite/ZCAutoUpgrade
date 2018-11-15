package com.zc.robot.autoupgrade.utils;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public final class DownloadTool {
    private DownloadTask downloadTask;
    private String saveFileFullPath;

    private DownloadTool() {
        downloadTask = new DownloadTask();
    }

    public static DownloadTool newInstance() {
        return new DownloadTool();
    }

    /**
     * 设置文件保存位置
     *
     * @param fullPath
     * @return
     */
    public DownloadTool setSavePosition(String fullPath) {
        this.saveFileFullPath = fullPath;
        return this;
    }


    /**
     * 设置文件保存位置
     *
     * @param saveFile
     * @return
     */
    public DownloadTool setSavePosition(File saveFile) {
        this.saveFileFullPath = saveFile.getAbsolutePath();
        return this;
    }

    /**
     * 开始下载
     *
     * @param downloadUrl 下载url
     * @param callBack    状态回调
     * @return
     */
    public DownloadTool startDownload(String downloadUrl, DownloadCallBack callBack) {
        downloadTask.setCallBack(callBack);
        downloadTask.execute(downloadUrl, saveFileFullPath);
        return this;
    }


    /**
     * 取消下载任务
     *
     * @return
     */
    public boolean cancelTask() {
        boolean success = false;
        try {
            success = downloadTask.cancel(true);
        } catch (Exception e) {
        }
        return success;
    }


    public interface DownloadCallBack {
        void onStart();

        void onProgress(int progress, int totalSize);

        void onSuccess(File file);

        void onFail();
    }

    private static class DownloadTask extends AsyncTask<String, Integer, File> {

        private DownloadCallBack callBack;

        public void setCallBack(DownloadCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        protected void onPreExecute() {
            if (callBack != null)
                callBack.onStart();
        }

        @Override
        protected File doInBackground(String... strings) {
            String path = strings[0];
            String saveFile = strings[1];
            try {
                URL url = new URL(path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(6000);
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    int totalSize = connection.getContentLength();
                    File file = new File(saveFile);
                    if (file.exists()) {
                        file.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    int progress = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        progress = len + progress;
                        publishProgress(progress, totalSize);
                    }
                    fos.flush();
                    fos.close();
                    inputStream.close();
                    return file;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            if (callBack != null)
                callBack.onFail();
        }

        @Override
        protected void onCancelled(File file) {
            if (callBack != null)
                callBack.onFail();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (callBack != null)
                callBack.onProgress(values[0], values[1]);
        }

        @Override
        protected void onPostExecute(File file) {
            if (callBack != null) {
                if (null != file && file.length() > 0) {
                    callBack.onSuccess(file);
                } else {
                    callBack.onFail();
                }
            }

        }

    }
}
