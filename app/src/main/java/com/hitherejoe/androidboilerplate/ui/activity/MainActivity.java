package com.hitherejoe.androidboilerplate.ui.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.hitherejoe.androidboilerplate.R;
import com.hitherejoe.androidboilerplate.data.DataManager;
import com.hitherejoe.androidboilerplate.data.model.Character;
import com.hitherejoe.androidboilerplate.ui.adapter.CharacterAdapter;
import com.hitherejoe.androidboilerplate.util.DataUtils;
import com.hitherejoe.androidboilerplate.util.DialogFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    @Bind(R.id.progress_indicator) ProgressBar mProgressBar;
    @Bind(R.id.recycler_characters) RecyclerView mCharactersRecycler;
    @Bind(R.id.swipe_container) SwipeRefreshLayout mSwipeRefresh;
    @Bind(R.id.toolbar) Toolbar mToolbar;

    @Inject DataManager mDataManager;
    @Inject CharacterAdapter mCharacterAdapter;
    private CompositeSubscription mSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: 8/15/16 6 inject component
        activityComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // TODO: 8/15/16 7 declare subscription
        // declare subscription that contain set of subcription
        mSubscriptions = new CompositeSubscription();
        // todo 8 declare toolbar
        setupToolbar();
        // todo 9 declare recylerview
        setupRecyclerView();
        // todo 10 set character to list
        loadCharacters();
    }

    // TODO: 8/15/16 7b remember to unsubscribe
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_github:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // TODO: 8/15/16 8b this act contains toolbar
    private void setupToolbar() {
        setSupportActionBar(mToolbar);
    }

    private void setupRecyclerView() {
        // TODO: 8/15/16 9b - declare recyclerview
        mCharactersRecycler.setLayoutManager(new LinearLayoutManager(this));
        mCharactersRecycler.setAdapter(mCharacterAdapter);

        // TODO: 8/15/16 9d - declare swipelayout
        mSwipeRefresh.setColorSchemeResources(R.color.primary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO: 8/15/16 9e - when swipe, reset data to empty, load char again
                mCharacterAdapter.setCharacters(new ArrayList<Character>());
                loadCharacters();
            }
        });
    }

    private void loadCharacters() {
        // TODO: 8/15/16 10a - check the network first before load datas to list
        if (DataUtils.isNetworkAvailable(this)) {
            int[] characterIds = getResources().getIntArray(R.array.characters);
            mSubscriptions.add(mDataManager.getCharacters(characterIds)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<List<Character>>() {
                        @Override
                        public void onCompleted() {

                        }

                        // TODO: 8/15/16 10b if fail, print the log, remove progress bar, force stop refresh, create a dialog to inform to user

                        @Override
                        public void onError(Throwable error) {
                            Timber.e("There was an error retrieving the characters " + error);
                            mProgressBar.setVisibility(View.GONE);
                            mSwipeRefresh.setRefreshing(false);
                            DialogFactory.createSimpleErrorDialog(MainActivity.this).show();
                        }

                        // TODO: 8/15/16 10c - update adapter
                        @Override
                        public void onNext(List<Character> characters) {
                            mProgressBar.setVisibility(View.GONE);
                            mSwipeRefresh.setRefreshing(false);
                            mCharacterAdapter.setCharacters(characters);
                        }
                    }));
        }
        // todo 10d if there is not a network, remove progressbar and swiperefresh + create a dialog
        else {
            mProgressBar.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);
            DialogFactory.createSimpleOkErrorDialog(
                    this,
                    getString(R.string.dialog_error_title),
                    getString(R.string.dialog_error_no_connection)
            ).show();
        }
    }

}
