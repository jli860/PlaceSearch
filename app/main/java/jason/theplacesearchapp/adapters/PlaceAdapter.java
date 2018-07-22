package jason.theplacesearchapp.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jason.theplacesearchapp.R;
import jason.theplacesearchapp.helper.PlaceCard;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    public interface OnPlaceCardClickListener {
        void onPlaceClick(PlaceCard placeCard);
        void onFavorClick(PlaceCard placeCard);
    }

    private final ArrayList<PlaceCard> placeCards;
    private final OnPlaceCardClickListener listener;

    public PlaceAdapter(ArrayList<PlaceCard> placeCards, OnPlaceCardClickListener listener) {
        this.placeCards = placeCards;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView placeIcon;
        TextView placeName;
        TextView placeVicinity;
        ImageButton favor;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.place_card);
            placeIcon = itemView.findViewById(R.id.place_icon);
            placeName = itemView.findViewById(R.id.place_name);
            placeVicinity = itemView.findViewById(R.id.place_vicinity);
            favor = itemView.findViewById(R.id.favor);
        }

        void bind(final PlaceCard placeCard, final OnPlaceCardClickListener listener) {
            Picasso.get().load(placeCard.getIcon()).into(placeIcon);
            placeName.setText(placeCard.getName());
            placeVicinity.setText(placeCard.getVicinity());
            if (placeCard.isFavored()) {
                favor.setImageResource(R.drawable.heart_fill_red);
            } else {
                favor.setImageResource(R.drawable.heart_outline_black);
            }
            favor.setOnClickListener(new ImageButton.OnClickListener() {
                @Override public void onClick(View v) {
                    if (placeCard.isFavored()) {
                        favor.setImageResource(R.drawable.heart_outline_black);
                        Toast.makeText(itemView.getContext(), placeCard.getName() + " was removed from favorites", Toast.LENGTH_LONG).show();
                    } else {
                        favor.setImageResource(R.drawable.heart_fill_red);
                        Toast.makeText(itemView.getContext(), placeCard.getName() + " was added to favorites", Toast.LENGTH_LONG).show();
                    }
                    placeCard.setFavored();
                    listener.onFavorClick(placeCard);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onPlaceClick(placeCard);
                }
            });
        }
    }

    @NonNull
    @Override
    public PlaceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_card, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(placeCards.get(i), listener);

    }

    @Override
    public int getItemCount() {
        return placeCards.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}