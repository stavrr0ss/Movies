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

import ro.atoming.movies.models.Movie;
import ro.atoming.movies.models.Trailer;

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

    public static final String TRAILER_PATH = "append_to_response=videos,reviews";

    public static final String JSON_RESULTS_ARRAY = "results";
    public static final String MOVIE_ID = "id";
    public static final String JSON_VOTES_KEY = "vote_average";
    public static final String JSON_TITLE_KEY = "title";
    public static final String JSON_POSTER_PATH = "poster_path";
    public static final String JSON_OVERVIEW_KEY = "overview";
    public static final String JSON_DATE_KEY = "release_date";

    private static String trailerPathString;

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

    public static String makeHttpRequest(URL url) throws IOException {
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
        trailerPathString = "";

        List<Movie> movieList = new ArrayList<>();
        try {

            JSONObject jsonMovie = new JSONObject(jsonMovieList);
            if (jsonMovie.has(JSON_RESULTS_ARRAY)) {
                JSONArray movieResults = jsonMovie.getJSONArray(JSON_RESULTS_ARRAY);
                for (int i = 0; i < movieResults.length(); i++) {
                    JSONObject currentMovie = movieResults.getJSONObject(i);
                    if (currentMovie.has(MOVIE_ID)) {
                        movieId = currentMovie.getInt(MOVIE_ID);
                        trailerPathString = buildTrailersReviewsUri(String.valueOf(movieId));
                        Log.v(LOG_TAG, "This is the trailerPath " + trailerPathString);
                        //trailerList = extractJsonTrailers(trailerPathString);
                    }
                    if (currentMovie.has(JSON_VOTES_KEY)) {
                        averageVotes = currentMovie.getDouble(JSON_VOTES_KEY);
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

    /**
     * helper method to extract the trailers keys and reviews from Json
     */
    public static Trailer extractJsonTrailers(String jsonTrailers) {
        Trailer trailer = null;
        String keyString = "";
        String trailerName = "";
        String reviewAuthor = "";
        String reviewContent = "";
        String reviewUrl = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonTrailers);
            if (jsonObject.has("videos")) {
                Log.v(LOG_TAG, "THE MOVIE HAS VIDEOS");
                JSONObject jsonVideos = jsonObject.getJSONObject("videos");

                if (jsonVideos.has("results")) {
                    JSONArray videoResults = jsonVideos.getJSONArray("results");
                    for (int i = 0; i < videoResults.length(); i++) {
                        JSONObject jsonKey = videoResults.getJSONObject(i);
                        if (jsonKey.has("type")) {
                            String trailerType = jsonKey.getString("type");
                            if (trailerType.matches("Trailer")) {
                                if (jsonKey.has("key")) {
                                    keyString = jsonKey.getString("key");
                                    Log.v(LOG_TAG, "This is json key string : " + keyString);
                                }
                                if (jsonKey.has("name")) {
                                    trailerName = jsonKey.getString("name");
                                }
                            }
                        }
                    }
                }
            }
            if (jsonObject.has("reviews")) {
                JSONObject jsonReviews = jsonObject.getJSONObject("reviews");
                if (jsonReviews.has("results")) {
                    JSONArray reviewsResults = jsonReviews.getJSONArray("results");
                    for (int i = 0; i < reviewsResults.length(); i++) {
                        JSONObject currentReview = reviewsResults.getJSONObject(i);
                        if (currentReview.has("author")) {
                            reviewAuthor = currentReview.getString("author");
                            Log.v(LOG_TAG, "This is the author : " + reviewAuthor);
                        }
                        if (currentReview.has("content")) {
                            reviewContent = currentReview.getString("content");
                        }
                        if (currentReview.has("url")) {
                            reviewUrl = currentReview.getString("url");
                        }
                    }
                }
            }
            trailer = new Trailer(keyString, trailerName, reviewAuthor, reviewContent, reviewUrl);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing JSON trailers response !");
        }
        return trailer;
    }

    private static String buildImageUri(String posterPath) {
        String imagePath = POSTER_BASE_URL + POSTER_SIZE + posterPath;
        return imagePath;
    }

    /**
     * helper method used to build the trailers and reviews path
     */
    public static String buildTrailersReviewsUri(String movieId) {
        //Uri trailerUri = Uri.parse(BASE_URL).buildUpon()
        //         .appendPath(movieId).appendPath(TRAILER_PATH)
        //        .appendQueryParameter(api_key,API_KEY)
        //        .build();
        String trailerPath = BASE_URL + movieId + "?" + api_key + "=" + API_KEY + "&" + TRAILER_PATH;
        //String trailerPath = trailerUri.toString();
        return trailerPath;
    }

}
