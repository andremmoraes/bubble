package com.nkanaev.comics.managers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.nkanaev.comics.parsers.Parser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class ReadComicsNetworkComicHandler extends RequestHandler {
    private final static String HANDLER_URI = "localcomic";
    private ArrayList<String> pages;

    public ReadComicsNetworkComicHandler(ArrayList<String> pages) {
        this.pages = pages;
    }

    @Override
    public boolean canHandleRequest(Request request) {
        return HANDLER_URI.equals(request.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        int pageNum = Integer.parseInt(request.uri.getFragment());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return new Result(BitmapFactory.decodeFile(pages.get(pageNum), options), Picasso.LoadedFrom.DISK);
    }

    public Uri getPageUri(int pageNum) {
        return Uri.parse(pages.get(pageNum));
    }
}
