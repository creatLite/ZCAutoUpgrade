package com.zc.robot.autoupgrade

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.zc.robot.autoupgrade.utils.ZCAppUpgradeTool

class AutoUpgradeReceiver : BroadcastReceiver() {
    companion object {
        private val packageSet: HashSet<String> = HashSet()


        fun putPackageName(packageName: String) {
            packageSet.add(packageName)
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        Log.e("AutoUpgradeReceiver", intent?.action)
        Log.e("AutoUpgradeReceiver", intent?.dataString)
        when (intent?.action) {
            Intent.ACTION_PACKAGE_REPLACED -> {
                val size = packageSet.size
                val i = size + 9
                val iterator = packageSet.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    if (("package:".plus(item)).contentEquals(intent?.dataString)) {
                        sendBroadcast(context, ZCAppUpgradeTool.ACTION_INSTALL_FINISHED, null)
                        launchApp(context, item)
                        iterator.remove()
                        break
                    }
                }
            }

            ZCAppUpgradeTool.ACTION_SEND_PACKAGE_NAME -> {
                var packageName = intent.getStringExtra(ZCAppUpgradeTool.EXTRA_PACKAGE_NAME)
                putPackageName(packageName)
            }
        }
    }

    fun launchApp(context: Context, packageName: String) {
        var intent = context.packageManager.getLaunchIntentForPackage(packageName)
        context.startActivity(intent)
    }

    private fun sendBroadcast(context: Context, action: String, extras: Map<String, String>?) {
        //这里的scheme需要配置成package才能正确发送到广播接收者
        val intent = Intent(action, Uri.parse("package:" + context.getPackageName()))

        if (null != extras) {
            for (key in extras.keys) {
                intent.putExtra(key, extras[key])
            }
        }
        context.sendBroadcast(intent)
    }
}
