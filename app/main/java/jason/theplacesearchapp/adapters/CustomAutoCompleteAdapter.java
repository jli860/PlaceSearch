package jason.theplacesearchapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import jason.theplacesearchapp.R;
import jason.theplacesearchapp.helper.Place;

public class CustomAutoCompleteAdapter extends ArrayAdapter {
    private static final String TAG = "CustomAutoCompAdapter";
    private List<Place> dataList;
    private GeoDataClient geoDataClient;

    private CustomAutoCompleteAdapter.CustomAutoCompleteFilter listFilter =
            new CustomAutoCompleteAdapter.CustomAutoCompleteFilter();

    public CustomAutoCompleteAdapter(Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<Place>());
        geoDataClient = Places.getGeoDataClient(context);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Place getItem(int position) {
        return dataList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view;

        if (position != (dataList.size() - 1)) {
            view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            TextView textOne = view.findViewById(android.R.id.text1);
            textOne.setText(dataList.get(position).getPlaceText());
            textOne.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.logo_powerd_by_google, parent, false);
            ImageView imageView = view.findViewById(R.id.powered_by_google);
            imageView.setImageResource(R.drawable.powered_by_google_light);
        }

        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    public class CustomAutoCompleteFilter extends Filter {
        private final Object lock = new Object();
        private final Object lockTwo = new Object();
        private boolean placeResults = false;

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            placeResults = false;
            final List<Place> placesList = new ArrayList<>();

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    results.values = new ArrayList<Place>();
                    results.count = 0;
                }
            } else {

                final String searchStrLowerCase = prefix.toString().toLowerCase();

                Task<AutocompletePredictionBufferResponse> task = getAutoCompletePlaces(searchStrLowerCase);

                task.addOnCompleteListener(new OnCompleteListener<AutocompletePredictionBufferResponse>() {

                    @Override
                    public void onComplete(@NonNull Task<AutocompletePredictionBufferResponse> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Auto complete prediction successful");
                            AutocompletePredictionBufferResponse predictions = task.getResult();
                            Place autoPlace;
                            for (AutocompletePrediction prediction : predictions) {
                                autoPlace = new Place();
                                //autoPlace.setPlaceId(prediction.getPlaceId());
                                autoPlace.setPlaceText(prediction.getFullText(null).toString());
                                placesList.add(autoPlace);
                            }
                            predictions.release();
                            Log.d(TAG, "Auto complete predictions size " + placesList.size());
                        } else {
                            Log.d(TAG, "Auto complete prediction unsuccessful");
                        }
                        placeResults = true;
                        synchronized (lockTwo) {
                            lockTwo.notifyAll();
                        }
                    }
                });

                while (!placeResults) {
                    synchronized (lockTwo) {
                        try {
                            lockTwo.wait();
                        } catch (InterruptedException ignored) {}
                    }
                }

                results.values = placesList;
                results.count = placesList.size();
                Log.d(TAG, "Autocomplete predictions size after wait" + results.count);
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                dataList = (ArrayList<Place>) results.values;
            } else {
                dataList = null;
            }
            dataList.add(new Place());
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        private Task<AutocompletePredictionBufferResponse> getAutoCompletePlaces(String query) {
            return geoDataClient.getAutocompletePredictions(query, null, null);
        }
    }
}
