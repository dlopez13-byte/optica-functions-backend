package com.example.optica.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.optica.LoginActivity;
import com.example.optica.R;
import com.example.optica.ui.history.ClientOrdersFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvName = view.findViewById(R.id.profileName);
        TextView tvEmail = view.findViewById(R.id.profileEmail);
        TextView tvRole = view.findViewById(R.id.profileRole);
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            tvName.setText(user.getDisplayName() != null && !user.getDisplayName().isEmpty() ? 
                    user.getDisplayName() : "Usuario de Óptica");
            
            // Obtener rol desde caché local
            SharedPreferences prefs = requireContext().getSharedPreferences("OpticaPrefs", Context.MODE_PRIVATE);
            String role = prefs.getString("user_role", "cliente");
            tvRole.setText(role.toUpperCase());
        }

        view.findViewById(R.id.btnMyOrders).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ClientOrdersFragment())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // Limpiar Firebase y caché de sesión
            FirebaseAuth.getInstance().signOut();
            SharedPreferences.Editor editor = requireContext().getSharedPreferences("OpticaPrefs", Context.MODE_PRIVATE).edit();
            editor.remove("user_role");
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
