package strip;


import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import donrenando.commitstrip.MainActivity;

import static com.squareup.picasso.Picasso.with;

public class AddInCache extends AsyncTask<Object, Integer, Boolean> {
    private MainActivity mainActivity;

    public AddInCache(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        String url = (String) params[0];
        String className = (String) params[1];
        Integer nbImageToCache = (Integer) params[2];


        for (int i = 0; i < nbImageToCache; i++) {
            if (isCancelled())
                return false;
            String imageUrl = siteToUrl(url, className);
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
        }
        return true;
    }

    private String siteToUrl(String url, String className) {
        Elements innerHTMLlinks;
        String image = null;
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.getElementsByClass(className);
            for (Element link : links) {
                innerHTMLlinks = link.getElementsByTag("img");
                for (Element inner : innerHTMLlinks) {
                    image = inner.attr("abs:src");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }



}
