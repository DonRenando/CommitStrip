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

package app.updateapk

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import app.MainActivity
import app.R

class MajActivity : AppCompatActivity() {
    private var dm: DownloadManager? = null
    private var btn_update: Button? = null
    private var attachmentDownloadCompleteReceive: BroadcastReceiver? = null
    private var currentVersionValue: TextView? = null
    private var lastVersionValue: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maj)

        val actionBar = supportActionBar
        actionBar!!.show()
        actionBar.title = resources.getString(R.string.action_mise_a_jour)
        actionBar.setDisplayHomeAsUpEnabled(true)

        btn_update = findViewById(R.id.button_update) as Button
        btn_update!!.isEnabled = false

        currentVersionValue = findViewById(R.id.currentVersionValue) as TextView
        lastVersionValue = findViewById(R.id.lastVersionValue) as TextView

        dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        object : CheckUpdate() {
            override fun onPostExecute(aBoolean: Boolean?) {
                currentVersionValue!!.text = this.currentVersion.toString()
                lastVersionValue!!.text = this.lastVersion.toString()
                if (!aBoolean!!) {
                    btn_update!!.isEnabled = true
                }
            }
        }.execute(context)


        btn_update!!.setOnClickListener(View.OnClickListener {
            if (Build.VERSION.SDK_INT > 22) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), ASK_WRITE_EXTERNAL_STORAGE_FOR_UPDATE)
                    return@OnClickListener
                }
            }
            CheckUpdate.downloadLastVersion(dm!!)
        })


        attachmentDownloadCompleteReceive = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                    val downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0)
                    val query = DownloadManager.Query()
                    query.setFilterById(downloadId)
                    val cursor = dm!!.query(query)
                    if (cursor.moveToFirst()) {
                        val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        val downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL && downloadLocalUri != null) {
                            CheckUpdate.openApk(context, downloadLocalUri)
                        }
                    }
                    cursor.close()
                }
            }
        }

        registerReceiver(attachmentDownloadCompleteReceive, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    public override fun onDestroy() {
        try {
            if (attachmentDownloadCompleteReceive != null)
                unregisterReceiver(attachmentDownloadCompleteReceive)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_maj, menu)
        return true
    }

    val context: Context
        get() = this.baseContext

    val activity: Activity
        get() = this

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ASK_WRITE_EXTERNAL_STORAGE_FOR_UPDATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CheckUpdate.downloadLastVersion(dm!!)
                } else {
                    Log.e("ERROR", "No write access")
                }
            }
        }
    }

    companion object {
        private val ASK_WRITE_EXTERNAL_STORAGE_FOR_UPDATE = 1
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val myIntent = Intent(applicationContext, MainActivity::class.java)
        startActivityForResult(myIntent, 0)
        return true

    }
}
