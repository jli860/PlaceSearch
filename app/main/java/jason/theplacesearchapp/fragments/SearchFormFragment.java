package jason.theplacesearchapp.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Objects;

import jason.theplacesearchapp.R;
import jason.theplacesearchapp.activities.MainActivity;
import jason.theplacesearchapp.activities.SearchResults;
import jason.theplacesearchapp.adapters.CustomAutoCompleteAdapter;

public class SearchFormFragment extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final String VALIDATION_ERROR_MESSAGE = "Please fix all fields with errors";
    private static final String LOADING_MESSAGE = "Fetching results";
    private final String DETAILS_ERROR_MESSAGE = "Fail to get details";

    private ProgressDialog progressDialog;

    private double latitude;
    private double longitude;

    private EditText keyword;
    private EditText distance;
    private AutoCompleteTextView address;
    private TextView alert1;
    private TextView alert2;
    private Spinner category;
    private RadioButton radioButton1;

    public SearchFormFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainview = inflater.inflate(R.layout.fragment_search_form, container, false);
        keyword = mainview.findViewById(R.id.keyword);
        distance = mainview.findViewById(R.id.radius);
        address = mainview.findViewById(R.id.address);
        alert1 = mainview.findViewById(R.id.alert1);
        alert2 = mainview.findViewById(R.id.alert2);
        category = mainview.findViewById(R.id.spinner);
        radioButton1 = mainview.findViewById(R.id.radioButton1);

        address.setAdapter(new CustomAutoCompleteAdapter(getContext()));
        Button search_button = mainview.findViewById(R.id.search_button);
        Button clear_button = mainview.findViewById(R.id.clear_button);
        radioButton1.setChecked(true);
        address.setEnabled(false);

        radioButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                originSelect(isChecked);
            }
        });

        search_button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (validate()) {
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage(LOADING_MESSAGE);
                    progressDialog.show();
                    fetchLocation();
                } else {
                    Toast.makeText(getContext(), VALIDATION_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                }
            }
        });

        clear_button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                keyword.setText("");
                distance.setText("");
                address.setText("");
                category.setSelection(0);
                radioButton1.setChecked(true);
                alert1.setVisibility(View.GONE);
                alert2.setVisibility(View.GONE);
            }
        });
        return mainview;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    if (locationManager != null) {
                        locationManager.removeUpdates(this);
                    }
                    requestResults();
                }
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                public void onProviderEnabled(String provider) {}
                public void onProviderDisabled(String provider) {}
            };

            String locationProvider = LocationManager.GPS_PROVIDER;
            if (locationManager != null) {
                locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
            }
        }
    }


    private boolean validate() {
        boolean keyword_invalid = keyword.getText().toString().trim().isEmpty();
        boolean address_invalid = address.isEnabled() && address.getText().toString().trim().isEmpty();
        if (keyword_invalid) {
            alert1.setVisibility(View.VISIBLE);
        } else {
            alert1.setVisibility(View.GONE);
        }
        if (address_invalid) {
            alert2.setVisibility(View.VISIBLE);
        } else {
            alert2.setVisibility(View.GONE);
        }
        return !keyword_invalid && !address_invalid;
    }

    private void originSelect(boolean isChecked) {

        if (isChecked) {
            address.setEnabled(false);
            address.setText("");
            address.setFocusableInTouchMode(false);
            alert2.setVisibility(View.GONE);
        } else {
            address.setEnabled(true);
            address.setFocusableInTouchMode(true);
        }
    }

    private void requestResults() {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url ="http://placessearch.us-west-1.elasticbeanstalk.com/NS";
        url += "?keyword=" + Uri.encode(keyword.getText().toString());
        url += "&type=" + category.getSelectedItem().toString().toLowerCase().replaceAll(" ", "_");
        url += "&radius=" + Uri.encode(distance.getText().toString());
        url += "&location=" + latitude + "," + longitude;
        if (address.isEnabled()) {
            url += "&address=" + Uri.encode(address.getText().toString());
        }
        Log.d("!!!!!", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RESPONSE", response);
                        Intent intent = new Intent(getActivity(), SearchResults.class);
                        intent.putExtra("results", response);
                        progressDialog.dismiss();
                        startActivity(intent);
                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), DETAILS_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);
    }
}
