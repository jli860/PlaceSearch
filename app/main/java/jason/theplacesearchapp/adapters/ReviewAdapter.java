package jason.theplacesearchapp.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jason.theplacesearchapp.R;
import jason.theplacesearchapp.helper.ReviewCard;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    public interface OnReviewClickListener {
        void onReviewClick(ReviewCard ReviewCard);
    }

    private final ArrayList<ReviewCard> ReviewCards;
    private final OnReviewClickListener listener;

    public ReviewAdapter(ArrayList<ReviewCard> ReviewCards, OnReviewClickListener listener) {
        this.ReviewCards = ReviewCards;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView userPhoto;
        TextView userName;
        RatingBar ratingBar;
        TextView reviewTime;
        TextView reviewText;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.review_card);
            userPhoto = itemView.findViewById(R.id.user_photo);
            userName = itemView.findViewById(R.id.user_name);
            ratingBar = itemView.findViewById(R.id.user_ratingBar);
            reviewTime = itemView.findViewById(R.id.review_time);
            reviewText = itemView.findViewById(R.id.review_text);
        }

        void bind(final ReviewCard ReviewCard, final OnReviewClickListener listener) {
            Picasso.get().load(ReviewCard.getPhoto()).into(userPhoto);
            userName.setText(ReviewCard.getName());
            ratingBar.setRating(ReviewCard.getRating());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            reviewTime.setText(dateFormat.format(new Date((long)(ReviewCard.getTime()) * 1000)));
            reviewText.setText(ReviewCard.getText());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onReviewClick(ReviewCard);
                }
            });
        }
    }

    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_card, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(ReviewCards.get(i), listener);

    }

    @Override
    public int getItemCount() {
        return ReviewCards.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}