package com.example.optica;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.optica.data.ApiService;
import com.example.optica.data.RetrofitClient;
import com.example.optica.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;
    private final ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Log.d("QA_LOGIN", "Resultado del selector: " + result.getResultCode());
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                        if (account != null) {
                            String idToken = account.getIdToken();
                            if (idToken != null) {
                                Log.d("QA_LOGIN", "Token obtenido con éxito. Iniciando Firebase...");
                                firebaseAuthWithGoogle(idToken);
                            } else {
                                Log.e("QA_LOGIN", "idToken es NULL. ¿Configuraste el SHA-1 en Firebase?");
                                Toast.makeText(this, "Error: Google no entregó el Token (Revisar SHA-1)", Toast.LENGTH_LONG).show();
                                setLoading(false);
                            }
                        }
                    } catch (ApiException e) {
                        Log.e("QA_LOGIN", "Error en API Google: " + e.getStatusCode(), e);
                        Toast.makeText(this, "Error Google: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                        setLoading(false);
                    }
                } else {
                    Log.e("QA_LOGIN", "Selector cancelado o falló: " + result.getResultCode());
                    setLoading(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.loginProgress);

        String clientId = getString(R.string.default_web_client_id);
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();
        
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogleLogin).setOnClickListener(v -> {
            setLoading(true);
            googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent());
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("QA_LOGIN", "Autenticación Firebase OK. Consultando rol...");
                        syncUserWithBackend(idToken);
                    } else {
                        Log.e("QA_LOGIN", "Firebase Auth falló", task.getException());
                        Toast.makeText(this, "Error Firebase: " + (task.getException() != null ? task.getException().getMessage() : "Desconocido"), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

    private void syncUserWithBackend(String idToken) {
        Map<String, String> body = new HashMap<>();
        body.put("idToken", idToken);

        apiService.login(body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (isFinishing()) return;
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d("QA_LOGIN", "Login completo. Rol: " + user.getRole());
                    saveUserRole(user.getRole());
                    
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("QA_LOGIN", "Backend respondió error: " + response.code());
                    Toast.makeText(LoginActivity.this, "Error de servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (isFinishing()) return;
                Log.e("QA_LOGIN", "Fallo total de red", t);
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Backend dormido. Reintenta en unos segundos.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnGoogleLogin).setEnabled(!loading);
    }

    private void saveUserRole(String role) {
        SharedPreferences prefs = getSharedPreferences("OpticaPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("user_role", role).apply();
    }
}
