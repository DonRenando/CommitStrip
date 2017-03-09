package strip


import android.os.AsyncTask
import app.MainActivity
import com.bumptech.glide.Glide
import model.Strip
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

open class AddInCache(private val mainActivity: MainActivity) : AsyncTask<Any, Int, Boolean>() {

    override fun doInBackground(vararg params: Any): Boolean? {
        val prop = params[0] as Properties
        val nbImageToCache = params[1] as Int
        val first = params[2] as Boolean
        var imageUrl: Strip
        var safe_url = ""

        val randUrl = prop.getProperty("RANDOM_URL")
        val firstUrl = prop.getProperty("FIRST_URL")
        val selectorImg = prop.getProperty("IMG_SELECTOR")
        val selectorTitle = prop.getProperty("TITLE_SELECTOR")
        val selectorFirst = prop.getProperty("FIRST_SELECTOR")

        for (i in 0..nbImageToCache - 1) {
            if (isCancelled)
                return false
            if (first) {
                try {
                    val url = firstImageUrl(firstUrl, selectorFirst)
                    imageUrl = siteToUrl(url, selectorImg, selectorTitle)
                } catch(e: NotFoundException) {
                    try {
                        System.err.println("GETTING FIRST IMAGE FAILED, TRYING RANDOM...")
                        imageUrl = siteToUrl(randUrl, selectorImg, selectorTitle)
                    } catch (e: NotFoundException) {
                        System.err.println(e.message)
                        e.printStackTrace()
                        return false
                    }
                }
            } else {
                try {
                    imageUrl = siteToUrl(randUrl, selectorImg, selectorTitle)
                } catch(e: NotFoundException) {
                    try {
                        System.err.println("FIRST RANDOM TRY FAIL, RETRY...")
                        imageUrl = siteToUrl(randUrl, selectorImg, selectorTitle)
                    } catch (e: NotFoundException) {
                        System.err.println(e.message)
                        e.printStackTrace()
                        return false
                    }
                }
            }

            try {
                if (isCancelled)
                    return false
                safe_url = imageUrl.url

                Glide.with(mainActivity).load(safe_url).downloadOnly(mainActivity.imageView.width, mainActivity.imageView.height)
                if (isCancelled)
                    return false
                mainActivity.imageCache.add(imageUrl)
            } catch (e: IOException) {
                e.printStackTrace()
                System.err.println("ERROR when loading url : " + safe_url)
            }

        }
        return true
    }

    @Throws(NotFoundException::class)
    private fun siteToUrl(url: String, selectorImg: String, selectorTitle: String): Strip {
        var image: String? = ""
        var titre: String? = ""

        try {
            val doc = Jsoup.connect(url).get()
            titre = doc.select(selectorTitle)?.text()
            image = doc.select(selectorImg)?.attr("abs:src")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (titre.isNullOrEmpty() || image.isNullOrEmpty())
            throw NotFoundException("Le parser a échoué dans la récupération des éléments : URL=$url TITRE=$titre IMAGE=$image")

        return Strip(titre!!, cleanUrl(image!!))
    }


    private fun cleanUrl(url: String): String {
        return url.replace("https", "http")
                .replace("é", "%C3%A9").replace("è", "%C3%A8").replace("ê", "%C3%AA")
                .replace("ë", "%C3%AB").replace("à", "%C3%A0").replace("ô", "%C3%B4")
                .replace("â", "%C3%A2").replace("ç", "%C3%A7").replace("î", "%C3%AE")
                .replace("ï", "%C3%AF").replace("û", "%C3%BB").replace("ü", "%C3%BC")
    }


    private fun firstImageUrl(url: String, selectorLink: String): String {
        var result: String? = null

        try {
            val doc = Jsoup.connect(url).get()
            result = doc.select(selectorLink)?.attr("abs:href")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (result.isNullOrEmpty())
            throw NotFoundException("Le parser a échoué dans la récupération du lien vers la première image : URL=$url RESULT=$result")

        return result!!
    }


}
