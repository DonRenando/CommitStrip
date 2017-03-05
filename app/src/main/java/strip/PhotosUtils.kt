package strip


import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object PhotosUtils {
    private fun writeImageView(imageView: ImageView): File? {
        val draw = imageView.drawable as BitmapDrawable
        val bitmap = draw.bitmap

        val outStream: FileOutputStream
        val sdCard = Environment.getExternalStorageDirectory()
        val dir = File(sdCard.absolutePath + "/commitStrip")
        dir.mkdirs()
        val fileName = String.format("%d.jpg", System.currentTimeMillis())
        val outFile = File(dir, fileName)
        try {
            outStream = FileOutputStream(outFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        try {
            outStream.flush()
            outStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return outFile
    }

    fun save(activity: Activity, imageView: ImageView): Boolean {
        val f = writeImageView(imageView) ?: return false
        val u = Uri.fromFile(f)
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = u
        activity.baseContext.sendBroadcast(intent)
        return true
    }


    private fun getImageContentUri(context: Context, absPath: String): Uri? {

        val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID), MediaStore.Images.Media.DATA + "=? ", arrayOf(absPath), null)

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(id))

        } else if (!absPath.isEmpty()) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, absPath)
            cursor!!.close()
            return context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            cursor!!.close()
            return null
        }
    }
}
