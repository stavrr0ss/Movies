package ro.atoming.movies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ro.atoming.movies.models.Movie;

/**
 * Created by Bogdan on 3/5/2018.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final ListItemClickListener mListItemClickListener;
    private List<Movie> mMovieList;
    private Context mContext;

    public MovieAdapter(Context context, List<Movie> movieList, ListItemClickListener listener) {
        mMovieList = movieList;
        mContext = context;
        mListItemClickListener = listener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        view.setFocusable(true);
        return new MovieViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie currentMovie = mMovieList.get(position);
        Picasso.with(mContext).load(currentMovie.getPoster()).into(holder.poster);
        holder.itemView.setTag(currentMovie);
    }

    @Override
    public int getItemCount() {
        if (mMovieList == null) return 0;
        return mMovieList.size();
    }

    public void setData(List<Movie> movieData) {
        mMovieList = movieData;
        notifyDataSetChanged();
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItem);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView poster;
        MovieAdapter mAdapter;

        public MovieViewHolder(View itemView, MovieAdapter movieAdapter) {
            super(itemView);
            poster = itemView.findViewById(R.id.movie_poster);
            mAdapter = movieAdapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedItem = getAdapterPosition();
            Movie movie = mMovieList.get(clickedItem);
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra("movie", movie);
            mContext.startActivity(intent);
            mListItemClickListener.onListItemClick(clickedItem);
        }
    }
}
