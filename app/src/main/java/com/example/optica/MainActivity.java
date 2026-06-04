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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.optica.ui.admin.AdminOrderDetailFragment;
import com.example.optica.ui.admin.AdminOrdersFragment;
import com.example.optica.ui.appointment.AppointmentFragment;
import com.example.optica.ui.catalog.CatalogFragment;
import com.example.optica.ui.contact.ContactFragment;
import com.example.optica.ui.history.HistoryFragment;
import com.example.optica.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;

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

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottom_navigation);
        
        setupNavigationByRole();

        checkNotificationPermission();
        handleIntent(getIntent());
    }

    private void setupNavigationByRole() {
        SharedPreferences prefs = getSharedPreferences("OpticaPrefs", Context.MODE_PRIVATE);
        String role = prefs.getString("user_role", "cliente");

        if ("admin".equals(role)) {
            // Unificar en barra inferior para Admin
            bottomNav.getMenu().clear();
            bottomNav.inflateMenu(R.menu.admin_bottom_menu);
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                loadFragment(new AdminOrdersFragment(), "Gestión de Pedidos");
            }
        } else {
            // Barra inferior estándar para Cliente
            bottomNav.getMenu().clear();
            bottomNav.inflateMenu(R.menu.bottom_nav_menu);
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                loadFragment(new CatalogFragment(), getString(R.string.nav_catalog));
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_admin_panel) {
                selectedFragment = new AdminOrdersFragment();
                title = "Gestión de Pedidos";
            } else if (itemId == R.id.nav_catalog) {
                selectedFragment = new CatalogFragment();
                title = getString(R.string.nav_catalog);
            } else if (itemId == R.id.nav_appointment) {
                selectedFragment = new AppointmentFragment();
                title = getString(R.string.nav_appointment);
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
                title = getString(R.string.nav_history);
            } else if (itemId == R.id.nav_contact) {
                selectedFragment = new ContactFragment();
                title = getString(R.string.nav_contact);
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = getString(R.string.nav_profile);
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
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
                    loadFragment(AdminOrderDetailFragment.newInstanceFromId(orderId), "Detalle del Pedido");
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

    private void loadFragment(Fragment fragment, String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
