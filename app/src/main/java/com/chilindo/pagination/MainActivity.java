package com.chilindo.pagination;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.chilindo.pagination.api.MovieApi;
import com.chilindo.pagination.api.MovieService;
import com.chilindo.pagination.models.MovieResponse;
import com.chilindo.pagination.models.Result;
import com.chilindo.pagination.utils.PaginationScrollListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PAGE_START = 0;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private PaginationAdapter paginationAdapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int TOTAL_PAGES = 4;
    private int currentPage = PAGE_START;
    private MovieService movieService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initAdapter();
        initListener();
        initNetworkCall();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.main_recycler);
        progressBar = findViewById(R.id.main_progress);
    }

    private void initAdapter() {
        paginationAdapter = new PaginationAdapter(this);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(paginationAdapter);
    }

    private void initListener() {
        recyclerView.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                }, 1000);
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        // mocking network delay for API call
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFirstPage();
            }
        }, 1000);
    }


    private void initNetworkCall() {
        movieService = MovieApi.getClient().create(MovieService.class);
        loadFirstPage();
    }

    private List<Result> fetchResults(Response<MovieResponse> movieResponseResult) {
        MovieResponse movieResponse = movieResponseResult.body();
        return movieResponse.getResults();
    }

    private Call<MovieResponse> callTopRatedMovieApi() {
        return movieService.getTopRatedMovies(
                getString(R.string.my_api_key),
                "en_US",
                currentPage
        );
    }

    private void loadFirstPage() {
        callTopRatedMovieApi().enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                List<Result> resultList = fetchResults(response);
                progressBar.setVisibility(View.GONE);
                paginationAdapter.addAll(resultList);

                if (currentPage <= TOTAL_PAGES)
                    paginationAdapter.addLoadingFooter();
                else
                    isLastPage = true;
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {

            }
        });

//        List<Movie> movieList = Movie.createMovies(paginationAdapter.getItemCount());
//        progressBar.setVisibility(View.GONE);
//
//        paginationAdapter.addAll(movieList);
//        if (currentPage <= TOTAL_PAGES)
//            paginationAdapter.addLoadingFooter();
//        else
//            isLastPage = true;
    }

    private void loadNextPage() {
        callTopRatedMovieApi().enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                paginationAdapter.removeLoadingFooter();
                isLoading = false;

                List<Result> resultList = fetchResults(response);
                paginationAdapter.addAll(resultList);

                if (currentPage != TOTAL_PAGES)
                    paginationAdapter.addLoadingFooter();
                else
                    isLastPage = true;
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {

            }
        });

//        List<Movie> movieList = Movie.createMovies(paginationAdapter.getItemCount());
//        paginationAdapter.removeLoadingFooter();
//        isLoading = false;
//        paginationAdapter.addAll(movieList);
//        if (currentPage != TOTAL_PAGES)
//            paginationAdapter.addLoadingFooter();
//        else
//            isLastPage = true;
    }
}
