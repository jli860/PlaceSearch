package jason.theplacesearchapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jason.theplacesearchapp.R;
import jason.theplacesearchapp.adapters.ReviewAdapter;
import jason.theplacesearchapp.helper.ReviewCard;


public class ReviewsFragment extends Fragment{

    private JSONArray google_reviews;
    private JSONArray yelp_reviews;
    private ReviewAdapter reviewAdapter;
    private ArrayList<ReviewCard> google_reviewCards;
    private ArrayList<ReviewCard> yelp_reviewCards;
    private int currentReviews;

    public ReviewsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        google_reviews = null;
        yelp_reviews = null;
        assert getArguments() != null;
        try {
            JSONObject mainObj = new JSONObject(getArguments().getString("reviews"));
            if (mainObj.has("google_reviews")) {
                google_reviews = mainObj.getJSONArray("google_reviews");
            }
            if (mainObj.has("yelp_reviews")) {
                yelp_reviews = mainObj.getJSONArray("yelp_reviews");
            }
            addReviewCards();
            currentReviews = 0;
            addAdapter();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View reviewsView = inflater.inflate(R.layout.fragment_reviews, container, false);
        final RecyclerView recyclerView = reviewsView.findViewById(R.id.reviews_container);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(reviewAdapter);

        Spinner reviews = reviewsView.findViewById(R.id.reviews);
        reviews.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentReviews = position;
                addAdapter();
                recyclerView.setAdapter(reviewAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner orders = reviewsView.findViewById(R.id.orders);
        orders.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0 ) {
                    Comparator<ReviewCard> comparator;
                    switch (position) {
                        case 1:
                            comparator = new Comparator<ReviewCard>() {
                                @Override
                                public int compare(ReviewCard o1, ReviewCard o2) {
                                    return o2.getRating() - o1.getRating();
                                }
                            };
                            break;
                        case 2:
                            comparator = new Comparator<ReviewCard>() {
                                @Override
                                public int compare(ReviewCard o1, ReviewCard o2) {
                                    return o1.getRating() - o2.getRating();
                                }
                            };
                            break;
                        case 3:
                            comparator = new Comparator<ReviewCard>() {
                                @Override
                                public int compare(ReviewCard o1, ReviewCard o2) {
                                    return o2.getTime() - o1.getTime();
                                }
                            };
                            break;
                        default:
                            comparator = new Comparator<ReviewCard>() {
                                @Override
                                public int compare(ReviewCard o1, ReviewCard o2) {
                                    return o1.getTime() - o2.getTime();
                                }
                            };
                            break;
                    }
                    Collections.sort(google_reviewCards, comparator);
                    Collections.sort(yelp_reviewCards, comparator);
                } else {
                    addReviewCards();
                }
                addAdapter();
                recyclerView.setAdapter(reviewAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        return reviewsView;
    }

    private void addAdapter() {
        ReviewAdapter.OnReviewClickListener listener = new ReviewAdapter.OnReviewClickListener() {
            @Override
            public void onReviewClick(ReviewCard reviewCard) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(reviewCard.getUrl()));
                startActivity(browserIntent);
            }
        };

        if (currentReviews == 0) {
            reviewAdapter = new ReviewAdapter(google_reviewCards, listener);
        } else {
            reviewAdapter = new ReviewAdapter(yelp_reviewCards, listener);
        }
    }

    private void addReviewCards() {
        try {
            google_reviewCards = new ArrayList<>();
            yelp_reviewCards = new ArrayList<>();
            if (google_reviews != null) {
                for (int i = 0; i < google_reviews.length(); i++) {
                    JSONObject reviewObj = google_reviews.getJSONObject(i);
                    google_reviewCards.add(new ReviewCard(reviewObj.getString("profile_photo_url"), reviewObj.getString("author_name"), reviewObj.getInt("rating"), reviewObj.getInt("time"), reviewObj.getString("text"), reviewObj.getString("author_url")));
                }
            }
            if (yelp_reviews != null) {
                for (int i = 0; i < yelp_reviews.length(); i++) {
                    JSONObject reviewObj = yelp_reviews.getJSONObject(i);
                    yelp_reviewCards.add(new ReviewCard(reviewObj.getString("profile_photo_url"), reviewObj.getString("author_name"), reviewObj.getInt("rating"), reviewObj.getInt("time"), reviewObj.getString("text"), reviewObj.getString("author_url")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
