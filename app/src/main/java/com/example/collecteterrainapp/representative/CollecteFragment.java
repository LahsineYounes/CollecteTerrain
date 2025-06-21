package com.example.collecteterrainapp.representative;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.adapter.CollecteAdapter;
import com.example.collecteterrainapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollecteFragment extends Fragment {

    private RecyclerView collecteRecyclerView;
    private CollecteAdapter adapter;
    private List<Map<String, Object>> collecteList = new ArrayList<>();
    private FirebaseFirestore db;
    private String username;
    private FloatingActionButton fabAddCollecte;
    private SessionManager sessionManager;

    public CollecteFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collecte, container, false);
        collecteRecyclerView = view.findViewById(R.id.collecteRecyclerView);
        fabAddCollecte = view.findViewById(R.id.fab_add_collecte);

        sessionManager = SessionManager.getInstance(requireContext());
        username = sessionManager.getUsername();

        db = FirebaseFirestore.getInstance();

        collecteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CollecteAdapter(collecteList, R.id.action_collecteFragment_to_collecteDetailFragment);
        collecteRecyclerView.setAdapter(adapter);

        if (username != null && !username.isEmpty()) {
            loadCollectes();
        } else {
            Toast.makeText(getContext(), "Erreur: Nom d'utilisateur non disponible.", Toast.LENGTH_LONG).show();
            fabAddCollecte.setEnabled(false);
        }

        fabAddCollecte.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.addCollecteFragment);
        });

        return view;
    }

    private void loadCollectes() {
        if (username == null || username.isEmpty()) {
            if (isAdded() && getView() != null) {
                Toast.makeText(getContext(), "Impossible de charger les collectes: utilisateur inconnu.", Toast.LENGTH_LONG).show();
            }
            return;
        }
        db.collection("collectes")
                .whereEqualTo("username", username)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isAdded() && getView() != null) {
                        List<Map<String, Object>> newCollectes = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            newCollectes.add(doc.getData());
                        }
                        collecteList.clear();
                        collecteList.addAll(newCollectes);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded() && getView() != null) {
                        Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
