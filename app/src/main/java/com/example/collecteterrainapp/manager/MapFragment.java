package com.example.collecteterrainapp.manager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.collecteterrainapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// Ce fragment est maintenant destiné au Manager
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    // private String username; // Le manager n'a pas besoin de son propre username ici pour filtrer toutes les collectes

    public MapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false); // Réutilise fragment_map pour l'instant
        // username = requireActivity().getIntent().getStringExtra("username"); // N'est plus nécessaire pour le manager
        db = FirebaseFirestore.getInstance();

        // L'ID correct du SupportMapFragment dans fragment_map.xml est R.id.mapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment); 

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getContext(), "Erreur: SupportMapFragment non trouvé dans le layout.", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        loadAllCollecteMarkers(); // Nouvelle méthode pour charger toutes les collectes
    }

    // Doit être modifié pour charger les collectes de tous les représentants
    private void loadAllCollecteMarkers() {
        db.collection("collectes") // Plus de filtre .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "Aucune collecte à afficher sur la carte.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    LatLng lastLatLng = null;
                    mMap.clear(); // Effacer les anciens marqueurs
                    for (DocumentSnapshot doc : querySnapshot) {
                        Double lat = doc.getDouble("latitude");
                        Double lon = doc.getDouble("longitude");
                        String nomCollecte = doc.getString("nomCollecte");
                        String repUsername = doc.getString("username"); // Pour savoir quel représentant
                        
                        if (lat != null && lon != null) {
                            LatLng pos = new LatLng(lat, lon);
                            lastLatLng = pos;

                            mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(nomCollecte != null ? nomCollecte : "Collecte") // Titre: Nom de la collecte
                                    .snippet("Rep: " + repUsername)); // Snippet: Nom du représentant
                        }
                    }

                    if (lastLatLng != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 10)); // Zoom un peu plus large pour voir plus de collectes
                    } else {
                         // Peut-être centrer sur une localisation par défaut si aucune collecte n'a de coordonnées
                         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.5731, -7.5898), 6)); // Casablanca par défaut, ajuster si besoin
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erreur chargement carte : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
} 