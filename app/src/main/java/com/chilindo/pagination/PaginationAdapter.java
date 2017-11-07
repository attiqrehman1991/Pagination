package com.chilindo.pagination;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chilindo.pagination.models.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Attiq on 11/6/2017 AD.
 */

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private Context context;
    private List<Result> movieList;
    private boolean isLoadingAdded = false;


    public PaginationAdapter(Context context) {
        this.context = context;
        movieList = new ArrayList<>();
    }

    public List<Result> getMovies() {
        return movieList;
    }

    public void setMovies(List<Result> movieList) {
        this.movieList = movieList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View view2 = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(view2);
                break;
        }

        return viewHolder;
    }

    private RecyclerView.ViewHolder getViewHolder(ViewGroup viewGroup, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View view = inflater.inflate(R.layout.item_list, viewGroup, false);
        viewHolder = new MovieVH(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Result movie = movieList.get(position);

        switch (getItemViewType(position)) {
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;

                movieVH.mMovieTitle.setText(movie.getTitle());
                movieVH.mYear.setText(movie.getReleaseDate().substring(0, 4)
                + " | "
                + movie.getOriginalLanguage().toUpperCase());
                movieVH.mMovieDesc.setText(movie.getOverview());
                Glide.with(context)
                        .load(BASE_URL_IMG + movie.getPosterPath())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                movieVH.mProgress.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                movieVH.mProgress.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .crossFade()
                        .into(movieVH.mPosterImg);
                break;
            case LOADING:
                // do nothing
                break;
        }

    }

    @Override
    public int getItemCount() {
        return movieList == null ? 0 : movieList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == movieList.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    /**
     * Helper functions
     */
    public void add(Result movie) {
        movieList.add(movie);
        notifyItemInserted(movieList.size() - 1);
    }

    public void addAll(List<Result> movieList) {
        for (Result movie : movieList) {
            add(movie);
        }
    }

    public void remove(Result movie) {
        int position = movieList.indexOf(movie);
        if (position > -1) {
            movieList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Result getItem(int position) {
        return movieList.get(position);
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0)
            remove(getItem(0));
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Result());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;
        int position = movieList.size() - 1;
        Result movie = getItem(position);
        if (movie != null) {
            movieList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * View Holders
     */

    protected class MovieVH extends RecyclerView.ViewHolder {
        private TextView mMovieTitle;
        private TextView mMovieDesc;
        private TextView mYear;
        private ImageView mPosterImg;
        private ProgressBar mProgress;

        public MovieVH(View view) {
            super(view);
            mMovieTitle = view.findViewById(R.id.movie_title);
            mMovieDesc = view.findViewById(R.id.movie_desc);
            mYear = view.findViewById(R.id.movie_year);
            mPosterImg = view.findViewById(R.id.movie_poster);
            mProgress = view.findViewById(R.id.movie_progress);
        }
    }

    protected class LoadingVH extends RecyclerView.ViewHolder {
        public LoadingVH(View view) {
            super(view);
        }
    }
}
