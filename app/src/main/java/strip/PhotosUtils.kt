package strip


import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import app.MainActivity
import app.R
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL
import java.util.regex.Pattern

class PhotosUtils : AsyncTask<Any, Void, Boolean>() {
    override fun doInBackground(vararg p: Any?): Boolean {
        val activity = p[0] as MainActivity?
        val url = p[1] as String?

        if (activity != null && url != null) {
            try {
                val f = writeImage(url) ?: return false
                val u = Uri.fromFile(f)
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = u
                activity.baseContext.sendBroadcast(intent)
                activity.popUpDown(activity.getString(R.string.saveSuccess))
            } catch(e: Exception) {
                e.printStackTrace()
                activity.popUpDown(activity.getString(R.string.saveFailed))
            }

            return true
        }

        return false
    }

    private fun writeImage(url: String): File? {
        val sdCard = Environment.getExternalStorageDirectory()
        val dir = File(sdCard.absolutePath + "/commitStrip")
        dir.mkdirs()
        var extension = ""
        val p = Pattern.compile("^.*\\.([A-Za-z]{3,4})$")
        val m = p.matcher(url)
        if (m.matches() && m.groupCount() == 1) {
            extension = m.group(1)
        } else {
            throw Exception("L'url ne correspond pas au pattern : " + url)
        }
        val fileName = String.format("%d.$extension", System.currentTimeMillis())
        val outFile = File(dir, fileName)
        FileUtils.copyURLToFile(URL(url), outFile)

        return outFile
    }
}
