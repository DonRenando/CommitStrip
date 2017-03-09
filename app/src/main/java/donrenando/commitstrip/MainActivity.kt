package donrenando.commitstrip


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import donrenando.commitstrip.updateapk.MajActivity
import model.FixedQueue
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
    var imageHistory: FixedQueue<Strip>? = null

    // Variables non-nulles
    var imageCache: LinkedList<Strip> = LinkedList()
    private var cacheThreads: ArrayList<AddInCache> = ArrayList()
    private var prop = Properties()

    // Constantes auto-évaluées
    val imageView: TouchImageView by lazy { findViewById(R.id.imageMadame) as TouchImageView }
    private val titreStrip: TextView by lazy { findViewById(R.id.titreCommitStrip) as TextView }
    private val v: Vibrator by lazy { this.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    private val mProgress: ProgressBar by lazy { findViewById(R.id.progress_bar) as ProgressBar }
    private val btnClose: Button by lazy { findViewById(R.id.btnClose) as Button }
    val context: Context by lazy { applicationContext }
    private val imageViewTarget: MyTarget by lazy { MyTarget(imageView, 10, context, imageView, mProgress, findViewById(R.id.imageTmpAnim) as ImageView) }
    private val NB_CACHE: Int by lazy { prop.getProperty("NB_CACHE").toInt() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_ACTION_BAR)
        setContentView(R.layout.activity_main)
        mProgress.visibility = View.VISIBLE
        imageView.parentActivity = this
        btnClose.visibility = View.GONE
        btnClose.setOnClickListener({ switchToNormal() })
        try {
            //load a properties file
            prop.load(baseContext.assets.open("app.properties"))
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        imageHistory = FixedQueue(prop.getProperty("STACK_SIZE").toInt())
        runAddInCache(NB_CACHE, true)
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

    /**
     * Reinit history and cache at mode change (UNUSED)
     */
    private fun initCount() {
        cacheThreads
                .filter { it.status != AsyncTask.Status.FINISHED }
                .forEach { it.cancel(true) }
        cacheThreads = ArrayList<AddInCache>()
        imageCache = LinkedList<Strip>()
        imageHistory = FixedQueue<Strip>(prop.getProperty("STACK_SIZE").toInt())
        currentImage = null
    }

    /**
     * Switch to another mode (UNUSED)
     */
    internal fun changeMode(mode: String) {
        initCount()
        runAddInCache(NB_CACHE, true)
        popUpDown(mode)
        v.vibrate(50)
    }

    /**
     * Choose to display Nex or older picture
     *
     * @param forward : True if new picture, false otherwise
     */
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

    /**
     * Display a new picture from cache and adding it to history
     */
    @Throws(IOException::class)
    private fun displayNewPicture() {
        if (!imageCache.isEmpty()) {
            if (currentImage != null)
                imageHistory!!.push(currentImage!!)
            currentImage = imageCache.poll()
            imageViewTarget.setAnimation(true)
            val safe_url = currentImage!!.url
                    .replace("https", "http")
                    .replace("é", "%C3%A9").replace("è", "%C3%A8").replace("ê", "%C3%AA").replace("ë", "%C3%AB")
                    .replace("à", "%C3%A0").replace("â", "%C3%A2")
                    .replace("ç", "%C3%A7")
                    .replace("î", "%C3%AE").replace("ï", "%C3%AF")
                    .replace("û", "%C3%BB").replace("ü", "%C3%BC")
                    .replace("ô", "%C3%B4")
            //println("Showing : "+safe_url)
            Glide.with(context)
                    .load(safe_url)
                    .priority(Priority.HIGH)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(imageViewTarget)
                    .into(imageViewTarget)
            titreStrip.text = currentImage!!.title
            runAddInCache(1, false)
        }
    }

    /**
     * Display a new picture from history and adding it on the top of cache
     */
    @Throws(IOException::class)
    private fun displayOldPicture() {
        if (!imageHistory!!.isEmpty()) {
            imageCache.addFirst(currentImage)
            currentImage = imageHistory!!.pop()
            imageViewTarget.setAnimation(false)
            val safe_url = currentImage!!.url
                    .replace("https", "http")
                    .replace("é", "%C3%A9").replace("è", "%C3%A8").replace("ê", "%C3%AA").replace("ë", "%C3%AB")
                    .replace("à", "%C3%A0").replace("â", "%C3%A2")
                    .replace("ç", "%C3%A7")
                    .replace("î", "%C3%AE").replace("ï", "%C3%AF")
                    .replace("û", "%C3%BB").replace("ü", "%C3%BC")
                    .replace("ô", "%C3%B4")
            Glide.with(context)
                    .load(safe_url)
                    .priority(Priority.HIGH)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.cast_ic_notification_disconnect)
                    .listener(imageViewTarget)
                    .into(imageView)
            titreStrip.text = currentImage!!.title

        } else {
            popUpDown("Vous êtes sur le premier CommitStrip")
        }
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
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.info)

                val dialogButton = dialog.findViewById(R.id.dialogButtonOK) as Button
                dialogButton.setOnClickListener { dialog.dismiss() }
                dialog.show()
                return true
            }


            R.id.action_fullscreen -> {
                switchToFullscreen()
                return true
            }

            R.id.action_mise_a_jour -> {
                val intentMaj = Intent(this@MainActivity, MajActivity::class.java)
                startActivity(intentMaj)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Hide ActionBar
     */
    private fun switchToFullscreen() {
        this.supportActionBar?.hide()
        btnClose.visibility = View.VISIBLE
    }

    /**
     * Show ActionBar
     */
    private fun switchToNormal() {
        this.supportActionBar?.show()
        btnClose.visibility = View.GONE
    }

    /**
     * Show "A propos"
     */
    fun alerte(text: String) {
        val alertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle("Info")
        alertDialog.setMessage(text)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }

    /**
     * Show message in a popup at the bottom of the screen
     *
     * @param text : Message to be shown
     */
    fun popUpDown(text: String) {
        Snackbar.make(imageView, text, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Run a background process lo preload pictures into the image cache
     *
     * @param nbImagesToCache : Cache size
     * @param displayFirst : True if this is the first picture to load
     */
    fun runAddInCache(nbImagesToCache: Int, displayFirst: Boolean) {
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
            t.execute(prop, 1, true)
        }
        t = AddInCache(this)
        t.execute(prop, if (displayFirst) nbImagesToCache - 1 else nbImagesToCache, false)
        cacheThreads.add(t)
    }

    companion object {
        private val ASK_WRITE_EXTERNAL_STORAGE_FOR_SAVE = 1
    }
}
