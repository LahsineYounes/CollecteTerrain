package com.example.collecteterrainapp.manager;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.shared.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManagerDashboardActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        // Initialisation des composants UI
        setupToolbar();
        setupNavigation();
        loadUserData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuration de la flèche de retour
        // NavigationUI.setupActionBarWithNavController will handle the title
        // if (getSupportActionBar() != null) {
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowTitleEnabled(false); // Removed
        // }
    }

    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.manager_nav_view);
        // navController = Navigation.findNavController(this, R.id.manager_nav_host_fragment); // Old way
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.manager_nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            // Handle error: NavHostFragment not found
            Toast.makeText(this, "Erreur: NavHostFragment introuvable", Toast.LENGTH_LONG).show();
            finish(); // Or some other error handling
            return;
        }

        // Configuration des destinations de niveau supérieur
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.teamStatsFragment,
                R.id.MapFragment,
                R.id.profileFragment)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Gestion des clics sur les items
        // Commenting out for now, ProfileFragment should handle its own refresh
        /*
        navView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.profileFragment) {
                refreshProfileData();
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
        */
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUser = documentSnapshot.toObject(User.class);
                            // updateUI(); // Removed - title handled by NavUI
                        } else {
                            Toast.makeText(ManagerDashboardActivity.this, "Profil manager non trouvé.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ManagerDashboardActivity.this, "Erreur chargement profil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(this, "Manager non connecté. Redirection...", Toast.LENGTH_LONG).show();
        }
    }

    // Commenting out for now - ProfileFragment should handle its own data.
    /*
    private void refreshProfileData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && navController.getCurrentDestination() != null &&
                navController.getCurrentDestination().getId() == R.id.profileFragment) {
            loadUserData();
        }
    }
    */

    // Removed - title handled by NavUI
    /*
    private void updateUI() {
        if (currentUser != null && getSupportActionBar() != null) {
            String displayName = currentUser.getDisplayName();
            getSupportActionBar().setTitle("Manager - " + (displayName != null ? displayName : ""));
        }
    }
    */

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Removed - onSupportNavigateUp should handle this
    /*
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // navController.navigateUp(); // Or some other logic if needed
            onBackPressed(); //
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    protected void onResume() {
        super.onResume();
        // Rafraîchir les données à chaque retour sur l'activité
        // Commenting out - let fragments handle their own data refresh on resume if needed
        /*
        if (currentUser != null) { // currentUser might be null if initial load failed or user signed out
            loadUserData();
        }
        */
    }
}
