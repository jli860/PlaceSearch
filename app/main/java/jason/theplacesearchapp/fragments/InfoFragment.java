package jason.theplacesearchapp.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import jason.theplacesearchapp.R;


public class InfoFragment extends Fragment{

    private JSONObject info;

    public InfoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            assert getArguments() != null;
            info = new JSONObject(getArguments().getString("info"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View infoView =  inflater.inflate(R.layout.fragment_info, container, false);

        TextView textView;
        TableLayout table = infoView.findViewById(R.id.table);
        TableRow row;
        try {
            if (info.isNull("address")) {
                row = infoView.findViewById(R.id.row_address);
                table.removeView(row);
            } else {
                textView = infoView.findViewById(R.id.td_address);
                textView.setText(info.getString("address"));
            }
            if (info.isNull("phone_number")) {
                row = infoView.findViewById(R.id.row_phone);
                table.removeView(row);
            } else {
                textView = infoView.findViewById(R.id.td_phone);
                textView.setText(info.getString("phone_number"));
            }
            if (info.isNull("price_level")) {
                row = infoView.findViewById(R.id.row_price);
                table.removeView(row);
            } else {
                textView = infoView.findViewById(R.id.td_price);
                textView.setText(new String(new char[info.getInt("price_level")]).replace("\0", "$"));
            }
            if (info.isNull("rating")) {
                row = infoView.findViewById(R.id.row_rating);
                table.removeView(row);
            } else {
                RatingBar ratingBar = infoView.findViewById(R.id.ratingBar);
                ratingBar.setRating(((float) info.getDouble("rating")));
            }
            if (info.isNull("google_page")) {
                row = infoView.findViewById(R.id.row_url);
                table.removeView(row);
            } else {
                textView = infoView.findViewById(R.id.td_url);
                textView.setText(info.getString("google_page"));
            }
            if (info.isNull("website")) {
                row = infoView.findViewById(R.id.row_website);
                table.removeView(row);
            } else {
                textView = infoView.findViewById(R.id.td_website);
                textView.setText(info.getString("website"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return infoView;
    }

}
