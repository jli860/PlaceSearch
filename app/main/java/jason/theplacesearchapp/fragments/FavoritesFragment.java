package jason.theplacesearchapp.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import jason.theplacesearchapp.R;
import jason.theplacesearchapp.activities.PlaceDetails;
import jason.theplacesearchapp.adapters.PlaceAdapter;
import jason.theplacesearchapp.helper.PlaceCard;


public class FavoritesFragment extends Fragment{

    private final String LOADING_MESSAGE_DETAILS = "Fetching details";
    private final String DETAILS_ERROR_MESSAGE = "Fail to get details";

    private SharedPreferences sharedPref;
    private ArrayList<PlaceCard> placeCards;
    private Gson gson;
    private PlaceAdapter adapter;
    private ProgressDialog progressDialog;
    private RequestQueue queue;
    private RecyclerView recyclerView;

    public FavoritesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        placeCards = new ArrayList<>();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        gson = new Gson();
        queue = Volley.newRequestQueue(getContext());
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            placeCards.add(gson.fromJson(entry.getValue().toString(), PlaceCard.class));
        }
        Log.d("pc", placeCards.size()+"");
        adapter = new PlaceAdapter(placeCards, new PlaceAdapter.OnPlaceCardClickListener() {
            @Override
            public void onPlaceClick(PlaceCard placeCard) {
                getDetails(placeCard);
            }

            @Override
            public void onFavorClick(PlaceCard placeCard) {
                deletePlaceCard(placeCard);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("RESUME","!!!");
        placeCards = new ArrayList<>();
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            placeCards.add(gson.fromJson(entry.getValue().toString(), PlaceCard.class));
        }
        Log.d("repc", placeCards.size()+"");
        adapter = new PlaceAdapter(placeCards, new PlaceAdapter.OnPlaceCardClickListener() {
            @Override
            public void onPlaceClick(PlaceCard placeCard) {
                getDetails(placeCard);
            }

            @Override
            public void onFavorClick(PlaceCard placeCard) {
                deletePlaceCard(placeCard);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View favoriteView = inflater.inflate(R.layout.fragment_favorites, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView = favoriteView.findViewById(R.id.favorite_list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        return favoriteView;
    }

    private void getDetails(PlaceCard placeCard) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(LOADING_MESSAGE_DETAILS);
        progressDialog.show();
        Gson gson = new Gson();
        final String json = gson.toJson(placeCard);
        Log.d("!!!json", json);
        String url ="http://placessearch.us-west-1.elasticbeanstalk.com/PD?placeId=" + placeCard.getPlaceId();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RESPONSE", response);
                        Bundle extras = new Bundle();
                        Intent intent = new Intent(getActivity(), PlaceDetails.class);
                        extras.putString("result", response);
                        extras.putString("PlaceCard", json);
                        intent.putExtras(extras);
                        startActivity(intent);
                        progressDialog.dismiss();
                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), DETAILS_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        queue.add(stringRequest);
    }

    private void deletePlaceCard(PlaceCard placeCard) {
        placeCards.remove(placeCard);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(placeCard.getPlaceId());
        editor.commit();
        Toast.makeText(getActivity().getApplicationContext(), placeCard.getName() + " was removed from favorites", Toast.LENGTH_LONG).show();
        adapter = new PlaceAdapter(placeCards, new PlaceAdapter.OnPlaceCardClickListener() {
            @Override
            public void onPlaceClick(PlaceCard placeCard) {
                getDetails(placeCard);
            }

            @Override
            public void onFavorClick(PlaceCard placeCard) {
                deletePlaceCard(placeCard);
            }
        });
        recyclerView.setAdapter(adapter);

    }

}
