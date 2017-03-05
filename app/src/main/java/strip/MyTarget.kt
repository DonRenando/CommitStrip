package strip

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar

import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

import donrenando.commitstrip.R


class MyTarget @JvmOverloads constructor(private val context: Context, private val image: strip.TouchImageView, private val progressBar: ProgressBar, private val imageTmp: ImageView? = null) : Target, Animation.AnimationListener {
    private var animationIn: Animation? = null
    private var animationOut: Animation? = null

    init {
        setAnimation(true)
    }

    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
        if (imageTmp != null) {
            imageTmp.setImageDrawable(image.drawable)
            image.setImageBitmap(bitmap)
            image.startAnimation(animationIn)
            imageTmp.startAnimation(animationOut)
        } else {
            image.setImageBitmap(bitmap)
        }
        progressBar.visibility = View.GONE
    }

    override fun onBitmapFailed(errorDrawable: Drawable?) {
        progressBar.visibility = View.GONE
        //TODO: ajouter une image d'erreur
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        progressBar.visibility = View.VISIBLE
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
        imageTmp!!.visibility = View.VISIBLE

    }

    override fun onAnimationEnd(animation: Animation) {
        imageTmp!!.visibility = View.GONE
    }

    override fun onAnimationRepeat(animation: Animation) {

    }
}
