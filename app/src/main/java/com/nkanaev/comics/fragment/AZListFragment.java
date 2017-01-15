package com.nkanaev.comics.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nkanaev.comics.R;
import com.nkanaev.comics.activity.MainActivity;
import com.nkanaev.comics.model.AZComic;
import com.nkanaev.comics.model.ReadComicsAPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Joel on 2017-01-15.
 */

public class AZListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public AZListFragment() {}

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private FastScroller mFastScroller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_list, container, false);

        /*mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragmentListLayout);
        //noinspection ResourceAsColor
        mRefreshLayout.setColorSchemeColors(R.color.primary);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setEnabled(true);*/

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_recyclerview);
        mFastScroller = (FastScroller) view.findViewById(R.id.list_fastscroll);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new AZAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mFastScroller.setRecyclerView(mRecyclerView);

        getActivity().setTitle(R.string.menu_list);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.library, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRefresh() {
        setLoading(false);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            mRefreshLayout.setRefreshing(true);
            //mGridView.setOnItemClickListener(null);
        }
        else {
            mRefreshLayout.setRefreshing(false);
            //mGridView.setOnItemClickListener(this);
        }
    }

    public class AZAdapter extends RecyclerView.Adapter<AZAdapter.ViewHolder> implements SectionTitleProvider {
        private List<AZComic> mComicList = new ArrayList<>();

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mName;
            public TextView mCompleted;
            public ViewHolder(View view) {
                super(view);
                mName = (TextView) view.findViewById(R.id.list_row_name);
                mCompleted = (TextView) view.findViewById(R.id.list_row_completed);
            }
        }

        public AZAdapter() {
            ReadComicsAPI.get("comics/all", null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    Type comicType = new TypeToken<List<AZComic>>(){}.getType();
                    List<AZComic> azComics = new Gson().fromJson(response.toString(), comicType);
                    mComicList = azComics;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public AZAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final AZComic comic = mComicList.get(position);
            if (comic.isCompleted()) {
                holder.mCompleted.setVisibility(View.VISIBLE);
            } else {
                holder.mCompleted.setVisibility(View.GONE);
            }
            holder.mName.setText(comic.getName());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LibraryBrowserFragment fragment = LibraryBrowserFragment.create(comic.getSlug(), comic.getName());
                    ((MainActivity)getActivity()).pushFragment(fragment);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mComicList.size();
        }

        @Override
        public String getSectionTitle(int position) {
            if (getItemCount() == 0)
                return "";
            return mComicList.get(position).getName().substring(0, 1);
        }
    }
}
