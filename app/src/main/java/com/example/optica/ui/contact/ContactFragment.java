package com.example.optica.ui.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.optica.R;
import com.example.optica.utils.IntentHelper;

public class ContactFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        view.findViewById(R.id.btnCall).setOnClickListener(v -> 
            IntentHelper.makeCall(requireContext(), "+123456789"));

        view.findViewById(R.id.btnWhatsApp).setOnClickListener(v -> 
            IntentHelper.openWhatsApp(requireContext(), "+123456789", "Hola, me gustaría información sobre sus productos."));

        view.findViewById(R.id.btnEmail).setOnClickListener(v -> 
            IntentHelper.sendEmail(requireContext(), "info@optica.com", "Consulta desde la App"));

        view.findViewById(R.id.btnMaps).setOnClickListener(v -> 
            IntentHelper.openMaps(requireContext(), "4.6097", "-74.0817", "Óptica Central"));

        return view;
    }
}
