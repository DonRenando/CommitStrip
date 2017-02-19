package strip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import donrenando.commitstrip.R;


public class MyTarget implements Target, Animation.AnimationListener {
    private strip.TouchImageView image;
    private ImageView imageTmp;
    private ProgressBar progressBar;
    private Context context;
    private Animation animationIn;
    private Animation animationOut;

    public MyTarget(Context context, strip.TouchImageView image, ProgressBar progressBar) {
        this(context, image, progressBar, null);
    }

    public MyTarget(Context context, strip.TouchImageView image, ProgressBar progressBar, ImageView imageTmp) {
        this.image = image;
        this.imageTmp = imageTmp;
        this.progressBar = progressBar;
        this.context = context;
        setAnimation(true);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if (imageTmp != null) {
            imageTmp.setImageDrawable(image.getDrawable());
            image.setImageBitmap(bitmap);
            image.startAnimation(animationIn);
            imageTmp.startAnimation(animationOut);
        } else {
            image.setImageBitmap(bitmap);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        progressBar.setVisibility(View.GONE);
        //TODO: ajouter une image d'erreur
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void setAnimation(boolean next) {
        if (!next) {
            animationOut = AnimationUtils.loadAnimation(
                    context, R.anim.slide_to_right
            );
            animationIn = AnimationUtils.loadAnimation(
                    context, R.anim.slide_from_left
            );
        } else {
            animationOut = AnimationUtils.loadAnimation(
                    context, R.anim.slide_to_left
            );
            animationIn = AnimationUtils.loadAnimation(
                    context, R.anim.slide_from_right
            );
        }
        animationIn.setAnimationListener(this);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        imageTmp.setVisibility(View.VISIBLE);

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        imageTmp.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
