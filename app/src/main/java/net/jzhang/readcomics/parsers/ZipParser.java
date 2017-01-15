package net.jzhang.readcomics.parsers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.jzhang.readcomics.managers.NaturalOrderComparator;
import net.jzhang.readcomics.managers.Utils;


public class ZipParser implements Parser {
    private ZipFile mZipFile;
    private ArrayList<ZipEntry> mEntries;

    @Override
    public void parse(File file) throws IOException {
        mZipFile = new ZipFile(file.getAbsolutePath());
        mEntries = new ArrayList<ZipEntry>();

        Enumeration<? extends ZipEntry> e = mZipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = e.nextElement();
            if (!ze.isDirectory() && Utils.isImage(ze.getName())) {
                mEntries.add(ze);
            }
        }

        Collections.sort(mEntries, new NaturalOrderComparator() {
            @Override
            public String stringValue(Object o) {
                return ((ZipEntry) o).getName();
            }
        });
    }

    @Override
    public int numPages() {
        return mEntries.size();
    }

    @Override
    public InputStream getPage(int num) throws IOException {
        return mZipFile.getInputStream(mEntries.get(num));
    }

    @Override
    public String getType() {
        return "zip";
    }

    @Override
    public void destroy() throws IOException {
        mZipFile.close();
    }
}
