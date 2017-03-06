/*
Copyright 2017 Quentin Rouland

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.

 */
package donrenando.commitstrip.updateapk

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import donrenando.commitstrip.updateapk.Version

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

internal open class CheckUpdate : AsyncTask<Context, Int, Boolean>() {

    var currentVersion: Version? = null
        private set
    var lastVersion: Version? = null
        private set

    override fun doInBackground(vararg params: Context): Boolean? {
        currentVersion = currentVersion(params[0])
        lastVersion = lastVersion()
        return isLastVersion(params[0])
    }

    companion object {
        private val URL_CHECK_UPDATE = "http://repo.rdrive.ovh/CommitStrip/CommitStrip/last"
        private val URL_DOWNLOAD_UPDATE = "http://repo.rdrive.ovh/download/CommitStrip/CommitStrip/last"

        private fun lastVersion(): Version? {
            var last: String?  = null
            val v = Version()
            try {
                val url = URL(URL_CHECK_UPDATE)
                val urlConnection = url.openConnection() as HttpURLConnection
                val `in` = urlConnection.inputStream
                try {
                    val bufferedReader = BufferedReader(InputStreamReader(`in`))
                    val stringBuilder = StringBuilder()

                    for (line in bufferedReader.readLine())
                        stringBuilder.append(line)
                    bufferedReader.close()
                    last = stringBuilder.toString()
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                Log.e("ERROR", e.message, e)
                return null
            }

            try {
                val obj = JSONObject(last.toString())
                v.version_major = obj.getString("version_major")
                v.version_minor = obj.getString("version_minor")
                v.version_release = obj.getString("version_release")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return v
        }

        private fun isLastVersion(context: Context): Boolean {
            val current_version = currentVersion(context)
            val last_version = lastVersion()

            assert(current_version != null)
            return current_version == last_version
        }

        private fun currentVersion(context: Context): Version? {
            var pInfo: PackageInfo? = null
            try {
                pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                return null
            }

            val version = pInfo!!.versionName
            var i = 0
            val current_version = Version()
            current_version.version_major = "0"
            current_version.version_minor = "0"
            current_version.version_release = "0"
            for (retval in version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (i == 0)
                    current_version.version_major = retval
                else if (i == 1)
                    current_version.version_minor = retval
                else if (i == 2)
                    current_version.version_release = retval
                i++
            }
            return current_version
        }

        fun urlLastVersion(): String {
            return URL_DOWNLOAD_UPDATE
        }

        fun downloadLastVersion(dm: DownloadManager) {
            val url = urlLastVersion()
            val request = DownloadManager.Request(Uri.parse(url))
            request.setDescription("Update")
            request.setTitle("Update")
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "BonjourSenorita_last.apk")
            dm.enqueue(request)
        }

        fun openApk(context: Context, fileName: String) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(Uri.parse(fileName), "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
