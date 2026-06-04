package com.example.optica.ui.history;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.optica.R;
import com.example.optica.data.AppDatabase;
import com.example.optica.model.Prescription;
import com.example.optica.utils.CryptoUtils;
import com.example.optica.utils.StorageHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private View emptyState, historyContent;
    private TextView tvDate, tvOdSph, tvOdCyl, tvOdAxis, tvOdAdd, tvOiSph, tvOiCyl, tvOiAxis, tvOiAdd;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final StorageHelper storageHelper = new StorageHelper();
    
    private Uri photoUri;
    private ImageView ivPreview;
    private View cardPhoto;

    private final ActivityResultLauncher<Uri> takePictureLauncher = 
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
        if (success && photoUri != null) {
            ivPreview.setImageURI(photoUri);
            cardPhoto.setVisibility(View.VISIBLE);
        }
    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        emptyState = view.findViewById(R.id.emptyStateLayout);
        historyContent = view.findViewById(R.id.historyScrollView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        tvDate = view.findViewById(R.id.prescriptionDate);
        tvOdSph = view.findViewById(R.id.odEsfera);
        tvOdCyl = view.findViewById(R.id.odCilindro);
        tvOdAxis = view.findViewById(R.id.odEje);
        tvOdAdd = view.findViewById(R.id.odAdicion);
        tvOiSph = view.findViewById(R.id.oiEsfera);
        tvOiCyl = view.findViewById(R.id.oiCilindro);
        tvOiAxis = view.findViewById(R.id.oiEje);
        tvOiAdd = view.findViewById(R.id.oiAdicion);

        loadPrescription();

        view.findViewById(R.id.btnRegisterNew).setOnClickListener(v -> showAddPrescriptionDialog());
        view.findViewById(R.id.emptyActionButton).setOnClickListener(v -> showAddPrescriptionDialog());

        swipeRefreshLayout.setOnRefreshListener(this::recuperarRecetasDeNube);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recuperarRecetasDeNube();
    }

    private void recuperarRecetasDeNube() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        db.collection("recetas")
            .whereEqualTo("userId", user.getUid())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }

                AppDatabase.databaseWriteExecutor.execute(() -> {
                    List<Prescription> locales = AppDatabase.getDatabase(requireContext()).prescriptionDao().getAllPrescriptions();
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String fecha = doc.getString("fecha");
                        // QA Security: Desencriptar datos antes de comparar o guardar
                        String odEsfera = CryptoUtils.decrypt(doc.getString("odEsfera"));
                        
                        boolean yaExiste = false;
                        for (Prescription p : locales) {
                            if (p.getDate().equals(fecha) && p.getOdSphere().equals(odEsfera)) {
                                yaExiste = true;
                                break;
                            }
                        }

                        if (!yaExiste) {
                            Prescription nueva = new Prescription(
                                    fecha,
                                    odEsfera,
                                    CryptoUtils.decrypt(doc.getString("odCilindro")),
                                    CryptoUtils.decrypt(doc.getString("odEje")),
                                    CryptoUtils.decrypt(doc.getString("odAdicion")),
                                    CryptoUtils.decrypt(doc.getString("oiEsfera")),
                                    CryptoUtils.decrypt(doc.getString("oiCilindro")),
                                    CryptoUtils.decrypt(doc.getString("oiEje")),
                                    CryptoUtils.decrypt(doc.getString("oiAdicion"))
                            );
                            nueva.setImageUrl(doc.getString("imageUrl"));
                            nueva.setSynced(true);
                            AppDatabase.getDatabase(requireContext()).prescriptionDao().insert(nueva);
                        }
                    }
                    
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            loadPrescription();
                            swipeRefreshLayout.setRefreshing(false);
                        });
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e("FIREBASE_FETCH", "Error recuperando datos", e);
                swipeRefreshLayout.setRefreshing(false);
            });
    }

    private void loadPrescription() {
        if (!isAdded()) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Prescription> list = AppDatabase.getDatabase(requireContext()).prescriptionDao().getAllPrescriptions();
            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    if (list.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        historyContent.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        historyContent.setVisibility(View.VISIBLE);
                        displayData(list.get(0));
                    }
                }
            });
        });
    }

    private void displayData(Prescription p) {
        tvDate.setText("Fecha: " + p.getDate());
        tvOdSph.setText("Esf: " + p.getOdSphere());
        tvOdCyl.setText("Cil: " + p.getOdCylinder());
        tvOdAxis.setText("Eje: " + p.getOdAxis());
        tvOdAdd.setText("Add: " + p.getOdAddition());
        tvOiSph.setText("Esf: " + p.getOiSphere());
        tvOiCyl.setText("Cil: " + p.getOiCylinder());
        tvOiAxis.setText("Eje: " + p.getOiAxis());
        tvOiAdd.setText("Add: " + p.getOiAddition());
    }

    private void showAddPrescriptionDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_prescription, null);
        
        TextInputLayout tilOdSph = dialogView.findViewById(R.id.tilOdSphere);
        TextInputEditText etOdSph = dialogView.findViewById(R.id.etOdSphere);
        ivPreview = dialogView.findViewById(R.id.ivPrescriptionPhoto);
        cardPhoto = dialogView.findViewById(R.id.cardPrescriptionPhoto);

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialogView.findViewById(R.id.btnCapturePhoto).setOnClickListener(v -> dispatchTakePictureIntent());

        alertDialog.setOnShowListener(dialogInterface -> {
            View button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String sphStr = etOdSph.getText() != null ? etOdSph.getText().toString() : "";
                
                try {
                    double sph = Double.parseDouble(sphStr);
                    if (sph < -20 || sph > 20) {
                        tilOdSph.setError("Valor de esfera fuera de rango (-20 a +20)");
                        return;
                    }
                } catch (NumberFormatException e) {
                    tilOdSph.setError("Ingrese un valor numérico válido");
                    return;
                }

                tilOdSph.setError(null);
                
                String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                Prescription p = new Prescription(date, sphStr, "0.00", "0", "0.00", "0.00", "0.00", "0", "0.00");
                
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    long id = AppDatabase.getDatabase(requireContext()).prescriptionDao().insert(p);
                    p.setId((int) id);
                    
                    requireActivity().runOnUiThread(() -> {
                        if (photoUri != null) {
                            subirImagenYSincronizar(p, photoUri);
                        } else {
                            sincronizarRecetaConNube(p);
                        }
                        loadPrescription();
                        alertDialog.dismiss();
                    });
                });
            });
        });

        alertDialog.show();
    }

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e("CAMERA_ERROR", "Error creando archivo", ex);
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(requireContext(),
                    "com.example.optica.fileprovider",
                    photoFile);
            takePictureLauncher.launch(photoUri);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void subirImagenYSincronizar(Prescription receta, Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        storageHelper.uploadPrescriptionImage(user.getUid(), uri, new StorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                receta.setImageUrl(downloadUrl);
                sincronizarRecetaConNube(receta);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("STORAGE_ERROR", "Error subiendo imagen", e);
                sincronizarRecetaConNube(receta);
            }
        });
    }

    private void sincronizarRecetaConNube(Prescription receta) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // QA Security: Encriptar datos sensibles antes de subirlos a la nube
        Map<String, Object> datosReceta = new HashMap<>();
        datosReceta.put("userId", user.getUid());
        datosReceta.put("idLocal", receta.getId());
        datosReceta.put("fecha", receta.getDate());
        datosReceta.put("odEsfera", CryptoUtils.encrypt(receta.getOdSphere()));
        datosReceta.put("odCilindro", CryptoUtils.encrypt(receta.getOdCylinder()));
        datosReceta.put("odEje", CryptoUtils.encrypt(receta.getOdAxis()));
        datosReceta.put("odAdicion", CryptoUtils.encrypt(receta.getOdAddition()));
        datosReceta.put("oiEsfera", CryptoUtils.encrypt(receta.getOiSphere()));
        datosReceta.put("oiCilindro", CryptoUtils.encrypt(receta.getOiCylinder()));
        datosReceta.put("oiEje", CryptoUtils.encrypt(receta.getOiAxis()));
        datosReceta.put("oiAdicion", CryptoUtils.encrypt(receta.getOiAddition()));
        datosReceta.put("imageUrl", receta.getImageUrl());
        datosReceta.put("fechaSincronizacion", System.currentTimeMillis());

        db.collection("recetas")
            .add(datosReceta)
            .addOnSuccessListener(documentReference -> {
                Log.d("FIREBASE_OK", "Receta clonada con éxito en internet. ID: " + documentReference.getId());
                receta.setSynced(true);
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    if (getContext() != null) {
                        AppDatabase.getDatabase(requireContext()).prescriptionDao().update(receta);
                    }
                });
                if (isAdded()) {
                    Toast.makeText(getContext(), "Receta guardada y sincronizada (Cifrada)", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e("FIREBASE_ERROR", "No se pudo subir la receta a la nube", e);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Receta guardada localmente", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
