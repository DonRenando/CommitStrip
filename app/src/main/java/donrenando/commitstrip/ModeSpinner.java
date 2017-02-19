package donrenando.commitstrip;

import android.view.View;
import android.widget.AdapterView;


class ModeSpinner implements AdapterView.OnItemSelectedListener {
    private MainActivity mainActivity;

    ModeSpinner(MainActivity a) {
        mainActivity = a;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (view != null)
            view.clearFocus();
        mainActivity.changeMode((String) adapterView.getItemAtPosition(i));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
