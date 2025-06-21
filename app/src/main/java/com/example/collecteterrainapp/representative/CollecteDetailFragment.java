package com.example.collecteterrainapp.representative;

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.collecteterrainapp.R;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CollecteDetailFragment extends Fragment {

    private TextView nomCollecteTextView, amountTextView, commentTextView, locationTextView, timestampTextView;
    private ImageView photoImageView;

    public CollecteDetailFragment() {
        // Constructeur public vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collecte_detail, container, false);

        nomCollecteTextView = view.findViewById(R.id.detail_nom_collecte_text_view);
        amountTextView = view.findViewById(R.id.detail_amount_text_view);
        commentTextView = view.findViewById(R.id.detail_comment_text_view);
        locationTextView = view.findViewById(R.id.detail_location_text_view);
        timestampTextView = view.findViewById(R.id.detail_timestamp_text_view);
        photoImageView = view.findViewById(R.id.detail_photo_image_view);

        Bundle args = getArguments();
        if (args != null) {
            nomCollecteTextView.setText("Nom: " + args.getString("nomCollecte", "N/A"));
            amountTextView.setText("Montant: " + args.getFloat("amount", 0.0f) + " dhs");
            commentTextView.setText("Commentaire: " + args.getString("comment", ""));
            String location = "Latitude: " + args.getFloat("latitude", 0.0f) + "\nLongitude: " + args.getFloat("longitude", 0.0f);
            locationTextView.setText(location);
            
            long timestampValue = args.getLong("timestamp", 0);
            if (timestampValue > 0) {
                Date date = new Date(timestampValue);
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm:ss", Locale.getDefault());
                timestampTextView.setText("Date: " + df.format(date));
            } else {
                timestampTextView.setText("Date: Inconnue");
            }

            String photoUrlString = args.getString("photoUrl");
            if (photoUrlString != null && !photoUrlString.isEmpty()) {
                photoImageView.setVisibility(View.VISIBLE);
                if (photoUrlString.startsWith("http://") || photoUrlString.startsWith("https://")) {
                    Glide.with(this)
                         .load(photoUrlString)
                         .placeholder(R.drawable.ic_profile_placeholder)
                         .error(R.drawable.ic_profile_placeholder)
                         .into(photoImageView);
                } else {
                    File imgFile = new File(photoUrlString);
                    if(imgFile.exists()){
                        photoImageView.setImageURI(Uri.fromFile(imgFile));
                    } else {
                         photoImageView.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                }
            } else {
                photoImageView.setVisibility(View.GONE);
            }
        }
        return view;
    }
} 