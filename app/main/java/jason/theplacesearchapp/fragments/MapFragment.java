package jason.theplacesearchapp.fragments;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import jason.theplacesearchapp.R;
import jason.theplacesearchapp.activities.PlaceDetails;
import jason.theplacesearchapp.adapters.CustomAutoCompleteAdapter;
import jason.theplacesearchapp.helper.Place;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String DIRECTIONS_ERROR_MESSAGE = "Fail to get directions";

    private LatLng latLng;
    private String origin;
    private String mode;
    private String destination;
    private RequestQueue queue;
    private List<LatLng> decoded;
    private Marker marker;
    private GoogleMap googleMap;
    private Polyline polyline;
    private String title;

    public MapFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        origin = null;
        mode = "driving";
        destination = null;
        queue = Volley.newRequestQueue(getContext());
        decoded = null;
        marker = null;
        polyline = null;
        title = "";
        try {
            assert getArguments() != null;
            JSONObject location = new JSONObject(getArguments().getString("location"));
            latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
            destination = location.getDouble("lat") + "," + location.getDouble("lng");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mapView =  inflater.inflate(R.layout.fragment_map, container, false);

        AutoCompleteTextView autoCompleteTextView = mapView.findViewById(R.id.map_ac);
        autoCompleteTextView.setAdapter(new CustomAutoCompleteAdapter(getContext()));
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                origin = ((Place) parent.getItemAtPosition(position)).getPlaceText();
                title = origin.split(",")[0];
                requestRoutes();
            }
        });

        final Spinner spinner = mapView.findViewById(R.id.travel_mode);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mode = spinner.getSelectedItem().toString().toLowerCase();
                requestRoutes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return mapView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (decoded == null) {
            googleMap.addMarker(new MarkerOptions().position(latLng).title(getArguments().getString("name"))).showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        } else {
            if (polyline != null) {
                polyline.remove();
            }
            if (marker != null) {
                marker.remove();
            }
            marker = googleMap.addMarker(new MarkerOptions().position(decoded.get(0)).title(title));
            marker.showInfoWindow();
            polyline = googleMap.addPolyline(new PolylineOptions().color(Color.BLUE).width(20).addAll(decoded));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : decoded) {
                builder.include(point);
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    private void requestRoutes() {
        if (origin == null) {
            return;
        }

        String url ="http://placessearch.us-west-1.elasticbeanstalk.com/GP?address=" + Uri.encode(origin) + "&destination=" + destination + "&mode=" + mode;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RESPONSE", response);
                        decoded = PolyUtil.decode(response);
                        onMapReady(googleMap);
                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), DIRECTIONS_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        });
        queue.add(stringRequest);
    }

}
