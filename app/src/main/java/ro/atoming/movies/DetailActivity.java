package ro.atoming.movies;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

import ro.atoming.movies.models.Movie;
import ro.atoming.movies.models.Trailer;
import ro.atoming.movies.utils.NetworkUtils;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Trailer> {

    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final int LOADER_ID = 33;
    public static final String TRAILER_BASE_URL = "http://www.youtube.com/watch?v=";
    private static String mMovieId;
    private Context mContext;
    private ImageView mPoster;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverage;
    private TextView mOverview;
    private ImageView mPlayTrailer;
    private TextView mTrailerName;
    private TextView mReviewAuthor;
    private TextView mReviewContent;
    private ImageView mReviewLinkImage;
    private String mTrailerNameString;
    private String mTrailerKey;
    private String mReviewLink;
    private String mReviewAuthorString;
    private String mReviewContentString;
    private String mShortReview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTitle = findViewById(R.id.movie_title_tv);
        mPoster = findViewById(R.id.detail_movie_poster);
        mReleaseDate = findViewById(R.id.release_date_tv);
        mVoteAverage = findViewById(R.id.vote_average_tv);
        mOverview = findViewById(R.id.overview_tv);
        mPlayTrailer = findViewById(R.id.play_icon);
        mTrailerName = findViewById(R.id.trailer_name_tv);
        mPlayTrailer = findViewById(R.id.play_icon);
        mReviewAuthor = findViewById(R.id.review_author);
        mReviewContent = findViewById(R.id.review_content);
        mReviewLinkImage = findViewById(R.id.link_icon);


        Intent intent = getIntent();

        Movie currentMovie = intent.getParcelableExtra(getString(R.string.parcel_reference_movie));
        mTitle.setText(currentMovie.getTitle());
        //set the image
        Picasso.with(mContext).load(currentMovie.getPoster()).into(mPoster);
        mReleaseDate.setText(currentMovie.getReleaseDate());
        //converting double to String
        double userVotes = currentMovie.getUserRating();
        String userRating = Double.toString(userVotes);
        mVoteAverage.setText(userRating);
        //set the overview of the movie
        mOverview.setText(currentMovie.getOverview());
        int movieId = currentMovie.getMovieId();
        mMovieId = Integer.toString(movieId);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_ID, null, this);

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
    }

    @Override
    public Loader<Trailer> onCreateLoader(int i, Bundle bundle) {
        return new TrailerLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Trailer> loader, Trailer trailer) {
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
    }

    @Override
    public void onLoaderReset(Loader<Trailer> loader) {

    }

    private String getShortReview(String review) {
        if (review.length() > 200) {
            mShortReview = review.substring(0, 200) + "...";
        }
        return mShortReview;
    }

    private static class TrailerLoader extends AsyncTaskLoader<Trailer> {

        public TrailerLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        public Trailer loadInBackground() {
            URL url = NetworkUtils.buildUrl(NetworkUtils.buildTrailersReviewsUri(mMovieId));
            String jsonResponse = "";
            try {
                jsonResponse = NetworkUtils.makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem with HTTP response !", e);
            }
            Trailer currentTrailer = NetworkUtils.extractJsonTrailers(jsonResponse);
            return currentTrailer;
        }
    }
}
