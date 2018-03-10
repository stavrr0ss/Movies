package ro.atoming.movies.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ro.atoming.movies.Movie;

/**
 * Created by Bogdan on 3/5/2018.
 */

public class NetworkUtils {
    public static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    public static final String BASE_URL = "https://api.themoviedb.org/3/movie/";
    public static final String API_KEY = "50a89c5e64a09dabf5717229df4d5319";

    public static final String api_key = "api_key";

    public static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String POSTER_SIZE = "w185/";

    public static final String JSON_RESULTS_ARRAY = "results";
    public static final String MOVIE_ID = "id";
    public static final String JSON_VOTES_KEY = "vote_average";
    public static final String JSON_TITLE_KEY = "title";
    public static final String JSON_POSTER_PATH = "poster_path";
    public static final String JSON_OVERVIEW_KEY = "overview";
    public static final String JSON_DATE_KEY = "release_date";

    public static List<Movie> searchMovies(String requestUrl) {
        URL url = buildUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem with HTTP response !", e);
        }
        List<Movie> movieList = extractJsonResponse(jsonResponse);
        Log.v(LOG_TAG, "This is the movie list " + movieList.size());
        return movieList;
    }

    public static URL buildUrl(String stringUrl) {
        URL returnedUrl = null;
        try {
            returnedUrl = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return returnedUrl;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        HttpURLConnection urlConnection = null;
        String jsonResponse = "";
        InputStream inputStream = null;
        if (url == null) {
            return jsonResponse;
        }
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                scanner.useDelimiter("\\A");

                if (scanner.hasNext()) {
                    jsonResponse = scanner.next();
                    return jsonResponse;
                } else {
                    return null;
                }
            } else {
                Log.v(LOG_TAG, "HTTP response is " + urlConnection.getResponseCode());
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    public static List<Movie> extractJsonResponse(String jsonMovieList) {
        double averageVotes = 0;
        int movieId = 0;
        String title = "";
        String posterPathString = "";
        String releaseDate = "";
        String overview = "";

        List<Movie> movieList = new ArrayList<>();
        try {

            JSONObject jsonMovie = new JSONObject(jsonMovieList);
            if (jsonMovie.has(JSON_RESULTS_ARRAY)) {
                JSONArray movieResults = jsonMovie.getJSONArray(JSON_RESULTS_ARRAY);
                for (int i = 0; i < movieResults.length(); i++) {
                    JSONObject currentMovie = movieResults.getJSONObject(i);
                    if (currentMovie.has(MOVIE_ID)) {
                        movieId = currentMovie.getInt(MOVIE_ID);
                        Log.v(LOG_TAG, "This is the movie ID: " + movieId);
                    }
                    if (currentMovie.has(JSON_VOTES_KEY)) {
                        averageVotes = currentMovie.getDouble(JSON_VOTES_KEY);
                        Log.v(LOG_TAG, "THIS IS THE USER RATING " + averageVotes);
                    }
                    if (currentMovie.has(JSON_TITLE_KEY)) {
                        title = currentMovie.getString(JSON_TITLE_KEY);
                    }
                    if (currentMovie.has(JSON_POSTER_PATH)) {
                        String posterPathFragment = currentMovie.getString(JSON_POSTER_PATH);
                        posterPathString = buildImageUri(posterPathFragment);
                    }
                    if (currentMovie.has(JSON_DATE_KEY)) {
                        releaseDate = currentMovie.getString(JSON_DATE_KEY);
                    }
                    if (currentMovie.has(JSON_OVERVIEW_KEY)) {
                        overview = currentMovie.getString(JSON_OVERVIEW_KEY);
                    }
                    Movie movie = new Movie(title, releaseDate, posterPathString, averageVotes, overview, movieId);
                    movieList.add(movie);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON Response !", e);
        }
        return movieList;
    }

    private static String buildImageUri(String posterPath) {
        String imagePath = POSTER_BASE_URL + POSTER_SIZE + posterPath;
        return imagePath;
    }
}
