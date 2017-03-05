package app.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat


object PermissionUtils {
    fun askPermission(activity: Activity, permission: String, requestCode: Int?): Boolean {
        var result = false
        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode!!)
            } else {
                result = true
            }
        } else {
            result = true
        }
        return result
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo: NetworkInfo? = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }


}
