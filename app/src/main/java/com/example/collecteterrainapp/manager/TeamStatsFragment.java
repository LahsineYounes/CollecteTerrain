package com.example.collecteterrainapp.manager;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.manager.adapters.RepresentativeAdapter;
import com.example.collecteterrainapp.shared.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeamStatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeamStatsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView representativesRecyclerView;
    private RepresentativeAdapter representativeAdapter;
    private List<User> representativeList;
    private FirebaseFirestore db;
    private ProgressBar loadingProgressBar;
    private TextView emptyViewText;

    public TeamStatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TeamStatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TeamStatsFragment newInstance(String param1, String param2) {
        TeamStatsFragment fragment = new TeamStatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_stats, container, false);

        representativesRecyclerView = view.findViewById(R.id.representatives_recycler_view);
        loadingProgressBar = view.findViewById(R.id.loading_progress_bar);
        emptyViewText = view.findViewById(R.id.empty_view_text);
        
        representativesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        representativeList = new ArrayList<>();
        representativeAdapter = new RepresentativeAdapter(representativeList);
        representativesRecyclerView.setAdapter(representativeAdapter);

        db = FirebaseFirestore.getInstance();
        loadRepresentatives();

        return view;
    }

    private void loadRepresentatives() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        representativesRecyclerView.setVisibility(View.GONE);
        emptyViewText.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("role", "representative")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        loadingProgressBar.setVisibility(View.GONE);
                        emptyViewText.setVisibility(View.VISIBLE);
                        representativesRecyclerView.setVisibility(View.GONE);
                        // No need to Toast here, emptyViewText handles it
                        return;
                    }

                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        User user = documentSnapshot.toObject(User.class);
                        users.add(user);
                    }
                    fetchCollecteCounts(users);
                })
                .addOnFailureListener(e -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    emptyViewText.setVisibility(View.GONE); // Keep it hidden on error, show toast
                    representativesRecyclerView.setVisibility(View.GONE);
                    if (getContext() != null) {
                       Toast.makeText(getContext(), "Erreur de chargement des représentants: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchCollecteCounts(List<User> users) {
        AtomicInteger counter = new AtomicInteger(users.size());
        representativeList.clear(); // Clear before adding new data

        if (users.isEmpty()) { // Should be handled by previous check, but good for safety
            loadingProgressBar.setVisibility(View.GONE);
            emptyViewText.setVisibility(View.VISIBLE);
            representativeAdapter.notifyDataSetChanged(); // Ensure adapter knows list is empty
            return;
        }

        for (User user : users) {
            db.collection("collectes")
                    .whereEqualTo("username", user.getDisplayName()) // Assuming username in 'collectes' matches displayName
                    .get()
                    .addOnSuccessListener(collectesSnapshot -> {
                        user.setCollecteCount(collectesSnapshot.size());
                        representativeList.add(user);

                        if (counter.decrementAndGet() == 0) {
                            // All counts fetched
                            loadingProgressBar.setVisibility(View.GONE);
                            if (representativeList.isEmpty()) { // Should not happen if users list was not empty
                                emptyViewText.setVisibility(View.VISIBLE);
                                representativesRecyclerView.setVisibility(View.GONE);
                            } else {
                                emptyViewText.setVisibility(View.GONE);
                                representativesRecyclerView.setVisibility(View.VISIBLE);
                                representativeAdapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        user.setCollecteCount(0); // Set to 0 on error for this user
                        representativeList.add(user); // Add user even if count failed, to show them
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Erreur compte collectes pour " + user.getDisplayName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        if (counter.decrementAndGet() == 0) {
                            // All counts attempted
                            loadingProgressBar.setVisibility(View.GONE);
                             if (representativeList.isEmpty()) {
                                emptyViewText.setVisibility(View.VISIBLE);
                                representativesRecyclerView.setVisibility(View.GONE);
                            } else {
                                emptyViewText.setVisibility(View.GONE);
                                representativesRecyclerView.setVisibility(View.VISIBLE);
                                representativeAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }
}