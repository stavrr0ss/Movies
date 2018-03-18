package ro.atoming.movies.models;

/**
 * Created by Bogdan on 3/13/2018.
 */

public class Trailer {
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
}
