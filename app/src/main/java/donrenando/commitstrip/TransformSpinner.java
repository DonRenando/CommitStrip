package donrenando.commitstrip;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.GrayscaleTransformation;
import jp.wasabeef.picasso.transformations.gpu.InvertFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.picasso.transformations.gpu.SketchFilterTransformation;

class TransformSpinner implements AdapterView.OnItemSelectedListener {
    private MainActivity mainActivity;

    TransformSpinner(MainActivity a) {
        mainActivity = a;
    }

    static List<Transformation> getTransformation(Context context, List<String> transformationName) {
        LinkedList<Transformation> transfos = new LinkedList<>();
        for(String t: transformationName)
            switch (t) {
                case "Grayscale":
                    transfos.add(new GrayscaleTransformation());
                    break;
                case "InvertFilter":
                    transfos.add(new InvertFilterTransformation(context));
                    break;
                case "SepiaFilter":
                    transfos.add(new SepiaFilterTransformation(context));
                    break;
                case "SketchFilter":
                    transfos.add(new SketchFilterTransformation(context));
                    break;
                case "Blur":
                    transfos.add(new BlurTransformation(context, 5));
                    break;
        }
        return transfos;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        ArrayList<String> t = new ArrayList<>();
        t.add((String) adapterView.getItemAtPosition(i));
        mainActivity.transformations = t;
        mainActivity.refreshImage();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
