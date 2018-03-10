package ro.atoming.movies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bogdan on 3/5/2018.
 */


    public class Movie implements Parcelable {

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
        private String mTitle;
        private String mReleaseDate;
    private int mMovieId;
        private String mPoster;
        private double mUserRating;
        private String mOverview;

    public Movie(String title, String releaseDate, String poster, double userRating, String overview
            , int movieId) {
            mTitle = title;
            mReleaseDate = releaseDate;
            mPoster = poster;
            mUserRating = userRating;
            mOverview = overview;
        mMovieId = movieId;
        }
        private Movie(Parcel in){

            mTitle = in.readString();
            mReleaseDate = in.readString();
            mPoster = in.readString();
            mUserRating = in.readDouble();
            mOverview = in.readString();
            mMovieId = in.readInt();
        }
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mTitle);
            parcel.writeString(mReleaseDate);
            parcel.writeString(mPoster);
            parcel.writeDouble(mUserRating);
            parcel.writeString(mOverview);
            parcel.writeInt(mMovieId);
        }

        public String getTitle(){
            return mTitle;
        }
        public String getReleaseDate(){
            return mReleaseDate;
        }
        public String getPoster(){
            return mPoster;
        }
        public double getUserRating(){
            return mUserRating;
        }
        public String getOverview(){
            return mOverview;
        }

    public int getMovieId() {
        return mMovieId;
    }
    }


