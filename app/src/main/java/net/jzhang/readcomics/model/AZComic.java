package net.jzhang.readcomics.model;

/**
 * Created by Joel on 2017-01-15.
 */

public class AZComic {
    private String url;
    private String slug;
    private String name;
    private boolean completed;

    public String getUrl() {
        return url;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return completed;
    }
}
