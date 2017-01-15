package net.jzhang.readcomics.managers;

import net.jzhang.readcomics.model.ComicPreview;

import java.util.List;

public class ComicsListingManager {
    private final List<ComicPreview> mComics;

    public ComicsListingManager(List<ComicPreview> comics) {
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

    public ComicPreview getComicAtIndex(int idx) {
        return mComics.get(idx);
    }

    public int getCount() {
        return mComics.size();
    }
}
