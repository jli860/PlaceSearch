package jason.theplacesearchapp.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import jason.theplacesearchapp.R;


public class PhotosFragment extends Fragment{
    private String place_id;

    public PhotosFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;
        place_id = getArguments().getString("place_id");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View photosView =  inflater.inflate(R.layout.fragment_photos, container, false);
        final LinearLayout linearLayout = photosView.findViewById(R.id.photos_container);

        final GeoDataClient geoDataClient = Places.getGeoDataClient(Objects.requireNonNull(this.getContext()));
        Task<PlacePhotoMetadataResponse> photoMetadataResponse = geoDataClient.getPlacePhotos(place_id);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                PlacePhotoMetadataResponse photos = task.getResult();
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                for (int i = 0; i < photoMetadataBuffer.getCount(); i++) {
                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);
                    Task<PlacePhotoResponse> photoResponse = geoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            ImageView imageView = new ImageView(linearLayout.getContext());
                            imageView.setImageBitmap(photo.getBitmap());
                            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            imageView.setAdjustViewBounds(true);
                            imageView.setPadding(0, 0, 0, 64);
                            linearLayout.addView(imageView);
                        }
                    });
                }
                photoMetadataBuffer.release();
            }
        });
        return photosView;
    }
}
