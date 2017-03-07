package donrenando.commitstrip


import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import app.utils.PermissionUtils.askPermission
import app.utils.PermissionUtils.isOnline
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.with
import model.Strip
import strip.AddInCache
import strip.MyTarget
import strip.PhotosUtils.save
import strip.TouchImageView
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    // Variables nullables
    private var currentImage: Strip? = null

    // Variables non-nulles
    var imageCache: LinkedList<Strip> = LinkedList()
    var imageHistory: LinkedList<Strip> = LinkedList()
    internal var transformations: ArrayList<String> = ArrayList()
    private var cacheThreads: ArrayList<AddInCache> = ArrayList()
    private var randomInAction = "http://www.commitstrip.com/?random=1"
    private var classInAction = "entry-content"
    private var firstimg: String = ""

    // Constantes auto-évaluées
    private val imageView: TouchImageView by lazy { findViewById(R.id.imageMadame) as TouchImageView }
    private val titreStrip: TextView by lazy { findViewById(R.id.titreCommitStrip) as TextView }
    private val v: Vibrator by lazy { this.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    private val mProgress: ProgressBar by lazy { findViewById(R.id.progress_bar) as ProgressBar }
    private val imageViewTarget: MyTarget by lazy { MyTarget(context, imageView, mProgress, findViewById(R.id.imageTmpAnim) as ImageView) }
    private val btnClose: Button by lazy { findViewById(R.id.btnClose) as Button }
    val context: Context by lazy { applicationContext }

    // Constantes fixes
    val NB_CACHE = 5


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_ACTION_BAR)
        setContentView(R.layout.activity_main)
        mProgress.visibility = View.VISIBLE
        imageView.parentActivity = this
        btnClose.visibility = View.GONE
        btnClose.setOnClickListener({ switchToNormal() })
        runAddInCache(randomInAction, classInAction, NB_CACHE, true, firstimg)
        v.vibrate(50)
    }

    public override fun onStart() {
        super.onStart()
    }

    override fun onRestart() {
        super.onRestart()
    }

    public override fun onStop() {
        super.onStop()
        // TODO: Stockage app pour reprise + charger la reprise
    }

    public override fun onDestroy() {
        super.onDestroy()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ASK_WRITE_EXTERNAL_STORAGE_FOR_SAVE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (save(this, imageView))
                        popUpDown("Image enregistrée !")
                    else
                        popUpDown("Erreur lors l'ajout de la photo !")
                }
            }
        }
    }

    private fun initCount() {
        cacheThreads
                .filter { it.status != AsyncTask.Status.FINISHED }
                .forEach { it.cancel(true) }
        cacheThreads = ArrayList<AddInCache>()
        imageCache = LinkedList<Strip>()
        imageHistory = LinkedList<Strip>()
        currentImage = null
    }

    internal fun changeMode(mode: String) {
        firstimg = "http://www.commitstrip.com/?random=1"
        randomInAction = "http://www.commitstrip.com/?random=1"
        classInAction = "entry-content"

        initCount()
        runAddInCache(randomInAction, classInAction, NB_CACHE, true, firstimg)
        popUpDown(mode)
        v.vibrate(50)
    }


    fun displayPicture(forward: Boolean) {
        try {
            if (forward)
                displayNewPicture()
            else
                displayOldPicture()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun displayNewPicture() {
        if (!imageCache.isEmpty()) {
            if (currentImage != null)
                imageHistory.push(currentImage)
            currentImage = imageCache.poll()
            val safe_url = currentImage!!.url
                    .replace("https", "http")
                    .replace("é", "%C3%A9").replace("è", "%C3%A8").replace("ê", "%C3%AA").replace("ë", "%C3%AB")
                    .replace("à", "%C3%A0").replace("â", "%C3%A2")
                    .replace("ç", "%C3%A7")
                    .replace("î", "%C3%AE").replace("ï", "%C3%AF")
                    .replace("û", "%C3%BB").replace("ü", "%C3%BC")
                    .replace("ô", "%C3%B4")
            //println("Showing : "+safe_url)
            imageViewTarget.setAnimation(true)
            with(context)
                    .load(safe_url)
                    .priority(Picasso.Priority.HIGH)
                    .into(imageViewTarget)
            titreStrip.text = currentImage!!.title
            runAddInCache(randomInAction, classInAction, NB_CACHE, false, null)
        }
    }

    @Throws(IOException::class)
    private fun displayOldPicture() {
        if (!imageHistory.isEmpty()) {
            imageCache.addFirst(currentImage)
            currentImage = imageHistory.pop()
            val safe_url = currentImage!!.url
                    .replace("https", "http")
                    .replace("é", "%C3%A9").replace("è", "%C3%A8").replace("ê", "%C3%AA").replace("ë", "%C3%AB")
                    .replace("à", "%C3%A0").replace("â", "%C3%A2")
                    .replace("ç", "%C3%A7")
                    .replace("î", "%C3%AE").replace("ï", "%C3%AF")
                    .replace("û", "%C3%BB").replace("ü", "%C3%BC")
                    .replace("ô", "%C3%B4")
            //println("Showing : "+safe_url)
            imageViewTarget.setAnimation(false)
            with(context)
                    .load(safe_url)
                    .priority(Picasso.Priority.HIGH)
                    .error(R.drawable.cast_ic_notification_disconnect)
                    .into(imageViewTarget)
            titreStrip.text = currentImage!!.title

        } else {
            popUpDown("Vous êtes sur le premier CommitStrip")
        }
    }

    fun refreshImage() {
        with(context)
                .load(currentImage!!.url)
                .priority(Picasso.Priority.HIGH)
                .into(imageViewTarget)
        titreStrip.text = currentImage!!.title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                if (askPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, ASK_WRITE_EXTERNAL_STORAGE_FOR_SAVE) && isOnline(context)) {
                    if (save(this, imageView))
                        popUpDown("Photo enregistrée")
                    else
                        popUpDown("Erreur lors de l'enregistrement !")
                }
                return true
            }

            R.id.action_settings -> {
                alerte("Application non-officielle du site internet\n" +
                        "www.commitstrip.com." +
                        "\n\nApplication développée par DonRenando" +
                        "\nAvec la contribution de sidya82 et danfr"
                )
                return true
            }


            R.id.action_fullscreen -> {
                switchToFullscreen()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun switchToFullscreen() {
        this.supportActionBar?.hide()
        btnClose.visibility = View.VISIBLE
    }

    private fun switchToNormal() {
        this.supportActionBar?.show()
        btnClose.visibility = View.GONE
    }

    fun alerte(text: String) {
        val alertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle("Info")
        alertDialog.setMessage(text)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }


    fun popUpDown(text: String) {
        Snackbar.make(imageView, text, Snackbar.LENGTH_LONG).show()
    }


    fun runAddInCache(url: String, classname: String, nbImagesToCache: Int, displayFirst: Boolean, urlFirst: String?) {
        if (!isOnline(context)) {
            alerte("Vous avez besoin d'une connexion internet !")
            return
        }
        var t: AddInCache
        if (displayFirst) {
            mProgress.visibility = View.VISIBLE
            t = object : AddInCache(this) {
                override fun onPostExecute(aBoolean: Boolean?) {
                    displayPicture(true)
                }
            }
            t.execute(urlFirst, classname, 1, true)
        }
        t = AddInCache(this)
        t.execute(url, classname, if (displayFirst) nbImagesToCache - 1 else nbImagesToCache, false)
        cacheThreads.add(t)
    }

    fun getImageCache(): Queue<Strip> {
        return imageCache
    }

    companion object {
        private val ASK_WRITE_EXTERNAL_STORAGE_FOR_SAVE = 1
    }
}
