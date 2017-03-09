package strip

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import app.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.bumptech.glide.request.target.Target
import java.lang.Exception


open class MyTarget(view: ImageView?, maxLoopCount: Int, private val context: Context, private val image: strip.TouchImageView, private val progressBar: ProgressBar, private var imageTmp: ImageView) : GlideDrawableImageViewTarget(view, maxLoopCount), RequestListener<String, GlideDrawable>,  Animation.AnimationListener {


    private var animationIn: Animation? = null
    private var animationOut: Animation? = null

    init {
        setAnimation(true)
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        progressBar.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
    }


    override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
        imageTmp.visibility = View.VISIBLE
        Glide.with(context).load("").placeholder(image.drawable).into(imageTmp)
        super.onResourceReady(resource, null)
        image.startAnimation(animationIn)
        imageTmp.startAnimation(animationOut)
        progressBar.visibility = View.GONE
        return true
    }

    override fun onException(e: Exception?, model: String?, target: Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
        progressBar.visibility = View.GONE
        imageTmp.visibility = View.GONE
        //TODO: ajouter une image d'erreur
        return true
    }

    fun setAnimation(next: Boolean) {
        if (!next) {
            animationOut = AnimationUtils.loadAnimation(
                    context, R.anim.slide_to_right
            )
            animationIn = AnimationUtils.loadAnimation(
                    context, R.anim.slide_from_left
            )
        } else {
            animationOut = AnimationUtils.loadAnimation(
                    context, R.anim.slide_to_left
            )
            animationIn = AnimationUtils.loadAnimation(
                    context, R.anim.slide_from_right
            )
        }
        animationIn!!.setAnimationListener(this)
    }

    override fun onAnimationStart(animation: Animation) {
        imageTmp.visibility = View.VISIBLE
    }

    override fun onAnimationEnd(animation: Animation) {
        imageTmp.visibility = View.GONE
    }

    override fun onAnimationRepeat(animation: Animation) {

    }

}


