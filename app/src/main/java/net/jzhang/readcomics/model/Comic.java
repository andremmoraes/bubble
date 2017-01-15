package net.jzhang.readcomics.model;

import java.util.ArrayList;


public class Comic {
    private String slug;
    private String cover;
    private String name;
    private String alternate_name;
    private int year;
    private String status;
    private String author;
    private ArrayList<Genre> genre;
    private String description;
    private ArrayList<Issue> issues;

    public String getSlug() {
        return slug;
    }

    public String getCover() {
        return cover;
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

    public String getStatus() {
        return status;
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
}