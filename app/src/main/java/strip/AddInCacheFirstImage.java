package strip;

import android.os.AsyncTask;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import donrenando.commitstrip.MainActivity;

import static com.squareup.picasso.Picasso.with;

/**
 * Created by renando on 19/02/17.
 */

public class AddInCacheFirstImage extends AsyncTask<Object, Integer, Boolean> {
        private MainActivity mainActivity;

    public AddInCacheFirstImage(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }
    @Override
    protected Boolean doInBackground(Object... params) {
        String url = (String) params[0];

            if (isCancelled())
                return false;
            String imageUrl = firstImage(url);
            try {
                if (isCancelled())
                    return false;
                with(mainActivity).load(imageUrl).noFade().get();
                if (isCancelled())
                    return false;
                mainActivity.getImageCache().add(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        return true;
    }

    


    public static String firstImage(String url){
        String urlImage="";
        org.w3c.dom.Document  doc  = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            doc = db.parse(new URL(url).openStream());
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Pattern p = Pattern.compile(
                "(.*?)(<img)(.*?)(src=\")(.*?)(\")(.*?)(\\/>)(.*?)",
                //                       ^^^^^
                // 1     2    3     4      5   6    7    8     9
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(doc.toString());
        if (m.find()) {
            urlImage=m.group(5);
        }
        System.out.println("------ URL : "+urlImage);
        return urlImage;

    }
}
