package com.nkanaev.comics.model;

import java.util.ArrayList;

/**
 * Created by Joel on 2017-01-14.
 */

public class Chapter {
    private transient int currentPage;
    private ArrayList<String> pages;

    public int getCurrentPage() {
        return currentPage;
    }

    public ArrayList<String> getPages() {
        return pages;
    }
}
