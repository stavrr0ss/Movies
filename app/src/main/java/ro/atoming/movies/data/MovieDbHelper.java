package ro.atoming.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ro.atoming.movies.data.MovieContract.MovieEntry;

/**
 * Created by Bogdan on 3/10/2018.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    private static final String CREATE_ENTRIES = "CREATE TABLE " +
            MovieEntry.TABLE_NAME + " (" +
            MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            MovieEntry.COLUMN_TITLE + " TEXT ," +
            MovieEntry.COLUMN_MOVIE_ID + " INTEGER ," +
            MovieEntry.COLUMN_RELEASE_DATE + " TEXT ," +
            MovieEntry.COLUMN_VOTE_AVERAGE + " INTEGER ," +
            MovieEntry.COLUMN_OVERVIEW + " TEXT ," +
            MovieEntry.COLUMN_POSTER + " TEXT ," +
            MovieEntry.COLUMN_TRAILER_KEY + " TEXT ," +
            MovieEntry.COLUMN_TRAILER_NAME + " TEXT ," +
            MovieEntry.COLUMN_REVIEW_AUTHOR + " TEXT ," +
            MovieEntry.COLUMN_REVIEW_CONTENT + " TEXT ," +
            MovieEntry.COLUMN_REVIEW_URL + " TEXT );";

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 6;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
