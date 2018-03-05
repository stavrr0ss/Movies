package ro.atoming.movies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private Context mContext;
    private ImageView mPoster;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverage;
    private TextView mOverview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTitle = findViewById(R.id.movie_title_tv);
        mPoster = findViewById(R.id.detail_movie_poster);
        mReleaseDate = findViewById(R.id.release_date_tv);
        mVoteAverage = findViewById(R.id.vote_average_tv);
        mOverview = findViewById(R.id.overview_tv);

        Intent intent = getIntent();

        Movie currentMovie = intent.getParcelableExtra("movie");
        mTitle.setText(currentMovie.getTitle());
        //set the image
        Picasso.with(mContext).load(currentMovie.getPoster() ).into(mPoster);
        mReleaseDate.setText(currentMovie.getReleaseDate());
        //converting double to String
        double userVotes = currentMovie.getUserRating();
        String userRating = Double.toString(userVotes);
        mVoteAverage.setText(userRating);
        //set the overview of the movie
        mOverview.setText(currentMovie.getOverview());

    }
}
