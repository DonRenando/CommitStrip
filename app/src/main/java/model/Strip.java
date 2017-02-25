package model;

/**
 * Created by renando on 24/02/17.
 */

public class Strip {
    public String title;
    public String url;

    public Strip(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return (title.isEmpty() || title.equals("") || title.length() == 0) ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
