package donrenando.commitstrip;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import strip.AddInCache;
import strip.MyTarget;
import strip.TouchImageView;

import static app.utils.PermissionUtils.askPermission;
import static app.utils.PermissionUtils.isOnline;
import static com.squareup.picasso.Picasso.with;
import static strip.PhotosUtils.save;

public class MainActivity extends AppCompatActivity {
    private static final int ASK_WRITE_EXTERNAL_STORAGE_FOR_SAVE = 1;
    public LinkedList<String> imageCache;
    public LinkedList<String> imageHistory;
    ArrayList<String> transformations;
    float initialX, initialY;
    private TouchImageView imageView;
    private String currentImage;
    private ArrayList<AddInCache> cacheThreads;
    private Context context;
    private String randomInAction;
    private String classInAction;
    private float finalX;
    private float finalY;
    private Vibrator v;
    private ProgressBar mProgress;
    private MyTarget imageViewTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        imageCache = new LinkedList<>();
        imageHistory = new LinkedList<>();
        currentImage = null;

        imageView = (TouchImageView) findViewById(R.id.imageMadame);

        cacheThreads = new ArrayList<>();

        v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);


        Spinner modeSpinner = (Spinner) findViewById(R.id.mode_spinner);

        ArrayAdapter<CharSequence> adapterModeSpinner = ArrayAdapter.createFromResource(this,
                R.array.modes_array, android.R.layout.simple_spinner_item);

        adapterModeSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(adapterModeSpinner);

        modeSpinner.setOnItemSelectedListener(new donrenando.commitstrip.ModeSpinner(this));



        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.setVisibility(View.VISIBLE);

        imageViewTarget = new MyTarget(context, imageView, mProgress, ((ImageView) findViewById(R.id.imageTmpAnim)));

        transformations = new ArrayList<>();

        imageView.setOnTouchListener((new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //On bloque les autres actions si on est en zoom
                if (Float.compare(imageView.getCurrentZoom(), (float) 1) != 0)
                    return true;

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN: {
                        initialX = event.getX();
                        initialY = event.getY();
                    }

                    case MotionEvent.ACTION_UP: {
                        finalX = event.getX();
                        finalY = event.getY();

                        if (initialX < finalX) {
                            displayPicture(false);
                            //System.out.println("Left to Right swipe performed");
                        }

                        if (initialX > finalX) {
                            displayPicture(true);
                            //System.out.println("Right to Left swipe performed");
                        }

                        if (initialY < finalY) {
                            //System.out.println("Up to Down swipe performed");
                        }

                        if (initialY > finalY) {
                            //System.out.println("Down to Up swipe performed");
                        }
                        break;
                    }
                }
                return true;
            }
        }));

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onStop() {
        super.onStop();
        // TODO: Stockage app pour reprise + charger la reprise
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ASK_WRITE_EXTERNAL_STORAGE_FOR_SAVE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (save(this, imageView))
                        popUpDown("Image enregistrée !");
                    else
                        popUpDown("Erreur lors l'ajout de la photo !");
                }
                break;
            }
        }
    }

    private void initCount() {
        for (AddInCache t : cacheThreads) {
            if (t.getStatus() != AsyncTask.Status.FINISHED)
                t.cancel(true);
        }
        cacheThreads = new ArrayList<>();
        imageCache = new LinkedList<>();
        imageHistory = new LinkedList<>();
        currentImage = null;
    }

    void changeMode(String mode) {
        String firstimg;
        switch (mode) {
            case "Bonjour CommitStrip":
            default: {
                firstimg = "http://www.commitstrip.com/?random=1";
                randomInAction = "http://www.commitstrip.com/?random=1";
                classInAction = "entry-content";
                break;
            }
        }
        initCount();
        runAddInCache(randomInAction, classInAction, 4, true, firstimg);
        popUpDown(mode);
        v.vibrate(50);
    }


    public void displayPicture(boolean forward) {
        try {
            if (forward)
                displayNewPicture();
            else
                displayOldPicture();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayNewPicture() throws IOException {
        if (!imageCache.isEmpty()) {
            if (currentImage != null)
                imageHistory.push(currentImage);
            currentImage = imageCache.poll();
            imageViewTarget.setAnimation(true);
            with(context)
                    .load(currentImage)
                    .priority(Picasso.Priority.HIGH)
                    .transform(donrenando.commitstrip.TransformSpinner.getTransformation(context, transformations))
                    .into(imageViewTarget);
            runAddInCache(randomInAction, classInAction, 1, false, null);
        }
    }

    private void displayOldPicture() throws IOException {
        if (!imageHistory.isEmpty()) {
            imageCache.addFirst(currentImage);
            currentImage = imageHistory.pop();
            imageViewTarget.setAnimation(false);
            with(context)
                    .load(currentImage)
                    .priority(Picasso.Priority.HIGH)
                    .transform(donrenando.commitstrip.TransformSpinner.getTransformation(context, transformations))
                    .error(R.drawable.cast_ic_notification_disconnect)
                    .into(imageViewTarget);
        } else {
            popUpDown("Vous êtes sur le premier CommitStrip");
        }
    }

    public void refreshImage() {
        with(context)
                .load(currentImage)
                .priority(Picasso.Priority.HIGH)
                .transform(donrenando.commitstrip.TransformSpinner.getTransformation(context, transformations))
                .into(imageViewTarget);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (askPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, ASK_WRITE_EXTERNAL_STORAGE_FOR_SAVE) && isOnline(context)) {
                    if (save(this, imageView))
                        popUpDown("Photo enregistrée");
                    else
                        popUpDown("Erreur lors de l'enregistrement !");
                }
                return true;

            case R.id.action_settings:
                alerte("Application non-officielle du site internet\n" +
                        "www.commitstrip.com." +
                        "\n\nApplication développée par DonRenando" +
                        "\nAvec la contribution de sidya82"
                );
                return true;


            case R.id.action_fullscreen:
                Intent intent = new Intent(MainActivity.this, donrenando.commitstrip.FullscreenActivity.class);
                Bundle extras = new Bundle();
                extras.putString("url_current_image", currentImage);
                extras.putStringArrayList("transfos_current_image", transformations);
                intent.putExtras(extras);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void alerte(String text) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Info");
        alertDialog.
                setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    public void popUpDown(String text) {
        Snackbar.make(imageView, text, Snackbar.LENGTH_LONG).show();
    }


    public void runAddInCache(String url, String classname, int nbImagesToCache, boolean displayFirst, String urlFirst) {
        if (!isOnline(context)) {
            alerte("Vous avez besoin d'une connexion internet !");
            return;
        }
        AddInCache t;
        if (displayFirst) {
            mProgress.setVisibility(View.VISIBLE);
            t = new AddInCache(this) {
                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    displayPicture(true);
                }
            };
            t.execute(urlFirst, classname, 1);
        }
        t = new AddInCache(this);
        t.execute(url, classname, displayFirst ? nbImagesToCache - 1 : nbImagesToCache);
        cacheThreads.add(t);
    }

    public Context getContext() {
        return context;
    }

    public Queue<String> getImageCache() {
        return imageCache;
    }
}
