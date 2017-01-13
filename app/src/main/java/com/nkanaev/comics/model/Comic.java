package com.nkanaev.comics.model;

import java.util.ArrayList;


public class Comic {
    private Storage mShelf;
    private int mCurrentPage;

    private String slug;
    public String cover;
    private String status;

    private String url;
    private String title;
    private int num_issues;
    private ArrayList<Genre> tags;

    public String name;
    private String alternate_name;
    private int year;
    private String author;
    private ArrayList<Genre> genre;
    private String description;
    private ArrayList<Issue> issues;

    public Storage getShelf() {
        return mShelf;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public String getSlug() {
        return slug;
    }

    public String getCover() {
        return cover;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public int getNumIssues() {
        return num_issues;
    }

    public ArrayList<Genre> getTags() {
        return tags;
    }

    public String getName() {
        return name;
    }

    public String getAlternateName() {
        return alternate_name;
    }

    public int getYear() {
        return year;
    }

    public String getAuthor() {
        return author;
    }

    public ArrayList<Genre> getGenre() {
        return genre;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Issue> getIssues() {
        return issues;
    }

    /*public void setCurrentPage(int page) {
        mShelf.bookmarkPage(slug, page);
        mCurrentPage = page;
    }*/

    @Override
    public boolean equals(Object o) {
        return (o instanceof Comic) && getSlug().equals(((Comic)o).getSlug());
    }
}