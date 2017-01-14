package com.nkanaev.comics.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.nkanaev.comics.R;
import com.nkanaev.comics.fragment.ReaderFragment;

import java.io.File;


public class ReaderActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_reader);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_reader);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                ReaderFragment fragment = ReaderFragment.create(new File(getIntent().getData().getPath()));
                setFragment(fragment);
            }
            else {
                Bundle extras = getIntent().getExtras();
                ReaderFragment fragment = null;
                ReaderFragment.Mode mode = (ReaderFragment.Mode) extras.getSerializable(ReaderFragment.PARAM_MODE);

                if (mode == ReaderFragment.Mode.MODE_LIBRARY)
                    fragment = ReaderFragment.create(extras.getString(ReaderFragment.PARAM_HANDLER), extras.getInt(ReaderFragment.PARAM_CHAPTER));
                else
                    fragment = ReaderFragment.create((File) extras.getSerializable(ReaderFragment.PARAM_HANDLER));
                setFragment(fragment);
            }
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame_reader, fragment)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
