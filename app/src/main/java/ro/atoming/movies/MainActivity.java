package ro.atoming.movies;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ro.atoming.movies.utils.NetworkUtils;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Movie>>,
        MovieAdapter.ListItemClickListener {

    public static final int LOADER_ID = 22;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final int SPAN_COUNT = 2;
    public static final String PATH_POPULAR = "popular";
    public static final String PATH_TOP_RATED = "top_rated";
    private static String PATH_DEFAULT = PATH_POPULAR;
    private RecyclerView mRecyclerview;
    private MovieAdapter mAdapter;
    private List<Movie> mMovieList;
    private ProgressBar mProgressBar;
    private TextView mEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerview = findViewById(R.id.recyclerView);
        mProgressBar = findViewById(R.id.progressBar);
        mEmptyTextView = findViewById(R.id.emptyTextView);
        if (isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(LOADER_ID, null, this);
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
                getLoaderManager().restartLoader(LOADER_ID, null, this);
                return true;
            case R.id.top_rated:
                //change the url path and restart the loader
                PATH_DEFAULT = PATH_TOP_RATED;
                getLoaderManager().restartLoader(LOADER_ID, null, this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * helper method to check the connectivity to internet
     */
    private boolean isConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int i, Bundle bundle) {
        String uriReturned = buildMovieUri(PATH_DEFAULT).toString();
        return new MovieLoader(this, uriReturned);
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movieList) {

        mProgressBar.setVisibility(View.GONE);
        if (movieList != null && !movieList.isEmpty()) {
            mAdapter.setData(movieList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
    }

    @Override
    public void onListItemClick(int clickedItem) {
    }

    /**
     * helper method for building the Uri used to query the MovieDb
     */
    private Uri buildMovieUri(String path) {
        Uri buildUri = Uri.parse(NetworkUtils.BASE_URL).buildUpon()
                .appendPath(path).appendQueryParameter(NetworkUtils.api_key, NetworkUtils.API_KEY)
                .build();
        return buildUri;
    }

    private static class MovieLoader extends AsyncTaskLoader<List<Movie>> {

        private String mUrl;

        public MovieLoader(Context context, String url) {
            super(context);
            mUrl = url;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public List<Movie> loadInBackground() {
            if (mUrl == null) {
                return null;
            }
            List<Movie> movies = NetworkUtils.searchMovies(mUrl);
            return movies;
        }
    }
}
