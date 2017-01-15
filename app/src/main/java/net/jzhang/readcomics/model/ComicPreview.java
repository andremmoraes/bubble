package net.jzhang.readcomics.model;

import java.util.ArrayList;


public class ComicPreview {
    private String url;
    private String slug;
    private String title;
    private String cover;
    private int num_issues;
    private String status;
    private ArrayList<Genre> tags;

    public String getUrl() {
        return url;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getCover() {
        return cover;
    }

    public int getNumIssues() {
        return num_issues;
    }

    public String getStatus() {
        return status;
    }

    public ArrayList<Genre> getTags() {
        return tags;
    }
}