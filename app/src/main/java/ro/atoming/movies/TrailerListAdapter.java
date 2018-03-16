package ro.atoming.movies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ro.atoming.movies.models.Trailer;

/**
 * Created by Bogdan on 3/13/2018.
 */

public class TrailerListAdapter extends ArrayAdapter<Trailer> {

    public static final String LOG_TAG = TrailerListAdapter.class.getSimpleName();

    public TrailerListAdapter(Context context, List<Trailer> trailers) {
        super(context, 0, trailers);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailers,
                    parent, false);

            Trailer currentTrailer = getItem(position);
            TextView trailerNameTv = listItemView.findViewById(R.id.trailer_name);
            String trailerName = currentTrailer.getName();
            trailerNameTv.setText(trailerName);

        }
        return listItemView;
    }
}
