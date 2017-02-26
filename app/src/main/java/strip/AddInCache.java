package strip;


import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import donrenando.commitstrip.MainActivity;
import model.Strip;

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
        Boolean first = (boolean) params[3];
        Strip imageUrl;


        for (int i = 0; i < nbImageToCache; i++) {
            if (isCancelled())
                return false;
            if (first)
                imageUrl = firstImage();
            else
                imageUrl = siteToUrl(url, className);
            try {
                if (isCancelled())
                    return false;
                with(mainActivity).load(imageUrl.getUrl()).noFade().get();
                if (isCancelled())
                    return false;
                mainActivity.getImageCache().add(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private Strip siteToUrl(String url, String className) {
        Elements innerHTMLlinks;
        String image = null;
        String titre = null;
        try {

            Document doc = Jsoup.connect(url).get();
            titre = doc.getElementsByClass("entry-title").text();
            Elements links = doc.getElementsByClass(className);
            for (Element link : links) {
                innerHTMLlinks = link.getElementsByTag("img");
                image = innerHTMLlinks.get(0).attr("abs:src");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Strip(titre, image);
    }


    private Strip firstImage() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        String src = "";
        String titre = "";
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        org.w3c.dom.Document doc = null;
        try {
            doc = db.parse(new URL("http://www.commitstrip.com/fr/feed/").openStream());
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        try {
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        Pattern pLink = Pattern.compile(
                "(.*?)(<img)(.*?)(src=\")(.*?)(\")(.*?)(\\/>)(.*?)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        Pattern pTitle = Pattern.compile("(<item><title>)(.*?)(<\\/title>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


        Matcher mLink = pLink.matcher(writer.toString());
        if (mLink.find()) { //use while to find all images, and if for only the first one
            src = mLink.group(5);
        }
        Matcher mTitle = pTitle.matcher(writer.toString().trim().replaceAll("\\r\\n|\\r|\\n|\\t", ""));
        if (mTitle.find()) { //use while to find all images, and if for only the first one
            titre = mTitle.group(2);
        }
        return new Strip(titre, src);
    }



}
