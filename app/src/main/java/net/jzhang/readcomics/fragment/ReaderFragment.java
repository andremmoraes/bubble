package net.jzhang.readcomics.fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.*;
import android.widget.*;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import net.jzhang.readcomics.Constants;
import net.jzhang.readcomics.R;
import net.jzhang.readcomics.activity.ReaderActivity;
import net.jzhang.readcomics.managers.LocalComicHandler;
import net.jzhang.readcomics.managers.ReadComicsNetworkComicHandler;
import net.jzhang.readcomics.managers.Utils;
import net.jzhang.readcomics.model.Chapter;
import net.jzhang.readcomics.model.Issue;
import net.jzhang.readcomics.model.ReadComicsAPI;
import net.jzhang.readcomics.parsers.ParserFactory;
import net.jzhang.readcomics.parsers.RarParser;
import net.jzhang.readcomics.view.ComicViewPager;
import net.jzhang.readcomics.view.PageImageView;
import net.jzhang.readcomics.parsers.Parser;

import com.squareup.picasso.*;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class ReaderFragment extends Fragment implements View.OnTouchListener {
    public static final int RESULT = 1;

    public static final String RESULT_CURRENT_PAGE = "fragment.reader.currentpage";

    public static final String PARAM_HANDLER = "PARAM_HANDLER";
    public static final String PARAM_NAME = "PARAM_NAME";
    public static final String PARAM_CHAPTER = "PARAM_CHAPTER";
    public static final String PARAM_POSITION = "PARAM_POSITION";
    public static final String PARAM_ISSUES = "PARAM_ISSUES";
    public static final String PARAM_MODE = "PARAM_MODE";

    public static final String STATE_FULLSCREEN = "STATE_FULLSCREEN";
    public static final String STATE_NEW_COMIC = "STATE_NEW_COMIC";
    public static final String STATE_NEW_COMIC_TITLE = "STATE_NEW_COMIC_TITLE";

    private ComicViewPager mViewPager;
    private LinearLayout mPageNavLayout;
    private SeekBar mPageSeekBar;
    private TextView mPageNavTextView;
    private ComicPagerAdapter mPagerAdapter;
    private SharedPreferences mPreferences;
    private GestureDetector mGestureDetector;

    private final static HashMap<Integer, Constants.PageViewMode> RESOURCE_VIEW_MODE;
    private boolean mIsFullscreen;
    private int mCurrentPage;
    private String mFilename;
    private Constants.PageViewMode mPageViewMode;
    private boolean mIsLeftToRight;
    private float mStartingX;

    private Parser mParser;
    private Picasso mPicasso;
    private RequestHandler mComicHandler;
    private SparseArray<Target> mTargets = new SparseArray<>();

    private Chapter mChapter;
    private Chapter mNewComic;
    private int mNewComicTitle;

    private Mode mMode;
    private String mSlug;
    private String mName;
    private String mChapterNum;
    private int mPosition;
    private List<Issue> mIssues;

    public enum Mode {
        MODE_LIBRARY,
        MODE_BROWSER;
    }

    static {
        RESOURCE_VIEW_MODE = new HashMap<Integer, Constants.PageViewMode>();
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fill, Constants.PageViewMode.ASPECT_FILL);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fit, Constants.PageViewMode.ASPECT_FIT);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_fit_width, Constants.PageViewMode.FIT_WIDTH);
    }

    public static ReaderFragment create(String slug, String name, String chapter, String issuesJson, int position) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_MODE, Mode.MODE_LIBRARY);
        args.putString(PARAM_HANDLER, slug);
        args.putString(PARAM_NAME, name);
        args.putString(PARAM_CHAPTER, chapter);
        args.putString(PARAM_ISSUES, issuesJson);
        args.putInt(PARAM_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReaderFragment create(File comicpath) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_MODE, Mode.MODE_BROWSER);
        args.putSerializable(PARAM_HANDLER, comicpath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        Mode mode = (Mode) bundle.getSerializable(PARAM_MODE);

        mMode = mode;

        if (mode == Mode.MODE_LIBRARY) {
            mSlug = bundle.getString(PARAM_HANDLER);
            mName = bundle.getString(PARAM_NAME);
            mChapterNum = bundle.getString(PARAM_CHAPTER);
            Type issueListType = new TypeToken<List<Issue>>(){}.getType();
            mIssues = new Gson().fromJson(bundle.getString(PARAM_ISSUES), issueListType);
            mPosition = bundle.getInt(PARAM_POSITION);
            //mChapter = Storage.getStorage(getActivity()).getComic(comicId);
            //file = mChapter.getFile();
            ReadComicsAPI.get("comic/" + mSlug + "/" + mChapterNum, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    mChapter = new Gson().fromJson(response.toString(), Chapter.class);
                    mCurrentPage = 0; // TODO mCurrentPage = mChapter.getCurrentPage();
                    mFilename = mSlug;

                    mCurrentPage = Math.max(1, Math.min(mCurrentPage, mChapter.getPages().size()));

                    mComicHandler = new ReadComicsNetworkComicHandler(mChapter.getPages());
                    mPicasso = new Picasso.Builder(getActivity())
                            .addRequestHandler(mComicHandler)
                            .build();

                    mPageSeekBar.setMax(mChapter.getPages().size() - 1);
                    if (mCurrentPage != -1) {
                        setCurrentPage(mCurrentPage);
                        mCurrentPage = -1;
                    }
                    mViewPager.setAdapter(mPagerAdapter);
                }
            });
        }
        else if (mode == Mode.MODE_BROWSER) {
            File file = (File) bundle.getSerializable(PARAM_HANDLER);
            mParser = ParserFactory.create(file);
            mFilename = file.getName();

            mCurrentPage = Math.max(1, Math.min(mCurrentPage, mParser.numPages()));

            mComicHandler = new LocalComicHandler(mParser);
            mPicasso = new Picasso.Builder(getActivity())
                    .addRequestHandler(mComicHandler)
                    .build();

            // workaround: extract rar achive
            if (mParser instanceof RarParser) {
                File cacheDir = new File(getActivity().getExternalCacheDir(), "c");
                if (!cacheDir.exists()) {
                    cacheDir.mkdir();
                }
                else {
                    for (File f : cacheDir.listFiles()) {
                        f.delete();
                    }
                }
                ((RarParser)mParser).setCacheDirectory(cacheDir);
            }
        }

        mPagerAdapter = new ComicPagerAdapter();
        mGestureDetector = new GestureDetector(getActivity(), new MyTouchListener());

        mPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, 0);
        int viewModeInt = mPreferences.getInt(
                Constants.SETTINGS_PAGE_VIEW_MODE,
                Constants.PageViewMode.ASPECT_FIT.native_int);
        mPageViewMode = Constants.PageViewMode.values()[viewModeInt];
        mIsLeftToRight = mPreferences.getBoolean(Constants.SETTINGS_READING_LEFT_TO_RIGHT, true);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_reader, container, false);

        mPageNavLayout = (LinearLayout) getActivity().findViewById(R.id.pageNavLayout);
        mPageSeekBar = (SeekBar) mPageNavLayout.findViewById(R.id.pageSeekBar);
        if (mMode == Mode.MODE_BROWSER) {
            mPageSeekBar.setMax(mParser.numPages() - 1);
        }
        mPageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mIsLeftToRight)
                        setCurrentPage(progress + 1);
                    else
                        setCurrentPage(mPageSeekBar.getMax() - progress + 1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPicasso.pauseTag(ReaderFragment.this.getActivity());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPicasso.resumeTag(ReaderFragment.this.getActivity());
            }
        });
        mPageNavTextView = (TextView) mPageNavLayout.findViewById(R.id.pageNavTextView);
        mViewPager = (ComicViewPager) view.findViewById(R.id.viewPager);
        if (mMode == Mode.MODE_BROWSER) {
            mViewPager.setAdapter(mPagerAdapter);
        }
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnTouchListener(this);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mIsLeftToRight) {
                    setCurrentPage(position + 1);
                }
                else {
                    setCurrentPage(mViewPager.getAdapter().getCount() - position);
                }
            }
        });
        mViewPager.setOnSwipeOutListener(new ComicViewPager.OnSwipeOutListener() {
            @Override
            public void onSwipeOutAtStart() {
                if (mIsLeftToRight)
                    hitBeginning();
                else
                    hitEnding();
            }

            @Override
            public void onSwipeOutAtEnd() {
                if (mIsLeftToRight)
                    hitEnding();
                else
                    hitBeginning();
            }
        });

        if (mMode == Mode.MODE_BROWSER && mCurrentPage != -1) {
            setCurrentPage(mCurrentPage);
            mCurrentPage = -1;
        }

        if (savedInstanceState != null) {
            boolean fullscreen = savedInstanceState.getBoolean(STATE_FULLSCREEN);
            setFullscreen(fullscreen);

            int newComicId = savedInstanceState.getInt(STATE_NEW_COMIC);
            if (newComicId != -1) {
                int titleRes = savedInstanceState.getInt(STATE_NEW_COMIC_TITLE);
                //confirmSwitch(Storage.getStorage(getActivity()).getComic(newComicId), titleRes);
            }
        }
        else {
            setFullscreen(true);
        }
        if (mMode == Mode.MODE_LIBRARY) {
            getActivity().setTitle(mName);
        } else {
            getActivity().setTitle(mFilename);
        }
        updateSeekBar();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reader, menu);

        switch (mPageViewMode) {
            case ASPECT_FILL:
                menu.findItem(R.id.view_mode_aspect_fill).setChecked(true);
                break;
            case ASPECT_FIT:
                menu.findItem(R.id.view_mode_aspect_fit).setChecked(true);
                break;
            case FIT_WIDTH:
                menu.findItem(R.id.view_mode_fit_width).setChecked(true);
                break;
        }

        if (mIsLeftToRight) {
            menu.findItem(R.id.reading_left_to_right).setChecked(true);
        }
        else {
            menu.findItem(R.id.reading_right_to_left).setChecked(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_FULLSCREEN, isFullscreen());
        outState.putString(STATE_NEW_COMIC, mNewComic != null ? mSlug : "");
        outState.putInt(STATE_NEW_COMIC_TITLE, mNewComic != null ? mNewComicTitle : -1);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (mChapter != null) {
            //mChapter.setCurrentPage(getCurrentPage());
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            mParser.destroy();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        mPicasso.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public int getCurrentPage() {
        if (mIsLeftToRight)
            return mViewPager.getCurrentItem() + 1;
        else
            return mViewPager.getAdapter().getCount() - mViewPager.getCurrentItem();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = mPreferences.edit();
        switch(item.getItemId()) {
            case R.id.view_mode_aspect_fill:
            case R.id.view_mode_aspect_fit:
            case R.id.view_mode_fit_width:
                item.setChecked(true);
                mPageViewMode = RESOURCE_VIEW_MODE.get(item.getItemId());
                editor.putInt(Constants.SETTINGS_PAGE_VIEW_MODE, mPageViewMode.native_int);
                editor.apply();
                updatePageViews(mViewPager);
                break;
            case R.id.reading_left_to_right:
            case R.id.reading_right_to_left:
                item.setChecked(true);
                int page = getCurrentPage();
                mIsLeftToRight = (item.getItemId() == R.id.reading_left_to_right);
                editor.putBoolean(Constants.SETTINGS_READING_LEFT_TO_RIGHT, mIsLeftToRight);
                editor.apply();
                setCurrentPage(page, false);
                mViewPager.getAdapter().notifyDataSetChanged();
                updateSeekBar();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCurrentPage(int page) {
        setCurrentPage(page, true);
    }

    private void setCurrentPage(int page, boolean animated) {
        if (mIsLeftToRight) {
            mViewPager.setCurrentItem(page - 1);
            mPageSeekBar.setProgress(page - 1);
        }
        else {
            mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - page, animated);
            mPageSeekBar.setProgress(mViewPager.getAdapter().getCount() - page);
        }

        int numPages;
        if (mMode == Mode.MODE_LIBRARY) {
            numPages = mChapter.getPages().size();
        } else {
            numPages = mParser.numPages();
        }
        String navPage = new StringBuilder()
                .append(page).append("/").append(numPages)
                .toString();

        mPageNavTextView.setText(navPage);
    }

    private class ComicPagerAdapter extends PagerAdapter {
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if (mMode == Mode.MODE_LIBRARY) {
                return mChapter.getPages().size();
            }
            return mParser.numPages();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final LayoutInflater inflater = (LayoutInflater)getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View layout = inflater.inflate(R.layout.fragment_reader_page, container, false);

            PageImageView pageImageView = (PageImageView) layout.findViewById(R.id.pageImageView);
            if (mPageViewMode == Constants.PageViewMode.ASPECT_FILL)
                pageImageView.setTranslateToRightEdge(!mIsLeftToRight);
            pageImageView.setViewMode(mPageViewMode);
            pageImageView.setOnTouchListener(ReaderFragment.this);

            container.addView(layout);

            MyTarget t = new MyTarget(layout, position);
            loadImage(t);
            mTargets.put(position, t);

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View layout = (View) object;
            mPicasso.cancelRequest(mTargets.get(position));
            mTargets.delete(position);
            container.removeView(layout);

            ImageView iv = (ImageView) layout.findViewById(R.id.pageImageView);
            Drawable drawable = iv.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bd = (BitmapDrawable) drawable;
                Bitmap bm = bd.getBitmap();
                if (bm != null) {
                    bm.recycle();
                }
            }
        }
    }

    private void loadImage(MyTarget t) {
        int pos;
        if (mIsLeftToRight) {
            pos = t.position;
        }
        else {
            pos = mViewPager.getAdapter().getCount() - t.position - 1;
        }

        Uri uri;
        if (mMode == Mode.MODE_LIBRARY) {
            uri = ((ReadComicsNetworkComicHandler)mComicHandler).getPageUri(pos);
        } else {
            uri = ((LocalComicHandler)mComicHandler).getPageUri(pos);
        }

        mPicasso.load(uri)
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .tag(getActivity())
                .resize(Constants.MAX_PAGE_WIDTH, Constants.MAX_PAGE_HEIGHT)
                .centerInside()
                .onlyScaleDown()
                .into(t);
    }

    private class MyTarget implements Target, View.OnClickListener {
        private WeakReference<View> mLayout;
        public final int position;

        public MyTarget(View layout, int position) {
            mLayout = new WeakReference<>(layout);
            this.position = position;
        }

        private void setVisibility(int imageView, int progressBar, int reloadButton) {
            View layout = mLayout.get();
            layout.findViewById(R.id.pageImageView).setVisibility(imageView);
            layout.findViewById(R.id.pageProgressBar).setVisibility(progressBar);
            layout.findViewById(R.id.reloadButton).setVisibility(reloadButton);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            View layout = mLayout.get();
            if (layout == null)
                return;

            setVisibility(View.VISIBLE, View.GONE, View.GONE);
            ImageView iv = (ImageView) layout.findViewById(R.id.pageImageView);
            iv.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            View layout = mLayout.get();
            if (layout == null)
                return;

            setVisibility(View.GONE, View.GONE, View.VISIBLE);

            ImageButton ib = (ImageButton) layout.findViewById(R.id.reloadButton);
            ib.setOnClickListener(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }

        @Override
        public void onClick(View v) {
            View layout = mLayout.get();
            if (layout == null)
                return;

            setVisibility(View.GONE, View.VISIBLE, View.GONE);
            loadImage(this);
        }
    }

    public boolean onKeyDown(int keyCode) {
        if (!mIsFullscreen) return false;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            if (getCurrentPage() == mViewPager.getAdapter().getCount())
                hitEnding();
            else
                setCurrentPage(getCurrentPage() + 1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (getCurrentPage() == 1)
                hitBeginning();
            else
                setCurrentPage(getCurrentPage() - 1);
            return true;
        }
        return false;
    }

    private class MyTouchListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!isFullscreen()) {
                setFullscreen(true, true);
                return true;
            }

            float x = e.getX();

            // tap left edge
            if (x < (float) mViewPager.getWidth() / 3) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == 1)
                        hitBeginning();
                    else
                        setCurrentPage(getCurrentPage() - 1);
                }
                else {
                    if (getCurrentPage() == mViewPager.getAdapter().getCount())
                        hitEnding();
                    else
                        setCurrentPage(getCurrentPage() + 1);
                }
            }
            // tap right edge
            else if (x > (float) mViewPager.getWidth() / 3 * 2) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == mViewPager.getAdapter().getCount())
                        hitEnding();
                    else
                        setCurrentPage(getCurrentPage() + 1);
                }
                else {
                    if (getCurrentPage() == 1)
                        hitBeginning();
                    else
                        setCurrentPage(getCurrentPage() - 1);
                }
            }
            else
                setFullscreen(false, true);

            return true;
        }
    }

    private void updatePageViews(ViewGroup parentView) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            final View child = parentView.getChildAt(i);
            if (child instanceof ViewGroup) {
                updatePageViews((ViewGroup)child);
            }
            else if (child instanceof PageImageView) {
                PageImageView view = (PageImageView) child;
                if (mPageViewMode == Constants.PageViewMode.ASPECT_FILL)
                    view.setTranslateToRightEdge(!mIsLeftToRight);
                view.setViewMode(mPageViewMode);
            }
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

    private void setFullscreen(boolean fullscreen) {
        setFullscreen(fullscreen, false);
    }

    private void setFullscreen(boolean fullscreen, boolean animated) {
        mIsFullscreen = fullscreen;

        ActionBar actionBar = getActionBar();

        if (fullscreen) {
            if (actionBar != null) actionBar.hide();

            int flag =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Utils.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.INVISIBLE);
        }
        else {
            if (actionBar != null) actionBar.show();

            int flag =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (Utils.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.VISIBLE);

            // status bar & navigation bar background won't show in some cases
            if (Utils.isLollipopOrLater()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Window w = getActivity().getWindow();
                        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    }
                }, 300);
            }
        }
    }

    private boolean isFullscreen() {
        return mIsFullscreen;
    }

    private void hitBeginning() {
        if (mChapter != null) {
            //Comic c = Storage.getStorage(getActivity()).getPrevComic(mChapter);
            //confirmSwitch(c, R.string.switch_prev_comic);
            if (mMode == Mode.MODE_LIBRARY) {
                confirmSwitch(mPosition - 1, R.string.switch_prev_comic);
            }
        }
    }

    private void hitEnding() {
        if (mChapter != null) {
            //Comic c = Storage.getStorage(getActivity()).getNextComic(mChapter);
            //confirmSwitch(c, R.string.switch_next_comic);
            if (mMode == Mode.MODE_LIBRARY) {
                confirmSwitch(mPosition + 1, R.string.switch_next_comic);
            }
        }
    }

    private void confirmSwitch(final int position, int titleRes) {
        if (position < 0 || position == mIssues.size())
            return;
        final Issue newIssue = mIssues.get(position);

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(titleRes)
                .setMessage(newIssue.getName())
                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ReaderActivity activity = (ReaderActivity) getActivity();
                        activity.setFragment(ReaderFragment.create(mSlug, newIssue.getName(), newIssue.getChapter(), new Gson().toJson(mIssues), position));
                    }
                })
                .setNegativeButton(R.string.switch_action_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //mNewComic = null;
                    }
                })
                .create();
        dialog.show();
    }

    private void confirmSwitch(Chapter newChapter, int titleRes) {
        if (newChapter == null)
            return;

        mNewComic = newChapter;
        mNewComicTitle = titleRes;

        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(titleRes)
//                .setMessage(newComic.getName())
                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //ReaderActivity activity = (ReaderActivity) getActivity();
                        //activity.setFragment(ReaderFragment.create(mNewComic.getSlug()));
                        mNewComic = null;
                    }
                })
                .setNegativeButton(R.string.switch_action_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNewComic = null;
                    }
                })
                .create();
        dialog.show();
    }

    private void updateSeekBar() {
        int seekRes = (mIsLeftToRight)
                ? R.drawable.reader_nav_progress
                : R.drawable.reader_nav_progress_inverse;

        Drawable d = getActivity().getResources().getDrawable(seekRes);
        Rect bounds = mPageSeekBar.getProgressDrawable().getBounds();
        mPageSeekBar.setProgressDrawable(d);
        mPageSeekBar.getProgressDrawable().setBounds(bounds);
    }
}
