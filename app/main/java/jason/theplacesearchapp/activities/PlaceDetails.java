package jason.theplacesearchapp.activities;

import jason.theplacesearchapp.helper.PlaceCard;
import jason.theplacesearchapp.fragments.InfoFragment;
import jason.theplacesearchapp.fragments.MapFragment;
import jason.theplacesearchapp.fragments.PhotosFragment;
import jason.theplacesearchapp.fragments.ReviewsFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jason.theplacesearchapp.R;

public class PlaceDetails extends AppCompatActivity {

    private JSONObject result;
    private String place_id;
    private Boolean favored;
    private PlaceCard placeCard;

    private SharedPreferences sharedPref;
    private TabLayout tabLayout;
    private int[] tabIcons = {
            R.drawable.info_outline,
            R.drawable.photos,
            R.drawable.maps,
            R.drawable.reviews
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        Toolbar toolbar = findViewById(R.id.pd_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            try {
                result = new JSONObject(extras.getString("result"));
                getSupportActionBar().setTitle(result.getString("name"));
                place_id = result.getString("place_id");
                Gson gson = new Gson();
                placeCard = gson.fromJson(extras.getString("PlaceCard"), PlaceCard.class);
                Log.d("!!!", extras.getString("PlaceCard"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ImageButton favor_button = findViewById(R.id.favor_button);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPref.getString(place_id, null) == null) {
            favored = false;
            favor_button.setImageResource(R.drawable.heart_outline_white);
        } else {
            favored = true;
            favor_button.setImageResource(R.drawable.heart_fill_white);
        }
        Log.d("!!!d", sharedPref.getString(place_id, "no"));
        ViewPager viewPager = findViewById(R.id.pd_viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.pd_tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        LinearLayout linearLayout = (LinearLayout)tabLayout.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.argb(255, 78, 172, 149));
        drawable.setSize(7, 1);
        linearLayout.setDividerPadding(10);
        linearLayout.setDividerDrawable(drawable);
    }

    public void favor(View view) {
        ImageButton imageButton = (ImageButton) view;
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        if (!favored) {
            placeCard.setFavored();
            editor.putString(place_id, gson.toJson(placeCard));
            imageButton.setImageResource(R.drawable.heart_fill_white);
            Toast.makeText(PlaceDetails.this.getApplicationContext(), placeCard.getName() + " was added to favorites", Toast.LENGTH_LONG).show();
        } else {
            placeCard.setFavored();
            editor.remove(place_id);
            imageButton.setImageResource(R.drawable.heart_outline_white);
            Toast.makeText(PlaceDetails.this.getApplicationContext(), placeCard.getName() + " was removed from favorites", Toast.LENGTH_LONG).show();
        }
        favored = !favored;
        editor.commit();
        Log.d("!!!d", sharedPref.getString(place_id, "no"));
    }

    public void share(View view) throws JSONException {
        String url = "https://twitter.com/intent/tweet?text=Check%20out%20" + Uri.encode(result.getString("name")) + "%20located%20at%20" + Uri.encode(result.getJSONObject("info").getString("address")) + "%2E%20Website%3A%20";
        if(result.getJSONObject("info").isNull("website")) {
            url += "&url=" + Uri.encode(result.getJSONObject("info").getString("google_page"));
        } else {
            url += "&url=" + Uri.encode(result.getJSONObject("info").getString("website"));
        }
        Intent tweet = new Intent(Intent.ACTION_VIEW);
        tweet.setData(Uri.parse(url));
        startActivity(tweet);
    }

    private void setupTabIcons() {
        LinearLayout tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView tabOne = tabLinearLayout.findViewById(R.id.tab);
        tabOne.setText("INFO");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(tabIcons[0], 0, 0, 0);
        tabOne.setCompoundDrawablePadding(32);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView tabTwo = tabLinearLayout.findViewById(R.id.tab);
        tabTwo.setText("PHOTOS");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(tabIcons[1], 0, 0, 0);
        tabTwo.setCompoundDrawablePadding(32);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

        tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView tabThree = tabLinearLayout.findViewById(R.id.tab);
        tabThree.setText("MAP");
        tabThree.setCompoundDrawablesWithIntrinsicBounds(tabIcons[2], 0, 0, 0);
        tabThree.setCompoundDrawablePadding(32);
        tabLayout.getTabAt(2).setCustomView(tabThree);

        tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView tabFour = tabLinearLayout.findViewById(R.id.tab);
        tabFour.setText("REVIEWS");
        tabFour.setCompoundDrawablesWithIntrinsicBounds(tabIcons[3], 0, 0, 0);
        tabFour.setCompoundDrawablePadding(32);
        tabLayout.getTabAt(3).setCustomView(tabFour);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        try {
            Bundle infoArgs = new Bundle();
            infoArgs.putString("info", result.getJSONObject("info").toString());
            Fragment infoFragment = new InfoFragment();
            infoFragment.setArguments(infoArgs);
            adapter.addFrag(infoFragment);

            Bundle photosArgs = new Bundle();
            photosArgs.putString("place_id", place_id);
            Fragment photosFragment = new PhotosFragment();
            photosFragment.setArguments(photosArgs);
            adapter.addFrag(photosFragment);

            Bundle mapArgs = new Bundle();
            mapArgs.putString("location", result.getJSONObject("location").toString());
            mapArgs.putString("name", result.getString("name"));
            Fragment mapFragment = new MapFragment();
            mapFragment.setArguments(mapArgs);
            adapter.addFrag(mapFragment);

            Bundle reviewsArgs = new Bundle();
            reviewsArgs.putString("reviews", result.getJSONObject("reviews").toString());
            Fragment reviewsFragment = new ReviewsFragment();
            reviewsFragment.setArguments(reviewsArgs);
            adapter.addFrag(reviewsFragment);

            viewPager.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);
        }
    }
}
