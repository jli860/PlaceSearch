package jason.theplacesearchapp.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jason.theplacesearchapp.R;
import jason.theplacesearchapp.fragments.SearchFormFragment;
import jason.theplacesearchapp.fragments.FavoritesFragment;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private int[] tabIcons = {
            R.drawable.search,
            R.drawable.heart_fill_white,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tabs);
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

    private void setupTabIcons() {
        LinearLayout tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView tabOne = tabLinearLayout.findViewById(R.id.tab);
        tabOne.setText("SEARCH");
        tabOne.setCompoundDrawablesWithIntrinsicBounds(tabIcons[0], 0, 0, 0);
        tabOne.setCompoundDrawablePadding(32);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView tabTwo = tabLinearLayout.findViewById(R.id.tab);
        tabTwo.setText("FAVORITES");
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(tabIcons[1], 0, 0, 0);
        tabTwo.setCompoundDrawablePadding(32);
        tabLayout.getTabAt(1).setCustomView(tabTwo);
    }

    public void setupViewPager(ViewPager viewPager) {
        MainActivity.ViewPagerAdapter adapter = new MainActivity.ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new SearchFormFragment());
        adapter.addFrag(new FavoritesFragment());
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        void addFrag(Fragment fragment) {
            fragmentList.add(fragment);
        }
    }
}
