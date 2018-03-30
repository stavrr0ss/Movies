package ro.atoming.movies;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.stetho.Stetho;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ro.atoming.movies.data.MovieContract;
import ro.atoming.movies.models.Movie;
import ro.atoming.movies.utils.NetworkUtils;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.ListItemClickListener {

    public static final int TASK_LOADER_ID = 22;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int SPAN_COUNT = 2;
    public static final String PATH_POPULAR = "popular";
    public static final String PATH_TOP_RATED = "top_rated";
    private static String PATH_DEFAULT = PATH_POPULAR;
    private static MovieAdapter mAdapter;
    private RecyclerView mRecyclerview;
    private MovieCursorAdapter mCursorAdapter;
    private List<Movie> mMovieList;
    private ProgressBar mProgressBar;
    private TextView mEmptyTextView;

    /**
     * helper method for building the Uri used to query the MovieDb
     */
    private static String buildMovieUri(String path) {
        Uri buildUri = Uri.parse(NetworkUtils.BASE_URL).buildUpon()
                .appendPath(path).appendQueryParameter(NetworkUtils.api_key, NetworkUtils.API_KEY)
                .build();
        return buildUri.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);

        mRecyclerview = findViewById(R.id.recyclerView);
        mProgressBar = findViewById(R.id.progressBar);
        mEmptyTextView = findViewById(R.id.emptyTextView);

        if (isConnected()) {
            ShowMoviesTask showPopularMovies = new ShowMoviesTask();
            showPopularMovies.execute();
            //LoaderManager loaderManager = getLoaderManager();
            //loaderManager.initLoader(TASK_LOADER_ID, null, this);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyTextView.setText(R.string.no_internet_text);
        }
        mAdapter = new MovieAdapter(this, mMovieList, this);
        mRecyclerview.setAdapter(mAdapter);
        mRecyclerview.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        mRecyclerview.setHasFixedSize(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.popular:
                //set the url path to return popular movies and restart the loader
                PATH_DEFAULT = PATH_POPULAR;
                showPopularMovies();
                return true;
            case R.id.top_rated:
                //change the url path and restart the loader
                PATH_DEFAULT = PATH_TOP_RATED;
                showTopRatedMovies();
                return true;
            case R.id.favorites:
                showFavoriteMovies();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPopularMovies() {
        ShowMoviesTask showPopularMovies = new ShowMoviesTask();
        showPopularMovies.execute();
        mAdapter = new MovieAdapter(this, mMovieList, this);
        mRecyclerview.setAdapter(mAdapter);
        mRecyclerview.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        mRecyclerview.setHasFixedSize(true);
    }

    private void showTopRatedMovies() {
        ShowMoviesTask showTopRatedMovies = new ShowMoviesTask();
        showTopRatedMovies.execute();
        mAdapter = new MovieAdapter(this, mMovieList, this);
        mRecyclerview.setAdapter(mAdapter);
        mRecyclerview.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        mRecyclerview.setHasFixedSize(true);
    }

    private void showFavoriteMovies() {
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(TASK_LOADER_ID, null, this);
        mCursorAdapter = new MovieCursorAdapter(this, null, this);
        mRecyclerview.setAdapter(mCursorAdapter);
        mRecyclerview.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        mRecyclerview.setHasFixedSize(true);
    }

    // helper method to check the connectivity to internet
    private boolean isConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_POSTER,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_TITLE
        };
        return new CursorLoader(this,
                MovieContract.MovieEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    /**
     @Override public Loader<List<Movie>> onCreateLoader(int i, Bundle bundle) {
     String uriReturned = buildMovieUri(PATH_DEFAULT).toString();
     Log.v(LOG_TAG, "This is the URL : " + uriReturned);
     return new MovieLoader(this, uriReturned);
     }
     */

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    /**
     @Override public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movieList) {

     mProgressBar.setVisibility(View.GONE);
     if (movieList != null && !movieList.isEmpty()) {
     mAdapter.setData(movieList);
     }
     }


     @Override public void onLoaderReset(Loader<List<Movie>> loader) {
     }
     */
    @Override
    public void onListItemClick(int clickedItem) {
    }

    /**
     private static class MovieLoader extends AsyncTaskLoader<List<Movie>> {

     private String mUrl;

     public MovieLoader(Context context, String url) {
     super(context);
     mUrl = url;
     }

     @Override protected void onStartLoading() {
     forceLoad();
     }

     @Override public List<Movie> loadInBackground() {
     if (mUrl == null) {
     return null;
     }
     List<Movie> movies = NetworkUtils.searchMovies(mUrl);
     return movies;
     }
     }
     */
    private class ShowMoviesTask extends AsyncTask<URL, Void, List<Movie>> {
        List<Movie> movies = new ArrayList<>();

        @Override
        protected List<Movie> doInBackground(URL... urls) {
            movies = NetworkUtils.searchMovies(buildMovieUri(PATH_DEFAULT));
            return movies;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies == null) {
                return;
            }
            mProgressBar.setVisibility(View.GONE);
            if (movies != null && !movies.isEmpty()) {
                mAdapter.setData(movies);
            }
        }
    }
}
