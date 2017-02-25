package donrenando.commitstrip;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import strip.MyTarget;
import strip.TouchImageView;

import static com.squareup.picasso.Picasso.with;


public class FullscreenActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);

        Bundle extras = getIntent().getExtras();
        String url_current_image = extras.getString("url_current_image");
        ArrayList<String> transfo_current_image = extras.getStringArrayList("transfos_current_image");

        TouchImageView imgDisplay;
        Button btnClose;

        imgDisplay = (TouchImageView) findViewById(R.id.imageMadameFullScreen);
        btnClose = (Button) findViewById(R.id.btnClose);

        ProgressBar mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.setVisibility(View.VISIBLE);

        MyTarget imageViewTarget = new MyTarget(this.getBaseContext(), imgDisplay, mProgress);


        btnClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FullscreenActivity.this.finish();
            }
        });



        with(this.getBaseContext())
                .load(url_current_image)
                .priority(Picasso.Priority.HIGH)
                .into(imageViewTarget);

    }
}
