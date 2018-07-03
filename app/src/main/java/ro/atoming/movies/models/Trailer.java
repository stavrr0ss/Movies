package ro.atoming.movies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bogdan on 3/13/2018.
 */

public class Trailer implements Parcelable{
    private String mKey;
    private String mName;
    private String mAuthor;
    private String mContent;
    private String mReviewUrl;

    public Trailer(String key, String name, String author, String content, String reviewUrl) {
        mKey = key;
        mName = name;
        mAuthor = author;
        mContent = content;
        mReviewUrl = reviewUrl;
    }

    protected Trailer(Parcel in) {
        mKey = in.readString();
        mName = in.readString();
        mAuthor = in.readString();
        mContent = in.readString();
        mReviewUrl = in.readString();
    }

    public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    public String getKey() {
        return mKey;
    }

    public String getName() {
        return mName;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    public String getReviewUrl() {
        return mReviewUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mKey);
        parcel.writeString(mName);
        parcel.writeString(mAuthor);
        parcel.writeString(mContent);
        parcel.writeString(mReviewUrl);
    }
}
