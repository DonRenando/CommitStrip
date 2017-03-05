package strip


import android.os.AsyncTask

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.xml.sax.SAXException

import java.io.IOException
import java.io.StringWriter
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import donrenando.commitstrip.MainActivity
import model.Strip

import com.squareup.picasso.Picasso.with

open class AddInCache(private val mainActivity: MainActivity) : AsyncTask<Any, Int, Boolean>() {

    override fun doInBackground(vararg params: Any): Boolean? {
        val url = params[0] as String
        val className = params[1] as String
        val nbImageToCache = params[2] as Int
        val first = params[3] as Boolean
        var imageUrl: Strip


        for (i in 0..nbImageToCache - 1) {
            if (isCancelled)
                return false
            if (first)
                imageUrl = firstImage()
            else
                imageUrl = siteToUrl(url, className)
            try {
                if (isCancelled)
                    return false
                with(mainActivity).load(imageUrl.url).noFade().get()
                if (isCancelled)
                    return false
                mainActivity.imageCache.add(imageUrl)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return true
    }

    private fun siteToUrl(url: String, className: String): Strip {
        var innerHTMLlinks: Elements
        var image: String? = null
        var titre: String? = null
        try {

            val doc = Jsoup.connect(url).get()
            titre = doc.getElementsByClass("entry-title").text()
            val links = doc.getElementsByClass(className)
            for (link in links) {
                innerHTMLlinks = link.getElementsByTag("img")
                image = innerHTMLlinks[0].attr("abs:src")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Strip(titre!!, image!!)
    }


    private fun firstImage(): Strip {
        val dbf = DocumentBuilderFactory.newInstance()
        var db: DocumentBuilder? = null
        var src = ""
        var titre = ""
        try {
            db = dbf.newDocumentBuilder()
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        }

        var doc: org.w3c.dom.Document? = null
        try {
            doc = db!!.parse(URL("http://www.commitstrip.com/fr/feed/").openStream())
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val domSource = DOMSource(doc)
        val writer = StringWriter()
        val result = StreamResult(writer)
        val tf = TransformerFactory.newInstance()
        var transformer: Transformer? = null
        try {
            transformer = tf.newTransformer()
        } catch (e: TransformerConfigurationException) {
            e.printStackTrace()
        }

        transformer!!.setOutputProperty(OutputKeys.INDENT, "yes")
        try {
            transformer.transform(domSource, result)
        } catch (e: TransformerException) {
            e.printStackTrace()
        }

        val pLink = Pattern.compile(
                "(.*?)(<img)(.*?)(src=\")(.*?)(\")(.*?)(\\/>)(.*?)",
                Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

        val pTitle = Pattern.compile("(<item><title>)(.*?)(<\\/title>)", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)


        val mLink = pLink.matcher(writer.toString())
        if (mLink.find()) { //use while to find all images, and if for only the first one
            src = mLink.group(5)
        }
        val mTitle = pTitle.matcher(writer.toString().trim { it <= ' ' }.replace("\\r\\n|\\r|\\n|\\t".toRegex(), ""))
        if (mTitle.find()) { //use while to find all images, and if for only the first one
            titre = mTitle.group(2)
        }
        return Strip(titre, src)
    }


}
