package com.nkanaev.comics.managers;

import com.nkanaev.comics.model.Comic;

import java.util.List;

public class ComicsListingManager {
    private final List<Comic> mComics;

    public ComicsListingManager(List<Comic> comics) {
        /*Collections.sort(comics, new Comparator<Comic>() {
            @Override
            public int compare(Comic lhs, Comic rhs) {
                String leftPath = lhs.getFile().getParentFile().getAbsolutePath();
                String rightPath = rhs.getFile().getParentFile().getAbsolutePath();
                return leftPath.compareTo(rightPath);
            }
        });*/
        mComics = comics;
    }

    public Comic getComicAtIndex(int idx) {
        return mComics.get(idx);
    }

    public int getCount() {
        return mComics.size();
    }
}
