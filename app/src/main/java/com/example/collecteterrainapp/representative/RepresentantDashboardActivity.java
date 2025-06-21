package com.example.collecteterrainapp.representative;

import android.content.Intent;
import android.os.Bundle;

// import androidx.activity.EdgeToEdge; // No longer used with Toolbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
// import androidx.core.graphics.Insets; // No longer used
// import androidx.core.view.ViewCompat; // No longer used
// import androidx.core.view.WindowInsetsCompat; // No longer used
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration; // Import AppBarConfiguration
import androidx.navigation.ui.NavigationUI;

import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.services.LocationService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

public class RepresentantDashboardActivity extends AppCompatActivity {

    private BottomNavigationView navView;
    private AppBarConfiguration appBarConfiguration; // Declare AppBarConfiguration
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_representant_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navView = findViewById(R.id.nav_view);
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController(); // Assign to class member

            // Define top-level destinations (those that don't show an Up arrow)
            // These should match the IDs of the items in your bottom_nav_menu.xml
            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.collecteFragment); // Main screen
            topLevelDestinations.add(R.id.profileFragment); // Other top-level from nav menu
            // Add any other top-level destinations from your bottom_nav_menu.xml

            appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        } else {
            // Handle error: NavHostFragment not found
        }

        startService(new Intent(this, LocationService.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Ensure navController is not null
        return navController != null && (NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Arrêter le service si besoin
        stopService(new Intent(this, LocationService.class));
    }
}
