package jason.theplacesearchapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import jason.theplacesearchapp.R;
import jason.theplacesearchapp.adapters.PlaceAdapter;
import jason.theplacesearchapp.helper.PlaceCard;

public class SearchResults extends AppCompatActivity {

    private final String LOADING_MESSAGE_NEXT_PAGE = "Fetching next page";
    private final String LOADING_MESSAGE_DETAILS = "Fetching details";
    private final String DETAILS_ERROR_MESSAGE = "Fail to get details";
    private final String RESULTS_ERROR_MESSAGE = "Fail to get results";

    private ArrayList<PlaceAdapter> adapters;
    private ArrayList<JSONObject> mainObjs;
    private Button button_previous;
    private Button button_next;
    private RequestQueue queue;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private int currentPage;
    private String next_page_token;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        adapters = new ArrayList<>();
        mainObjs = new ArrayList<>();
        queue = Volley.newRequestQueue(this);
        recyclerView = findViewById(R.id.RecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        button_previous = findViewById(R.id.button_previous);
        button_next = findViewById(R.id.button_next);
        currentPage = -1;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            try {
                mainObjs.add(new JSONObject(extras.getString("results")));
                addAdapter(mainObjs.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("BACK", "!!!bACK");
        final ArrayList<PlaceCard> placeCards = new ArrayList<>();
        try {
            for (int i = 0; i < mainObjs.get(currentPage).getJSONArray("results").length(); i++) {
                JSONObject placeObj = mainObjs.get(currentPage).getJSONArray("results").getJSONObject(i);
                Boolean favored = false;
                if (sharedPref.getString(placeObj.getString("placeid"), null) != null) {
                    favored = true;
                }
                placeCards.add(new PlaceCard(placeObj.getString("icon"), placeObj.getString("name"), placeObj.getString("vicinity"), placeObj.getString("placeid"), favored));
            }
            adapters.set(currentPage, new PlaceAdapter(placeCards, new PlaceAdapter.OnPlaceCardClickListener() {
                @Override
                public void onPlaceClick(PlaceCard placeCard) {
                    progressDialog = new ProgressDialog(SearchResults.this);
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
                                    Intent intent = new Intent(SearchResults.this, PlaceDetails.class);
                                    extras.putString("result", response);
                                    extras.putString("PlaceCard", json);
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                    progressDialog.dismiss();
                                }}, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), DETAILS_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
                    queue.add(stringRequest);
                }

                @Override
                public void onFavorClick(PlaceCard placeCard) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    if (placeCard.isFavored()) {
                        Gson gson = new Gson();
                        String json = gson.toJson(placeCard);
                        editor.putString(placeCard.getPlaceId(), json);
                    } else {
                        editor.remove(placeCard.getPlaceId());
                    }
                    editor.commit();
                    Log.d("!!!!", sharedPref.getString(placeCard.getPlaceId(), "no"));
                }
            }));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        recyclerView.setAdapter(adapters.get(currentPage));
    }

    private void addAdapter(JSONObject mainObj) {
        ArrayList<PlaceCard> placeCards = new ArrayList<>();
        try {
            if(mainObj.has("next_page_token")) {
                next_page_token = mainObj.getString("next_page_token");
            } else {
                next_page_token = null;
            }
            for (int i = 0; i < mainObj.getJSONArray("results").length(); i++) {
                JSONObject placeObj = mainObj.getJSONArray("results").getJSONObject(i);
                Boolean favored = false;
                if (sharedPref.getString(placeObj.getString("placeid"), null) != null) {
                    favored = true;
                }
                placeCards.add(new PlaceCard(placeObj.getString("icon"), placeObj.getString("name"), placeObj.getString("vicinity"), placeObj.getString("placeid"), favored));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapters.add(new PlaceAdapter(placeCards, new PlaceAdapter.OnPlaceCardClickListener() {
            @Override
            public void onPlaceClick(PlaceCard placeCard) {
                progressDialog = new ProgressDialog(SearchResults.this);
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
                                Intent intent = new Intent(SearchResults.this, PlaceDetails.class);
                                extras.putString("result", response);
                                extras.putString("PlaceCard", json);
                                intent.putExtras(extras);
                                startActivity(intent);
                                progressDialog.dismiss();
                            }}, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), DETAILS_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
                queue.add(stringRequest);
            }

            @Override
            public void onFavorClick(PlaceCard placeCard) {
                SharedPreferences.Editor editor = sharedPref.edit();
                if (placeCard.isFavored()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(placeCard);
                    editor.putString(placeCard.getPlaceId(), json);
                } else {
                    editor.remove(placeCard.getPlaceId());
                }
                editor.commit();
                Log.d("!!!!", sharedPref.getString(placeCard.getPlaceId(), "no"));
            }
        }));
        currentPage++;
        recyclerView.setAdapter(adapters.get(currentPage));
        setButton_previous();
        setButton_next();
    }

    private void setButton_previous() {
        if (currentPage == 0) {
            button_previous.setEnabled(false);
        } else {
            button_previous.setEnabled(true);
            button_previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPage--;
                    recyclerView.setAdapter(adapters.get(currentPage));
                    setButton_previous();
                    setButton_next();
                }
            });
        }
    }

    private void setButton_next() {
        if (currentPage == adapters.size() - 1) {
            button_next.setEnabled(false);
            if (next_page_token != null) {
                button_next.setEnabled(true);
                button_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog = new ProgressDialog(SearchResults.this);
                        progressDialog.setMessage(LOADING_MESSAGE_NEXT_PAGE);
                        progressDialog.show();
                        String url ="http://placessearch.us-west-1.elasticbeanstalk.com/NP?pagetoken=" + next_page_token;
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("RESPONSE", response);
                                        try {
                                            mainObjs.add(new JSONObject(response));
                                            progressDialog.dismiss();
                                            addAdapter(mainObjs.get(currentPage + 1));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }}, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), RESULTS_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                            }
                        });
                        queue.add(stringRequest);
                    }
                });
            }
        } else {
            button_next.setEnabled(true);
            button_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPage++;
                    recyclerView.setAdapter(adapters.get(currentPage));
                    setButton_previous();
                    setButton_next();
                }
            });
        }
    }
}
