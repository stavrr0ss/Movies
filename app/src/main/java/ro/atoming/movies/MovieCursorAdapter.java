package ro.atoming.movies;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ro.atoming.movies.data.MovieContract;
import ro.atoming.movies.models.Movie;

/**
 * Created by Bogdan on 3/28/2018.
 */

public class MovieCursorAdapter extends RecyclerView.Adapter<MovieCursorAdapter.CursorViewHolder> {


    private final MovieAdapter.ListItemClickListener mListItemClickListener;
    private List<Movie> mMovieList;
    private Cursor mCursor;
    private Context mContext;

    public MovieCursorAdapter(Context context, Cursor cursor, MovieAdapter.ListItemClickListener listItemClickListener) {
        mCursor = cursor;
        mContext = context;
        mListItemClickListener = listItemClickListener;
    }

    @Override
    public CursorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
        return new CursorViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(CursorViewHolder holder, int position) {
        int idIndex = mCursor.getColumnIndex(MovieContract.MovieEntry._ID);
        int posterIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);
        mCursor.moveToPosition(position);
        int id = mCursor.getInt(idIndex);
        holder.itemView.setTag(id);
        String posterPath = mCursor.getString(posterIndex);
        Picasso.with(mContext).load(posterPath).into(holder.poster);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    public class CursorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView poster;
        MovieCursorAdapter mAdapter;

        public CursorViewHolder(View itemView, MovieCursorAdapter movieCursorAdapter) {
            super(itemView);
            poster = itemView.findViewById(R.id.movie_poster);
            mAdapter = movieCursorAdapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int clickedItem = (int) view.getTag();
            mCursor.moveToPosition(clickedItem);
            Uri curentMovieUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, clickedItem);
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.setData(curentMovieUri);
            mContext.startActivity(intent);
            mListItemClickListener.onListItemClick(clickedItem);
        }
    }
}
