package donrenando.commitstrip

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar

import com.squareup.picasso.Picasso

import java.util.ArrayList

import strip.MyTarget
import strip.TouchImageView

import com.squareup.picasso.Picasso.with


class FullscreenActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_fullscreen)

        val extras = intent.extras
        val url_current_image = extras.getString("url_current_image")
        val transfo_current_image = extras.getStringArrayList("transfos_current_image")

        val imgDisplay: TouchImageView = findViewById(R.id.imageMadameFullScreen) as TouchImageView
        val btnClose: Button = findViewById(R.id.btnClose) as Button

        val mProgress = findViewById(R.id.progress_bar) as ProgressBar
        mProgress.visibility = View.VISIBLE

        val imageViewTarget = MyTarget(this.baseContext, imgDisplay, mProgress)

        btnClose.setOnClickListener { this@FullscreenActivity.finish() }

        with(this.baseContext)
                .load(url_current_image)
                .priority(Picasso.Priority.HIGH)
                .into(imageViewTarget)

    }
}
