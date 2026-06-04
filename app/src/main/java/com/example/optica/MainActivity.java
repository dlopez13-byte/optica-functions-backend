package com.example.optica;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.optica.ui.admin.AdminOrderDetailFragment;
import com.example.optica.ui.admin.AdminOrdersFragment;
import com.example.optica.ui.appointment.AppointmentFragment;
import com.example.optica.ui.catalog.CatalogFragment;
import com.example.optica.ui.contact.ContactFragment;
import com.example.optica.ui.history.HistoryFragment;
import com.example.optica.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNav = findViewById(R.id.bottom_navigation);
        
        setupNavigationByRole();

        checkNotificationPermission();
        handleIntent(getIntent());

        // Manejo moderno de retroceso
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupNavigationByRole() {
        SharedPreferences prefs = getSharedPreferences("OpticaPrefs", Context.MODE_PRIVATE);
        String role = prefs.getString("user_role", "cliente");

        if ("admin".equals(role)) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            bottomNav.setVisibility(View.GONE);
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                loadFragment(new AdminOrdersFragment());
            }
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            bottomNav.setVisibility(View.VISIBLE);
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                loadFragment(new CatalogFragment());
            }
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin_panel) {
                loadFragment(new AdminOrdersFragment());
            } else if (id == R.id.nav_home) {
                loadFragment(new CatalogFragment());
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
            } else if (id == R.id.nav_logout) {
                logout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_catalog) {
                selectedFragment = new CatalogFragment();
            } else if (itemId == R.id.nav_appointment) {
                selectedFragment = new AppointmentFragment();
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            } else if (itemId == R.id.nav_contact) {
                selectedFragment = new ContactFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("target_fragment")) {
            String target = intent.getStringExtra("target_fragment");
            if ("admin_detail".equals(target)) {
                String orderId = intent.getStringExtra("orderId");
                if (orderId != null) {
                    loadFragment(AdminOrderDetailFragment.newInstanceFromId(orderId));
                }
            }
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void logout() {
        mAuth.signOut();
        SharedPreferences prefs = getSharedPreferences("OpticaPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove("user_role").apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
