package com.dhiyaulhaqza.popvies.features.detail.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.dhiyaulhaqza.popvies.R;
import com.dhiyaulhaqza.popvies.config.ApiCfg;
import com.dhiyaulhaqza.popvies.config.Const;
import com.dhiyaulhaqza.popvies.data.MovieDbManager;
import com.dhiyaulhaqza.popvies.databinding.ActivityDetailBinding;
import com.dhiyaulhaqza.popvies.features.detail.model.Trailer;
import com.dhiyaulhaqza.popvies.features.detail.model.TrailerResults;
import com.dhiyaulhaqza.popvies.features.detail.presenter.DetailPresenter;
import com.dhiyaulhaqza.popvies.features.detail.presenter.DetailView;
import com.dhiyaulhaqza.popvies.features.home.model.MovieResults;
import com.dhiyaulhaqza.popvies.features.review.view.ReviewActivity;
import com.dhiyaulhaqza.popvies.utility.PicassoImg;
import com.dhiyaulhaqza.popvies.utility.PreferencesUtil;

public class DetailActivity extends AppCompatActivity implements DetailView{

    private static final String TAG = DetailActivity.class.getSimpleName();
    private ActivityDetailBinding binding;
    private MovieResults results;
    private Trailer trailer;
    private DetailPresenter detailPresenter;
    private TrailerAdapter mAdapter;
    private boolean isFavorite;
    private final TrailerAdapterClickHandler clickHandler = new TrailerAdapterClickHandler() {
        @Override
        public void onAdapterClickHandler(TrailerResults results) {
            Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse(
                    ApiCfg.BASE_YOUTUBE_WATCH + results.getKey()));
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setupRv();
        detailPresenter = new DetailPresenter(this);
        binding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (binding.collapsingToolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(binding.collapsingToolbar)) {
                    //on closed
                    binding.collapsingToolbar.setTitle(getString(R.string.app_name));
                } else {
                    //on opened
                    binding.collapsingToolbar.setTitle("");
                }
            }
        });
        if (!isHasExtra()) return;

        writeUi();
        setAsFavoriteMovie(isFavorite);

        if (savedInstanceState == null) {
            detailPresenter.fetchTrailers(results.getId());
            Log.d(TAG, results.getId());
        }
    }

    private void setAsFavoriteMovie(boolean isFavorite) {
        int imageResourceId;
        if (isFavorite) {
            imageResourceId = R.drawable.ic_favorite_red_500_24dp;
            MovieDbManager.insertFavoriteMovie(this, results);
        } else {
            imageResourceId = R.drawable.ic_favorite_red_200_24dp;
            MovieDbManager.deleteFavoritedMovie(this, results.getId());
        }

        binding.imgFavorite.setImageDrawable(ContextCompat.getDrawable(this, imageResourceId));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Const.SAVE_INSTANCE_TRAILER, trailer);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        addTrailers((Trailer) savedInstanceState.getParcelable(Const.SAVE_INSTANCE_TRAILER));
    }

    private void addTrailers(Trailer trailer) {
        this.trailer = trailer;
        mAdapter.addTrailers(trailer.getResults());
    }

    private void setupRv() {
        binding.rvDetailTrailer.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvDetailTrailer.setLayoutManager(layoutManager);
        mAdapter = new TrailerAdapter(clickHandler);
        binding.rvDetailTrailer.setAdapter(mAdapter);
    }

    private void writeUi() {
        binding.tvTitle.setText(results.getTitle());
        binding.tvDate.setText(results.getRelease_date());
        binding.tvRating.setText(results.getVote_average());
        binding.tvSynopsis.setText(results.getOverview());

        PicassoImg.setImage(binding.imgPoster, ApiCfg.BASE_IMG_URL + results.getPoster_path());
        PicassoImg.setImage(binding.imgBackdrop, ApiCfg.BASE_BACKDROP_URL + results.getBackdrop_path());
    }

    private boolean isHasExtra() {
        Intent intent = getIntent();
        if (intent.hasExtra(Const.DATA) && intent.hasExtra(Const.IS_FAVORITE)) {
            results = intent.getParcelableExtra(Const.DATA);
            isFavorite = intent.getBooleanExtra(Const.IS_FAVORITE, false);
            return true;
        }
        return false;
    }

    public void readReviews(View view) {
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra(Const.MOVIE_EXTRA, results);
        startActivity(intent);
    }

    public void onFavoriteClick(View view) {
        isFavorite = !isFavorite;
        setAsFavoriteMovie(isFavorite);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponse(Trailer trailer) {
        addTrailers(trailer);
    }

    @Override
    public void onFailure(String errMsg) {
        Log.e(TAG, errMsg);
    }

    @Override
    public void onLoading(boolean isLoading) {
        int visibility;
        if (isLoading) {
            visibility = View.GONE;
        } else {
            visibility = View.VISIBLE;
        }

        binding.tvTrailerLabel.setVisibility(visibility);
        binding.rvDetailTrailer.setVisibility(visibility);
    }
}
