package com.example.collecteterrainapp.manager;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.adapter.CollecteAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepresentativeDetailFragment extends Fragment {

    private TextView representativeNameTextView;
    private RecyclerView representativeCollectesRecyclerView;
    private CollecteAdapter collecteAdapter;
    private List<Map<String, Object>> collecteList;
    private FirebaseFirestore db;
    private String representativeUsername;

    public RepresentativeDetailFragment() {
        // Constructeur public vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_representative_detail, container, false);
        representativeNameTextView = view.findViewById(R.id.representative_detail_name);
        representativeCollectesRecyclerView = view.findViewById(R.id.representative_collectes_recycler_view);
        
        representativeCollectesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        collecteList = new ArrayList<>();
        collecteAdapter = new CollecteAdapter(collecteList, R.id.action_representativeDetailFragment_to_collecteDetailFragment_manager);
        representativeCollectesRecyclerView.setAdapter(collecteAdapter);

        db = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        if (args != null) {
            representativeUsername = args.getString("representative_username");
            if (representativeUsername != null && !representativeUsername.isEmpty()) {
                representativeNameTextView.setText("Collectes de: " + representativeUsername);
                loadRepresentativeCollectes();
            } else {
                representativeNameTextView.setText("Nom du représentant non fourni.");
                Toast.makeText(getContext(), "Nom du représentant manquant.", Toast.LENGTH_LONG).show();
            }
        } else {
            representativeNameTextView.setText("Aucun argument reçu.");
            Toast.makeText(getContext(), "Arguments manquants pour afficher les détails.", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    private void loadRepresentativeCollectes() {
        db.collection("collectes")
                .whereEqualTo("username", representativeUsername)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    collecteList.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        collecteList.add(documentSnapshot.getData());
                    }
                    collecteAdapter.notifyDataSetChanged();
                    if (collecteList.isEmpty()) {
                        Toast.makeText(getContext(), "Aucune collecte trouvée pour ce représentant.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur de chargement des collectes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
} 