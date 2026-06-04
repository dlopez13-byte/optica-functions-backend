package com.example.optica.ui.appointment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.optica.R;
import com.example.optica.data.AppDatabase;
import com.example.optica.model.Appointment;
import com.example.optica.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AppointmentFragment extends Fragment {

    private String selectedDate = "";
    private String selectedTime = "";
    private String selectedService = "";
    private long selectedTimestamp = 0;
    private ChipGroup timeChipGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);

        AutoCompleteTextView serviceDropdown = view.findViewById(R.id.serviceDropdown);
        MaterialButton btnSelectDate = view.findViewById(R.id.btnSelectDate);
        timeChipGroup = view.findViewById(R.id.timeChipGroup);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmAppointment);

        // Paso 1: Servicios
        String[] services = {"Examen de la vista", "Consulta contactología", "Mantenimiento de montura"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, services);
        serviceDropdown.setAdapter(adapter);
        serviceDropdown.setOnItemClickListener((parent, v, position, id) -> selectedService = services[position]);

        // Paso 2: Fecha (QA: No pasado)
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        btnSelectDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecciona Fecha")
                    .setCalendarConstraints(constraints)
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDate = datePicker.getHeaderText();
                selectedTimestamp = selection;
                btnSelectDate.setText("Fecha: " + selectedDate);
            });
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        // Paso 3: Horarios (Chips)
        setupTimeChips();

        // Confirmación
        btnConfirm.setOnClickListener(v -> {
            int selectedChipId = timeChipGroup.getCheckedChipId();
            if (selectedChipId != View.NO_ID) {
                Chip selectedChip = timeChipGroup.findViewById(selectedChipId);
                selectedTime = selectedChip.getText().toString();
            }

            if (selectedService.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(getContext(), "Por favor completa todos los pasos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Room: Ejecución en hilo secundario (QA ANR Prevention)
            Appointment appointment = new Appointment(selectedService, selectedDate, selectedTime);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase.getDatabase(requireContext()).appointmentDao().insert(appointment);
                
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        NotificationHelper.createNotificationChannel(requireContext());
                        NotificationHelper.scheduleAppointmentNotification(requireContext(), selectedTimestamp, selectedService);
                        
                        // Analytics: Registro de éxito
                        Bundle bundle = new Bundle();
                        bundle.putString("tipo_servicio", selectedService);
                        FirebaseAnalytics.getInstance(requireContext()).logEvent("solicitar_cita", bundle);

                        Toast.makeText(getContext(), "Cita agendada con éxito", Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        return view;
    }

    private void setupTimeChips() {
        String[] hours = {"08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", 
                         "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM", "06:00 PM"};
        
        for (String hour : hours) {
            Chip chip = new Chip(getContext());
            chip.setText(hour);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setChipCornerRadius(12f);
            timeChipGroup.addView(chip);
        }
    }
}
