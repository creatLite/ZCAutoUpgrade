package com.zc.robot.autoupgrade

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.zc.robot.autoupgrade.constants.PermissionConstants
import com.zc.robot.autoupgrade.utils.PermissionUtils
import com.zc.robot.autoupgrade.utils.RootUtils
import com.zc.robot.autoupgrade.utils.ZCAppUpgradeTool

class MainActivity : AppCompatActivity() {

    private var receiver: MyReceiver? = null
    private var infoTextView: TextView? = null
    private val handler: Handler? = Handler()

    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ZCAppUpgradeTool.ACTION_DOWNLOADING -> {
                    infoTextView?.text = "正在下载,请稍候..."
                }
                ZCAppUpgradeTool.ACTION_DOWNLOAD_FAIL -> {
                    infoTextView?.text = "下载失败"
                    DelayCloseAct()
                }
                ZCAppUpgradeTool.ACTION_INSTALLING_PACKAGE -> {
                    infoTextView?.text = "正在安装"
                    DelayCloseAct()
                }
                ZCAppUpgradeTool.ACTION_INSTALL_FINISHED -> {
                    infoTextView?.text = "安装完成,正在启动应用"
                    DelayCloseAct()
                }
            }
        }
    }

    fun DelayCloseAct() {
        handler?.postDelayed(object : Runnable {
            override fun run() {
                finish()
            }
        }, 3000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        infoTextView = findViewById(R.id.info_text)

        if (RootUtils.checkRoot()) {
            RootUtils.grantRoot(this)
        }

        PermissionUtils.permission(PermissionConstants.STORAGE).callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                initReceiver()
                getParams()
            }

            override fun onDenied() {
                Toast.makeText(this@MainActivity, "授权失败,您的APP使用可能会受限", Toast.LENGTH_LONG).show()
            }

        }).request()
    }

    private fun getParams() {
        val downlaod_url = intent.getStringExtra(ZCAppUpgradeTool.EXTRA_DOWNLOAD_URL)
        val package_name = intent.getStringExtra(ZCAppUpgradeTool.EXTRA_PACKAGE_NAME)

        if (!TextUtils.isEmpty(downlaod_url) && !TextUtils.isEmpty(downlaod_url)) {
            startDownloadServer(downlaod_url, package_name)
        } else {
            finish()
        }

    }

    private fun initReceiver() {
        receiver = MyReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ZCAppUpgradeTool.ACTION_INSTALL_FINISHED)
        intentFilter.addAction(ZCAppUpgradeTool.ACTION_INSTALLING_PACKAGE)
        intentFilter.addAction(ZCAppUpgradeTool.ACTION_DOWNLOAD_FAIL)
        intentFilter.addAction(ZCAppUpgradeTool.ACTION_DOWNLOADING)
        intentFilter.addDataScheme("package")
        registerReceiver(receiver, intentFilter)
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            if (null != receiver) {
                unregisterReceiver(receiver)
                receiver = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


    fun startDownloadServer(download_url: String, package_name: String) {
        val intent = Intent(this, DownloadService::class.java)
        intent.putExtra(ZCAppUpgradeTool.EXTRA_DOWNLOAD_URL, download_url)
        intent.putExtra(ZCAppUpgradeTool.EXTRA_PACKAGE_NAME, package_name)
        startService(intent)
    }

    fun test(view: View) {
        /*val intent = Intent(this, DownloadService::class.java)
        intent.putExtra(ZCAppUpgradeTool.EXTRA_DOWNLOAD_URL, "1111111111111ooopppppppp")
        intent.putExtra(ZCAppUpgradeTool.EXTRA_SAVE_FILE_PATH, "fdsafdsafdsafdsa777777777777777")
        startService(intent)*/

        /* val intent = packageManager.getLaunchIntentForPackage("com.rhwl.osteoporosis")
         startActivity(intent)*/
    }
}
