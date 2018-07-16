package ro.atoming.movies;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import ro.atoming.movies.data.MovieContract;
import ro.atoming.movies.data.MovieDbHelper;
import ro.atoming.movies.models.Movie;
import ro.atoming.movies.models.Trailer;
import ro.atoming.movies.utils.NetworkUtils;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final int LOADER_ID = 33;
    public static final String TRAILER_BASE_URL = "http://www.youtube.com/watch?v=";
    public static final int REVIEW_LENGTH = 200;
    private static int mMovieId;
    private Context mContext;
    @BindView(R.id.detail_movie_poster)ImageView mPoster;
    @BindView(R.id.movie_title_tv) TextView mTitle;
    @BindView(R.id.release_date_tv)TextView mReleaseDate;
    @BindView(R.id.vote_average_tv)TextView mVoteAverage;
    @BindView(R.id.overview_tv)TextView mOverview;
    @BindView(R.id.play_icon)ImageView mPlayTrailer;
    @BindView(R.id.trailer_name_tv)TextView mTrailerName;
    @BindView(R.id.review_author)TextView mReviewAuthor;
    @BindView(R.id.review_content)TextView mReviewContent;
    @BindView(R.id.link_icon)ImageView mReviewLinkImage;
    @BindView(R.id.favorite_image)ImageView mFavoriteImage;
    @BindView(R.id.add_fav_button)Button mFavoriteButton;

    private String mReleaseDateString;
    private double mUserVotes;
    private String mOverviewString;
    private String mTrailerNameString;
    private String mTrailerKey;
    private String mReviewLink;
    private String mReviewAuthorString;
    private String mReviewContentString;
    private String mShortReview;
    private String mMovieTitle;
    private String mPosterString;
    private MovieDbHelper mMovieHelper;
    private Uri mUri;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mMovieHelper = new MovieDbHelper(getApplicationContext());
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle!=null) {
            Movie currentMovie = bundle.getParcelable("movie");
            mMovieTitle = currentMovie.getTitle();
            mTitle.setText(mMovieTitle);
            //set the image
            mPosterString = currentMovie.getPoster();
            Picasso.with(mContext).load(mPosterString).into(mPoster);
            mReleaseDateString = currentMovie.getReleaseDate();
            mReleaseDate.setText(mReleaseDateString);
            //converting double to String
            mUserVotes = currentMovie.getUserRating();
            String userRating = Double.toString(mUserVotes);
            mVoteAverage.setText(userRating);
            //set the overview of the movie
            mOverviewString = currentMovie.getOverview();
            mOverview.setText(mOverviewString);
            mMovieId = currentMovie.getMovieId();
            TrailerTask trailerTask = new TrailerTask();
            trailerTask.execute();
        }
        //TODO - SOLVED: try to send only the Uri through intent.setData and retrieve it here
        else if (intent.getData()!= null){
            mUri = intent.getData();
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(LOADER_ID, null, this);
        }

        //removed the Trailer task at we call it only first time, after that we have it in the dataBase

        mPlayTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trailerUrl = TRAILER_BASE_URL + mTrailerKey;
                Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                Log.v(LOG_TAG, "THIS IS THE TRAILER URL:" + trailerUrl);
                startActivity(trailerIntent);
            }
        });
        mReviewLinkImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reviewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mReviewLink));
                startActivity(reviewIntent);
            }
        });

        mReviewLinkImage.setVisibility(View.VISIBLE);
        if (addedToFavorite(mMovieId)) {
            mFavoriteImage.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            mFavoriteButton.setText(getResources().getString(R.string.fav_button_remove_text));
        }
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!addedToFavorite(mMovieId)) {
                    addMovieToFavorite();
                } else {
                    deleteMovieFromDb(mMovieId);
                }
            }
        });
    }

    /**
     * This method is used to shorten the reviws longer than 200 characters
     */
    private String getShortReview(String review) {
        if (review.length() > REVIEW_LENGTH) {
            mShortReview = review.substring(0, REVIEW_LENGTH) + "...";
        }
        return mShortReview;
    }

    /**
     * method used to check if the movie is already in the Database, giving its ID
     */
    private boolean addedToFavorite(int movieId) {
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(movieId)};//try with the same as selection - COLUMN_MOVIE_ID
        //Uri currentMovieUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id);
        SQLiteDatabase db = mMovieHelper.getReadableDatabase();
        String[] projection = {MovieContract.MovieEntry.COLUMN_MOVIE_ID};
        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
        boolean addedMovie = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return addedMovie;
    }

    /**
     * this method is used to insert the movie into the Database
     */
    private void addMovieToFavorite() {
        AddToFavoriteTask addMovieTask = new AddToFavoriteTask();
        addMovieTask.execute();
        mFavoriteImage.setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }
    private class AddToFavoriteTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovieTitle);
            Log.v(LOG_TAG, "This is the value of movieTitle " + mMovieTitle);
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovieId);
            values.put(MovieContract.MovieEntry.COLUMN_POSTER, mPosterString);
            Log.v(LOG_TAG, "This is the poster path string : " + mPosterString);
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mReleaseDateString);
            values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, mUserVotes);
            values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mOverviewString);
            values.put(MovieContract.MovieEntry.COLUMN_TRAILER_KEY,mTrailerKey);
            values.put(MovieContract.MovieEntry.COLUMN_TRAILER_NAME,mTrailerNameString);
            values.put(MovieContract.MovieEntry.COLUMN_REVIEW_AUTHOR,mReviewAuthorString);
            values.put(MovieContract.MovieEntry.COLUMN_REVIEW_CONTENT,mReviewContentString);
            values.put(MovieContract.MovieEntry.COLUMN_REVIEW_URL,mReviewLink);
            getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
            return null;
        }
    }

    private void deleteMovieFromDb(int mMovieId) {
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(mMovieId)};
        int rowsDeleted = getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, selection, selectionArgs);
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        if (rowsDeleted != 0) {
            Log.v(LOG_TAG, "Number of rows deleted " + rowsDeleted);
            Toast.makeText(DetailActivity.this, "Movie removed from Favorites!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(DetailActivity.this, "Problem removing movie from Favorites!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_TITLE,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_POSTER,
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                MovieContract.MovieEntry.COLUMN_OVERVIEW,
                MovieContract.MovieEntry.COLUMN_TRAILER_KEY,
                MovieContract.MovieEntry.COLUMN_TRAILER_NAME,
                MovieContract.MovieEntry.COLUMN_REVIEW_AUTHOR,
                MovieContract.MovieEntry.COLUMN_REVIEW_CONTENT,
                MovieContract.MovieEntry.COLUMN_REVIEW_URL
        };
        return new CursorLoader(this,
                mUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int movieIdIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            int movieTitleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
            int moviePosterIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);
            int movieReleaseDateIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            int movieVotesIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
            int movieOverviewIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW);
            int trailerKeyIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TRAILER_KEY);
            int trailerNameIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TRAILER_NAME);
            int reviewAuthorIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_REVIEW_AUTHOR);
            int reviewContentIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_REVIEW_CONTENT);
            int reviewUrlIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_REVIEW_URL);

            mPosterString = cursor.getString(moviePosterIndex);
            mMovieId = cursor.getInt(movieIdIndex);
            mMovieTitle = cursor.getString(movieTitleIndex);
            mUserVotes = cursor.getDouble(movieVotesIndex);
            mReleaseDateString = cursor.getString(movieReleaseDateIndex);
            mOverviewString = cursor.getString(movieOverviewIndex);
            mTrailerKey = cursor.getString(trailerKeyIndex);
            mTrailerNameString = cursor.getString(trailerNameIndex);
            mReviewAuthorString = cursor.getString(reviewAuthorIndex);
            mReviewContentString = cursor.getString(reviewContentIndex);
            mReviewLink = cursor.getString(reviewUrlIndex);

            mTitle.setText(mMovieTitle);
            Picasso.with(getApplicationContext()).load(mPosterString).into(mPoster);
            String userRating = Double.toString(mUserVotes);
            mVoteAverage.setText(userRating);
            mReleaseDate.setText(mReleaseDateString);
            mOverview.setText(mOverviewString);
            mTrailerName.setText(mTrailerNameString);
            mReviewAuthor.setText(mReviewAuthorString);
            mReviewContent.setText(mReviewContentString);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private class TrailerTask extends AsyncTask<URL, Void, Trailer> {

        @Override
        protected Trailer doInBackground(URL... urls) {
            URL url = NetworkUtils.buildUrl(NetworkUtils.buildTrailersReviewsUri(String.valueOf(mMovieId)));
            String jsonResponse = "";
            try {
                jsonResponse = NetworkUtils.makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem with HTTP response !", e);
            }
            Trailer currentTrailer = NetworkUtils.extractJsonTrailers(jsonResponse);
            return currentTrailer;
        }

        @Override
        protected void onPostExecute(Trailer trailer) {
            super.onPostExecute(trailer);
            if (trailer != null) {
                mTrailerNameString = "Trailer name: " + trailer.getName();
                mTrailerName.setText(mTrailerNameString);
                //extract the review author and set the text
                mReviewAuthorString = trailer.getAuthor();
                if (!TextUtils.isEmpty(mReviewAuthorString)) {
                    mReviewAuthor.setText(mReviewAuthorString);
                } else {
                    mReviewAuthor.setText("No authors available.");
                }
                //extract the review content and set the text
                mReviewContentString = trailer.getContent();
                if (!TextUtils.isEmpty(mReviewContentString)) {
                    mReviewContent.setText(getShortReview(mReviewContentString));
                } else {
                    mReviewContent.setText("No reviews available.");
                }
                mTrailerKey = trailer.getKey();
                mReviewLink = trailer.getReviewUrl();
                Log.v(LOG_TAG, "This is the REVIEW LINK:" + mReviewLink);
                if (TextUtils.isEmpty(mReviewLink)) {
                    mReviewLinkImage.setVisibility(View.INVISIBLE);
                }
            } else {
                Log.v(LOG_TAG, "Trailer not available !");
            }
        }
    }
}
