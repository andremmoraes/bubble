package net.jzhang.readcomics.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import net.jzhang.readcomics.Constants;
import net.jzhang.readcomics.R;
import net.jzhang.readcomics.activity.MainActivity;
import net.jzhang.readcomics.activity.ReaderActivity;
import net.jzhang.readcomics.managers.Utils;
import net.jzhang.readcomics.model.Comic;
import net.jzhang.readcomics.model.Issue;
import net.jzhang.readcomics.model.ReadComicsAPI;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class LibraryBrowserFragment extends Fragment
        implements SearchView.OnQueryTextListener {
    public static final String PARAM_SLUG = "slug";
    public static final String PARAM_URL = "url";
    public static final String PARAM_NAME = "name";

    final int ITEM_VIEW_TYPE_COMIC = 1;
    final int ITEM_VIEW_TYPE_HEADER_RECENT = 2;
    final int ITEM_VIEW_TYPE_HEADER_ALL = 3;

    final int NUM_HEADERS = 2;

    private List<Issue> mIssues = new ArrayList<>();
    private List<Issue> mAllItems = new ArrayList<>();
    private List<Issue> mRecentItems = new ArrayList<>();

    private String mSlug;
    private String mUrl;
    private String mName;
    private String mFilterSearch = "";
    private Picasso mPicasso;
    private int mFilterRead = R.id.menu_browser_filter_all;

    private RecyclerView mComicListView;

    public static LibraryBrowserFragment create(String slug, String name) {
        LibraryBrowserFragment fragment = new LibraryBrowserFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_SLUG, slug);
        args.putString(PARAM_URL, ReadComicsAPI.getAbsoluteUrl("comic/" + slug));
        args.putString(PARAM_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    public LibraryBrowserFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSlug = getArguments().getString(PARAM_SLUG);
        mUrl = getArguments().getString(PARAM_URL);
        mName = getArguments().getString(PARAM_NAME);
        getComics();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_librarybrowser, container, false);

        final int numColumns = calculateNumColumns();
        int spacing = (int) getResources().getDimension(R.dimen.grid_margin);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        layoutManager.setSpanSizeLookup(createSpanSizeLookup());

        mComicListView = (RecyclerView) view.findViewById(R.id.library_grid);
        mComicListView.setHasFixedSize(true);
        mComicListView.setLayoutManager(layoutManager);
        mComicListView.setAdapter(new ComicGridAdapter());
        mComicListView.addItemDecoration(new GridSpacingItemDecoration(numColumns, spacing));

        getActivity().setTitle(mName);
        mPicasso = ((MainActivity) getActivity()).getPicasso();

        return view;
    }

    @Override
    public void onResume() {
        getComics();
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.browser, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_browser_filter_all:
            case R.id.menu_browser_filter_read:
            case R.id.menu_browser_filter_unread:
            case R.id.menu_browser_filter_unfinished:
            case R.id.menu_browser_filter_reading:
                item.setChecked(true);
                mFilterRead = item.getItemId();
                filterContent();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mFilterSearch = s;
        filterContent();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    public void openComic(Issue issue, int position) {
        Intent intent = new Intent(getActivity(), ReaderActivity.class);
        intent.putExtra(ReaderFragment.PARAM_HANDLER, mSlug);
        intent.putExtra(ReaderFragment.PARAM_NAME, issue.getName());
        intent.putExtra(ReaderFragment.PARAM_CHAPTER, issue.getChapter());
        intent.putExtra(ReaderFragment.PARAM_ISSUES, new Gson().toJson(mIssues));
        intent.putExtra(ReaderFragment.PARAM_POSITION, position);
        intent.putExtra(ReaderFragment.PARAM_MODE, ReaderFragment.Mode.MODE_LIBRARY);
        startActivity(intent);
    }

    private void getComics() {
        ReadComicsAPI.get("comic/" + mSlug, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Comic comic = new Gson().fromJson(response.toString(), Comic.class);
                mIssues = comic.getIssues();
                findRecents();
                filterContent();
            }
        });
    }

    private void findRecents() {
        mRecentItems.clear();

        for (Issue c : mIssues) {
            /*if (c.updatedAt > 0) {
                mRecentItems.add(c);
            }*/
        }

        if (mRecentItems.size() > 0) {
            /*Collections.sort(mRecentItems, new Comparator<Comic>() {
                @Override
                public int compare(Issue lhs, Issue rhs) {
                    return lhs.updatedAt > rhs.updatedAt ? -1 : 1;
                }
            });*/
        }

        if (mRecentItems.size() > Constants.MAX_RECENT_COUNT) {
            mRecentItems
                    .subList(Constants.MAX_RECENT_COUNT, mRecentItems.size())
                    .clear();
        }
    }

    private void filterContent() {
        mAllItems.clear();

        for (Issue c : mIssues) {
            if (mFilterSearch.length() > 0 && !c.getName().contains(mFilterSearch))
                continue;
            if (mFilterRead != R.id.menu_browser_filter_all) {
                /*if (mFilterRead == R.id.menu_browser_filter_read && c.getCurrentPage() != c.getTotalPages())
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_unread && c.getCurrentPage() != 0)
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_unfinished && c.getCurrentPage() == c.getTotalPages())
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_reading &&
                        (c.getCurrentPage() == 0 || c.getCurrentPage() == c.getTotalPages()))
                    continue;*/
            }
            mAllItems.add(c);
        }

        if (mComicListView != null) {
            mComicListView.getAdapter().notifyDataSetChanged();
        }
    }

    private Issue getIssueAtPosition(int position) {
        Issue issue;
        if (hasRecent()) {
            if (position > 0 && position < mRecentItems.size() + 1)
                issue = mRecentItems.get(position - 1);
            else
                issue = mAllItems.get(position - mRecentItems.size() - NUM_HEADERS);
        }
        else {
            issue = mAllItems.get(position);
        }
        return issue;
    }

    private int getItemViewTypeAtPosition(int position) {
        if (hasRecent()) {
            if (position == 0)
                return ITEM_VIEW_TYPE_HEADER_RECENT;
            else if (position == mRecentItems.size() + 1)
                return ITEM_VIEW_TYPE_HEADER_ALL;
        }
        return ITEM_VIEW_TYPE_COMIC;
    }

    private boolean hasRecent() {
        return mFilterSearch.length() == 0
                && mFilterRead == R.id.menu_browser_filter_all
                && mRecentItems.size() > 0;
    }

    private int calculateNumColumns() {
        int deviceWidth = Utils.getDeviceWidth(getActivity());
        int columnWidth = getActivity().getResources().getInteger(R.integer.grid_comic_column_width);

        return Math.round((float) deviceWidth / columnWidth);
    }

    private GridLayoutManager.SpanSizeLookup createSpanSizeLookup() {
        final int numColumns = calculateNumColumns();

        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (getItemViewTypeAtPosition(position) == ITEM_VIEW_TYPE_COMIC)
                    return 1;
                return numColumns;
            }
        };
    }

    private final class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpanCount;
        private int mSpacing;

        public GridSpacingItemDecoration(int spanCount, int spacing) {
            mSpanCount = spanCount;
            mSpacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            if (hasRecent()) {
                // those are headers
                if (position == 0 || position == mRecentItems.size() + 1)
                    return;

                if (position > 0 && position < mRecentItems.size() + 1) {
                    position -= 1;
                }
                else {
                    position -= (NUM_HEADERS + mRecentItems.size());
                }
            }

            int column = position % mSpanCount;

            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            outRect.right = (column + 1) * mSpacing / mSpanCount;

            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing;
        }
    }


    private final class ComicGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {
            if (hasRecent()) {
                return mAllItems.size() + mRecentItems.size() + NUM_HEADERS;
            }
            return mAllItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return getItemViewTypeAtPosition(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

            if (i == ITEM_VIEW_TYPE_HEADER_RECENT) {
                TextView view = (TextView) LayoutInflater.from(ctx)
                        .inflate(R.layout.header_library, viewGroup, false);
                view.setText(R.string.library_header_recent);

                int spacing = (int) getResources().getDimension(R.dimen.grid_margin);
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                lp.setMargins(0, spacing, 0, 0);

                return new HeaderViewHolder(view);
            }
            else if (i == ITEM_VIEW_TYPE_HEADER_ALL) {
                TextView view = (TextView) LayoutInflater.from(ctx)
                        .inflate(R.layout.header_library, viewGroup, false);
                view.setText(R.string.library_header_all);

                return new HeaderViewHolder(view);
            }

            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_comic, viewGroup, false);
            return new ComicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder.getItemViewType() == ITEM_VIEW_TYPE_COMIC) {
                Issue comic = getIssueAtPosition(i);
                ComicViewHolder holder = (ComicViewHolder) viewHolder;
                holder.setupComic(comic);
            }
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }

        public void setTitle(int titleRes) {
            ((TextView) itemView).setText(titleRes);
        }
    }

    private class ComicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mCoverView;
        private TextView mTitleTextView;
        private TextView mPagesTextView;

        public ComicViewHolder(View itemView) {
            super(itemView);
            mCoverView = (ImageView) itemView.findViewById(R.id.comicImageView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.comicTitleTextView);
            //mPagesTextView = (TextView) itemView.findViewById(R.id.comicPagerTextView);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        public void setupComic(Issue comic) {
            mTitleTextView.setText(comic.getName());
            //mPagesTextView.setText(Integer.toString(comic.getCurrentPage()) + '/' + Integer.toString(comic.getTotalPages()));

            mPicasso.load(comic.getCover())
                    .into(mCoverView);
        }

        @Override
        public void onClick(View v) {
            int i = getAdapterPosition();
            Issue issue = getIssueAtPosition(i);
            openComic(issue, i);
        }
    }
}
